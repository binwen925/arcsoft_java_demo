package com.zbw.arcsoft.age;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.zbw.arcsoft.MRECT;

import java.util.Arrays;
import java.util.List;

public class ASAE_FSDK_AGEFACEINPUT extends Structure {

    public static class ByReference extends ASAE_FSDK_AGEFACEINPUT implements Structure.ByReference {
        public ByReference() {
        }

        public ByReference(Pointer pointer) {
            super(pointer);
        }
    }

    public MRECT.ByReference pFaceRectArray;
    public IntByReference pFaceOrientArray;
    public int lFaceNumber;

    public ASAE_FSDK_AGEFACEINPUT() {

    }

    public ASAE_FSDK_AGEFACEINPUT(Pointer pointer) {
        super(pointer);
        read();
    }


    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("pFaceRectArray", "pFaceOrientArray", "lFaceNumber");
    }
}
