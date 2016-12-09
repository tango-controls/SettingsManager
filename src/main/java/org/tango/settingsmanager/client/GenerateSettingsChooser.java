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
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import org.tango.settingsmanager.client.gui_utils.SplashUtils;
import org.tango.settingsmanager.commons.ICommons;
import org.tango.settingsmanager.commons.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


//===============================================================
/**
 *	JFileChooser Class to get parameters to generate a settings file
 *
 *	@author  Pascal Verdier
 */
//===============================================================


public class GenerateSettingsChooser extends JFileChooser {
    private JFrame parent;
    private DeviceProxy managerProxy;
    private List<String> attributeList;
    private String settingsPath;
    private JTextField authorTextField;
    private JTextArea  commentsTextArea;
    private List<SettingsManagedListener> listenerList;
    private String fileName;

    private DevFailed devFailed;
    private boolean isStandAlone;

    private static final SettingsFileFilter settingsFileFilter =
            new SettingsFileFilter(ICommons.extension, "Settings files");
	//===============================================================
	/**
	 *	Creates new form InputSettingsParameters
	 */
	//===============================================================
	public GenerateSettingsChooser(JFrame parent,
                                   List<String> attributeList,
                                   DeviceProxy managerProxy,
                                   List<SettingsManagedListener> listenerList) throws DevFailed {
		super();
        this.parent = parent;
        this.attributeList = attributeList;
        this.managerProxy = managerProxy;
        this.listenerList = listenerList;
        // get settings path and set it to chooser
        settingsPath = Utils.getSettingsPath(managerProxy);
        setCurrentDirectory(new File(settingsPath));
        setFileFilter(settingsFileFilter);
        setApproveButtonText("Save Settings");

        //  Build additional panel
        add(buildParametersPanel(), BorderLayout.WEST);
        authorTextField.setText(System.getProperty("user.name"));

        //	Check if stand alone or from another application
        String s = System.getProperty("StandAlone");
        isStandAlone = (s!=null && s.equals("true"));
	}
	//===============================================================
    /**
     *  This method is called from within the constructor to initialize the form.
     */
	//===============================================================
    private JPanel buildParametersPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;

        //  Author
        JLabel authorLabel = new JLabel("Author :");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(10, 10, 10, 10);
        panel.add(authorLabel, constraints);

        authorTextField = new JTextField();
        authorTextField.setColumns(25);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.insets = new Insets(10, 0, 10, 30);
        panel.add(authorTextField, constraints);

        //  Comments
        JLabel commentsLabel= new JLabel("Comments :");
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.insets = new Insets(10, 10, 0, 10);
        panel.add(commentsLabel, constraints);

        commentsTextArea = new JTextArea();
        commentsTextArea.setColumns(30);
        commentsTextArea.setRows(8);
        JScrollPane scrollPane = new JScrollPane(commentsTextArea);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(0, 10, 10, 30);
        panel.add(scrollPane, constraints);

        return panel;
    }
    //===============================================================
    //===============================================================
    private String checkFileName(File file) throws DevFailed {
        //  Check path with settingsPath
        String absolutePath = Utils.getLinuxPath(file.getAbsolutePath());
        if (!absolutePath.startsWith(settingsPath)) {
            Except.throw_exception("BadPath", "Path is out of settings directory");
        }

        //  Check if already exists
        if (file.exists()) {
            if (JOptionPane.showConfirmDialog(this,
                    "file " + file.getName() + " already exists.\n\n" +
                            "Overwrite it ?", "",
                    JOptionPane.OK_CANCEL_OPTION)==JOptionPane.CANCEL_OPTION) {
                return null;
            }
        }
        //  return name from root dir
        return absolutePath.substring(settingsPath.length() + 1); // +1 for last '/'
    }
    //===============================================================
    //===============================================================
    private static void generateSettings(List<String> list, DeviceProxy managerProxy) throws DevFailed {
        int timeOut = managerProxy.get_timeout_millis();
        managerProxy.set_timeout_millis(Utils.getDefaultTimeout());
        DeviceData argIn = new DeviceData();
        argIn.insert(list.toArray(new String[list.size()]));
        managerProxy.command_inout("GenerateSettingsFile", argIn);
        managerProxy.set_timeout_millis(timeOut);
    }
    //===============================================================
    //===============================================================
    public static void generateSettingsFile(String fileName,
                                            String author,
                                            List<String> comments,
                                            List<String> attributeList,
                                            DeviceProxy managerProxy)throws DevFailed {
        List<String> list = new ArrayList<>();
        list.add("File: " + fileName);
        if (author!=null)
            list.add("Author: " + author);
        if (comments!=null)
            for (String comment : comments)
                list.add("Comments: " + comment);
        if (attributeList!=null)
            for (String attribute : attributeList)
                list.add("Attribute: " + attribute);
        generateSettings(list, managerProxy);
    }
    //===============================================================
    //===============================================================
    private void generateSettings(String fileName) throws DevFailed {
        if (isStandAlone) {
            SplashUtils.getInstance().startSplash();
            SplashUtils.getInstance().setSplashProgress(10, "Generating setting");
            SplashUtils.getInstance().startAutoUpdate();
        }

        //  Build the input arguments
        List<String> list = new ArrayList<>();
        list.add("File: " + fileName);
        if (!authorTextField.getText().isEmpty())
            list.add("Author: " + authorTextField.getText());
        if (!commentsTextArea.getText().isEmpty()) {
            StringTokenizer stk = new StringTokenizer(commentsTextArea.getText(), "\n");
            while (stk.hasMoreTokens())
                list.add("Comments: " + stk.nextElement());
        }
        //  Attributes from property or from code
        if (attributeList!=null)
            for (String attribute : attributeList)
                list.add("Attribute: " + attribute);

        //  OK to apply
        generateSettings(list, managerProxy);
        if (isStandAlone) SplashUtils.getInstance().stopSplash();
    }
    //===============================================================
    //===============================================================
    public String showChooser() throws DevFailed {
        int option = showOpenDialog(parent);
        if (option==JFileChooser.APPROVE_OPTION) {
            File file = getSelectedFile();
            if (file!=null) {
                if (!file.isDirectory()) {
                    if ((fileName = checkFileName(file))!=null) {
                        //subscribeForSettingsGenerated();
                        if (listenerList==null || listenerList.isEmpty())
                            generateSettings(fileName);
                        else {
                            //	Start a thread to wait end of thread to return results
                            new GenerateThread().start();
                        }
                    }
                    return fileName;
                }
            }
        }
        return null;
    }
    //===============================================================
    //===============================================================






    //=========================================================================
    //=========================================================================
    private class GenerateThread extends Thread {
        //=====================================================================
        public void run() {
            System.out.println("Generate in asynchronous mode");
            String managerStatus;
            DevState managerState;
            try {
                //  Generate the settings file
                generateSettings(fileName);
                managerState = DevState.ON;
                managerStatus = ICommons.OK_MESSAGE;
            }
            catch (DevFailed e) {
                if (isStandAlone) SplashUtils.getInstance().stopSplash();
                devFailed = e;
                managerState = DevState.ALARM;
                managerStatus = "Generate settings file failed";
            }
            final SettingsManagedEvent event =new SettingsManagedEvent(fileName,
                    managerState, managerStatus, SettingsManagerClient.GENERATED, devFailed);
            //  Call listeners
            for (SettingsManagedListener listener : listenerList) {
                listener.settingsManaged(event);
            }
        }
        //=====================================================================
    }
    //=========================================================================
    //=========================================================================
}
