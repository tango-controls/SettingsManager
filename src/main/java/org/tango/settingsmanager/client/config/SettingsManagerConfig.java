//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  java source code for main swing class.
//
// $Author: verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,2015
//						European Synchrotron Radiation Facility
//                      BP 220, Grenoble 38043
//                      FRANCE
//
// This file is part of Tango.
//
// Tango is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// Tango is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with Tango.  If not, see <http://www.gnu.org/licenses/>.
//
// $Revision:  $
//
// $Log:  $
//
//-======================================================================

package org.tango.settingsmanager.client.config;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.settingsmanager.client.SettingsFileFilter;
import org.tango.settingsmanager.client.SettingsManagerClient;
import org.tango.settingsmanager.client.gui_utils.ListSelectionDialog;
import org.tango.settingsmanager.commons.ICommons;
import org.tango.settingsmanager.commons.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

//=======================================================
/**
 *	JFrame Class to display info
 *
 * @author  Pascal Verdier
 */
//=======================================================
@SuppressWarnings({"Convert2Diamond", "MagicConstant"})
public class SettingsManagerConfig extends JFrame {
    private JFrame parent;
    private String projectName;
    private String rootPath;
    private AttributeTree attributeTree;
    private DeviceProxy deviceProxy;
    private boolean attributeListChanged = false;
    private static AttributeListPopupMenu popupMenu;
    private boolean rootPathAlreadyDefined = false;
    private boolean projectAlreadyDefined = false;
    static final Dimension dimension = new Dimension(350, 500);
	//=======================================================
    /**
	 *	Creates new form SettingsManageCreation
	 */
	//=======================================================
    public SettingsManagerConfig(JFrame parent) throws DevFailed {
        this.parent = parent;
        initComponents();
        attributeTree = new AttributeTree(attributeList);
        treeScrollPane.setViewportView(attributeTree);
        treeScrollPane.setPreferredSize(dimension);
        listScrollPane.setPreferredSize(dimension);
        popupMenu = new AttributeListPopupMenu(attributeList);
        //	Add Action listeners for list clicked, menu,...
        attributeList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                listMouseClicked(event);
            }
        });

        pack();
        ATKGraphicsUtils.centerFrameOnScreen(this);
    }
	//=======================================================
	//=======================================================
    public void initializeProject() throws DevFailed {
        //  Get the root path, project name, and project path.
        rootPath = getRootPath();
        if (rootPath!=null) {
            System.out.println("Root dir = " + rootPath);
            projectName = getProjectName();
            if (projectName!=null) {
                deviceProxy = getDeviceProxy(ICommons.deviceHeader + projectName);
                String path = getProjectPath(deviceProxy);
                if (path!=null) {
                    System.out.println("Settings files will be saved at: " + path);
                    pathLabel.setText(path);
                    File dir = new File(path);
                    if (!dir.exists()) {
                        if (!dir.mkdir())
                            Except.throw_exception("CreationFailed", "Failed to create " + path);
                    }
                    //  Display attribute names in list
                    if (deviceProxy!=null) {
                        String[] attributeNames = getDefaultAttributeNames();
                        attributeList.setListData(attributeNames);
                    }

                    pack();
                    setVisible(true);
                }
            } else
                abandon();
        } else
            abandon();
    }
    //=======================================================
    //=======================================================
    private void listMouseClicked(MouseEvent event) {
        String  attributeName = attributeList.getSelectedValue();
        if (attributeName==null)
            return;

        //  Check button clicked
        if ((event.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
            popupMenu.showMenu(event, attributeName);
        }
    }
	//=======================================================
	//=======================================================
    private String getProjectName() throws DevFailed {

        if (rootPathAlreadyDefined) {
            List<String> projects = SettingsManagerClient.getSettingsProjectList();
            if (!projects.isEmpty()) {
                ListSelectionDialog dialog = new ListSelectionDialog(this, projects, "Settings Project ?");
                int choice = dialog.showDialog();
                if (choice==JOptionPane.CANCEL_OPTION)
                    return null;
                else if (choice==JOptionPane.OK_OPTION) {
                    projectAlreadyDefined = true;
                    return dialog.getSelectedItem();
                }
            }
        }
        //  Else ask to create a new one
        return JOptionPane.showInputDialog(this, "Project name ?");
    }
	//=======================================================
	//=======================================================
    private String[] getDefaultAttributeNames() throws DevFailed {
        DbDatum datum = deviceProxy.get_property("DefaultAttributeList");
        if (datum.is_empty())
            return new String[0];
        else
            return datum.extractStringArray();
    }
	//=======================================================
	//=======================================================
    private DeviceProxy getDeviceProxy(String deviceName) {
        DeviceProxy proxy = null;
        try {
            proxy = new DeviceProxy(deviceName);
        }
        catch (DevFailed e) {
            System.out.println(e.errors[0].desc);
        }
        return proxy;
    }
	//=======================================================
	//=======================================================
    private String getProjectPath(DeviceProxy proxy) throws DevFailed {
        if (proxy!=null) {
            DbDatum datum = proxy.get_property("SettingsFilesPath");
            if (!datum.is_empty())
            return rootPath+'/'+datum.extractString();
        }
        //  Get path from chooser
        JFileChooser chooser = new JFileChooser(rootPath);
        chooser.addChoosableFileFilter(new SettingsFileFilter("", "Directory"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int choice = chooser.showDialog(this, "Root Dir.");
        if (choice == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null)
                if (file.isDirectory()) {
                    String absolute = file.getAbsolutePath();
                    if (!absolute.startsWith(rootPath)) {
                        Except.throw_exception("BadPath",
                                absolute + "\n  is out of RootPath class property");
                    }
                    return absolute;
                }
        }
        return null;
    }
	//=======================================================
	//=======================================================
    private String getRootPath() {
        String rootPath;
        try {
            rootPath = Utils.getSettingsRootPath();
            rootPathAlreadyDefined = true;
            return rootPath;
        } catch (DevFailed e) {
            System.out.println(e.errors[0].desc);
        }

        //  If not defined get it from a file chooser
        JFileChooser chooser = new JFileChooser(".");
        chooser.addChoosableFileFilter(new SettingsFileFilter("", "Directory"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int choice = chooser.showDialog(this, "Root Dir.");
        if (choice == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null)
                if (file.isDirectory()) {
                    return file.getAbsolutePath();
                }
        }
        return null;
    }
	//=======================================================
	//=======================================================
    private void removeAttribute() {
        int selectedIndex = attributeList.getSelectedIndex();

        //  Get attribute and selection lists
        List<String> attributes = new ArrayList<>();
        for (int i=0 ; i<attributeList.getModel().getSize() ; i++) {
            String attribute = attributeList.getModel().getElementAt(i);
            if (i!=selectedIndex)
                attributes.add(attribute);
        }
        attributeList.setListData(attributes.toArray(new String[attributes.size()]));
        attributeListChanged = true;
    }
	//=======================================================
	//=======================================================

	//=======================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//=======================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        pathLabel = new javax.swing.JLabel();
        javax.swing.JSplitPane jSplitPane1 = new javax.swing.JSplitPane();
        treeScrollPane = new javax.swing.JScrollPane();
        listScrollPane = new javax.swing.JScrollPane();
        attributeList = new javax.swing.JList<String>();
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem saveItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem jMenuItem1 = new javax.swing.JMenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel1.setText("Settings files: ");
        jPanel1.add(jLabel1);

        pathLabel.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        pathLabel.setText("      ");
        jPanel1.add(pathLabel);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jSplitPane1.setLeftComponent(treeScrollPane);

        listScrollPane.setViewportView(attributeList);

        jSplitPane1.setRightComponent(listScrollPane);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");

        saveItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveItem.setMnemonic('S');
        saveItem.setText("Save in database");
        saveItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveItem);

        exitItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        exitItem.setMnemonic('E');
        exitItem.setText("Exit");
        exitItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText("help");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        jMenuItem1.setMnemonic('A');
        jMenuItem1.setText("About");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        helpMenu.add(jMenuItem1);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	//=======================================================
	//=======================================================
    @SuppressWarnings("UnusedParameters")
    private void saveItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveItemActionPerformed
        try {
            saveProject();
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_saveItemActionPerformed
	//=======================================================
	//=======================================================
    @SuppressWarnings("UnusedParameters")
    private void exitItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitItemActionPerformed
        doClose();
    }//GEN-LAST:event_exitItemActionPerformed

	//=======================================================
	//=======================================================
    @SuppressWarnings("UnusedParameters")
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        doClose();
    }//GEN-LAST:event_exitForm

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        String  message =
                "This application is able to create and configure\n" +
                "a SettingsManager device for a specified project" +
                "\nPascal Verdier - ESRF: Accelerator Control Unit";
        JOptionPane.showMessageDialog(this, message, "Help Window", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

	//=======================================================
	//=======================================================
    private void abandon() {
        if (parent==null)
            System.exit(0);
        dispose();
        setVisible(false);
    }
	//=======================================================
	//=======================================================
    private void doClose() {

        //  Check if changes
        if (hasChanged()) {
            //if (JOptionPane.showConfirmDialog(this, ))
            Object[] options = {"Save", "Discard", "Cancel"};
            switch (JOptionPane.showOptionDialog(this,
                    "Save changes in database ?\n\n",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, options, options[0])) {
                case 0:    //	Save
                    try {
                        saveProject();
                    }
                    catch (DevFailed e) {
                        ErrorPane.showErrorMessage(this, null, e);
                        return;
                    }
                    break;
                case 1:    // Discard
                    break;
                case 2:    //	Cancel
                case -1:   //	escape
                    return;
            }
        }
        abandon();
    }
	//=======================================================
    /*
     *  Save project part
     */
	//=======================================================
    private boolean hasChanged() {
        return attributeListChanged || attributeTree.isAttributeListChanged() ||
                !projectAlreadyDefined || !rootPathAlreadyDefined;
    }
	//=======================================================
	//=======================================================
	private void saveProject() throws DevFailed {
        System.out.println("Saving project !");
        if (deviceProxy==null) {
            deviceProxy = createDevice();
        }
        manageDeviceProperties();
        manageRootPath();

        rootPathAlreadyDefined = true;
        projectAlreadyDefined = true;
        attributeListChanged = false;
        attributeTree.setAttributeListChanged(false);
    }
	//=======================================================
	//=======================================================
    private void manageDeviceProperties() throws DevFailed {
        //  Get attribute list and set property
        List<String> attributes = new ArrayList<>();
        for (int i = 0 ; i<attributeList.getModel().getSize() ; i++) {
            String attribute = attributeList.getModel().getElementAt(i);
            attributes.add(attribute);
        }
        DbDatum[] data = new DbDatum[]{
                new DbDatum("DefaultAttributeList", attributes.toArray(new String[attributes.size()])),
                new DbDatum("SettingsFilesPath", projectName),
        };
        deviceProxy.put_property(data);
    }
	//=======================================================
	//=======================================================
    private void manageRootPath() throws DevFailed {
        if (!rootPathAlreadyDefined) {
            //  Set as class property
            DbDatum datum = new DbDatum("RootPath", rootPath);
            new DbClass(ICommons.className).put_property(new DbDatum[]{datum});
        }
    }
	//=======================================================
	//=======================================================
    private DeviceProxy createDevice() throws DevFailed {
        String serverName = ICommons.className + '/' + projectName;
        String deviceName = ICommons.deviceHeader + projectName;

        //	DServer device
        DbDevInfo[] dev_info = new DbDevInfo[] {
                new DbDevInfo("dserver/" + serverName, "DServer", serverName),
        };
        //	And create server in database
        System.out.println("creating " + serverName);
        ApiUtil.get_db_obj().add_server(serverName, dev_info);
        ApiUtil.get_db_obj().add_device(dev_info[0]);
        ApiUtil.get_db_obj().add_device(deviceName, ICommons.className, serverName);
        return new DeviceProxy(deviceName);
    }
	//=======================================================
	//=======================================================











	//=======================================================
    /**
     * @param args the command line arguments
     */
	//=======================================================
    public static void main(String args[]) {
		try {
      		SettingsManagerConfig  client = new SettingsManagerConfig(null);
            client.initializeProject();

		}
		catch(DevFailed e) {
            ErrorPane.showErrorMessage(new Frame(), null, e);
            e.printStackTrace();
			System.exit(0);
		}
    }



	//=======================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> attributeList;
    private javax.swing.JScrollPane listScrollPane;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JScrollPane treeScrollPane;
    // End of variables declaration//GEN-END:variables
	//=======================================================



    //==========================================================
    //==========================================================
    private static final int REMOVE= 0;
    private static final int OFFSET = 2;    //	Label And separator

    private static String[] menuLabels = {
            "Remove attribute",
    };

    private class AttributeListPopupMenu extends JPopupMenu {
        private JList jList;
        private JLabel title;
        //======================================================
        private AttributeListPopupMenu(JList jList) {
            this.jList = jList;
            title = new JLabel();
            title.setFont(new java.awt.Font("Dialog", Font.BOLD, 16));
            add(title);
            add(new JPopupMenu.Separator());

            for (String menuLabel : menuLabels) {
                if (menuLabel==null)
                    add(new Separator());
                else {
                    JMenuItem btn = new JMenuItem(menuLabel);
                    btn.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            hostActionPerformed(evt);
                        }
                    });
                    add(btn);
                }
            }
        }
        //======================================================
        private void showMenu(MouseEvent evt, String attributeName) {
            title.setText(attributeName);

            show(jList, evt.getX(), evt.getY());
        }
        //======================================================
        private void hostActionPerformed(ActionEvent evt) {
            //	Check component source
            Object obj = evt.getSource();
            int itemIndex = 0;
            for (int i = 0 ; i<menuLabels.length ; i++)
                if (getComponent(OFFSET + i)==obj)
                    itemIndex = i;

            switch (itemIndex) {
                case REMOVE:
                    removeAttribute();
            }
        }
        //======================================================
    }
    //==========================================================
    //==========================================================
}
