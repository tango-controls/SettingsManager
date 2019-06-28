//+======================================================================
// :  $
//
// Project:   Tango
//
// Description:  java source code for Tango manager tool.
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

package org.tango.settingsmanager;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoDs.Except;
import org.tango.settingsmanager.commons.*;

import java.util.Collections;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;


/**
 * This class is able to manage the settings file generation.
 *
 * @author verdier
 */

public class FileGenerator {

    private TangoDeviceList tangoDeviceList;
    private String fileName = null;
    private String fullFileName = null;
    private String author = "";
    private List<String> comments = new ArrayList<>();
    private DevFailed devFailed = null;
    private boolean writeFile = true;
    private String fileContent;
    //===============================================================
    /**
     * Create object to generate a string like a settings file for specified attributes
     * @param attributeNames list of attribute for settings
     * @throws DevFailed i case of syntax error
     */
    //===============================================================
    public FileGenerator(List<String> attributeNames, boolean useFormat) throws DevFailed {
        this(attributeNames.toArray(new String[0]), useFormat);
    }
    //===============================================================
    /**
     * Create object to generate a string like a settings file for specified attributes
     * @param attributeNames list of attribute for settings
     * @throws DevFailed i case of syntax error
     */
    //===============================================================
    public FileGenerator(String[] attributeNames, boolean useFormat) throws DevFailed {
        writeFile = false;

        if (attributeNames.length==0)
                Except.throw_exception("SyntaxError", "No attribute name specified");

        //  Build a device list
        tangoDeviceList = new TangoDeviceList(ICommons.GENERATE, useFormat);
        for (String  attributeName : attributeNames) {
            tangoDeviceList.addAttribute(attributeName);
        }
        //  And check if it is alive
        for (TangoDevice tangoDevice : tangoDeviceList) {
            tangoDevice.ping();
        }
    }
    //===============================================================
    /**
     * Create object to generate a settings file for specified attributes
     * @param inArgs see syntax in "WriteSettingsFile" command input parameter
     * @param path relative path to generate file
     * @param defaultAttributeList list of attribute for settings
     * @throws DevFailed i case of syntax error
     */
    //===============================================================
    public FileGenerator(String[] inArgs, String path, String[] defaultAttributeList, boolean useFormat) throws DevFailed {
        //  Parse file parameters
        List<String> attributeNames = new ArrayList<>();
        for (String element : inArgs) {
            String value = getElementValue(element);
            if (element.toLowerCase().startsWith("attribute"))
                attributeNames.add(value);
            else if (element.toLowerCase().startsWith("author"))
                author = value;
            else if (element.toLowerCase().startsWith("comments"))
                comments.add(value);
            else if (element.toLowerCase().startsWith("file"))
                fileName = value;
        }
        //  Verify coherency
        if (fileName == null)
            Except.throw_exception("SyntaxError", "No file name specified");
        fullFileName = Utils.settingsFile(path + '/' + fileName);

        //  Verify Attributes (if empty, Get them from default ones)
        if (attributeNames.isEmpty() && defaultAttributeList!=null) {
            Collections.addAll(attributeNames, defaultAttributeList);
        }
        if (attributeNames.isEmpty())
            Except.throw_exception("SyntaxError", "No attribute name specified");

        //  Build a device list
        tangoDeviceList = new TangoDeviceList(ICommons.GENERATE, useFormat);
        for (String  attributeName : attributeNames) {
            tangoDeviceList.addAttribute(attributeName);
        }
        //  And check if it is alive
        for (TangoDevice tangoDevice : tangoDeviceList) {
            tangoDevice.ping();
        }
    }
    //===============================================================
    //===============================================================
    public String getFileName() {
        return fileName;
    }
    //===============================================================
    //===============================================================
    private String getElementValue(String element) throws DevFailed {
        int idx = element.indexOf(':');
        if (idx<0)
            Except.throw_exception("SyntaxError", "Element \'" + element + "\' needs a colon separator");
        return element.substring(++idx).trim();
    }
    //===============================================================
    //===============================================================
    private String buildFileHeader() {
        StringBuilder sb = new StringBuilder(ICommons.identifier + "\n#\n");
        sb.append("#  Author: ").append(author).append('\n');
        sb.append("#  Date:   ").append(new Date().toString()).append('\n');
        sb.append("#\n");
        for (String s : comments)
            sb.append("#  ").append(s).append('\n');
        sb.append("#\n");
        return sb.toString();
    }
    //===============================================================
    //===============================================================
    public TangoDeviceList generate() {
        fileContent = "";
        tangoDeviceList.manageSettings();
        DevState generateState = tangoDeviceList.getRunningState();
        if (generateState==DevState.ON) {
            try {
                generatedSettingsFile();
            }
            catch (DevFailed e) {
                devFailed = e;
            }
        }
        return tangoDeviceList;
    }
    //===============================================================
    //===============================================================
    public DevState getGenerateState() {
        if (devFailed!=null)
            return DevState.ALARM;
        else
            return tangoDeviceList.getRunningState();
    }
    //===============================================================
    //===============================================================
    public String getGenerateStatus() {
        if (devFailed!=null)
            return devFailed.errors[0].desc;
        else
            return tangoDeviceList.getRunningStatus();
    }
    //===============================================================
    //===============================================================
    private void generatedSettingsFile() throws DevFailed {
        StringBuilder sb = new StringBuilder(buildFileHeader());
        for (TangoDevice tangoDevice : tangoDeviceList) {
            for (TangoAttribute attribute : tangoDevice) {
                ReadTangoAttribute readAttribute = (ReadTangoAttribute) attribute;
                sb.append(readAttribute.toProperty()).append('\n');
            }
        }
        fileContent = sb.toString();
        if (writeFile)
            Utils.writeFile(fullFileName, fileContent);

    }
    //===============================================================
    //===============================================================
    public String getFileContent() {
        return fileContent;
    }
    //===============================================================
    //===============================================================
}
