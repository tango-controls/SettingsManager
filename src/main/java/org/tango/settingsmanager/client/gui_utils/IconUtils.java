//+======================================================================
// $Source: /segfs/tango/cvsroot/jclient/RipsProto/src/rips_proto/commons/Utils.java,v $
//
// Project:   Tango
//
// Description:  java source code for main swing class.
//
// $Author: verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2009,2010,2011,2012,2013,2014,2015,2016
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
// $Revision: 1.3 $
//
//-======================================================================

package org.tango.settingsmanager.client.gui_utils;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.Except;

import javax.swing.*;
import java.awt.*;

public class IconUtils {
    private static IconUtils instance = null;
    private static final String DefaultImagePath = "/org/tango/settingsmanager/client/";
    //======================================================================
    //======================================================================
    private IconUtils() {
    }
    //======================================================================
    //======================================================================
    public static IconUtils getInstance() {
        if (instance==null)
            instance = new IconUtils();
        return instance;
    }
    //===============================================================
    //===============================================================
    public ImageIcon getIcon(String filename) throws DevFailed {
        java.net.URL url =
                getClass().getResource(DefaultImagePath + filename);
        if (url == null) {
            Except.throw_exception("FILE_NOT_FOUND",
                    "Icon file  " + filename + "  not found");
            return null;
        }

        return new ImageIcon(url);
    }

    //===============================================================
    //===============================================================
    public ImageIcon getIcon(String filename, double ratio) throws DevFailed {
        ImageIcon icon = getIcon(filename);
        return getIcon(icon, ratio);
    }

    //===============================================================
    //===============================================================
    public ImageIcon getIcon(ImageIcon icon, double ratio) {
        if (icon != null) {
            int width = icon.getIconWidth();
            int height = icon.getIconHeight();

            width = (int) (ratio * width);
            height = (int) (ratio * height);

            icon = new ImageIcon(
                    icon.getImage().getScaledInstance(
                            width, height, Image.SCALE_SMOOTH));
        }
        return icon;
    }
    //======================================================================
    //======================================================================
}
