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
import fr.esrf.TangoDs.Except;
import org.tango.settingsmanager.commons.ICommons;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class is able to define a Tango attribute
 *
 * @author verdier
 */

public class BrowseFiles {
    private List<BrowsedFile> fileList = new ArrayList<>();

    //===============================================================
    //===============================================================
    public BrowseFiles(String inputPath) throws DevFailed {
        File inputDir = new File(inputPath);
        if (!inputDir.isDirectory())
            Except.throw_exception("NotDirectory", inputPath + " is not a directory");

        String[] fileNames = inputDir.list();
        for (String fileName : fileNames)
            fileList.add(new BrowsedFile(new File(inputPath+'/'+fileName)));
        Collections.sort(fileList, new FileComparator());
    }
    //===============================================================
    //===============================================================
    public String[] toStringArray() {
        String[] array = new String[fileList.size()];
        int i=0;
        for (BrowsedFile file : fileList) {
            array[i++] = file.toString();
        }
        return array;
    }
    //===============================================================
    //===============================================================
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (BrowsedFile file : fileList) {
            sb.append(file).append('\n');
        }
        return sb.toString();
    }
    //===============================================================
    //===============================================================
    public static void main(String[] args) {
        try {
            System.out.println(new BrowseFiles("."));
        }
        catch (DevFailed e) {
            Except.print_exception(e);
        }
    }
    //===============================================================
    //===============================================================

    //===============================================================
    //===============================================================
    private class BrowsedFile {
        private String name;
        private boolean isDirectory;
        private BrowsedFile(File file) {
            this.name = file.getName();
            this.isDirectory = file.isDirectory();
        }
        public String toString() {
            return ((isDirectory)? ICommons.DIR_HEADER : ICommons.FILE_HEADER) + name;
        }
    }
    //===============================================================
    //===============================================================


    //======================================================
    /**
     * Comparators class to sort files
     * Dir first and alphabetic
     */
    //======================================================
    class FileComparator implements Comparator<BrowsedFile> {
        public int compare(BrowsedFile file1, BrowsedFile file2) {

            if (file1.isDirectory) {
                if (file2.isDirectory)
                    return file1.name.compareToIgnoreCase(file2.name);
                else
                    return -1;
            }
            else {
                if (file2.isDirectory)
                    return 1;
                else
                    return file1.name.compareToIgnoreCase(file2.name);
            }
        }
    }
}
