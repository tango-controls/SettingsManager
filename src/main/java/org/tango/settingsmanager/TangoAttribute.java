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

import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoDs.TangoConst;

import java.util.List;

/**
 * This class is able to define a Tango attribute
 *
 * @author verdier
 */

public class TangoAttribute implements TangoConst {
    protected String attributeName;
    protected String deviceName;
    protected DeviceAttribute deviceAttribute;
    protected String format = null;
    //===============================================================
    //===============================================================
    public TangoAttribute(String attributeName) {
        //  Split device and attribute names
        int index = attributeName.lastIndexOf('/');
        deviceName = attributeName.substring(0, index);
        this.attributeName = attributeName.substring(++index);
        deviceAttribute = new DeviceAttribute(this.attributeName);
    }
    //===============================================================
    //===============================================================
    public String getAttributeName() {
        return attributeName;
    }
    //===============================================================
    //===============================================================
    @SuppressWarnings("unused")
    public String getDeviceName() {
        return deviceName;
    }
    //===============================================================
    //===============================================================
    public DeviceAttribute getDeviceAttribute() {
        return deviceAttribute;
    }
    //===============================================================
    //===============================================================
    public void setDeviceAttribute(DeviceAttribute deviceAttribute) {
        this.deviceAttribute = deviceAttribute;
    }
    //===============================================================
    //===============================================================
    public String getFormat() {
        return format;
    }
    //===============================================================
    //===============================================================
    public void setFormat(String format) {
        this.format = format;
    }
    //===============================================================
    //===============================================================
}
