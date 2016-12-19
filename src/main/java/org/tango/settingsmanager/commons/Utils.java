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

package org.tango.settingsmanager.commons;


import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.*;
import fr.esrf.TangoDs.Except;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class is able to propose a set of static methods
 *
 * @author verdier
 */

public class Utils {
    private static final int wPadding = 30;
    private static final int hPadding = 2;
    private static boolean debugMode;
    //===============================================================
    /*
     *  Initialize the timeout value for client
     */
    //===============================================================
    private static int defaultTimeout = ICommons.DEFAULT_TIMEOUT;
    static {
        String s = System.getProperty("SETTINGS_TIMEOUT");
        if (s==null)
            s = System.getenv("SETTINGS_TIMEOUT");
        if (s!=null) {
            try { defaultTimeout = Integer.parseInt(s); } catch (NumberFormatException e) { /* */ }
        }
        System.out .println("Default timeout value = " + defaultTimeout);
    }
    //===============================================================
    //===============================================================
    public static void debugTrace(String s) {
        if (debugMode) System.out.println(s);
    }
    //===============================================================
    //===============================================================
    public static void setDebugMode(boolean b) {
        debugMode = b;
    }
    //===============================================================
    //===============================================================
    public static int getDefaultTimeout() {
        return defaultTimeout;
    }
    //===============================================================
    //===============================================================
    public static void setDefaultTimeout(int defaultTimeout) {
        Utils.defaultTimeout = defaultTimeout;
    }
    //===============================================================
    //===============================================================
    public static String getDisplayFileName(String fileName) {
        if (fileName.startsWith("/")) fileName = fileName.substring(1);
        if (fileName.endsWith(".ts")) fileName = fileName.substring(0, fileName.length()-3);
        return fileName;
    }
    //===============================================================
    /**
     * SettingsTimeout is between manager and devices
     */
    //===============================================================
    private static int settingsTimeout = 3000;
    public static void setSettingsTimeout(int ms) {
        settingsTimeout = ms;
    }
    //===============================================================
    //===============================================================
    public static int getSettingsTimeout() {
        return settingsTimeout;
    }
    //===============================================================
    /**
     * @param textArea specified text area
     * @param maxDimension maximum dimension to be used
     * @return the specified text dimension to be displayed
     */
    //===============================================================
    public static Dimension getTextDimension(JTextArea textArea, Dimension maxDimension) {
        Font font = textArea.getFont();
        // get metrics from the graphics
        FontMetrics metrics = textArea.getGraphics().getFontMetrics(font);
        int width  =  metrics.stringWidth(getLongestLine(textArea.getText())) + wPadding;
        int height = (metrics.getHeight()+ hPadding) * countLines(textArea.getText());
        if (width>maxDimension.width)
            width = maxDimension.width;
        if (height>maxDimension.height)
            height = maxDimension.height;

        return new Dimension(width, height);
    }
    //===============================================================
    //===============================================================
    private static int countLines(String str) {
        StringTokenizer stk = new StringTokenizer(str, "\n");
        return stk.countTokens();
    }
    //===============================================================
    //===============================================================
    private static String getLongestLine(String text) {
        StringTokenizer stk = new StringTokenizer(text, "\n");
        int max = 0;
        String str = "";
        while (stk.hasMoreTokens()) {
            String s = stk.nextToken();
            int length = s.length();
            if (length>max) {
                max = length;
                str = s;
            }
        }
        return str;
    }
    //===============================================================
    //===============================================================
    public static String getSettingsRootPath() throws DevFailed {
        DbDatum datum = new DbClass("SettingsManager").get_property("RootPath");
        if (datum.is_empty())
            Except.throw_exception("PropertyNotSet", "Class property RootPath is not set");

        return datum.extractString();
    }
    //===============================================================
    //===============================================================
    public static void setSettingsRootPath(String rootPath) throws DevFailed {
        DbDatum datum = new DbDatum("RootPath", rootPath);
        new DbClass("SettingsManager").put_property(new DbDatum[] { datum });
    }
    //===============================================================
    //===============================================================
    public static List<String> getSettingsSystemList() throws DevFailed {

        //  Get list of admin devices to get server list
        String[] adminDevices = ApiUtil.get_db_obj().get_device_list("dserver/"+ICommons.className+"/*");
        List<String> list = new ArrayList<>();
        for (String adminDevice : adminDevices) {
            DbServer server = new DbServer(adminDevice.substring(adminDevice.indexOf('/')+1));
            //  And then for each server get device list
            String[] devices = server.get_device_name(ICommons.className);
            Collections.addAll(list, devices);
        }
        return list;
    }
    //===============================================================
    //===============================================================
    public static String getSettingsPath(DeviceProxy managerProxy) throws DevFailed {
        DeviceAttribute attribute = managerProxy.read_attribute("SettingsPath");
        return attribute.extractString();
    }
    //===============================================================
    /**
     * Check if OS is Windows
     *
     * @return true if OS is windows
     */
    //===============================================================
    public static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().startsWith("windows");
    }
    //===============================================================
    /**
     * Open a file and return text read.
     *
     * @param fileName file to be read.
     * @return the file content read lines.
     * @throws DevFailed in case of failure during read file.
     */
    //===============================================================
    public static List<String> readFileLines(String fileName) throws DevFailed {
        String code = readFile(fileName);
        List<String> lines = new ArrayList<>();
        StringTokenizer stk = new StringTokenizer(code, "\n");
        while (stk.hasMoreTokens())
            lines.add(stk.nextToken());
        return lines;
    }
    //===============================================================
    /**
     * Open a file and return text read.
     *
     * @param fileName file to be read.
     * @return the file content read.
     * @throws DevFailed in case of failure during read file.
     */
    //===============================================================
    public static String readFile(String fileName) throws DevFailed {
        String str = "";
        try {
            FileInputStream inputStream = new FileInputStream(fileName);
            int nb = inputStream.available();
            byte[] inStr = new byte[nb];
            nb = inputStream.read(inStr);
            inputStream.close();

            if (nb > 0)
                str = takeOffWindowsChar(inStr);
        } catch (Exception e) {
            Except.throw_exception(e.toString(), e.toString());
        }
        return str;
    }
    //===============================================================
    /**
     * Take off Cr eventually added by Windows editor.
     *
     * @param b_in specified byte array to be modified.
     * @return the modified byte array as String.
     */
    //===============================================================
    public static String takeOffWindowsChar(byte[] b_in) {
        //	Take off Cr (0x0d) eventually added by Windows editor
        int nb = 0;
        for (byte b : b_in)
            if (b != 13)
                nb++;
        byte[] b_out = new byte[nb];
        for (int i=0, j=0 ; i<b_in.length; i++)
            if (b_in[i] != 13)
                b_out[j++] = b_in[i];
        return new String(b_out);
    }

    //===============================================================
    //===============================================================
    private static String checkOsFormat(String code) {
        if (Utils.isWindows())
            return setWindowsFileFormat(code);
        else
            return code;
    }

    //===============================================================
    //===============================================================
    public static String setWindowsFileFormat(String code) {
        //	Convert default Unix format to Windows format
        byte[] b = {0xd, 0xa};
        String lsp = new String(b); //System.getProperty("line.separator");
        code = code.replaceAll("\n", lsp);
        return code;
    }

    //===============================================================
    //===============================================================
    public static void writeFile(String fileName, String code) throws DevFailed {
        try {
            code = checkOsFormat(code);
            FileOutputStream outputStream = new FileOutputStream(fileName);
            outputStream.write(code.getBytes());
            outputStream.close();
        } catch (Exception e) {
            Except.throw_exception(e.toString(), e.toString());
        }
    }
    //===============================================================
    //===============================================================
    public static String getLinuxPath(final String path) {
        if (isWindows()) {
            StringBuilder sb = new StringBuilder();
            int start = 0;
            if (path.charAt(1)==':')
                start = 2;
            int end;
            while ((end=path.indexOf('\\', start))>0) {
                sb.append(path.substring(start, end)).append('/');
                start = end+1;
            }
            sb.append(path.substring(start));
            return sb.toString();
        }
        else
            return path;
    }
    //===============================================================
    //===============================================================
    public static String settingsFile(String fileName) throws DevFailed {
        if (fileName.contains(".."))
            Except.throw_exception("BadPath",
                    fileName + " is out of RootPath class property");
        if (!fileName.endsWith("." + ICommons.extension))
            fileName += "." + ICommons.extension;
        return fileName;
    }
    //===============================================================
    //===============================================================
}
