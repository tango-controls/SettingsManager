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
import fr.esrf.TangoApi.AttributeInfoEx;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoDs.TangoConst;
import org.tango.settingsmanager.commons.ICommons;
import org.tango.settingsmanager.commons.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is able to define a Tango device as a list of attributes.
 *
 * @author verdier
 */

public class TangoDevice extends ArrayList<TangoAttribute> implements TangoConst {
    private DeviceProxy proxy;
    private List<TangoAttribute> attributeList = this;
    private String deviceName;
    private DevState runningState = DevState.ON;
    private String status=null;
    private ApplyingThread applyingThread;
    private ReadingThread readingThread;
    //===============================================================
    //===============================================================
    public TangoDevice(String deviceName) throws DevFailed {
        this.deviceName = deviceName;
        proxy = new DeviceProxy(deviceName);
        proxy.set_timeout_millis(Utils.getSettingsTimeout());
    }
    //===============================================================
    //===============================================================
    public String getDeviceName() {
        return deviceName;
    }
    //===============================================================
    //===============================================================
    DevState getRunningState() {
        return runningState;
    }
    //===============================================================
    //===============================================================
    public String getStatus() {
        return status;
    }
    //===============================================================
    //===============================================================
    public long ping() throws DevFailed {
        return proxy.ping();
    }
    //===============================================================
    //===============================================================
    public String getAttributeFormat(String attributeName) throws DevFailed {
        //  Get attribute config for attribute
        AttributeInfoEx info = proxy.get_attribute_info_ex(attributeName);
        return info.format;
    }
    //===============================================================
    //===============================================================
    public void reset() {
        status = null;
        runningState = DevState.ON;
    }
    //===============================================================
    //===============================================================
    public ReadingThread getReadingThread() {
        return readingThread;
    }
    //===============================================================
    //===============================================================
    public ApplyingThread getApplyingThread() {
        return applyingThread;
    }
    //===============================================================
    //===============================================================
     void applySettings() {
         Utils.debugTrace(this.toString());
         applyingThread = new ApplyingThread(ICommons.APPLY);
         applyingThread.start();
    }
    //===============================================================
    //===============================================================
     void fileContentPipe() {
         Utils.debugTrace(this.toString());
         applyingThread = new ApplyingThread(ICommons.PIPE_CONTENT);
         applyingThread.start();
    }
    //===============================================================
    //===============================================================
     void readSettings() {
         readingThread = new ReadingThread();
         readingThread.start();
    }
    //===============================================================
    //===============================================================
    public String toString() {
        StringBuilder sb = new StringBuilder(deviceName+":\n");
        for (TangoAttribute attribute : this)
            sb.append(" - ").append(attribute).append('\n');
        return sb.toString().trim();
    }
    //===============================================================
    //===============================================================




    //===============================================================
    /*
     * A thread class to perform reading
     */
    //===============================================================
    private class ReadingThread extends Thread {
        //===========================================================
        public void run() {
            //System.out.println("Reading " + deviceName + " : " + attributeList.size() + " device(s)");
            runningState = DevState.MOVING;
            try {
                //  Build attribute list
                String[] attributeNames = new String[attributeList.size()];
                for (int i=0 ; i<attributeList.size() ; i++) {
                    attributeNames[i] = attributeList.get(i).attributeName;
                }
                //  Read attributes and set each TangoAttribute
                DeviceAttribute[] deviceAttributes = proxy.read_attribute(attributeNames);
                for (int i=0 ; i<attributeList.size() ; i++) {
                    attributeList.get(i).setDeviceAttribute(deviceAttributes[i]);
                }
                runningState = DevState.ON;

            } catch (DevFailed e) {
                status = e.errors[0].desc;
                runningState = DevState.ALARM;
                System.err.println(status);
            }
        }
    }
    //===============================================================
    //===============================================================





    //===============================================================
    /*
     * A thread class to perform applying
     */
    //===============================================================
    private class ApplyingThread extends Thread {
        private int mode;
        //===========================================================
        private ApplyingThread(int mode) {
            this.mode = mode;
        }
        //===========================================================
        public void run() {
            runningState = DevState.MOVING;
            try {
                checkAttributeType();
                if (mode==ICommons.APPLY)
                    applySettings();
                runningState = DevState.ON;
            }
            catch (DevFailed e) {
                status = e.errors[0].desc;
                runningState = DevState.ALARM;
                System.err.println(status);
                e.printStackTrace();
            }
        }
        //===========================================================
        private void checkAttributeType() throws DevFailed {
            //  Build attribute list
            String[] attributeNames = new String[attributeList.size()];
            for (int i=0 ; i<attributeList.size() ; i++)
                attributeNames[i] = attributeList.get(i).getAttributeName();

            //  Get attribute config for attributes
            AttributeInfoEx[] info = proxy.get_attribute_info_ex(attributeNames);
            for (int i=0 ; i<attributeList.size() && i< info.length ; i++) {
                ApplyTangoAttribute attribute = (ApplyTangoAttribute) attributeList.get(i);
                //  And set each one
                attribute.setDataType(info[i]);
            }
        }
        //===========================================================
        private void applySettings() {
            try {
                //  Build an array of device attribute
                DeviceAttribute[] deviceAttributes = new DeviceAttribute[size()];
                int i = 0;
                for (TangoAttribute attribute : attributeList) {
                    ApplyTangoAttribute applyTangoAttribute = (ApplyTangoAttribute) attribute;
                    deviceAttributes[i++] = applyTangoAttribute.getDeviceAttribute();
                }
                //  And write attributes
                proxy.write_attribute(deviceAttributes);

                //  Check if at least one has failed
                for (DeviceAttribute deviceAttribute : deviceAttributes) {
                    if (deviceAttribute.hasFailed()) {
                        status = deviceAttribute.getName() + ": " + deviceAttribute.getErrStack()[0].desc;
                        runningState = DevState.ALARM;
                    }
                }
            }
            catch (DevFailed e) {
                status = e.errors[0].desc;
                runningState = DevState.ALARM;
            }
        }
        //===========================================================
    }
    //===============================================================
    //===============================================================
}
