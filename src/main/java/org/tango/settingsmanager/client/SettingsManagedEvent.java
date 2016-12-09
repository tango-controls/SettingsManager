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


//===============================================================

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoDs.Except;

import java.util.Date;

/**
 *	@author  Pascal Verdier
 */
//===============================================================


@SuppressWarnings("MagicConstant")
public class SettingsManagedEvent {
    private DevFailed devFailed;
    private DevState  state;
    private String    status;
    private String    fileName;
    private int       action;
    private long      time;
    //===============================================================
    //===============================================================
    public SettingsManagedEvent(String fileName,
                                DevState state, String status, int action, DevFailed devFailed) {
        this.fileName = fileName;
        this.state  = state;
        this.status = status;
        this.action = action;
        this.devFailed = devFailed;
        time = System.currentTimeMillis();
    }
    //===============================================================
    /**
     * @return time in milliseconds when event has been generated
     */
    //===============================================================
    public long getTime() {
        return time;
    }
    //===============================================================
    /**
     * @return the applied/generated file name
     */
    //===============================================================
    public String getFileName() {
        return fileName;
    }
    //===============================================================
    /**
     * @return true if an exception occurs during applied/generated,
     *          false otherwise
     */
    //===============================================================
    public boolean hasFailed() {
        return devFailed!=null;
    }
    //===============================================================
    /**
     * @return exception during applied/generated if any, null if no exception
     */
    //===============================================================
    public DevFailed getDevFailed() {
        return devFailed;
    }
    //===============================================================
    /**
     * @return settings manager state
     */
    //===============================================================
    public DevState getState() {
        return state;
    }
    //===============================================================
    /**
     * @return settings manager status
     */
    //===============================================================
    public String getStatus() {
        return status;
    }
    //===============================================================
    /**
     * @return SettingsManagerClient.APPLIED or SettingsManagerClient.GENERATED
     */
    //===============================================================
    public int getAction() {
        return action;
    }
    //===============================================================
    //===============================================================
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getAction()==SettingsManagerClient.APPLIED)
            sb.append("Applying file ");
        else
            sb.append("Generating file ");
        sb.append(getFileName()).append(" ").append(new Date(getTime())).append("\n");
        if (hasFailed()) {
            sb.append(Except.str_exception(getDevFailed()));
        }
        else {
            sb.append(getStatus());
        }
        return sb.toString();
    }
    //===============================================================
    //===============================================================
}
