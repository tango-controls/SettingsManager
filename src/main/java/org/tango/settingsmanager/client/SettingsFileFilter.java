//+======================================================================
//
// Project:   Tango
//
// Description:  Basic Dialog Class to define s file filter
//
// $Author: verdier $
//
// Copyright (C) :      2004,2005,2006,2007,2008,2009,2009,2010,2011,2012,2013,2014
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
// $Revision: $
// $Date:  $
//
// $HeadURL: $
//
//-======================================================================

package org.tango.settingsmanager.client;


import fr.esrf.TangoDs.Except;
import org.tango.settingsmanager.commons.ICommons;
import org.tango.settingsmanager.commons.Utils;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SettingsFileFilter extends FileFilter {

    private List<String> filters = new ArrayList<>();
    private String description = null;
    private String fullDescription = null;

    /**
     * Creates a file filter. If no filters are added, then all
     * files are accepted.
     *
     * @see #addExtension
     */
    public SettingsFileFilter() {

    }

    /**
     * Creates a file filter that accepts the given file type.
     * Example: new SettingsFileFilter("jpg", "JPEG Image Images");
     * <p/>
     * Note that the "." before the extension is not needed. If
     * provided, it will be ignored.
     *
     * @param extension   extension to filter
     * @param description filter description
     * @see #addExtension
     */
    public SettingsFileFilter(String extension, String description) {
        this();
        if (extension != null)
            addExtension(extension);
        if (description != null)
            setDescription(description);
    }

    /**
     * Return true if this file should be shown in the directory pane,
     * false if it shouldn't.
     * <p/>
     * Files that begin with "." are ignored.
     *
     * @see #getExtension
     * @see FileFilter
     */
    public boolean accept(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                return true;
            }
            String extension = getExtension(file);
            if (extension != null) {
                for (String filter : filters) {
                    if (filter.equals(extension)) {
                        try {
                            return isSettingsFile(file.getCanonicalPath());
                        } catch (IOException e) { /* */}
                    }
                }
            }
        }
        return false;
    }

    /**
     * Return the extension portion of the file's name .
     *
     * @param file file to br checked
     * @return return file extension
     * @see #getExtension
     * @see FileFilter#accept
     */
    static public String getExtension(File file) {
        if (file != null) {
            String filename = file.getName();
            return getExtension(filename);
        }
        return null;
    }

    /**
     * Return the extension portion of the file's name .
     *
     * @param fileName file to br checked
     * @return return file extention
     * @see #getExtension
     * @see FileFilter#accept
     */
    static public String getExtension(String fileName) {
        if (fileName != null) {
            int i = fileName.lastIndexOf('.');
            if (i>0 && i<fileName.length() - 1)
                return fileName.substring(++i).toLowerCase();
        }
        return null;
    }

    /**
     * Adds a filetype "dot" extension to filter against.
     * <p/>
     * For example: the following code will create a filter that filters
     * out all files except those that end in ".jpg" and ".tif":
     * <p/>
     * SettingsFileFilter filter = new SettingsFileFilter();
     * filter.addExtension("jpg");
     * filter.addExtension("tif");
     *
     * @param extension extention to be added.
     *                  <p/>
     *                  Note that the "." before the extension is not needed and will be ignored.
     */
    public void addExtension(String extension) {
        //noinspection unchecked
        filters.add(extension.toLowerCase());
        fullDescription = null;
    }


    /**
     * Returns the human readable description of this filter. For
     * example: "JPEG and GIF Image Files (*.jpg, *.gif)"
     *
     * @see FileFilter#getDescription
     */
    public String getDescription() {
        if (fullDescription == null) {
            if (description == null || isExtensionListInDescription()) {
                fullDescription = description == null ? "" : description + "  (";
                // build the description from the extension list
                //Enumeration extensions = filters.keys();
                for (int i = 0; i < filters.size(); i++) {
                    fullDescription += "*." + filters.get(i);
                    if (i < filters.size() - 1)
                        fullDescription += ", ";
                }
                fullDescription += ")";
            } else
                fullDescription = description;
        }
        return fullDescription;
    }

    /**
     * Sets the human readable description of this filter. For
     * example: filter.setDescription("Gif and JPG Images");
     *
     * @param description description to be added
     */
    public void setDescription(String description) {
        this.description = description;
        fullDescription = null;
    }

    /**
     * Returns whether the extension list (.jpg, .gif, etc) should
     * show up in the human readable description.
     * <p/>
     * Only relevent if a description was provided in the constructor
     * or using setDescription();
     *
     * @return whether the extension list (.jpg, .gif, etc) should
     *         show up in the human readable description.
     */
    public boolean isExtensionListInDescription() {
        return true;
    }


    //===============================================================
    /**
     * Return true if a Tango device impl class and generated by Pogo-6
     *
     * @param filename file's name to be checked.
     * @return true if specified file is a TANGO code source file generated by Pogo-6.
     */
    //===============================================================
    public static boolean isSettingsFile(String filename) {
        try {
            //	Read file content.
            String readCode = Utils.readFile(filename);

            //	Check if new POGO generated code (oAW)
            if (readCode.startsWith(ICommons.identifier)) {
                return true;
            }
        }
        catch (Exception e) {
            Except.print_exception(e);
            return true;
        }
        return false;
    }
}
