//+======================================================================
// $Source:  $
//
// Project:   Tango
//
// Description:  Basic Dialog Class to display info
//
// $Author: pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2009,2010,2011,2012,2013,2014,2015
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

package org.tango.settingsmanager.client.file_browser;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.settingsmanager.commons.ICommons;
import org.tango.settingsmanager.commons.Utils;

import javax.swing.*;
import java.awt.*;


//===============================================================
/**
 *	JDialog Class to browse remote directories and files.
 *
 *	@author  Pascal Verdier
 */
//===============================================================


@SuppressWarnings("MagicConstant")
public class FileBrowserDialog extends JDialog {
	private Component	parent;
	private String relativePath;
    private DeviceProxy managerProxy;
	protected FileBrowserTree fileBrowserTree;
	private int returnValue = JOptionPane.OK_OPTION;
	private static final Dimension dimension = new Dimension(400, 400);
	//===============================================================
	/**
	 *	Creates new form FileBrowserDialog
	 */
	//===============================================================
	public FileBrowserDialog(JDialog parent, DeviceProxy managerProxy, String relativePath) throws DevFailed {
		super(parent, true);
		this.parent = parent;
        this.managerProxy = managerProxy;
        this.relativePath = relativePath;
		initOwnComponents(relativePath);
	}
	//===============================================================
	/**
	 *	Creates new form FileBrowserDialog
	 */
	//===============================================================
	public FileBrowserDialog(JFrame parent, DeviceProxy managerProxy, String relativePath) throws DevFailed {
		super(parent, true);
		this.parent = parent;
        this.managerProxy = managerProxy;
        this.relativePath = relativePath;
		initOwnComponents(relativePath);
	}
	//===============================================================
	//===============================================================
	public void setDialogTitle(String title) {
		titleLabel.setText(title);
	}
	//===============================================================
	/**
	 *	Creates new form FileBrowserDialog
	 */
	//===============================================================
	public FileBrowserDialog(JFrame parent, DeviceProxy managerProxy) throws DevFailed {
		super(parent, true);
		this.parent = parent;
        this.managerProxy = managerProxy;
		this.relativePath = managerProxy.get_property("SettingsFilesPath").extractString();
		initOwnComponents(relativePath);
	}
	//===============================================================
	//===============================================================
	private void initOwnComponents(String rootPath) throws DevFailed {
		initComponents();
		titleLabel.setText("Settings Manager File Browser");

		//	Build users_tree to display info
		fileBrowserTree = new FileBrowserTree(this, managerProxy);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(fileBrowserTree);
		scrollPane.setPreferredSize(dimension);
        contentScrollPane.setPreferredSize(dimension);
        centerPanel.add(scrollPane, BorderLayout.WEST);

        //	Get last member of relative path to display
		int index = relativePath.lastIndexOf('/', relativePath.length()-2);
		if (index<0)
    		fileNameLabel.setText("File  ../"+relativePath + '/'); // path without '/'
		else
	    	fileNameLabel.setText("File  ../"+relativePath.substring(index) + '/');

		pack();
		ATKGraphicsUtils.centerDialog(this);
	}

	//===============================================================
	//===============================================================
    public void setContentTextAreaVisible(boolean b) {
        contentScrollPane.setVisible(b);
    }
	//===============================================================
	//===============================================================
    public void setSelectedFileInfo(String fileName) {
	    if (fileName.startsWith("/"))
	        fileName = fileName.substring(1);
        fileTextField.setText(fileName);

        if (contentScrollPane.isVisible()) {
            String content;
            if (fileName.isEmpty()) {
                content = "Root directory";
            } else if (fileName.endsWith("/"))
                content = "Directory: " + fileName;
            else {
                try {
                    //  Read file content and display it
                    if (fileName.startsWith(relativePath+'/'))
                        fileName = fileName.substring(relativePath.length() + 1);
                    content = getFileContent(managerProxy, fileName);
                } catch (DevFailed e) {
                    content = Except.str_exception(e);
                }
            }
            contentTextArea.setText(Utils.checkLinesLength(content));
        }
    }
    //===============================================================
    //===============================================================
    private String getFileContent(DeviceProxy managerProxy, String fileName) throws DevFailed {
        DeviceData argIn = new DeviceData();
        argIn.insert(fileName);
        DeviceData argOut = managerProxy.command_inout("GetSettingsFileContent", argIn);
        return argOut.extractString();
    }
	//===============================================================
	//===============================================================
	public void setSelectedFile(String fileName) throws DevFailed {
		fileBrowserTree.setSelectedFile(fileName);
	}
	//===============================================================
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
	//===============================================================
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        centerPanel = new javax.swing.JPanel();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        fileNameLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        contentScrollPane = new javax.swing.JScrollPane();
        contentTextArea = new javax.swing.JTextArea();
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        applyBtn = new javax.swing.JButton();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel.setText("Dialog Title");
        topPanel.add(titleLabel);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        centerPanel.setLayout(new java.awt.BorderLayout());

        fileNameLabel.setText("File name: ");
        jPanel1.add(fileNameLabel);

        fileTextField.setColumns(35);
        fileTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileTextFieldActionPerformed(evt);
            }
        });
        jPanel1.add(fileTextField);

        centerPanel.add(jPanel1, java.awt.BorderLayout.SOUTH);

        contentTextArea.setColumns(20);
        contentTextArea.setRows(5);
        contentScrollPane.setViewportView(contentTextArea);

        centerPanel.add(contentScrollPane, java.awt.BorderLayout.CENTER);

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        applyBtn.setText("Apply");
        applyBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(applyBtn);

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });
        bottomPanel.add(cancelBtn);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	//===============================================================
	//===============================================================
	private String selectedFile = "";
	void manageSelection() {
		String str = fileTextField.getText();
        if (str.startsWith("/"))
            str = str.substring(1);
		if (!str.isEmpty() && !str.endsWith("/")) {
			selectedFile = str;
			if (selectedFile.startsWith(relativePath+"/"))
				selectedFile = selectedFile.substring(relativePath.length()+1); //	+1 for '/'
			System.out.println(selectedFile);
			returnValue = JOptionPane.OK_OPTION;
			doClose();
		}
		//	Else do nothing until cancel
	}
	//===============================================================
	//===============================================================
    @SuppressWarnings("UnusedParameters")
	private void applyBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyBtnActionPerformed
		manageSelection();
	}//GEN-LAST:event_applyBtnActionPerformed
	//===============================================================
	//===============================================================
	@SuppressWarnings("UnusedParameters")
	private void fileTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileTextFieldActionPerformed
		manageSelection();
	}//GEN-LAST:event_fileTextFieldActionPerformed
	//===============================================================
	//===============================================================
	@SuppressWarnings("UnusedParameters")
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
		returnValue = JOptionPane.CANCEL_OPTION;
		doClose();
	}//GEN-LAST:event_cancelBtnActionPerformed
	//===============================================================
	//===============================================================
    @SuppressWarnings("UnusedParameters")
	private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		returnValue = JOptionPane.CANCEL_OPTION;
		doClose();
	}//GEN-LAST:event_closeDialog

	//===============================================================
	/**
	 *	Closes the dialog
	 */
	//===============================================================
	private void doClose() {
	
		if (parent==null)
			System.exit(0);
		else {
			setVisible(false);
			dispose();
		}
	}
	//===============================================================
	//===============================================================
	public void setApproveButtonText(String text) {
		applyBtn.setText(text);
	}
	//===============================================================
	//===============================================================
	@SuppressWarnings("unused")
	public void setTitle(String text) {
		//  Used by programmer to customize
		super.setTitle(text);
	}
	//===============================================================
	//===============================================================
	public String getSelectedFile() {
		return selectedFile;
	}
	//===============================================================
	//===============================================================
	public int showDialog() {
		setVisible(true);
		return returnValue;
	}

	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyBtn;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JScrollPane contentScrollPane;
    private javax.swing.JTextArea contentTextArea;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
	//===============================================================




	//===============================================================
	/**
	* @param args the command line arguments
	*/
	//===============================================================
	public static void main(String args[]) {
		String path = "test";
		try {
			FileBrowserDialog	dialog =
					new FileBrowserDialog(new JFrame(), new DeviceProxy(ICommons.deviceHeader+path), path);
            dialog.setTitle("BlaBlaBla");
			dialog.setDialogTitle("RIPS settings");
			dialog.setApproveButtonText("Write settings");
			if (dialog.showDialog()==JOptionPane.OK_OPTION){
				String fileName = dialog.getSelectedFile();
				System.out.println(fileName);
			}
		}
		catch(DevFailed e) {
            ErrorPane.showErrorMessage(new Frame(), null, e);
		}
		System.exit(0);
	}

}
