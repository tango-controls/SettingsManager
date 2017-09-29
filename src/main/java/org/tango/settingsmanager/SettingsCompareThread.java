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

package org.tango.settingsmanager;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoDs.Except;
import org.tango.settingsmanager.commons.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * This class is a thread to compare device settings and
 * settings file content and set an alarm if at least one setting haa changed.
 * The comparison is done on strings to avoid round number.
 *
 * @author verdier
 */

public class SettingsCompareThread extends Thread {
    private FileGenerator fileGenerator;
    private boolean endOfThread = false;
    private List<FileParser.Attribute> appliedAttributes;
    private String errorMessage = null;
    private String lastErrorMessage = null;
    private List<String> alarmAttributes = new ArrayList<>();
    private List<String> lastAlarmAttributes = new ArrayList<>();
    private int period;
    private final Object monitor = new Object();
    //===============================================================
    /**
     *
     * @param fileName settings file to compare with real settings
     * @param period   Period to compare
     * @throws DevFailed in case of file not found or bad parameters
     */
    //===============================================================
    public SettingsCompareThread(String fileName, int period, boolean useFormat) throws DevFailed {
        //  Read file an keep content
        List<String> fileContent = getLineList(Utils.readFile(fileName));
        appliedAttributes = new FileParser().parseAttributes(fileContent);
        //  Get attribute list to manage
        List<String> attributes = getAttributeList(fileContent);
        //  Create a file generator to compare generated code and file content
        //  With same attribute list than applied
        fileGenerator = new FileGenerator(attributes, useFormat);
        this.period = period;
    }
    //===============================================================
    //===============================================================
    private static List<String> getAttributeList(List<String> lines) {
        List<String> attributes = new ArrayList<>();
        for (String line : lines) {
            int idx = line.indexOf(':');
            if (idx>0) {
                String name = line.substring(0, idx);
                StringTokenizer stk = new StringTokenizer(name, "/");
                if (stk.countTokens()==4)   //  Attribute (4 fields)
                    attributes.add(name);
            }
        }
        return attributes;
    }
    //===============================================================
    //===============================================================
    private static List<String> getLineList(String code) {
        StringTokenizer stk = new StringTokenizer(code, "\n");
        List<String> lines = new ArrayList<>();
        while (stk.hasMoreTokens()){
            String line = stk.nextToken().trim();
            if (!line.startsWith("#"))
                lines.add(line);
        }
        return lines;
    }
    //===============================================================
    //===============================================================
    public String[] getAlarmAttributes() {
        synchronized (monitor) {
            return lastAlarmAttributes.toArray(new String[lastAlarmAttributes.size()]);
        }
    }
    //===============================================================
    //===============================================================
    public synchronized void setPeriod(int period) {
        this.period = period;
        notify();
    }
    //===============================================================
    //===============================================================
    public synchronized void stopThread() {
        endOfThread = true;
        notify();
    }
    //===============================================================
    //===============================================================
    private synchronized void waitNextLoop() {
        try { wait(period*1000); } catch (InterruptedException e) { System.err.println(e.toString()); }
    }
    //===============================================================
    //===============================================================
    public void run() {
        waitNextLoop(); //  Wait a bit before getting settings
        while(!endOfThread) {
            errorMessage = null;
            try {
                TangoDeviceList deviceList = fileGenerator.generate();
                if (deviceList.getRunningState()==DevState.ALARM) {
                    errorMessage = "";
                    for (TangoDevice tangoDevice : deviceList) {
                        if (tangoDevice.getRunningState()==DevState.ALARM) {
                            errorMessage += tangoDevice.getDeviceName() + " failed\n"+
                                            tangoDevice.getStatus() + "\n";
                        }
                    }
                }
                else {
                    //  Get generated string and build attribute Objects
                    List<FileParser.Attribute> settings =
                            new FileParser().parseAttributes(getLineList(fileGenerator.getFileContent()));
                    alarmAttributes = compareSettings(settings);

                    Utils.debugTrace((alarmAttributes.isEmpty())?
                                "Nothing changed..." : "Attribute(s) changed...");
                }
            }
            catch (DevFailed e) {
                System.err.println(e.errors[0].desc);
                errorMessage = e.errors[0].desc;
            }
            synchronized (monitor) {
                lastAlarmAttributes.clear();
                lastAlarmAttributes.addAll(alarmAttributes);
                lastErrorMessage = errorMessage;
            }
            waitNextLoop();
        }
        Utils.debugTrace("-------------------> Thread exiting....");
    }
    //===============================================================
    //===============================================================
    private boolean done = false;
    private List<String> compareSettings(List<FileParser.Attribute> settings) throws DevFailed {
        if (settings.size() != appliedAttributes.size()) {
            if (!done) {
                for (int i=0 ; i<settings.size() ; i++)
                    System.out.println(i+": " + settings.get(i).getName());
                System.out.println("---------------------------------------------------");
                for (int i=0 ; i<appliedAttributes.size() ; i++)
                    System.out.println(i+": " + appliedAttributes.get(i).getName());
                done = true;
            }
            Except.throw_exception("CompareError",
                    "Apply and Read have different size\nAttributes could be badly initialized (?)");
        }
        List<String> alarmAttributes = new ArrayList<>();
        errorMessage = "";
        for (FileParser.Attribute appliedAttribute : appliedAttributes) {
            FileParser.Attribute settingsAttribute = null;
            for (FileParser.Attribute attribute : settings) {
                if (attribute.getName().equals(appliedAttribute.getName())) {
                    settingsAttribute = attribute;
                }
            }
            if (settingsAttribute==null)
                Except.throw_exception("CompareError", "Attribute " +appliedAttribute + " not found in settings");
            String str;
            if ((str=appliedAttribute.compareSettings(settingsAttribute))!=null) {
                alarmAttributes.add(appliedAttribute.getName());
                errorMessage += str+"\n";
            }
        }
        return alarmAttributes;
    }
    //===============================================================
    //===============================================================
    public boolean isAlarm() {
        synchronized (monitor) {
            return lastErrorMessage != null && !lastAlarmAttributes.isEmpty();
        }
    }
    //===============================================================
    //===============================================================
    public String getErrorMessage() {
        synchronized (monitor) {
            if (lastErrorMessage == null)
                return "";
            return lastErrorMessage.trim();
        }
    }
    //===============================================================
    //===============================================================


    //===============================================================
    //===============================================================
    public static void main(String[] args) {
        try {
            String fileName = "/segfs/tango/tmp/settings/test/toto.ts";
            SettingsCompareThread client = new SettingsCompareThread(fileName, 2, true);
            client.start();
        } catch (DevFailed e) {
            Except.print_exception(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
