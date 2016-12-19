//+======================================================================
// :  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool..
//
// : pascal_verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,
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
// :  $
//
//-======================================================================

package org.tango.settingsmanager.client;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.Except;
import org.tango.settingsmanager.client.gui_utils.SplashUtils;
import org.tango.settingsmanager.commons.ICommons;
import org.tango.settingsmanager.commons.Utils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


/**
 * This class is able to give a client class
 * for a majority of SettingsManager calls
 *
 * @author verdier
 */

public class SettingsManagerClient {
    private DeviceProxy managerProxy;
    private List<SettingsManagedListener> listeners = new ArrayList<>();

    public static final int APPLIED   = ICommons.APPLY;
    public static final int GENERATED = ICommons.GENERATE;
    //===============================================================
    /**
     * Constructor for a settings manage client.
     * @param settingsDeviceName SettingsManager device name or member
     * @throws DevFailed
     */
    //===============================================================
    public SettingsManagerClient(String settingsDeviceName) throws DevFailed {
        if (!settingsDeviceName.contains("/"))
            settingsDeviceName = ICommons.deviceHeader + settingsDeviceName;
        managerProxy = new DeviceProxy(settingsDeviceName);
    }
    //===============================================================
    /**
     * @return the timeout used to apply or generate settings
     */
    //===============================================================
    @SuppressWarnings("unused")
    public int getTimeout() {
        return Utils.getDefaultTimeout();
    }
    //===============================================================
    /**
     * Set the timeout used to apply or generate settings
     * @param timeout timeout value in milliseconds
     */
    //===============================================================
    @SuppressWarnings("unused")
    public void setTimeout(int timeout) {
        Utils.setDefaultTimeout(timeout);
    }
    //===============================================================
    //===============================================================
    public void addSettingsAppliedListener(SettingsManagedListener listener) {
        listeners.add(listener);
    }
    //===============================================================
    //===============================================================
    @SuppressWarnings("unused")
    public boolean removeSettingsAppliedListener(SettingsManagedListener listener) {
        return listeners.remove(listener);
    }
    //===============================================================
    /**
     * @return the last applied settings file
     */
    //===============================================================
    @SuppressWarnings("unused")
    public String getLastAppliedFile() throws DevFailed {
        return managerProxy.read_attribute("LastAppliedFile").extractString();
    }
    //===============================================================
    /**
     * @return the last generated settings file
     */
    //===============================================================
    @SuppressWarnings("unused")
    public String getLastGeneratedFile() throws DevFailed {
        return managerProxy.read_attribute("LastGeneratedFile").extractString();
    }
    //===============================================================
    /**
     * @return the settings path
     */
    //===============================================================
    @SuppressWarnings("unused")
    public String getSettingsPath() throws DevFailed {
        return managerProxy.read_attribute("SettingsPath").extractString();
    }
    //===============================================================
    /**
     * @return the settings manager device name
     */
    //===============================================================
    @SuppressWarnings("unused")
    public String getManagerDeviceName() {
        return managerProxy.name();
    }
    //===============================================================
    /**
     * @return the settings manager device proxy
     */
    //===============================================================
    @SuppressWarnings("unused")
    public DeviceProxy getManagerProxy() {
        return managerProxy;
    }
    //===============================================================
    /**
     * Create a GUI to generate a Settings file (!!! no parent frame --> close after generation)
     * @return selected file name if any, null otherwise
     * @throws DevFailed in case of generation has failed
     */
    //===============================================================
    @SuppressWarnings("unused")
    public String  generateSettingsFile() throws DevFailed {
        return generateSettingsFile(null, null);
    }
    //===============================================================
    /**
     * Create a GUI to generate a Settings file.
     * @param frame     parent frame.
     * @return selected file name if any, null otherwise
     * @throws DevFailed in case of generation has failed
     */
    //===============================================================
    @SuppressWarnings("unused")
    public String generateSettingsFile(JFrame frame) throws DevFailed {
        return generateSettingsFile(frame, null);
    }
    //===============================================================
    /**
     * Create a GUI to generate a Settings file.
     * @param frame     parent frame.
     * @param attributeList List of attribute if different of SettingManager property
     * @return selected file name if any, null otherwise
     * @throws DevFailed in case of generation has failed
     */
    //===============================================================
    public String  generateSettingsFile(JFrame frame, List<String> attributeList) throws DevFailed {
        return generateSettingsFile(frame, attributeList, null);
    }
    //===============================================================
    /**
     * Create a GUI to generate a Settings file.
     * @param frame     parent frame.
     * @param attributeList List of attribute if different of SettingManager property
     * @param fileName default file name to be generated
     * @return selected file name if any, null otherwise
     * @throws DevFailed in case of generation has failed
     */
    //===============================================================
    public String  generateSettingsFile(JFrame frame, List<String> attributeList, String fileName) throws DevFailed {
        GenerateSettingsBrowser    generateSettingsBrowser =
                    new GenerateSettingsBrowser(frame, attributeList, managerProxy, listeners);
        generateSettingsBrowser.setSelectedFile(fileName);
        return generateSettingsBrowser.showChooser();
    }
    //===============================================================
    /**
     * Generate a Settings file.
     * @param fileName settings file name
     * @param author settings author
     * @param comments settings comments
     * @param attributeList List of attribute if different of SettingManager property
     * @throws DevFailed in case of generation has failed
     */
    //===============================================================
    @SuppressWarnings("unused")
    public void generateSettingsFile(String fileName,
                                     String author,
                                     List<String> comments,
                                     List<String> attributeList)throws DevFailed {
        GenerateSettingsChooser.generateSettingsFile(
                fileName, author, comments, attributeList, managerProxy);
    }
    //===============================================================
    //===============================================================



    //===============================================================
    /**
     * Display a file chooser and  selected settings file content.
     * @param frame parent frame
     * @return selected file name if any, null otherwise
     * @throws DevFailed in case of read file has failed
     */
    //===============================================================
    public String readSettingsFileFromPipe(JFrame frame) throws DevFailed {
        return new ViewSettingsDialog(frame, managerProxy, ICommons.ContentPipeName).showDialog();
    }
    //===============================================================
    /**
     * Display a file chooser and  selected settings file content.
     * @param frame parent frame
     * @return selected file name if any, null otherwise
     * @throws DevFailed in case of read file has failed
     */
    //===============================================================
    public String viewSettingsFile(JFrame frame) throws DevFailed {
        return viewSettingsFile(frame, null);
    }
    //===============================================================
    /**
     * Display a file chooser and  selected settings file content.
     * @param frame parent frame
     * @param fileName file to be viewed
     * @return selected file name if any, null otherwise
     * @throws DevFailed in case of read file has failed
     */
    //===============================================================
    public String viewSettingsFile(JFrame frame, String fileName) throws DevFailed {
        return new ViewSettingsDialog(frame, managerProxy, false, fileName).showDialog();
    }
    //===============================================================
    /**
     * Display a file chooser and apply selected settings file.
     * @param frame parent frame
     * @return selected file name if any, null otherwise
     * @throws DevFailed in case of apply has failed
     */
    //===============================================================
    public String applySettings(JFrame frame) throws DevFailed {
        return applySettings(frame, null);
    }
    //===============================================================
    /**
     * Display a file chooser and apply selected settings file.
     * @param frame parent frame
     * @param fileName file to be applied
     * @return selected file name if any, null otherwise
     * @throws DevFailed in case of apply has failed
     */
    //===============================================================
    public String applySettings(JFrame frame, String fileName) throws DevFailed {
        return new ApplySettings(frame, managerProxy, listeners, fileName).apply();
    }
    //===============================================================
    /**
     * Display a list of settings systems found and their path
     * @throws DevFailed if read Database failed or property is missing
     */
    //===============================================================
    public static List<String> getSettingsProjectList() throws DevFailed {
        List<String> devices = Utils.getSettingsSystemList();
        List<String> projects = new ArrayList<>();
        for (String device : devices) {
            if (device.startsWith(ICommons.deviceHeader))
                projects.add(device.substring(device.lastIndexOf('/')+1));
            else
                projects.add(device);
        }
        return projects;
    }
    //===============================================================
    /**
     * Display a list of settings systems found and their path
     * @throws DevFailed if read Database failed or property is missing
     */
    //===============================================================
    public static void displaySettingsDeviceList() throws DevFailed {
        List<String> systems = Utils.getSettingsSystemList();
        for (String system : systems) {
            System.out.println(system);
        }
    }
    //===============================================================
    private boolean applied = false;
    //===============================================================
    private void settingsAppliedPerformed(SettingsManagedEvent event) {
        System.out.println("settingsAppliedPerformed()");
        System.out.println(event);
        applied = true;
    }
    //===============================================================
    //===============================================================



    //===============================================================
    //===============================================================
    private static void displaySyntax() {
        System.out.println("SettingsManagerClient  <option>  <settings system or device name>");
        System.out.println("Display a chooser to select a settings file on specified system");
        System.out.println("options are:");
        System.out.println("        -?  display settings system list");
        System.out.println("        -g  Generate a settings file");
        System.out.println("        -d  Display a settings file content");
        System.out.println("        -a  Apply a settings file");
        System.out.println("        -content  Read settings file and display content through a pipe");
        System.out.println("i.e: SettingsManagerClient  -a srco       Apply selected settings file on srco");

        System.exit(0);
    }
    //===============================================================
    //===============================================================
     public static void main(String[] args) {
        String settingsDeviceName = null;
        int mode = -1;
        //  Parse args
        for (String arg : args) {
            switch (arg) {
                case "-g":
                    mode = ICommons.GENERATE;
                    break;
                case "-d":
                    mode = ICommons.READ_CONTENT;
                    break;
                case "-a":
                    mode = ICommons.APPLY;
                    break;
                case "-content":
                    mode = ICommons.PIPE_CONTENT;
                    break;
                case "-?":
                    mode = ICommons.LIST_PROJECTS;
                    break;
                default:
                    settingsDeviceName = arg;
                    break;
            }
        }
        //  Check if args are OK
        if (settingsDeviceName==null || mode<0)
            displaySyntax();

        System.setProperty("StandAlone", "true");
        String selectedFile = null;
        try {
            final SettingsManagerClient client = new SettingsManagerClient(settingsDeviceName);
            client.addSettingsAppliedListener(new SettingsManagedListener() {
                @Override
                public void settingsManaged(SettingsManagedEvent e) {
                    client.settingsAppliedPerformed(e);
                }
            });
            //System.out.println(client.getSettingsPath());
            switch (mode) {
                case ICommons.READ_CONTENT:
                    selectedFile = client.viewSettingsFile(new JFrame());
                    break;
                case ICommons.GENERATE:
                    selectedFile = client.generateSettingsFile();
                    break;
                case ICommons.APPLY:
                    selectedFile = client.applySettings(new JFrame());
                    break;
                case ICommons.LIST_PROJECTS:
                    SettingsManagerClient.displaySettingsDeviceList();
                    break;
                case ICommons.PIPE_CONTENT:
                    client.readSettingsFileFromPipe(new JFrame());
                    break;
                default:
                    System.err.println("Mode is not implemented yet !");
            }

            if ((mode==ICommons.APPLY || mode==ICommons.GENERATE) && selectedFile!=null) {
                //  Wait listener called
                while (!client.applied) {
                    try { Thread.sleep(200); } catch (InterruptedException e) { /* */ }
                }
            }
        } catch (DevFailed e) {
            SplashUtils.getInstance().stopSplash();
            Except.print_exception(e);
        } catch (Exception e) {
            SplashUtils.getInstance().stopSplash();
            e.printStackTrace();
        }


        System.exit(0);
    }
}
