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

package org.tango.settingsmanager.client;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import org.tango.settingsmanager.client.file_browser.FileBrowserDialog;
import org.tango.settingsmanager.commons.PipeToString;
import org.tango.settingsmanager.commons.Utils;

import javax.swing.*;
import java.awt.*;

/**
 *	JDialog Class to display a file content after selection
 *
 *	@author  Pascal Verdier
 */
@SuppressWarnings("MagicConstant")
public class ViewSettingsDialog extends JDialog {
	private String  selectedFile = null;
    private String approveButtonText = "View";
    private String title = null;
	private static final Dimension maxDimension = new Dimension(600, 800);
	//===============================================================
	/**
	 *	Creates new form ViewSettingsDialog
	 */
	//===============================================================
	public ViewSettingsDialog(JFrame parent, DeviceProxy managerProxy, String pipeName) throws DevFailed {
		super(parent, true);
		initComponents();
		applyBtn.setVisible(false);

		//  Select file name
		String fileName = selectFile(managerProxy);
		if (fileName!=null) {
			PipeBlob pipeBlob = new PipeBlob("ReadFile");
			pipeBlob.add(new PipeDataElement("FileName", fileName));
			//	ToDo Must be done in one call
			int timeOut = managerProxy.get_timeout_millis();
			managerProxy.set_timeout_millis(Utils.getDefaultTimeout());
			managerProxy.writePipe(pipeName, pipeBlob);
			DevicePipe devicePipe = managerProxy.readPipe(pipeName);
			String content = PipeToString.getString(devicePipe);
			managerProxy.set_timeout_millis(timeOut);

			contentTextArea.setText(content);
			//	resize text area
			contentScrollPane.setPreferredSize(
					Utils.getTextDimension(contentTextArea, maxDimension));

			selectedFile = fileName;
			titleLabel.setText(fileName);
			pack();
			ATKGraphicsUtils.centerDialog(this);
		}
		else {
			cancelBtnActionPerformed(null);
		}
	}
	//===============================================================
	/**
	 *	Creates new form ViewSettingsDialog
	 */
	//===============================================================
	@SuppressWarnings("unused")
	public ViewSettingsDialog(JFrame parent, DeviceProxy managerProxy, boolean apply) throws DevFailed {
		this(parent, managerProxy, apply, null, null, null);
	}
	//===============================================================
	//===============================================================
	public ViewSettingsDialog(JFrame parent,
							  DeviceProxy managerProxy,
							  boolean apply, String fileName,
							  String title, String approveButtonText) throws DevFailed {
		super(parent, true);
		initComponents();
		applyBtn.setVisible(apply);
		this.title = title;
		if (approveButtonText!=null)
			this.approveButtonText = approveButtonText;

		//  Select file name
		if (fileName==null)
			fileName = selectFile(managerProxy);
		if (fileName!=null) {
			// Display file name then read and display file content
			titleLabel.setText("File: " + fileName);
			String content;
			content = getFileContent(managerProxy, fileName);
			contentTextArea.setText(Utils.checkLinesLength(content));

			//	resize text area
			contentScrollPane.setPreferredSize(
					Utils.getTextDimension(contentTextArea, maxDimension));

			selectedFile = fileName;
			pack();
			ATKGraphicsUtils.centerDialog(this);
		}
		else {
			cancelBtnActionPerformed(null);
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
	private String  selectFile(DeviceProxy managerProxy) throws DevFailed {
		try {
			DbDatum datum = managerProxy.get_property("SettingsFilesPath");
			FileBrowserDialog dialog =
					new FileBrowserDialog(this, managerProxy, datum.extractString());
			dialog.setApproveButtonText(approveButtonText);
			if (title!=null) {
			    dialog.setTitle(title);
                dialog.setDialogTitle(title);
            }
			if (dialog.showDialog()==JOptionPane.OK_OPTION) {
				return dialog.getSelectedFile();
			}
		} catch (DevFailed e) {
			ErrorPane.showErrorMessage(this, null, e);
		}
		return null;
	}
	//===============================================================
	//===============================================================
	@SuppressWarnings("unused")
    public void setApproveButtonText(String text) {
	    //  Used by programmer to customize
	    approveButtonText = text;
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
        javax.swing.JPanel bottomPanel = new javax.swing.JPanel();
        applyBtn = new javax.swing.JButton();
        javax.swing.JButton cancelBtn = new javax.swing.JButton();
        contentScrollPane = new javax.swing.JScrollPane();
        contentTextArea = new javax.swing.JTextArea();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        titleLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        titleLabel.setText("Dialog Title");
        topPanel.add(titleLabel);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

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

        contentTextArea.setEditable(false);
        contentTextArea.setColumns(20);
        contentTextArea.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        contentTextArea.setRows(5);
        contentScrollPane.setViewportView(contentTextArea);

        getContentPane().add(contentScrollPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	//===============================================================
	//===============================================================
    @SuppressWarnings("UnusedParameters")
	private void applyBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyBtnActionPerformed
		doClose();
	}//GEN-LAST:event_applyBtnActionPerformed
	//===============================================================
	//===============================================================
	@SuppressWarnings("UnusedParameters")
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
		selectedFile = null;
		doClose();
	}//GEN-LAST:event_cancelBtnActionPerformed

	//===============================================================
	//===============================================================
    @SuppressWarnings("UnusedParameters")
	private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		selectedFile = null;
		doClose();
	}//GEN-LAST:event_closeDialog

	//===============================================================
	/**
	 *	Closes the dialog
	 */
	//===============================================================
	private void doClose() {
		setVisible(false);
		dispose();
	}
	//===============================================================
	//===============================================================
	public String showDialog() throws DevFailed {
		if (selectedFile!=null)
			setVisible(true);
		return selectedFile;
	}

	//===============================================================
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyBtn;
    private javax.swing.JScrollPane contentScrollPane;
    private javax.swing.JTextArea contentTextArea;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
	//===============================================================
}
