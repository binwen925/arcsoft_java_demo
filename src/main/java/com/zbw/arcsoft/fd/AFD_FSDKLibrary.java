package com.zbw.arcsoft.fd;

import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import com.zbw.arcsoft.ASVLOFFSCREEN;
import com.zbw.arcsoft.LoadUtils;

public interface AFD_FSDKLibrary extends Library {

    AFD_FSDKLibrary INSTANCE = LoadUtils.loadLibrary(LoadUtils.Function.FD, AFD_FSDKLibrary.class);

    NativeLong AFD_FSDK_InitialFaceEngine(String appid, String sdkid, Pointer pMem, int lMemSize, PointerByReference phEngine, int iOrientPriority, int nScale, int nMaxFaceNum);

    NativeLong AFD_FSDK_StillImageFaceDetection(Pointer hEngine, ASVLOFFSCREEN pImgData, PointerByReference pFaceRes);

    NativeLong AFD_FSDK_UninitialFaceEngine(Pointer hEngine);

    AFD_FSDK_Version AFD_FSDK_GetVersion(Pointer hEngine);
}