package com.zbw.arcsoft.age;

import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import com.zbw.arcsoft.ASVLOFFSCREEN;
import com.zbw.arcsoft.LoadUtils;

public interface ASGE_FSDKAgeLibrary extends Library {
    ASGE_FSDKAgeLibrary INSTANCE = LoadUtils.loadLibrary(LoadUtils.Function.AGE, ASGE_FSDKAgeLibrary.class);

    /**
     * This function is used to initialize the age estimation engine
     *
     * @param AppId
     * @param SDKKey
     * @param pMem
     * @param lMemSize
     * @param pEngine
     * @return
     */
    NativeLong ASAE_FSDK_InitAgeEngine(
            String AppId,
            String SDKKey,
            Pointer pMem,
            int lMemSize,
            PointerByReference pEngine
    );

    /**
     * This function is used to estimate age in static image mode automatically.
     *
     * @param hEngine
     * @param pImgInfo
     * @param pFaceRes
     * @param pAgeRes
     * @return
     */
    NativeLong ASAE_FSDK_AgeEstimation_StaticImage(
            Pointer hEngine,
            ASVLOFFSCREEN pImgInfo,
            ASAE_FSDK_AGEFACEINPUT pFaceRes,
            ASAE_FSDK_AGERESULT pAgeRes
    );

    /**
     * This function is used to estimate age in preview mode automatically.
     *
     * @param hEngine
     * @param pImgInfo
     * @param pFaceRes
     * @param pAgeRes
     * @return
     */
    NativeLong ASAE_FSDK_AgeEstimation_Preview(
            Pointer hEngine,
            ASVLOFFSCREEN pImgInfo,
            ASAE_FSDK_AGEFACEINPUT pFaceRes,
            ASAE_FSDK_AGERESULT pAgeRes
    );

    /**
     * This function is used to release the age estimation engine.
     *
     * @param hEngine
     * @return
     */
    NativeLong ASAE_FSDK_UninitAgeEngine(Pointer hEngine);

    /**
     * This function is used to get the version information of the library.
     *
     * @param hEngine
     * @return
     */
    ASGE_FSDKAge_Version ASAE_FSDK_GetVersion(Pointer hEngine);
}
