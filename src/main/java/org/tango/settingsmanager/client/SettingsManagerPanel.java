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
//-======================================================================

package org.tango.settingsmanager.client;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.core.AttributeList;
import fr.esrf.tangoatk.core.ConnectionException;
import fr.esrf.tangoatk.core.IDevStateScalar;
import fr.esrf.tangoatk.core.IStringScalar;
import fr.esrf.tangoatk.widget.attribute.SimpleScalarViewer;
import fr.esrf.tangoatk.widget.attribute.StateViewer;
import fr.esrf.tangoatk.widget.attribute.StatusViewer;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.settingsmanager.client.gui_utils.IconUtils;
import org.tango.settingsmanager.client.gui_utils.ListSelectionDialog;
import org.tango.settingsmanager.client.gui_utils.PopupHtml;
import org.tango.settingsmanager.commons.ICommons;
import org.tango.settingsmanager.commons.ReleaseNotes;
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
public class SettingsManagerPanel extends JFrame {

    private SettingsManagerClient settingsClient;
    private AttributeList attributeList = new AttributeList();
	//=======================================================
    /**
	 *	Creates new form ApiTestFrame
	 */
	//=======================================================
    public SettingsManagerPanel() throws DevFailed {
        initComponents();
        if (startProject()) {
            ImageIcon tangoLogo = IconUtils.getInstance().getIcon("TangoClass.gif", 0.75);
            setTitle(Utils.getInstance().getApplicationName());
            setIconImage(tangoLogo.getImage());
            addStateViewer(settingsClient.getManagerDeviceName());
        }
        pack();
        ATKGraphicsUtils.centerFrameOnScreen(this);
	}
    //=======================================================
    //=======================================================
    private boolean startProject() throws DevFailed {
        String projectName = getProjectName();
        if (projectName!=null) {
            //  Create a settings manager client and add a listener
            settingsClient = new SettingsManagerClient(projectName);
            settingsClient.addSettingsAppliedListener(new SettingsManagedListener() {
                @Override
                public void settingsManaged(SettingsManagedEvent event) {
                    settingsAppliedPerformed(event);
                }
            });
            return true;
        }
        else
            return false;
    }
    //=======================================================
    //=======================================================
    private String getProjectName() throws DevFailed {
        List<String> projects = SettingsManagerClient.getSettingsProjectList();
        if (projects.isEmpty())
            Except.throw_exception("NoProject", "No project found in database");
        if (projects.size()==1) {
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
    private void settingsAppliedPerformed(SettingsManagedEvent event) {
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
                    JOptionPane.showMessageDialog(new JFrame(),
                            "Settings saved in  " + event.getFileName());
                }
                break;
        }
    }
    //=======================================================
    //=======================================================
    private void addStateViewer(String managerName) throws DevFailed {
        try {
            //  Add a State viewer
            StateViewer stateViewer = new StateViewer();
            stateViewer.setLabel("");
            IDevStateScalar attState =
                    (IDevStateScalar) attributeList.add(managerName + "/state");
            stateViewer.setModel(attState);
            topPanel.add(new JLabel("     "));
            topPanel.add(stateViewer);


            //  Add a Status viewer
            StatusViewer statusViewer = new StatusViewer();
            IStringScalar stringScalar =
                    (IStringScalar) attributeList.add(managerName + "/status");
            statusViewer.setPreferredSize(new Dimension(640, 200));
            statusViewer.setModel(stringScalar);
            stringScalar.refresh();
            attributeList.startRefresher();
            getContentPane().add(statusViewer, BorderLayout.CENTER);

            //  Add a string attribute viewer
            IStringScalar attLoadFile =
                    (IStringScalar) attributeList.add(managerName + "/LastAppliedFile");
            SimpleScalarViewer loadFileViewer = new SimpleScalarViewer();
            loadFileViewer.setModel(attLoadFile);
            loadFileViewer.setBackgroundColor(Color.white);
            loadFileViewer.setFont(new Font("Dialog", Font.BOLD, 14));
            //loadFileViewer.setPreferredSize(new Dimension(250, 15));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx=0;
            gbc.gridy=1;
            gbc.fill=GridBagConstraints.HORIZONTAL;
            bottomPanel.add(loadFileViewer, gbc);

            attState.refresh();
        }
        catch (ConnectionException e) {
            Except.throw_exception("ConnectionException", e.toString());
        }
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
        java.awt.GridBagConstraints gridBagConstraints;

        topPanel = new javax.swing.JPanel();
        bottomPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem loadItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem saveItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem releaseMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        bottomPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Applied File: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
        bottomPanel.add(jLabel1, gridBagConstraints);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");

        loadItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        loadItem.setMnemonic('L');
        loadItem.setText("Load Settings");
        loadItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadItemActionPerformed(evt);
            }
        });
        fileMenu.add(loadItem);

        saveItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveItem.setMnemonic('S');
        saveItem.setText("Save Settings");
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
    private void loadItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadItemActionPerformed
        try {
            //  Apply settings
            settingsClient.setApproveButtonText("Apply");
            String fileName = settingsClient.applySettings(this);
            if (fileName!=null) {
                System.out.println("Applying " + fileName);
            }
        }
        catch (DevFailed e) {
            ErrorPane.showErrorMessage(this, null, e);
        }
    }//GEN-LAST:event_loadItemActionPerformed
    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void saveItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveItemActionPerformed
        // TODO add your handling code here:
        try {
            settingsClient.generateSettingsFile(this);
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
    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        String  message = "This application is able to\n" +
                " manage SettingsManager TANGO class\n" +
                "\nPascal Verdier - ESRF: Accelerator Control Unit";
        JOptionPane.showMessageDialog(this, message, "Help Window", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_aboutMenuItemActionPerformed
    //=======================================================
    //=======================================================
    @SuppressWarnings("UnusedParameters")
    private void releaseMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_releaseMenuItemActionPerformed
        new PopupHtml(this).show(ReleaseNotes.htmlString);
    }//GEN-LAST:event_releaseMenuItemActionPerformed
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
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
	//=======================================================

}
