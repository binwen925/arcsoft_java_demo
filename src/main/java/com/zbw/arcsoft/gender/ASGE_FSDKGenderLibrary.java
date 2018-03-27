package com.zbw.arcsoft.gender;

import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import com.zbw.arcsoft.ASVLOFFSCREEN;
import com.zbw.arcsoft.LoadUtils;

public interface ASGE_FSDKGenderLibrary extends Library {
    ASGE_FSDKGenderLibrary INSTANCE = LoadUtils.loadLibrary(LoadUtils.Function.GENDER, ASGE_FSDKGenderLibrary.class);

    NativeLong ASGE_FSDK_InitGenderEngine(
            String AppId,
            String SDKKey,
            Pointer pMem,
            int lMemSize,
            PointerByReference pEngine
    );

    NativeLong ASGE_FSDK_UninitGenderEngine(Pointer hEngine);

    ASGE_FSDKGender_Version ASGE_FSDK_GetVersion(Pointer hEngine);

    NativeLong ASGE_FSDK_GenderEstimation_StaticImage(
            Pointer hEngine,
            ASVLOFFSCREEN pImageInfo,
            ASGE_FSDK_GENDERFACEINPUT pFaceRes,
            ASGE_FSDK_GENDERRESULT pGenderRes
    );

    NativeLong ASGE_FSDK_GenderEstimation_Preview(
            Pointer hEngine,
            ASVLOFFSCREEN pImageInfo,
            ASGE_FSDK_GENDERFACEINPUT pFaceRes,
            ASGE_FSDK_GENDERRESULT pGenderRes
    );
}
