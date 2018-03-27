package com.zbw.arcsoft.gender;

import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

import java.util.Arrays;
import java.util.List;

public class ASGE_FSDK_GENDERRESULT extends Structure {

    public IntByReference pGenderResultArray;
    public int lFaceNumber;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("pGenderResultArray", "lFaceNumber");
    }
}
