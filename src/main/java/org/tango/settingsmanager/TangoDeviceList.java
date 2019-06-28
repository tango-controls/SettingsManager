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
import org.tango.settingsmanager.commons.ICommons;
import org.tango.settingsmanager.commons.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is able to define a Tango attribute
 * and its value to be written (read from file).
 *
 * @author verdier
 */

public class TangoDeviceList extends ArrayList<TangoDevice>  {
    private int mode;
    private boolean useFormat;
    //===============================================================
    //===============================================================
    public TangoDeviceList(int mode, boolean useFormat) {
        this.mode = mode;
        this.useFormat = useFormat;
    }
    //===============================================================
    //===============================================================
    @SuppressWarnings("unused")
    public int getMode() {
        return mode;
    }
    //===============================================================
    //===============================================================
    public void addAttribute(String attributeName) throws DevFailed {
        addAttribute(attributeName, null);
    }
    //===============================================================
    //===============================================================
    public void addAttribute(String attributeName, List<String[]> strValues) throws DevFailed {
        int index = attributeName.lastIndexOf('/');
        if (index<0)
            Except.throw_exception("SyntaxError", attributeName + " is not a valid attribute name");
        String deviceName = attributeName.substring(0, index);
        TangoDevice tangoDevice = null;

        //  Check if already exists
        for (TangoDevice device : this) {
            if (device.getDeviceName().equalsIgnoreCase(deviceName)) {
                tangoDevice = device;
            }
        }
        //  If not create and add it to the list
        if (tangoDevice==null) {
            tangoDevice = new TangoDevice(deviceName);
            add(tangoDevice);
        }

        //  Add attribute for specified device
        switch (mode) {
            case ICommons.APPLY:
            case ICommons.PIPE_CONTENT:
                ApplyTangoAttribute attribute = new ApplyTangoAttribute(attributeName, strValues);
                tangoDevice.add(attribute);
                break;
            case ICommons.GENERATE:
                tangoDevice.add(new ReadTangoAttribute(attributeName));
                break;
        }
        //  Add format to last attribute if needed
        if (useFormat) {
            String  attName = attributeName.substring(deviceName.length()+1);
            String[] format = tangoDevice.getAttributeFormat(attName);

            //  first is display format, second is display unit
            tangoDevice.get(tangoDevice.size()-1).setFormat(format[0]);
            try {
                double displayUnit = Double.parseDouble(format[1]);
                tangoDevice.get(tangoDevice.size()-1).setDisplayUnit(displayUnit);
            }
            catch (NumberFormatException e) { /* display unit not set */ }
            //System.out.println(deviceName+'/'+attributeName + ": " + format[0]
            //    + " -> display unit = " + tangoDevice.get(tangoDevice.size()-1).getDisplayUnit());
        }
    }
    //===============================================================
    //===============================================================
    public void reset() {
        for (TangoDevice device : this)
            device.reset();
    }
    //===============================================================
    //  Apply methods
    //===============================================================
    public DevState getRunningState() {
        List<DevState> states = new ArrayList<>();
        for (TangoDevice device : this) {
            states.add(device.getRunningState());
        }
        if (states.contains(DevState.MOVING))
            return DevState.MOVING;
        else
        if (states.contains(DevState.ALARM))
            return DevState.ALARM;
        else
            return DevState.ON;
    }
    //===============================================================
    //===============================================================
    public String getRunningStatus() {
        StringBuilder status = new StringBuilder();
        for (TangoDevice device : this) {
            String deviceStatus = device.getStatus();
            if (deviceStatus!=null)
                status.append(device.getDeviceName()).append(": ").append(deviceStatus).append("\n");
        }
        if (status.length() == 0) {
            status = new StringBuilder(ICommons.OK_MESSAGE);
        }
        else {
            status = new StringBuilder(ICommons.actionName[mode] + " settings failed:\n" + status.toString().trim());
        }
        return status.toString();
    }
    //===============================================================
    //===============================================================
    public void manageSettings() {
        //  Do action on each device (start a thread to do it)
        for (TangoDevice device : this) {
            switch (mode) {
                case ICommons.APPLY:
                    device.applySettings();
                    break;
                case ICommons.GENERATE:
                    device.readSettings();
                    break;
                case ICommons.PIPE_CONTENT:
                    device.fileContentPipe();
                    break;
            }
        }

        //  Wait for end of all threads
        Thread thread;
        for (TangoDevice device : this) {
            if (mode==ICommons.GENERATE)
                thread = device.getReadingThread();
            else
                thread = device.getApplyingThread();
            try {
                thread.join(Utils.getDefaultTimeout());
            } catch (InterruptedException e) { /* */ }
        }
    }
    //===============================================================
    //===============================================================
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (TangoDevice device : this)
            sb.append(device).append('\n');
        return sb.toString().trim();
    }
    //===============================================================
    //===============================================================
}
