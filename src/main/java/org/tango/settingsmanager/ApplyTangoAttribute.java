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

import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.AttributeInfoEx;
import fr.esrf.TangoDs.Except;

import java.util.List;


/**
 * This class is able to define a Tango attribute
 * and its value to be applied
 *
 * @author verdier
 */

public class ApplyTangoAttribute extends TangoAttribute {
    protected AttributeInfoEx attributeInfo;
    protected List<String[]> strValues;
    private static final String NotInitialized = "Not initialised";
    //===============================================================
    //===============================================================
    public ApplyTangoAttribute(String attributeName, List<String[]> strValues) {
        super(attributeName);
        this.strValues = strValues;
    }
    //===============================================================
    //===============================================================
    public String getAttributeName() {
        return attributeName;
    }
    //===============================================================
    //===============================================================
    public void setDataType(AttributeInfoEx attributeInfo) throws DevFailed {
        this.attributeInfo = attributeInfo;
        insertValues();
    }
    //===============================================================
    //===============================================================
    private void insertValues() throws DevFailed {
        if (displayUnit==0.0)
            displayUnit = 1.0;
        switch (attributeInfo.data_type) {
            case Tango_DEV_BOOLEAN:
                insertBoolean();
                break;
            case Tango_DEV_UCHAR:
                insertUChar();
                break;
            case Tango_DEV_SHORT:
                insertShort();
                break;
            case Tango_DEV_USHORT:
                insertUShort();
                break;
            case Tango_DEV_LONG:
                insertLong();
                break;
            case Tango_DEV_ULONG:
                insertULong();
                break;
            case Tango_DEV_LONG64:
                insertLong64();
                break;
            case Tango_DEV_FLOAT:
                insertFloat();
                break;
            case Tango_DEV_DOUBLE:
                insertDouble();
                break;
            case Tango_DEV_ENUM:
                insertShort();
                break;
            case Tango_DEV_STRING:
                insertString();
                break;
            default:
                Except.throw_exception("NotImplemented",
                        "Tango type " + deviceAttribute.getType() + " is not yet implemented");
        }
    }
    //===============================================================
    //===============================================================




    //===============================================================
    /*
     *  Insert methods
     */
    //===============================================================
    private void insertBoolean() throws DevFailed {
        try {
            switch (attributeInfo.data_format.value()) {
                case AttrDataFormat._SCALAR:
                    deviceAttribute.insert((Boolean.parseBoolean(strValues.get(0)[0])));
                    break;
                case AttrDataFormat._SPECTRUM:
                    deviceAttribute.insert(parseBooleanLine(strValues.get(0)));
                    break;
                case AttrDataFormat._IMAGE:
                    BooleanImage image = new BooleanImage();
                    deviceAttribute.insert(image.values, image.dimX, strValues.size());
                    break;
                default:
                    Except.throw_exception("BadFormat", attributeName+": Unknown attribute format");
            }
        }
        catch (Exception e) {
            if (e instanceof DevFailed)
                throw e;
            Except.throw_exception(e.getMessage(), e.toString());
        }
    }
    //===============================================================
    //===============================================================
    private void insertShort() throws DevFailed {
        try {
            switch (attributeInfo.data_format.value()) {
                case AttrDataFormat._SCALAR:
                    deviceAttribute.insert((short)
                            ((Short.parseShort(strValues.get(0)[0]))/displayUnit));
                    break;
                case AttrDataFormat._SPECTRUM:
                    deviceAttribute.insert(parseShortLine(strValues.get(0)));
                    break;
                case AttrDataFormat._IMAGE:
                    ShortImage image = new ShortImage();
                    deviceAttribute.insert(image.values, image.dimX, strValues.size());
                    break;
                default:
                    Except.throw_exception("BadFormat", attributeName + ": Unknown attribute format");
            }
        } catch (Exception e) {
            if (e instanceof DevFailed)
                throw e;
            Except.throw_exception(e.getMessage(), e.toString());
        }
    }
    //===============================================================
    //===============================================================
    private void insertUChar() throws DevFailed {
        try {
            switch (attributeInfo.data_format.value()) {
                case AttrDataFormat._SCALAR:
                    deviceAttribute.insert_uc((short)
                            ((Short.parseShort(strValues.get(0)[0]))/displayUnit));
                    break;
                case AttrDataFormat._SPECTRUM:
                    deviceAttribute.insert_uc(parseShortLine(strValues.get(0)));
                    break;
                case AttrDataFormat._IMAGE:
                    ShortImage image = new ShortImage();
                    deviceAttribute.insert_uc(image.values, image.dimX, strValues.size());
                    break;
                default:
                    Except.throw_exception("BadFormat", attributeName+": Unknown attribute format");
            }
        }
        catch (Exception e) {
            if (e instanceof DevFailed)
                throw e;
            Except.throw_exception(e.getMessage(), e.toString());
        }
    }
    //===============================================================
    //===============================================================
    private void insertUShort() throws DevFailed {
        try {
            switch (attributeInfo.data_format.value()) {
                case AttrDataFormat._SCALAR:
                    deviceAttribute.insert_us((int)
                            ((Integer.parseInt(strValues.get(0)[0]))/displayUnit));
                    break;
                case AttrDataFormat._SPECTRUM:
                    deviceAttribute.insert_us(parseLongLine(strValues.get(0)));
                    break;
                case AttrDataFormat._IMAGE:
                    LongImage image = new LongImage();
                    deviceAttribute.insert_us(image.values, image.dimX, strValues.size());
                    break;
                default:
                    Except.throw_exception("BadFormat", attributeName+": Unknown attribute format");
            }
        }
        catch (Exception e) {
            if (e instanceof DevFailed)
                throw e;
            Except.throw_exception(e.getMessage(), e.toString());
        }
    }
    //===============================================================
    //===============================================================
    private void insertLong() throws DevFailed {
        try {
            switch (attributeInfo.data_format.value()) {
                case AttrDataFormat._SCALAR:
                    deviceAttribute.insert((int)
                            ((Integer.parseInt(strValues.get(0)[0]))/displayUnit));
                    break;
                case AttrDataFormat._SPECTRUM:
                    deviceAttribute.insert(parseLongLine(strValues.get(0)));
                    break;
                case AttrDataFormat._IMAGE:
                    LongImage image = new LongImage();
                    deviceAttribute.insert(image.values, image.dimX, strValues.size());
                    break;
                default:
                    Except.throw_exception("BadFormat", attributeName+": Unknown attribute format");
            }
        }
        catch (Exception e) {
            if (e instanceof DevFailed)
                throw e;
            Except.throw_exception(e.getMessage(), e.toString());
        }
    }
    //===============================================================
    //===============================================================
    private void insertULong() throws DevFailed {
        try {
            switch (attributeInfo.data_format.value()) {
                case AttrDataFormat._SCALAR:
                    deviceAttribute.insert_ul((long)
                            ((Long.parseLong(strValues.get(0)[0]))/displayUnit));
                    break;
                case AttrDataFormat._SPECTRUM:
                    deviceAttribute.insert_ul(parseLong64Line(strValues.get(0)));
                    break;
                case AttrDataFormat._IMAGE:
                    Long64Image image = new Long64Image();
                    deviceAttribute.insert_ul(image.values, image.dimX, strValues.size());
                    break;
                default:
                    Except.throw_exception("BadFormat", attributeName+": Unknown attribute format");
            }
        }
        catch (Exception e) {
            if (e instanceof DevFailed)
                throw e;
            Except.throw_exception(e.getMessage(), e.toString());
        }
    }
    //===============================================================
    //===============================================================
    private void insertLong64() throws DevFailed {
        try {
            switch (attributeInfo.data_format.value()) {
                case AttrDataFormat._SCALAR:
                    deviceAttribute.insert((long)
                            ((Long.parseLong(strValues.get(0)[0]))/displayUnit));
                    break;
                case AttrDataFormat._SPECTRUM:
                    deviceAttribute.insert(parseLong64Line(strValues.get(0)));
                    break;
                case AttrDataFormat._IMAGE:
                    Long64Image image = new Long64Image();
                    deviceAttribute.insert(image.values, image.dimX, strValues.size());
                    break;
                default:
                    Except.throw_exception("BadFormat", attributeName+": Unknown attribute format");
            }
        }
        catch (Exception e) {
            if (e instanceof DevFailed)
                throw e;
            Except.throw_exception(e.getMessage(), e.toString());
        }
    }
    //===============================================================
    //===============================================================
    private void insertFloat() throws DevFailed {
        try {
            switch (attributeInfo.data_format.value()) {
                case AttrDataFormat._SCALAR:
                    deviceAttribute.insert((float)
                            ((Float.parseFloat(strValues.get(0)[0]))/displayUnit));
                    break;
                case AttrDataFormat._SPECTRUM:
                    deviceAttribute.insert(parseFloatLine(strValues.get(0)));
                    break;
                case AttrDataFormat._IMAGE:
                    FloatImage image = new FloatImage();
                    deviceAttribute.insert(image.values, image.dimX, strValues.size());
                    break;
                default:
                    Except.throw_exception("BadFormat", attributeName+": Unknown attribute format");
            }
        }
        catch (Exception e) {
            if (e instanceof DevFailed)
                throw e;
            Except.throw_exception(e.getMessage(), e.toString());
        }
    }
    //===============================================================
    //===============================================================
    private void insertDouble() throws DevFailed {
        try {
            switch (attributeInfo.data_format.value()) {
                case AttrDataFormat._SCALAR:
                    deviceAttribute.insert(Double.parseDouble(strValues.get(0)[0])/displayUnit);
                    break;
                case AttrDataFormat._SPECTRUM:
                    deviceAttribute.insert(parseDoubleLine(strValues.get(0)));
                    break;
                case AttrDataFormat._IMAGE:
                    DoubleImage image = new DoubleImage();
                    deviceAttribute.insert(image.values, image.dimX, strValues.size());
                    break;
                default:
                    Except.throw_exception("BadFormat", attributeName+": Unknown attribute format");
            }
        }
        catch (Exception e) {
            if (e instanceof DevFailed)
                throw e;
            System.err.println(attributeName);
            for (String[] str : strValues) {
                for (String s : str)
                    System.err.println(s+", ");
            }
            System.err.println();
            Except.throw_exception(e.getMessage(), e.toString());
        }
    }
    //===============================================================
    //===============================================================
    private void insertString() throws DevFailed {
        boolean noValue = strValues.isEmpty() ||
                strValues.get(0).length==0 || strValues.get(0)[0].equals(NotInitialized);
        try {
            switch (attributeInfo.data_format.value()) {
                case AttrDataFormat._SCALAR:
                    if (noValue)
                        deviceAttribute.insert("");
                    else
                        deviceAttribute.insert(strValues.get(0)[0]);
                    break;
                case AttrDataFormat._SPECTRUM:
                    if (noValue) {
                        deviceAttribute.insert(new String[] { "" });
                    }
                    else {
                        String[] strArray = new String[strValues.size()];
                        for (int i = 0 ; i<strValues.size() ; i++)
                            strArray[i] = strValues.get(i)[0];
                        deviceAttribute.insert(strArray);
                    }
                    break;
                default:
                    Except.throw_exception("BadFormat", "Image format not implemented for DEV_STRING type");
            }
        }
        catch (Exception e) {
            if (e instanceof DevFailed)
                throw e;
            Except.throw_exception(e.getMessage(), e.toString());
        }
    }
    //===============================================================
    //===============================================================




    //===============================================================
    /*
     * Parsing line methods
     */
    //===============================================================
    private boolean[] parseBooleanLine(String[] lineValues) {
        boolean[] values = new boolean[lineValues.length];
        int i = 0;
        for (String strValue : lineValues)
            values[i++] = Boolean.parseBoolean(strValue);
        return values;
    }
    //===============================================================
    //===============================================================
    private short[] parseShortLine(String[] lineValues) {
        short[] values = new short[lineValues.length];
        int i = 0;
        for (String strValue : lineValues)
            values[i++] = (short)(Short.parseShort(strValue)/displayUnit);
        return values;
    }
    //===============================================================
    //===============================================================
    private int[] parseLongLine(String[] lineValues) {
        int[] values = new int[lineValues.length];
        int i = 0;
        for (String strValue : lineValues)
            values[i++] = (int)(Integer.parseInt(strValue)/displayUnit);
        return values;
    }
    //===============================================================
    //===============================================================
    private long[] parseLong64Line(String[] lineValues) {
        long[] values = new long[lineValues.length];
        int i = 0;
        for (String strValue : lineValues)
            values[i++] = (long)(Long.parseLong(strValue)/displayUnit);
        return values;
    }
    //===============================================================
    //===============================================================
    private float[] parseFloatLine(String[] lineValues) {
        float[] values = new float[lineValues.length];
        int i = 0;
        for (String strValue : lineValues)
            values[i++] = (float)(Float.parseFloat(strValue)/displayUnit);
        return values;
    }
    //===============================================================
    //===============================================================
    private double[] parseDoubleLine(String[] lineValues) {
        double[] values = new double[lineValues.length];
        int i = 0;
        for (String strValue : lineValues)
            values[i++] = Double.parseDouble(strValue)/displayUnit;
        return values;
    }
    //===============================================================
    //===============================================================




    //===============================================================
    //===============================================================
    public String toString() {
        StringBuilder sb = new StringBuilder(attributeName);
        if (attributeInfo!=null)
            sb.append(" (").append(attributeInfo.data_type).append("): ");
        for (String[] lineValues : strValues) {
            for (String lineValue : lineValues) {
                sb.append(lineValue).append("  ");
            }
            sb.append('\n');
        }
        return sb.toString().trim();
    }
    //===============================================================
    //===============================================================



    //===============================================================
    //===============================================================
    private class BooleanImage {
        private int dimX;
        private boolean[] values;
        BooleanImage() {
            boolean[][] array = new boolean[strValues.size()][];
            for (int y=0 ; y<strValues.size() ; y++) {
                array[y] = parseBooleanLine(strValues.get(y));
            }
            values = new boolean[array.length*array[0].length];
            int i=0;
            for (boolean[] lineArray : array) // Y
                for (boolean value : lineArray)  // X
                    values[i++] = value;
            dimX = array[0].length;
        }
    }
    //===============================================================
    //===============================================================
    private class ShortImage {
        private int dimX;
        private short[] values;
        ShortImage() {
            short[][] array = new short[strValues.size()][];
            for (int y=0 ; y<strValues.size() ; y++) {
                array[y] = parseShortLine(strValues.get(y));
            }
            values = new short[array.length*array[0].length];
            int i=0;
            for (short[] lineArray : array) // Y
                for (short value : lineArray)  // X
                    values[i++] = (short) (value/displayUnit);
            dimX = array[0].length;
        }
    }
    //===============================================================
    //===============================================================
    private class LongImage {
        private int dimX;
        private int[] values;
        LongImage() {
            int[][] array = new int[strValues.size()][];
            for (int y=0 ; y<strValues.size() ; y++) {
                array[y] = parseLongLine(strValues.get(y));
            }
            values = new int[array.length*array[0].length];
            int i=0;
            for (int[] lineArray : array) // Y
                for (int value : lineArray)  // X
                    values[i++] = (int)(value/displayUnit);
            dimX = array[0].length;
        }
    }
    //===============================================================
    //===============================================================
    private class Long64Image {
        private int dimX;
        private long[] values;
        Long64Image() {
            long[][] array = new long[strValues.size()][];
            for (int y=0 ; y<strValues.size() ; y++) {
                array[y] = parseLong64Line(strValues.get(y));
            }
            values = new long[array.length*array[0].length];
            int i=0;
            for (long[] lineArray : array) // Y
                for (long value : lineArray)  // X
                    values[i++] = (long)(value/displayUnit);
            dimX = array[0].length;
        }
    }
    //===============================================================
    //===============================================================
    private class FloatImage {
        private int dimX;
        private float[] values;
        FloatImage() {
            float[][] array = new float[strValues.size()][];
            for (int y=0 ; y<strValues.size() ; y++) {
                array[y] = parseFloatLine(strValues.get(y));
            }
            values = new float[array.length*array[0].length];
            int i=0;
            for (float[] lineArray : array) // Y
                for (float value : lineArray)  // X
                    values[i++] = (float)(value/displayUnit);
            dimX = array[0].length;
        }
    }
    //===============================================================
    //===============================================================
    private class DoubleImage {
        private int dimX;
        private double[] values;
        DoubleImage() {
            double[][] array = new double[strValues.size()][];
            for (int y=0 ; y<strValues.size() ; y++) {
                array[y] = parseDoubleLine(strValues.get(y));
            }
            values = new double[array.length*array[0].length];
            int i=0;
            for (double[] lineArray : array) // Y
                for (double value : lineArray)  // X
                    values[i++] = value/displayUnit;
            dimX = array[0].length;
        }
    }
    //===============================================================
    //===============================================================
}
