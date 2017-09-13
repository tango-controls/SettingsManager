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

/**
 * This class is able to propose a set of static methods
 *
 * @author verdier
 */

public interface ICommons {
    public static final String revNumber =
            "2.0  -  13-09-2017  15:20:39";
    String className = "SettingsManager";
    String extension = "ts";
    String identifier = "#  TANGO " + className + " file";
    String deviceHeader = "sys/settings/";
    String OK_MESSAGE = "Ready to manage settings";
    String ContentPipeName = "fileContent";

    int INIT = -1;
    int APPLY = 0;
    int GENERATE = 1;
    int READ_CONTENT = 2;
    int LIST_PROJECTS = 3;
    int PIPE_CONTENT = 4;

    int DEFAULT_TIMEOUT = 5000;
    String DIR_HEADER  = "DIR:";
    String FILE_HEADER = "FILE:";

    String[] actionName = { "Applying", "Generating", "Reading",
            "Listing projects", "Reading from pipe" };
}
