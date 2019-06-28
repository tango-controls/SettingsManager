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
import org.tango.settingsmanager.commons.Utils;

import java.util.*;


/**
 * This class is able to parse a settings file
 *
 * @author verdier
 */

public class FileParser {
    private int mode;
    private boolean applyAnyWay;
    private List<String> lines = new ArrayList<>();
    //===============================================================
    //===============================================================
    public FileParser(String fileName, int mode, boolean applyAnyWay) throws DevFailed {
        this.mode = mode;
        this.applyAnyWay = applyAnyWay;
        Utils.debugTrace("Parsing file: " + fileName);
        lines = Utils.readFileLines(fileName);
        if (!lines.get(0).startsWith(ICommons.identifier))
            Except.throw_exception("BadFile", fileName + " is not a settings file");
    }
    //===============================================================
    //===============================================================
    public TangoDeviceList parseAttributes(boolean useFormat) throws DevFailed {
        //  Parse lines from file
        List<Attribute> attributes = parseAttributes(lines);
        //  Convert to TangoAttributes
        TangoDeviceList deviceList = new TangoDeviceList(mode, useFormat);
        for (Attribute attribute : attributes) {
            try {
                deviceList.addAttribute(attribute.getName(), attribute.getStrValues());
            } catch (DevFailed e) {
                if (!applyAnyWay)
                    throw e;
            }
         }
        return deviceList;
    }
    //===============================================================
    //===============================================================
    public FileParser() {
    }
    //===============================================================
    //===============================================================
    public List<Attribute> parseAttributes(List<String> codeLines) throws DevFailed {
        List<Attribute> attributeStrings = new ArrayList<>();
        int x = 0;
        boolean multiLines = false;
        String attributeName= "";
        String strValues;
        List<String[]> valueLines = new ArrayList<>();
        for (String line : codeLines) {
            //  Check if not a comment line
            if (!line.startsWith("#")) {
                if (multiLines) {
                    strValues = line.trim();
                }
                else {
                    //  Single or first line
                    //  Get attribute name and value(s)
                    int index = line.indexOf(':');
                    if (index<0)
                        Except.throw_exception("SyntaxError",
                            "Syntax error in line " + x);
                    attributeName = line.substring(0, index).trim();
                    strValues = line.substring(++index).trim();
                }
                //  Check if values on multiple lines
                multiLines =  strValues.endsWith("\\");
                if (multiLines) {
                    //  Remove '\' char at end
                    strValues = strValues.substring(0, strValues.length()-1).trim();
                }
                //  Add values to list
                valueLines.add(parseLine(strValues));

                //  If not multiple line or last line
                if (!multiLines) {
                    //  Add attribute and it values
                    Attribute attribute = new Attribute(attributeName, valueLines);
                    attributeStrings.add(attribute);

                    //  Start a new line list
                    valueLines= new ArrayList<>();
                }
            }
            x++;
        }
        return attributeStrings;
    }
    //===============================================================
    //===============================================================
    private static String[] parseLine(String line) {
        StringTokenizer stk = new StringTokenizer(line, ",");
        List<String>    strValues = new ArrayList<>();
        while (stk.hasMoreTokens())
            strValues.add(stk.nextToken().trim());
        return strValues.toArray(new String[0]);
    }
    //===============================================================
    //===============================================================


    //===============================================================
    /**
     * This class is able to define a Tango attribute
     *  but with values as list of Strings to compare
     *  settings coming from file and from device
     *
     * @author verdier
     */
    //===============================================================
    public class Attribute extends ArrayList<String> {
        protected String name;
        protected List<String[]> strValues;
        //===============================================================
        public Attribute(String name, List<String[]> strValues) {
            this.name = name;
            this.strValues = strValues;
            for (String[] array : strValues) {
                Collections.addAll(this, array);
            }
        }
        //===============================================================
        public String getName() {
            return name;
        }
        //===============================================================
        public List<String[]> getStrValues() {
            return strValues;
        }
        //===============================================================
        public String  compareSettings(Attribute attribute) throws DevFailed {
            //  Compare name
            if (!attribute.getName().equals(name))
                Except.throw_exception("CompareError",
                        "Apply and Read have different name (?)\n"+
                                name + " != " + attribute.getName());

            //  Compare data size
            if (attribute.size()!=this.size())
                Except.throw_exception("CompareError", "Apply and Read have different size");

            //  Compare values
            for (int i=0 ; i<size() ; i++) {
                String set = attribute.get(i);
                String applied = this.get(i);
                if (!applied.equals(set)) {
                    return name + ": Apply and Read have different value " + set + " ( " + applied + " expected)";
                }
            }
            return null;
        }
        //===============================================================
        public String toString() {
            StringBuilder sb = new StringBuilder(name + ":  ");
            for (String[] str : strValues) {
                for (String s : str)
                    sb.append(s).append(", ");
            }
            return sb.toString();
        }
        //===============================================================
    }
    //===============================================================
    //===============================================================
}
