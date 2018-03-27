package com.zbw.arcsoft.gender;

import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.zbw.arcsoft.MRECT;

import java.util.Arrays;
import java.util.List;

public class ASGE_FSDK_GENDERFACEINPUT extends Structure {

    public MRECT.ByReference pFaceRectArray;
    public IntByReference pFaceOrientArray;
    private int lFaceNumber;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("pFaceRectArray", "pFaceOrientArray", "lFaceNumber");
    }
}
