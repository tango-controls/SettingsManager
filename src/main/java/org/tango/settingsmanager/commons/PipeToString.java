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

import fr.esrf.Tango.DevEncoded;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.DevicePipe;
import fr.esrf.TangoApi.PipeBlob;
import fr.esrf.TangoApi.PipeDataElement;
import fr.esrf.TangoDs.TangoConst;

import java.util.Date;


/**
 * This class is able to test getConfig/read/write on pipe
 *
 * @author verdier
 */

public class PipeToString {
    private static int deep = 0;
    private static final String INDENT = "    ";
    //===============================================================
    //===============================================================
    public static String  getString(DevicePipe devicePipe) throws DevFailed {
        return devicePipe.getPipeName() + ": " +
                new Date(devicePipe.getTimeValMillisSec()) + "\n" +
                getString(devicePipe.getPipeBlob());
    }
    //===============================================================
    //===============================================================
    public static String  getString(PipeBlob pipeBlob) throws DevFailed {
        deep++;
        StringBuilder   sb = new StringBuilder(indent() + "["+ pipeBlob.getName() + "]:\n");
        for (PipeDataElement dataElement : pipeBlob) {
            int type = dataElement.getType();
            sb.append(indent()).append(" - ").append(dataElement.getName())
                    .append(":  ").append(TangoConst.Tango_CmdArgTypeName[type]).append('\n');
            switch(type) {
                case TangoConst.Tango_DEV_PIPE_BLOB:
                    //  Do a re entrance to display inner blob
                    sb.append(getString(dataElement.extractPipeBlob()));
                    break;

                case TangoConst.Tango_DEV_BOOLEAN:
                    sb.append(display(dataElement.extractBooleanArray()));
                    break;
                case TangoConst.Tango_DEV_CHAR:
                    sb.append(display(dataElement.extractCharArray()));
                    break;
                case TangoConst.Tango_DEV_UCHAR:
                    sb.append(display(dataElement.extractUCharArray()));
                    break;
                case TangoConst.Tango_DEV_SHORT:
                    sb.append(display(dataElement.extractShortArray()));
                    break;
                case TangoConst.Tango_DEV_USHORT:
                    sb.append(display(dataElement.extractUShortArray()));
                    break;
                case TangoConst.Tango_DEV_LONG:
                    sb.append(display(dataElement.extractLongArray()));
                    break;
                case TangoConst.Tango_DEV_ULONG:
                    sb.append(display(dataElement.extractULongArray()));
                    break;
                case TangoConst.Tango_DEV_LONG64:
                    sb.append(display(dataElement.extractLong64Array()));
                    break;
                case TangoConst.Tango_DEV_DOUBLE:
                    sb.append(display(dataElement.extractDoubleArray()));
                    break;
                case TangoConst.Tango_DEV_FLOAT:
                    sb.append(display(dataElement.extractFloatArray()));
                    break;
                case TangoConst.Tango_DEV_STRING:
                    sb.append(display(dataElement.extractStringArray()));
                    break;
                case TangoConst.Tango_DEV_STATE:
                    sb.append(display(dataElement.extractDevStateArray()));
                    break;
                case TangoConst.Tango_DEV_ENCODED:
                    sb.append(display(dataElement.extractDevEncodedArray()));
                    break;
            }
        }
        deep--;
        return sb.toString();
    }
    //===============================================================
    //===============================================================
    private static String  display(boolean[] array) {
        StringBuilder sb = new StringBuilder();
        for (boolean b : array)
            sb.append(indent()).append(INDENT).append(b).append('\n');
        return sb.toString();
    }
    //===============================================================
    //===============================================================
    private static String  display(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array)
            sb.append(indent()).append(INDENT).append(b).append('\n');
        return sb.toString();
    }
    //===============================================================
    //===============================================================
    private static String display(short[] array) {
        StringBuilder sb = new StringBuilder();
        for (short s : array)
            sb.append(indent()).append(INDENT).append(s).append('\n');
        return sb.toString();
    }
    //===============================================================
    //===============================================================
    private static String display(int[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i : array)
            sb.append(indent()).append(INDENT).append(i).append('\n');
        return sb.toString();
    }
    //===============================================================
    //===============================================================
    private static String display(long[] array) {
        StringBuilder sb = new StringBuilder();
        for (long l : array)
            sb.append(indent()).append(INDENT).append(l).append('\n');
        return sb.toString();
    }
    //===============================================================
    //===============================================================
    private static String display(float[] array) {
        StringBuilder sb = new StringBuilder();
        for (float f : array)
            sb.append(indent()).append(INDENT).append(f).append('\n');
        return sb.toString();
    }
    //===============================================================
    //===============================================================
    private static String display(double[] array) {
        StringBuilder sb = new StringBuilder();
        for (double d : array)
            sb.append(indent()).append(INDENT).append(d).append('\n');
        return sb.toString();
    }
    //===============================================================
    //===============================================================
    private static String display(String[] array) {
        StringBuilder sb = new StringBuilder();
        for (String s : array)
            sb.append(indent()).append(INDENT).append(s).append('\n');
        return sb.toString();
    }
    //===============================================================
    //===============================================================
    private static String display(DevState[] array) {
        StringBuilder sb = new StringBuilder();
        for (DevState state : array)
            sb.append(indent()).append(INDENT).append(ApiUtil.stateName(state)).append('\n');
        return sb.toString();
    }
    //===============================================================
    //===============================================================
    private static String display(DevEncoded[] array) {
        StringBuilder sb = new StringBuilder();
        for (DevEncoded e : array)
            sb.append(indent()).append(INDENT).append("Encoded format: ").append(e.encoded_format).append('\n');
        return sb.toString();
    }
    //===============================================================
    //===============================================================
    private static String indent() {
        StringBuilder sb = new StringBuilder();
        for (int i=0 ; i<deep ; i++) {
            sb.append(INDENT);
        }
        return sb.toString();
    }
    //===============================================================
    //===============================================================
}
