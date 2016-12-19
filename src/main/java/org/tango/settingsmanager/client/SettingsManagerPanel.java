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

package org.tango.settingsmanager.client;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.core.AttributeList;
import fr.esrf.tangoatk.core.ConnectionException;
import fr.esrf.tangoatk.core.IDevStateScalar;
import fr.esrf.tangoatk.core.IStringScalar;
import fr.esrf.tangoatk.widget.attribute.StateViewer;
import fr.esrf.tangoatk.widget.attribute.StatusViewer;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.settingsmanager.client.config.SettingsManagerConfig;
import org.tango.settingsmanager.client.file_browser.FileBrowserTree;
import org.tango.settingsmanager.client.gui_utils.IconUtils;
import org.tango.settingsmanager.client.gui_utils.ListSelectionDialog;
import org.tango.settingsmanager.client.gui_utils.PopupHtml;
import org.tango.settingsmanager.client.gui_utils.SplashUtils;
import org.tango.settingsmanager.commons.ICommons;
import org.tango.settingsmanager.commons.ReleaseNote;
import org.tango.settingsmanager.commons.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

//=======================================================
/**
 *	JFrame Class to display info
 *
 * @author  Pascal Verdier
 */
//=======================================================
@SuppressWarnings("MagicConstant")
public class SettingsManagerPanel extends JFrame {
    private String rootPath;
    private DeviceProxy managerProxy;
    private JTextField fileTextField = null;
    private String relativePath;
    private SettingsManagerClient settingsClient;
    private JScrollPane treeScrollPane = null;
    private FileBrowserTree fileBrowserTree = null;
    private AttributeList attributeList = new AttributeList();
    private static final Dimension dimension = new Dimension(350, 400);
	//=======================================================
    /**
	 *	Creates new form SettingsManagerPanel
	 */
	//=======================================================
    public SettingsManagerPanel() throws DevFailed {
        initComponents();
        rootPath = getRootPath();
        if (startProject()) {
            ImageIcon tangoLogo = IconUtils.getInstance().getIcon("TangoClass.gif", 0.75);
            setTitle(ICommons.revNumber);
            setIconImage(tangoLogo.getImage());
            addStateViewer();

            pack();
            ATKGraphicsUtils.centerFrameOnScreen(this);
        }
        else
            doClose();
	}
	//=======================================================
	//=======================================================
    private void addStateViewer() throws DevFailed {
        try {
            StateViewer stateViewer = new StateViewer();
            //stateViewer.setFont(font);
            stateViewer.setLabel("");
            //stateViewer.setStatePreferredSize(new Dimension(60, 30));
            //errorHistory.add(stateViewer);

            IDevStateScalar attState =
                    (IDevStateScalar) attributeList.add(managerProxy.get_name() + "/state");
            stateViewer.setModel(attState);
            topTopPanel.add(new JLabel("     "));
            topTopPanel.add(stateViewer);
            //attState.addDevStateScalarListener(this);


            StatusViewer statusViewer = new StatusViewer();
            IStringScalar stringScalar =
                    (IStringScalar) attributeList.add(managerProxy.get_name() + "/status");
            //statusViewer.setPreferredSize(new Dimension(300, 150));
            statusViewer.setModel(stringScalar);
            stringScalar.refresh();
            attributeList.startRefresher();
            viewerPanel.add(statusViewer);

            attState.refresh();
        }
        catch (ConnectionException e) {
            Except.throw_exception("ConnectionException", e.toString());
        }
    }
    //=======================================================
    //=======================================================
    private String  getSelectedFile() {
        String selectionType = fileBrowserTree.getSelectionType();
        if (selectionType.equalsIgnoreCase("file")) {
            String selectedFile = fileTextField.getText();
            if (selectedFile.startsWith(relativePath + "/"))
                selectedFile = selectedFile.substring(relativePath.length() + 1); //	+1 for '/'
            System.out.println(selectedFile);

            return selectedFile;
        }
        else {
            JOptionPane.showMessageDialog(this, selectionType, "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
	//=======================================================
	//=======================================================
    private boolean startProject() throws DevFailed {
        String projectName = getProjectName();
        if (projectName!=null) {
            projectLabel.setText(projectName);

            //  Create a settings manager client and add a listener
            settingsClient = new SettingsManagerClient(projectName);
            settingsClient.addSettingsAppliedListener(new SettingsManagedListener() {
                @Override
                public void settingsManaged(SettingsManagedEvent event) {
                    settingsAppliedPerformed(event);
                }
            });
            relativePath = getProjectPath(projectName);
            pathLabel.setText("");
            //  Build a JTextField to display file name
            if (fileTextField==null) {
                JPanel panel = new JPanel();
                panel.add(new JLabel("File: ../"+projectName+"/"));
                fileTextField = new JTextField(25);
                panel.add(fileTextField);
                centerPanel.add(panel, BorderLayout.SOUTH);
            }
            //relativePath = managerProxy.get_property("SettingsFilesPath").extractString();
            buildFilesTree();
            return true;
        }
        else
            return false;
    }
	//=======================================================
	//=======================================================
    private void buildFilesTree() throws DevFailed {
        //  Build a JScrollPane to display files tree
        if (treeScrollPane==null) {
            treeScrollPane = new JScrollPane();
            treeScrollPane.setPreferredSize(dimension);
            centerPanel.add(treeScrollPane, BorderLayout.CENTER);
        }
        else
            treeScrollPane.remove(fileBrowserTree);

        //	Build the files tree
        fileBrowserTree = new FileBrowserTree(this, managerProxy);
        treeScrollPane.setViewportView(fileBrowserTree);
    }
	//=======================================================
	//=======================================================
    public void setSelectedFileInfo(String fileName) {
        if (fileName.startsWith("/"))
            fileName = fileName.substring(1);
        fileTextField.setText(fileName);
    }
	//=======================================================
	//=======================================================
    private String getProjectPath(String projectName) throws DevFailed {
        String deviceName;
        if (projectName.contains("/"))
            deviceName = projectName;
        else
            deviceName = ICommons.deviceHeader+projectName;
        managerProxy = new DeviceProxy(deviceName);
        DbDatum datum = managerProxy.get_property("SettingsFilesPath");
        if (datum.is_empty())
            return rootPath+'/';
        else
            return rootPath + '/' + datum.extractString();
    }
	//=======================================================
	//=======================================================
    private String getRootPath() throws DevFailed {
        String rootPath = "";
        try {
            rootPath = Utils.getSettingsRootPath();
        } catch (DevFailed e) {
            System.out.println(e.errors[0].desc);
            doClose();
        }
        return rootPath;
    }
	//=======================================================
	//=======================================================
    private String getProjectName() throws DevFailed {
        List<String> projects = SettingsManagerClient.getSettingsProjectList();
        if (projects.isEmpty())
            Except.throw_exception("NoProject", "No project found in database");
        if (projects.size()==1) {
            changeProjectItem.setEnabled(false);
            return projects.get(0);
        }
        ListSelectionDialog dialog = new ListSelectionDialog(this, projects, "Project ?", false);
        int choice = dialog.showDialog();
        if (choice==JOptionPane.OK_OPTION)
            return dialog.getSelectedItem();
        else
            return null;
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

        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        topTopPanel = new javax.swing.JPanel();
        projectLabel = new javax.swing.JLabel();
        viewerPanel = new javax.swing.JPanel();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        pathLabel = new javax.swing.JLabel();
        centerPanel = new javax.swing.JPanel();
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem refreshItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem generateItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem applyItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitItem = new javax.swing.JMenuItem();
        javax.swing.JMenu viewMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem resetMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem configureItem = new javax.swing.JMenuItem();
        changeProjectItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem releaseMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        topPanel.setLayout(new java.awt.BorderLayout());

        projectLabel.setFont(new java.awt.Font("Dialog", 1, 24)); // NOI18N
        projectLabel.setText("        ");
        topTopPanel.add(projectLabel);

        topPanel.add(topTopPanel, java.awt.BorderLayout.NORTH);
        topPanel.add(viewerPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        pathLabel.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        pathLabel.setText("        ");
        bottomPanel.add(pathLabel);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        centerPanel.setLayout(new java.awt.BorderLayout());
        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");

        refreshItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        refreshItem.setMnemonic('G');
        refreshItem.setText("Refresh");
        refreshItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshItemActionPerformed(evt);
            }
        });
        fileMenu.add(refreshItem);

        generateItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
        generateItem.setMnemonic('G');
        generateItem.setText("Generate Settings");
        generateItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateItemActionPerformed(evt);
            }
        });
        fileMenu.add(generateItem);

        applyItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        applyItem.setMnemonic('A');
        applyItem.setText("Apply Settings");
        applyItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyItemActionPerformed(evt);
            }
        });
        fileMenu.add(applyItem);

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

        viewMenu.setMnemonic('T');
        viewMenu.setText("Tools");

        resetMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        resetMenuItem.setMnemonic('R');
        resetMenuItem.setText("Reset");
        resetMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(resetMenuItem);

        configureItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        configureItem.setMnemonic('D');
        configureItem.setText("Configure device");
        configureItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureItemActionPerformed(evt);
            }
        });
        viewMenu.add(configureItem);

        changeProjectItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        changeProjectItem.setMnemonic('P');
        changeProjectItem.setText("Change Project");
        changeProjectItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeProjectItemActionPerformed(evt);
            }
        });
        viewMenu.add(changeProjectItem);

        menuBar.add(viewMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText("help");

        releaseMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        releaseMenuItem.setMnemonic('R');
        releaseMenuItem.setText("Release Notes");
        releaseMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                releaseMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(releaseMenuItem);

        aboutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        aboutMenuItem.setMnemonic('A');
        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void refreshItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshItemActionPerformed
        // TODO add your handling code here:
        try {
            buildFilesTree();
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_refreshItemActionPerformed
	//=======================================================
	//=======================================================
    @SuppressWarnings("UnusedParameters")
    private void generateItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateItemActionPerformed
        try {
            //  Generate settings file
            String fileName = fileTextField.getText(); //getSelectedFile();
            if (fileName!=null && fileName.startsWith(relativePath + "/"))
                fileName = fileName.substring(relativePath.length() + 1); //	+1 for '/'
            settingsClient.generateSettingsFile(this, null, fileName);
            buildFilesTree();
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_generateItemActionPerformed
    //=======================================================
    //=======================================================
    public void applySettingsFile() {
        try {
            String fileName = getSelectedFile();
            if (fileName != null && !fileName.isEmpty())
                settingsClient.applySettings(this, fileName);
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, e.getMessage(), e);
        }
    }
    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void applyItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyItemActionPerformed
        //  apply settings file
        applySettingsFile();
    }//GEN-LAST:event_applyItemActionPerformed
    //=======================================================
    //=======================================================
    private void settingsAppliedPerformed(SettingsManagedEvent event) {
        SplashUtils.getInstance().stopSplash();
        String fileName = event.getFileName();
        switch (event.getAction()) {
            case SettingsManagerClient.APPLIED:
                //  Display applied results
                if (event.hasFailed()) {
                    ErrorPane.showErrorMessage(new JFrame(),
                            "Applying file " + fileName, event.getDevFailed());
                } else {
                    JOptionPane.showMessageDialog(new JFrame(),
                            "Settings loaded from  " + event.getFileName());
                }
                break;
            case SettingsManagerClient.GENERATED:
                //  Display generated results
                if (event.hasFailed()) {
                    ErrorPane.showErrorMessage(new JFrame(),
                            "Generated file " + fileName, event.getDevFailed());
                } else {
                        try {
                        fileBrowserTree.collapseNodes();
                        fileBrowserTree.setSelectedFile(event.getFileName());
                    }
                    catch (DevFailed e) {
                        ErrorPane.showErrorMessage(this, null, e);
                    }
                    JOptionPane.showMessageDialog(new JFrame(),
                            "Settings saved in  " + event.getFileName());
                }
                break;
        }
    }
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
    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        String  message = "This application is able to\n" +
                " manage SettingsManager TANGO class\n" +
                "\nPascal Verdier - ESRF: Accelerator Control Unit";
        JOptionPane.showMessageDialog(this, message, "Help Window", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_aboutMenuItemActionPerformed
    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void configureItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureItemActionPerformed
        try {
            new SettingsManagerConfig(this).initializeProject();
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_configureItemActionPerformed
    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void changeProjectItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeProjectItemActionPerformed
        try {
            startProject();
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_changeProjectItemActionPerformed
    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void releaseMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_releaseMenuItemActionPerformed
        new PopupHtml(this).show(ReleaseNote.str);
    }//GEN-LAST:event_releaseMenuItemActionPerformed
    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void resetMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetMenuItemActionPerformed
        // TODO add your handling code here:
        try {
            managerProxy.command_inout("Reset");
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_resetMenuItemActionPerformed
    //=======================================================
	//=======================================================
    private void doClose() {
        System.exit(0);
    }
	//=======================================================
    /**
     * @param args the command line arguments
     */
	//=======================================================
    public static void main(String args[]) {
		try {
      		new SettingsManagerPanel().setVisible(true);
		}
		catch(DevFailed e) {
            ErrorPane.showErrorMessage(new Frame(), null, e);
			System.exit(0);
		}
    }


	//=======================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centerPanel;
    private javax.swing.JMenuItem changeProjectItem;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JPanel topTopPanel;
    private javax.swing.JPanel viewerPanel;
    // End of variables declaration//GEN-END:variables
	//=======================================================

}
