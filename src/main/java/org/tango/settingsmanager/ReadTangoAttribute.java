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
import fr.esrf.TangoDs.TangoConst;


/**
 * This class is able to define a Tango attribute
 * and its read value (to be written in a file)
 *
 * @author verdier
 */

public class ReadTangoAttribute extends TangoAttribute implements TangoConst {
    private String attributeName;
    private int nbRead;
    private int dimX, dimY;
    private String startLine = "";
    private static final String SemiColonSeparator = ":  ";
    //===============================================================
    //===============================================================
    public ReadTangoAttribute(String attributeName) throws DevFailed {
        super(attributeName);
        this.attributeName = attributeName;
        //  Starting line depends on attribute name length
        int length = attributeName.length() + SemiColonSeparator.length()-1;
        for (int i=0 ; i<length ; i++)
            startLine += " ";
        startLine += '\t';
    }
    //===============================================================
    //===============================================================




    //===============================================================
    /**
     * @return value as a property to be put in a file
     * @throws DevFailed
     */
    //===============================================================
    public String toProperty() throws DevFailed {
        StringBuilder sb = new StringBuilder(attributeName);
        try {
            sb.append(SemiColonSeparator);
            nbRead = deviceAttribute.getNbRead();
            dimX = deviceAttribute.getDimX();
            dimY = deviceAttribute.getDimY();
            Object[] values;
            switch (deviceAttribute.getType()) {
                case Tango_DEV_BOOLEAN:
                    boolean[] bv = deviceAttribute.extractBooleanArray();
                    values = new Object[bv.length];
                    for (int i = 0 ; i<bv.length ; i++) values[i] = bv[i];
                    sb.append(toProperty(values));
                    break;
                case Tango_DEV_UCHAR:
                    short[] ucv = deviceAttribute.extractShortArray();
                    values = new Object[ucv.length];
                    for (int i = 0 ; i<ucv.length ; i++) values[i] = ucv[i];
                    sb.append(toProperty(values));
                    break;
                case Tango_DEV_SHORT:
                    short[] sv = deviceAttribute.extractShortArray();
                    values = new Object[sv.length];
                    for (int i = 0 ; i<sv.length ; i++) values[i] = sv[i];
                    sb.append(toProperty(values));
                    break;
                case Tango_DEV_USHORT:
                    int[] usv = deviceAttribute.extractUShortArray();
                    values = new Object[usv.length];
                    for (int i = 0 ; i<usv.length ; i++) values[i] = usv[i];
                    sb.append(toProperty(values));
                    break;
                case Tango_DEV_LONG:
                    int[] iv = deviceAttribute.extractLongArray();
                    values = new Object[iv.length];
                    for (int i = 0 ; i<iv.length ; i++) values[i] = iv[i];
                    sb.append(toProperty(values));
                    break;
                case Tango_DEV_ULONG:
                    long[] uiv = deviceAttribute.extractULongArray();
                    values = new Object[uiv.length];
                    for (int i = 0 ; i<uiv.length ; i++) values[i] = uiv[i];
                    sb.append(toProperty(values));
                    break;
                case Tango_DEV_LONG64:
                    long[] lv = deviceAttribute.extractLong64Array();
                    values = new Object[lv.length];
                    for (int i = 0 ; i<lv.length ; i++) values[i] = lv[i];
                    sb.append(toProperty(values));
                    break;
                case Tango_DEV_FLOAT:
                    float[] fv = deviceAttribute.extractFloatArray();
                    values = new Object[fv.length];
                    for (int i = 0 ; i<fv.length ; i++) values[i] = fv[i];
                    sb.append(toProperty(values));
                    break;
                case Tango_DEV_DOUBLE:
                    double[] dv = deviceAttribute.extractDoubleArray();
                    values = new Object[dv.length];
                    for (int i = 0 ; i<dv.length ; i++) values[i] = dv[i];
                    sb.append(toProperty(values));
                    break;
                case Tango_DEV_ENUM:
                    short[] ev = deviceAttribute.extractShortArray();
                    values = new Object[ev.length];
                    for (int i = 0 ; i<ev.length ; i++) values[i] = ev[i];
                    sb.append(toProperty(values));
                    break;
                case Tango_DEV_STRING:
                    sb.append(manageString(deviceAttribute.extractStringArray()));
                    break;
                default:
                    Except.throw_exception("NotImplemented",
                            "Tango type " + deviceAttribute.getType() + " is not yet implemented");
            }
        }
        catch (DevFailed e) {
            e.errors[0].desc = deviceName+": " + e.errors[0].desc;
            System.err.println(attributeName +":  " + e.errors[0].desc);
            throw e;
        }
        return sb.toString();
    }
    //===============================================================
    //===============================================================
    private String toProperty(Object[] values) throws DevFailed {
        StringBuilder sb = new StringBuilder();
        int x = 0;
        for (int i=nbRead ; i<values.length ; i++) {
            //  Check if value must be formatted
            sb.append(values[i]);
            /*
            if (format==null)
                sb.append(values[i]);
            else
                sb.append(String.format(format, values[i]));
            */
            //  Check if image
            if (++x==dimX && dimY>1) {
                if (i<values.length-1) { // not the last one
                    x = 0;
                    sb.append(" \\\n").append(startLine);
                }
            }
            else
            if (i<values.length-1) { // not the last one
                sb.append(",");
            }
        }
        return sb.toString();
    }
    //===============================================================
    //===============================================================
    private String manageString(String[] values) throws DevFailed {
        StringBuilder sb = new StringBuilder();
        for (int i=nbRead ; i<values.length ; i++) {
            sb.append(values[i]);
            if (i<values.length-1)
                sb.append(" \\\n").append(startLine);
        }
        return sb.toString();
    }
    //===============================================================
    //===============================================================
    public String toString() {
        return attributeName;
    }
    //===============================================================
    //===============================================================
}
