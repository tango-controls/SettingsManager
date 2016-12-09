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
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.PipeBlob;
import fr.esrf.TangoApi.PipeDataElement;
import fr.esrf.TangoDs.Except;
import fr.esrf.TangoDs.TangoConst;

/**
 * This class is able to define a Tango attribute
 *
 * @author verdier
 */

public class TangoPipeBlob extends PipeBlob implements TangoConst {
    //===============================================================
    //===============================================================
    public TangoPipeBlob(String name) {
        super(name);
    }
    //===============================================================
    //===============================================================
    public void add( ApplyTangoAttribute attribute) throws DevFailed {
        String attributeName = attribute.getDeviceName()+'/'+attribute.getAttributeName();
        try {
            DeviceAttribute deviceAttribute = attribute.getDeviceAttribute();
            switch (deviceAttribute.getType()) {
                case Tango_DEV_BOOLEAN:
                    if (deviceAttribute.getDataFormat()==AttrDataFormat.SCALAR)
                        add(new PipeDataElement(attributeName, deviceAttribute.extractBoolean()));
                    else
                        add(new PipeDataElement(attributeName, deviceAttribute.extractBooleanArray()));
                    break;
                case Tango_DEV_UCHAR:
                    if (deviceAttribute.getDataFormat()==AttrDataFormat.SCALAR)
                        add(new PipeDataElement(attributeName, deviceAttribute.extractUChar()));
                    else
                        add(new PipeDataElement(attributeName, deviceAttribute.extractUCharArray()));
                    break;
                case Tango_DEV_SHORT:
                    if (deviceAttribute.getDataFormat()==AttrDataFormat.SCALAR)
                        add(new PipeDataElement(attributeName, deviceAttribute.extractShort()));
                    else
                        add(new PipeDataElement(attributeName, deviceAttribute.extractShortArray()));
                    break;
                case Tango_DEV_USHORT:
                    if (deviceAttribute.getDataFormat()==AttrDataFormat.SCALAR)
                        add(new PipeDataElement(attributeName, deviceAttribute.extractUShort()));
                    else
                        add(new PipeDataElement(attributeName, deviceAttribute.extractUShortArray()));
                    break;
                case Tango_DEV_LONG:
                    if (deviceAttribute.getDataFormat()==AttrDataFormat.SCALAR)
                        add(new PipeDataElement(attributeName, deviceAttribute.extractLong()));
                    else
                        add(new PipeDataElement(attributeName, deviceAttribute.extractLongArray()));
                    break;
                case Tango_DEV_ULONG:
                    if (deviceAttribute.getDataFormat()==AttrDataFormat.SCALAR)
                        add(new PipeDataElement(attributeName, deviceAttribute.extractULong()));
                    else
                        add(new PipeDataElement(attributeName, deviceAttribute.extractULongArray()));
                    break;
                case Tango_DEV_LONG64:
                    if (deviceAttribute.getDataFormat()==AttrDataFormat.SCALAR)
                        add(new PipeDataElement(attributeName, deviceAttribute.extractLong64()));
                    else
                        add(new PipeDataElement(attributeName, deviceAttribute.extractLong64Array()));
                    break;
                case Tango_DEV_FLOAT:
                    if (deviceAttribute.getDataFormat()==AttrDataFormat.SCALAR)
                        add(new PipeDataElement(attributeName, deviceAttribute.extractFloat()));
                    else
                        add(new PipeDataElement(attributeName, deviceAttribute.extractFloatArray()));
                    break;
                case Tango_DEV_DOUBLE:
                    if (deviceAttribute.getDataFormat()==AttrDataFormat.SCALAR)
                        add(new PipeDataElement(attributeName, deviceAttribute.extractDouble()));
                    else
                        add(new PipeDataElement(attributeName, deviceAttribute.extractDoubleArray()));
                    break;
                case Tango_DEV_ENUM:
                    if (deviceAttribute.getDataFormat()==AttrDataFormat.SCALAR)
                        add(new PipeDataElement(attributeName, deviceAttribute.extractShort()));
                    else
                        add(new PipeDataElement(attributeName, deviceAttribute.extractShortArray()));
                    break;
                case Tango_DEV_STRING:
                    if (deviceAttribute.getDataFormat()==AttrDataFormat.SCALAR)
                        add(new PipeDataElement(attributeName, deviceAttribute.extractString()));
                    else
                        add(new PipeDataElement(attributeName, deviceAttribute.extractStringArray()));
                    break;
                case Tango_DEV_STATE:
                    if (deviceAttribute.getDataFormat()==AttrDataFormat.SCALAR)
                        add(new PipeDataElement(attributeName, deviceAttribute.extractDevState()));
                    else
                        add(new PipeDataElement(attributeName, deviceAttribute.extractDevStateArray()));
                    break;
                default:
                    Except.throw_exception("NotImplemented",
                            "Tango type " + deviceAttribute.getType() + " is not yet implemented");
            }
        }
        catch (DevFailed e) {
            //e.errors[0].desc = deviceName+": " + e.errors[0].desc;
            System.err.println(attributeName +":  " + e.errors[0].desc);
            throw e;
        }
    }
    //===============================================================
    /**
     *
     * @param attribute attribute to be inserted in blob.
     * @param mode TangoConst.ACCESS_READ or TangoConst.ACCESS_WRITE
     *             (read or write part to be inserted).
     * @throws DevFailed in case of attribute extraction failed.
     */
    //===============================================================
    public void add(TangoAttribute attribute, int mode) throws DevFailed {
        String attributeName = attribute.getDeviceName()+'/'+attribute.getAttributeName();
        try {
            DeviceAttribute deviceAttribute = attribute.getDeviceAttribute();
            int dimX = deviceAttribute.getDimX();
            if (dimX==0) dimX = 1;
            boolean scalar = deviceAttribute.getDataFormat()==AttrDataFormat.SCALAR;
            switch (deviceAttribute.getType()) {
                case Tango_DEV_BOOLEAN:
                    add(getDataElement(attributeName,
                            deviceAttribute.extractBooleanArray(), dimX, mode, scalar));
                    break;
                case Tango_DEV_UCHAR:
                    add(getDataElement(attributeName,
                            deviceAttribute.extractShortArray(), dimX, mode, scalar, true));
                    break;
                case Tango_DEV_SHORT:
                    add(getDataElement(attributeName,
                            deviceAttribute.extractShortArray(), dimX, mode, scalar, false));
                    break;
                case Tango_DEV_USHORT:
                    add(getDataElement(attributeName,
                            deviceAttribute.extractUShortArray(), dimX, mode, scalar, true));
                    break;
                case Tango_DEV_LONG:
                    add(getDataElement(attributeName,
                            deviceAttribute.extractLongArray(), dimX, mode, scalar, false));
                    break;
                case Tango_DEV_ULONG:
                    add(getDataElement(attributeName,
                            deviceAttribute.extractLong64Array(), dimX, mode, scalar, true));
                    break;
                case Tango_DEV_LONG64:
                    add(getDataElement(attributeName,
                            deviceAttribute.extractLong64Array(), dimX, mode, scalar, false));
                    break;
                case Tango_DEV_FLOAT:
                    add(getDataElement(attributeName,
                            deviceAttribute.extractFloatArray(), dimX, mode, scalar));
                    break;
                case Tango_DEV_DOUBLE:
                    add(getDataElement(attributeName,
                            deviceAttribute.extractDoubleArray(), dimX, mode, scalar));
                    break;
                case Tango_DEV_ENUM:
                    add(getDataElement(attributeName,
                            deviceAttribute.extractShortArray(), dimX, mode, scalar, false));
                    break;
                case Tango_DEV_STRING:
                    add(getDataElement(attributeName,
                            deviceAttribute.extractStringArray(), dimX, mode, scalar));
                    break;
                case Tango_DEV_STATE:
                    add(getDataElement(attributeName,
                            deviceAttribute.extractDevStateArray(), dimX, mode, scalar));
                    break;
                default:
                    Except.throw_exception("NotImplemented",
                            "Tango type " + deviceAttribute.getType() + " is not yet implemented");
            }
        }
        catch (DevFailed e) {
            //e.errors[0].desc = deviceName+": " + e.errors[0].desc;
            System.err.println(attributeName +":  " + e.errors[0].desc);
            throw e;
        }
    }
    //===============================================================
    //===============================================================
    private PipeDataElement getDataElement(String name, boolean[] allValues,
                                           int dimX, int mode, boolean scalar) throws DevFailed {
        boolean[] values = new boolean[dimX];
        int offset =  (mode==TangoConst.ACCESS_READ)? 0 : dimX;
        System.arraycopy(allValues, offset, values, 0, dimX);
        if (scalar)
            return new PipeDataElement(name, values[0]);
        else
            return new PipeDataElement(name, values);
    }
    //===============================================================
    //===============================================================
    private PipeDataElement getDataElement(String name, short[] allValues,
                                           int dimX, int mode, boolean scalar, boolean asUChar) throws DevFailed {
        short[] values = new short[dimX];
        int offset =  (mode==TangoConst.ACCESS_READ)? 0 : dimX;
        System.arraycopy(allValues, offset, values, 0, dimX);
        if (scalar)
            return new PipeDataElement(name, values[0]);
        else
            return new PipeDataElement(name, values, asUChar);
    }
    //===============================================================
    //===============================================================
    private PipeDataElement getDataElement(String name, int[] allValues,
                                           int dimX, int mode, boolean scalar, boolean asUShort) throws DevFailed {
        int[] values = new int[dimX];
        int offset =  (mode==TangoConst.ACCESS_READ)? 0 : dimX;
        System.arraycopy(allValues, offset, values, 0, dimX);
        if (scalar)
            return new PipeDataElement(name, values[0]);
        else
            return new PipeDataElement(name, values, asUShort);
    }
    //===============================================================
    //===============================================================
    private PipeDataElement getDataElement(String name, long[] allValues,
                                           int dimX, int mode, boolean scalar, boolean asULong) throws DevFailed {
        long[] values = new long[dimX];
        int offset =  (mode==TangoConst.ACCESS_READ)? 0 : dimX;
        System.arraycopy(allValues, offset, values, 0, dimX);
        if (scalar)
            return new PipeDataElement(name, values[0]);
        else
            return new PipeDataElement(name, values, asULong);
    }
    //===============================================================
    //===============================================================
    private PipeDataElement getDataElement(String name, float[] allValues,
                                           int dimX, int mode, boolean scalar) throws DevFailed {
        float[] values = new float[dimX];
        int offset =  (mode==TangoConst.ACCESS_READ)? 0 : dimX;
        System.arraycopy(allValues, offset, values, 0, dimX);
        if (scalar)
            return new PipeDataElement(name, values[0]);
        else
            return new PipeDataElement(name, values);
    }
    //===============================================================
    //===============================================================
    private PipeDataElement getDataElement(String name, double[] allValues,
                                           int dimX, int mode, boolean scalar) throws DevFailed {
        double[] values = new double[dimX];
        int offset =  (mode==TangoConst.ACCESS_READ)? 0 : dimX;
        System.arraycopy(allValues, offset, values, 0, dimX);
        if (scalar)
            return new PipeDataElement(name, values[0]);
        else
            return new PipeDataElement(name, values);
    }
    //===============================================================
    //===============================================================
    private PipeDataElement getDataElement(String name, String[] allValues,
                                           int dimX, int mode, boolean scalar) throws DevFailed {
        String[] values = new String[dimX];
        int offset =  (mode==TangoConst.ACCESS_READ)? 0 : dimX;
        System.arraycopy(allValues, offset, values, 0, dimX);
        if (scalar)
            return new PipeDataElement(name, values[0]);
        else
            return new PipeDataElement(name, values);
    }
    //===============================================================
    //===============================================================
    private PipeDataElement getDataElement(String name, DevState[] allValues,
                                           int dimX, int mode, boolean scalar) throws DevFailed {
        DevState[] values = new DevState[dimX];
        int offset =  (mode==TangoConst.ACCESS_READ)? 0 : dimX;
        System.arraycopy(allValues, offset, values, 0, dimX);
        if (scalar)
            return new PipeDataElement(name, values[0]);
        else
            return new PipeDataElement(name, values);
    }
    //===============================================================
    //===============================================================
}
