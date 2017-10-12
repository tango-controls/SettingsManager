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
import org.tango.settingsmanager.client.gui_utils.SplashUtils;
import org.tango.settingsmanager.commons.ICommons;
import org.tango.settingsmanager.commons.Utils;

import javax.swing.*;
import java.util.List;


//===============================================================
/**
 *	A class to select, display and apply settings file
 *
 *	@author  Pascal Verdier
 */
//===============================================================

@SuppressWarnings("MagicConstant")
public class ApplySettings {
	private DeviceProxy managerProxy;
	private String fileName;
	private List<SettingsManagedListener> listenerList;

	private DevFailed devFailed;
	private boolean isStandAlone;
	//===============================================================
	/**
	 *	Creates new form ApplySettingsDialog
	 */
	//===============================================================
	public ApplySettings(JFrame parent, DeviceProxy managerProxy,
						 List<SettingsManagedListener> listenerList,
						 String fileName, String title,
						 String approveButtonText) throws DevFailed {
		this.managerProxy = managerProxy;
		this.listenerList = listenerList;

		//	And select file
		ViewSettingsDialog dialog = new ViewSettingsDialog(parent,
                managerProxy, true, fileName, title, approveButtonText);
		this.fileName = dialog.getSelectedFile();

		//	Check if stand alone or from another application
		String s = System.getProperty("StandAlone");
		isStandAlone = (s!=null && s.equals("true"));
	}
	//===============================================================
	//===============================================================
	public String apply() throws DevFailed {
		//	If no file selected do nothing
		if (fileName==null)
			return null;

		//	Apply Settings
		if (listenerList==null || listenerList.isEmpty())
			applySettings();
		else {
			//	Start a thread to apply settings and call listeners
			new ApplyThread().start();
		}
		return fileName;
	}
	//=====================================================================
	//=====================================================================
	private void applySettings() throws DevFailed {
		if (isStandAlone) {
			SplashUtils.getInstance().startSplash();
			SplashUtils.getInstance().setSplashProgress(10, "Applying setting");
			SplashUtils.getInstance().startAutoUpdate();
		}
		int timeOut = managerProxy.get_timeout_millis();
		managerProxy.set_timeout_millis(Utils.getDefaultTimeout());
		//	Call manager device to apply
		DeviceData argIn = new DeviceData();
		argIn.insert(fileName);
		managerProxy.command_inout("ApplySettings", argIn);
		managerProxy.set_timeout_millis(timeOut);

		if (isStandAlone) SplashUtils.getInstance().stopSplash();
	}
	//===============================================================
	//===============================================================




	//=========================================================================
	//=========================================================================
	private class ApplyThread extends Thread {
		//=====================================================================
		public void run() {
			System.out.println("Apply in asynchronous mode");
			String managerStatus;
			DevState managerState;
			try {
				//	Apply settings
				applySettings();
				managerState = DevState.ON;
				managerStatus = ICommons.OK_MESSAGE;
			}
			catch (DevFailed e) {
				if (isStandAlone) SplashUtils.getInstance().stopSplash();
				devFailed = e;
				managerState = DevState.ALARM;
				managerStatus = "Apply settings failed";
			}
			final SettingsManagedEvent event = new SettingsManagedEvent(fileName,
					managerState, managerStatus, SettingsManagerClient.APPLIED, devFailed);
			//	Call listeners
			for (SettingsManagedListener listener : listenerList) {
				listener.settingsManaged(event);
			}
		}
		//=====================================================================
	}
	//=========================================================================
	//=========================================================================
}
