package com.zbw.arcsoft.age;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

import java.util.Arrays;
import java.util.List;

public class ASAE_FSDK_AGERESULT extends Structure {

    public static class ByReference extends ASAE_FSDK_AGERESULT implements Structure.ByReference {
        public ByReference() {

        }

        public ByReference(Pointer pointer) {
            super(pointer);
        }
    }

    public ASAE_FSDK_AGERESULT() {

    }

    public ASAE_FSDK_AGERESULT(Pointer pointer) {
        super(pointer);
        read();
    }

    public Pointer pAgeResultArray;
    public int lFaceNumber;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("pAgeResultArray", "lFaceNumber");
    }
}
