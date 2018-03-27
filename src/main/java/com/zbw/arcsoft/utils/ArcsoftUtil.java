package com.zbw.arcsoft.utils;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.PointerByReference;
import com.zbw.arcsoft.*;
import com.zbw.arcsoft.age.ASAE_FSDK_AGEFACEINPUT;
import com.zbw.arcsoft.age.ASAE_FSDK_AGERESULT;
import com.zbw.arcsoft.age.ASGE_FSDKAgeLibrary;
import com.zbw.arcsoft.age.ASGE_FSDKAge_Version;
import com.zbw.arcsoft.fd.AFD_FSDKLibrary;
import com.zbw.arcsoft.fd.AFD_FSDK_FACERES;
import com.zbw.arcsoft.fd.AFD_FSDK_Version;
import com.zbw.arcsoft.fd._AFD_FSDK_OrientPriority;
import com.zbw.arcsoft.fr.AFR_FSDKLibrary;
import com.zbw.arcsoft.fr.AFR_FSDK_FACEINPUT;
import com.zbw.arcsoft.fr.AFR_FSDK_FACEMODEL;
import com.zbw.arcsoft.fr.AFR_FSDK_Version;
import com.zbw.arcsoft.gender.ASGE_FSDKGenderLibrary;
import com.zbw.arcsoft.gender.ASGE_FSDKGender_Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

public final class ArcsoftUtil {
    public static final int FD_WORKBUF_SIZE = 20 * 1024 * 1024;
    public static final int FR_WORKBUF_SIZE = 40 * 1024 * 1024;
    public static final int AGE_WORKBUF_SIZE = 20 * 1024 * 1024;
    public static final int GENDER_WORKBUF_SIZE = 20 * 1024 * 1024;
    public static final int MAX_FACE_NUM = 50;
    public static final boolean bUseBGRToEngine = true;
    private static final Logger logger = LoggerFactory.getLogger(ArcsoftUtil.class);
    private static Pointer pFDWorkMem = CLibrary.INSTANCE.malloc(FD_WORKBUF_SIZE);
    private static Pointer pFRWorkMem = CLibrary.INSTANCE.malloc(FR_WORKBUF_SIZE);
    private static Pointer pAgeWorkMem = CLibrary.INSTANCE.malloc(AGE_WORKBUF_SIZE);
    private static Pointer pGenderWorkMem = CLibrary.INSTANCE.malloc(GENDER_WORKBUF_SIZE);

    private static boolean isExist(Pointer pointer) {
        return pointer != null && pointer != Pointer.NULL;
    }

    private static void throwExceptionIfNotOK(NativeLong ret, String methodName) throws Exception {
        if (ret.longValue() != 0) {
            throw new Exception(String.format("%s code:0x%s", methodName, Long.toHexString(ret.longValue())));
        }
    }

    public static Pointer getFDEngine() throws Exception {
        PointerByReference phFDEngine = new PointerByReference();
        if (!isExist(pFDWorkMem)) {
            pFDWorkMem = CLibrary.INSTANCE.malloc(FD_WORKBUF_SIZE);
        }
        NativeLong ret = AFD_FSDKLibrary.INSTANCE.AFD_FSDK_InitialFaceEngine(Config.ARCSOFT_APPID, Config.ARCSOFT_FD_SDKKEY, pFDWorkMem, FD_WORKBUF_SIZE, phFDEngine, _AFD_FSDK_OrientPriority.AFD_FSDK_OPF_0_HIGHER_EXT, 32, MAX_FACE_NUM);
        throwExceptionIfNotOK(ret, "AFD_FSDK_InitialFaceEngine");
        return phFDEngine.getValue();
    }

    public static Optional<AFD_FSDK_Version> getFDVersion(Pointer fdEngine) {
        if (isExist(fdEngine)) {
            AFD_FSDK_Version version = AFD_FSDKLibrary.INSTANCE.AFD_FSDK_GetVersion(fdEngine);
            if (version != null) {
                return Optional.of(version);
            }
        }
        return Optional.empty();
    }

    public static void freeFDEngine(Pointer fdEngine) throws Exception {
        if (isExist(fdEngine)) {
            NativeLong ret = AFD_FSDKLibrary.INSTANCE.AFD_FSDK_UninitialFaceEngine(fdEngine);
            throwExceptionIfNotOK(ret, "AFD_FSDK_UninitialFaceEngine");
        }
        if (isExist(pFDWorkMem)) {
            CLibrary.INSTANCE.free(pFDWorkMem);
        }
    }

    public static Pointer getFREngine() throws Exception {
        PointerByReference phFREngine = new PointerByReference();
        if (!isExist(pFRWorkMem)) {
            pFRWorkMem = CLibrary.INSTANCE.malloc(FR_WORKBUF_SIZE);
        }
        NativeLong ret = AFR_FSDKLibrary.INSTANCE.AFR_FSDK_InitialEngine(Config.ARCSOFT_APPID, Config.ARCSOFT_FR_SDKKEY, pFRWorkMem, FR_WORKBUF_SIZE, phFREngine);
        throwExceptionIfNotOK(ret, "AFR_FSDK_InitialEngine");
        return phFREngine.getValue();
    }

    public static Optional<AFR_FSDK_Version> getFRVersion(Pointer frEngine) {
        if (isExist(frEngine)) {
            AFR_FSDK_Version version = AFR_FSDKLibrary.INSTANCE.AFR_FSDK_GetVersion(frEngine);
            if (version != null) {
                return Optional.of(version);
            }
        }
        return Optional.empty();
    }

    public static void freeFREngine(Pointer frEngine) throws Exception {
        if (isExist(frEngine)) {
            NativeLong ret = AFR_FSDKLibrary.INSTANCE.AFR_FSDK_UninitialEngine(frEngine);
            throwExceptionIfNotOK(ret, "AFR_FSDK_UninitialEngine");
        }
        if (isExist(pFRWorkMem)) {
            CLibrary.INSTANCE.free(pFRWorkMem);
        }
    }

    public static Optional<AFR_FSDK_FACEMODEL> getImageFaceFeature(String imagePath, boolean classpath) throws Exception {
        ASVLOFFSCREEN inputImg = loadImage(imagePath, classpath);
        Optional<FaceInfo[]> faces = doFaceDetection(ArcsoftUtil.getFDEngine(), inputImg);
        if (!faces.isPresent()) {
            logger.warn("no face in Image of Input! ");
            return Optional.empty();
        }
        Optional<AFR_FSDK_FACEMODEL> faceFeature = extractFRFeature(ArcsoftUtil.getFREngine(), inputImg, faces.get()[0]);
        if (faceFeature == null) {
            logger.warn("extract face feature in Image failed");
            return Optional.empty();
        }
        return faceFeature;
    }

    public static Optional<AFR_FSDK_FACEMODEL> getRAWImageFaceFeature(String imagePath) throws Exception {
        ASVLOFFSCREEN inputImg = loadRAWImage(imagePath, 640, 480, ASVL_COLOR_FORMAT.ASVL_PAF_I420);
        Optional<FaceInfo[]> faces = doFaceDetection(ArcsoftUtil.getFDEngine(), inputImg);
        if (!faces.isPresent()) {
            logger.warn("no face in Image of Input! ");
            return Optional.empty();
        }
        Optional<AFR_FSDK_FACEMODEL> faceFeature = extractFRFeature(ArcsoftUtil.getFREngine(), inputImg, faces.get()[0]);
        if (!faceFeature.isPresent()) {
            logger.warn("extract face feature in Image  failed");
            return Optional.empty();
        }
        return faceFeature;
    }

    public static Optional<FaceInfo[]> doFaceDetection(Pointer hFDEngine, ASVLOFFSCREEN inputImg) throws Exception {
        PointerByReference ppFaceRes = new PointerByReference();
        NativeLong ret = AFD_FSDKLibrary.INSTANCE.AFD_FSDK_StillImageFaceDetection(hFDEngine, inputImg, ppFaceRes);
        throwExceptionIfNotOK(ret, "AFD_FSDK_StillImageFaceDetection");

        AFD_FSDK_FACERES faceRes = new AFD_FSDK_FACERES(ppFaceRes.getValue());
        if (faceRes.nFace > 0) {
            FaceInfo[] faceInfo = new FaceInfo[faceRes.nFace];
            for (int i = 0; i < faceRes.nFace; i++) {
                MRECT rect = new MRECT(new Pointer(Pointer.nativeValue(faceRes.rcFace.getPointer()) + faceRes.rcFace.size() * i));
                int orient = faceRes.lfaceOrient.getPointer().getInt(i * 4);
                faceInfo[i] = new FaceInfo();
                faceInfo[i].left = rect.left;
                faceInfo[i].top = rect.top;
                faceInfo[i].right = rect.right;
                faceInfo[i].bottom = rect.bottom;
                faceInfo[i].orient = orient;
            }
            return Optional.of(faceInfo);
        }
        return Optional.empty();
    }

    public static Optional<ASAE_FSDK_AGEFACEINPUT> doFaceDetectionOfAgeInput(Pointer hFDEngine, ASVLOFFSCREEN inputImg) throws Exception {
        PointerByReference ppFaceRes = new PointerByReference();
        NativeLong ret = AFD_FSDKLibrary.INSTANCE.AFD_FSDK_StillImageFaceDetection(hFDEngine, inputImg, ppFaceRes);
        throwExceptionIfNotOK(ret, "AFD_FSDK_StillImageFaceDetection");

        AFD_FSDK_FACERES faceRes = new AFD_FSDK_FACERES(ppFaceRes.getValue());
        if (faceRes.nFace > 0) {
            ASAE_FSDK_AGEFACEINPUT input = new ASAE_FSDK_AGEFACEINPUT();
            input.pFaceRectArray = faceRes.rcFace;
            input.lFaceNumber = faceRes.nFace;
            input.pFaceOrientArray = faceRes.lfaceOrient;
            return Optional.of(input);
        }
        return Optional.empty();
    }

    public static Optional<AFR_FSDK_FACEMODEL> extractFRFeature(Pointer hFREngine, ASVLOFFSCREEN inputImg, FaceInfo faceInfo) throws Exception {
        AFR_FSDK_FACEINPUT faceinput = new AFR_FSDK_FACEINPUT();
        faceinput.lOrient = faceInfo.orient;
        faceinput.rcFace.left = faceInfo.left;
        faceinput.rcFace.top = faceInfo.top;
        faceinput.rcFace.right = faceInfo.right;
        faceinput.rcFace.bottom = faceInfo.bottom;

        AFR_FSDK_FACEMODEL faceFeature = new AFR_FSDK_FACEMODEL();
        NativeLong ret = AFR_FSDKLibrary.INSTANCE.AFR_FSDK_ExtractFRFeature(hFREngine, inputImg, faceinput, faceFeature);
        throwExceptionIfNotOK(ret, "AFR_FSDK_ExtractFRFeature");
        return Optional.of(faceFeature.deepCopy());
    }

    public static Optional<Float> facePairMatching(Pointer pFREngine, AFR_FSDK_FACEMODEL faceA, AFR_FSDK_FACEMODEL faceB) throws Exception {
        FloatByReference reference = new FloatByReference();
        NativeLong ret = AFR_FSDKLibrary.INSTANCE.AFR_FSDK_FacePairMatching(pFREngine, faceA, faceB, reference);
        throwExceptionIfNotOK(ret, "AFR_FSDK_FacePairMatching");
        return Optional.of(reference.getValue());
    }

    public static ASVLOFFSCREEN loadImage(String filePath, boolean classpath) throws IOException {
        ASVLOFFSCREEN inputImg = new ASVLOFFSCREEN();
        if (bUseBGRToEngine) {
            BufferInfo bufferInfo = classpath ? ImageLoader.getBGRFromClasspathFile(filePath) : ImageLoader.getBGRFromFile(filePath);
            inputImg.u32PixelArrayFormat = ASVL_COLOR_FORMAT.ASVL_PAF_RGB24_B8G8R8;
            inputImg.i32Width = bufferInfo.width;
            inputImg.i32Height = bufferInfo.height;
            inputImg.pi32Pitch[0] = inputImg.i32Width * 3;
            inputImg.ppu8Plane[0] = new Memory(inputImg.pi32Pitch[0] * inputImg.i32Height);
            inputImg.ppu8Plane[0].write(0, bufferInfo.buffer, 0, inputImg.pi32Pitch[0] * inputImg.i32Height);
            inputImg.ppu8Plane[1] = Pointer.NULL;
            inputImg.ppu8Plane[2] = Pointer.NULL;
            inputImg.ppu8Plane[3] = Pointer.NULL;
        } else {
            BufferInfo bufferInfo = ImageLoader.getI420FromFile(filePath);
            inputImg.u32PixelArrayFormat = ASVL_COLOR_FORMAT.ASVL_PAF_I420;
            inputImg.i32Width = bufferInfo.width;
            inputImg.i32Height = bufferInfo.height;
            inputImg.pi32Pitch[0] = inputImg.i32Width;
            inputImg.pi32Pitch[1] = inputImg.i32Width / 2;
            inputImg.pi32Pitch[2] = inputImg.i32Width / 2;
            inputImg.ppu8Plane[0] = new Memory(inputImg.pi32Pitch[0] * inputImg.i32Height);
            inputImg.ppu8Plane[0].write(0, bufferInfo.buffer, 0, inputImg.pi32Pitch[0] * inputImg.i32Height);
            inputImg.ppu8Plane[1] = new Memory(inputImg.pi32Pitch[1] * inputImg.i32Height / 2);
            inputImg.ppu8Plane[1].write(0, bufferInfo.buffer, inputImg.pi32Pitch[0] * inputImg.i32Height, inputImg.pi32Pitch[1] * inputImg.i32Height / 2);
            inputImg.ppu8Plane[2] = new Memory(inputImg.pi32Pitch[2] * inputImg.i32Height / 2);
            inputImg.ppu8Plane[2].write(0, bufferInfo.buffer, inputImg.pi32Pitch[0] * inputImg.i32Height + inputImg.pi32Pitch[1] * inputImg.i32Height / 2, inputImg.pi32Pitch[2] * inputImg.i32Height / 2);
            inputImg.ppu8Plane[3] = Pointer.NULL;
        }
        inputImg.setAutoRead(false);
        return inputImg;
    }

    public static ASVLOFFSCREEN loadRAWImage(String yuv_filePath, int yuv_width, int yuv_height, int yuv_format) throws Exception {
        int yuv_rawdata_size = 0;
        ASVLOFFSCREEN inputImg = new ASVLOFFSCREEN();
        inputImg.u32PixelArrayFormat = yuv_format;
        inputImg.i32Width = yuv_width;
        inputImg.i32Height = yuv_height;
        if (ASVL_COLOR_FORMAT.ASVL_PAF_I420 == inputImg.u32PixelArrayFormat) {
            inputImg.pi32Pitch[0] = inputImg.i32Width;
            inputImg.pi32Pitch[1] = inputImg.i32Width / 2;
            inputImg.pi32Pitch[2] = inputImg.i32Width / 2;
            yuv_rawdata_size = inputImg.i32Width * inputImg.i32Height * 3 / 2;
        } else if (ASVL_COLOR_FORMAT.ASVL_PAF_NV12 == inputImg.u32PixelArrayFormat) {
            inputImg.pi32Pitch[0] = inputImg.i32Width;
            inputImg.pi32Pitch[1] = inputImg.i32Width;
            yuv_rawdata_size = inputImg.i32Width * inputImg.i32Height * 3 / 2;
        } else if (ASVL_COLOR_FORMAT.ASVL_PAF_NV21 == inputImg.u32PixelArrayFormat) {
            inputImg.pi32Pitch[0] = inputImg.i32Width;
            inputImg.pi32Pitch[1] = inputImg.i32Width;
            yuv_rawdata_size = inputImg.i32Width * inputImg.i32Height * 3 / 2;
        } else if (ASVL_COLOR_FORMAT.ASVL_PAF_YUYV == inputImg.u32PixelArrayFormat) {
            inputImg.pi32Pitch[0] = inputImg.i32Width * 2;
            yuv_rawdata_size = inputImg.i32Width * inputImg.i32Height * 2;
        } else if (ASVL_COLOR_FORMAT.ASVL_PAF_RGB24_B8G8R8 == inputImg.u32PixelArrayFormat) {
            inputImg.pi32Pitch[0] = inputImg.i32Width * 3;
            yuv_rawdata_size = inputImg.i32Width * inputImg.i32Height * 3;
        } else {
            throw new Exception("unsupported  your format");
        }

        // load YUV Image Data from File
        byte[] imagedata = new byte[yuv_rawdata_size];
        File f = new File(yuv_filePath);
        try (InputStream ios = new FileInputStream(f);) {
            ios.read(imagedata, 0, yuv_rawdata_size);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (ASVL_COLOR_FORMAT.ASVL_PAF_I420 == inputImg.u32PixelArrayFormat) {
            inputImg.ppu8Plane[0] = new Memory(inputImg.pi32Pitch[0] * inputImg.i32Height);
            inputImg.ppu8Plane[0].write(0, imagedata, 0, inputImg.pi32Pitch[0] * inputImg.i32Height);
            inputImg.ppu8Plane[1] = new Memory(inputImg.pi32Pitch[1] * inputImg.i32Height / 2);
            inputImg.ppu8Plane[1].write(0, imagedata, inputImg.pi32Pitch[0] * inputImg.i32Height, inputImg.pi32Pitch[1] * inputImg.i32Height / 2);
            inputImg.ppu8Plane[2] = new Memory(inputImg.pi32Pitch[2] * inputImg.i32Height / 2);
            inputImg.ppu8Plane[2].write(0, imagedata, inputImg.pi32Pitch[0] * inputImg.i32Height + inputImg.pi32Pitch[1] * inputImg.i32Height / 2, inputImg.pi32Pitch[2] * inputImg.i32Height / 2);
            inputImg.ppu8Plane[3] = Pointer.NULL;
        } else if (ASVL_COLOR_FORMAT.ASVL_PAF_NV12 == inputImg.u32PixelArrayFormat) {
            inputImg.ppu8Plane[0] = new Memory(inputImg.pi32Pitch[0] * inputImg.i32Height);
            inputImg.ppu8Plane[0].write(0, imagedata, 0, inputImg.pi32Pitch[0] * inputImg.i32Height);
            inputImg.ppu8Plane[1] = new Memory(inputImg.pi32Pitch[1] * inputImg.i32Height / 2);
            inputImg.ppu8Plane[1].write(0, imagedata, inputImg.pi32Pitch[0] * inputImg.i32Height, inputImg.pi32Pitch[1] * inputImg.i32Height / 2);
            inputImg.ppu8Plane[2] = Pointer.NULL;
            inputImg.ppu8Plane[3] = Pointer.NULL;
        } else if (ASVL_COLOR_FORMAT.ASVL_PAF_NV21 == inputImg.u32PixelArrayFormat) {
            inputImg.ppu8Plane[0] = new Memory(inputImg.pi32Pitch[0] * inputImg.i32Height);
            inputImg.ppu8Plane[0].write(0, imagedata, 0, inputImg.pi32Pitch[0] * inputImg.i32Height);
            inputImg.ppu8Plane[1] = new Memory(inputImg.pi32Pitch[1] * inputImg.i32Height / 2);
            inputImg.ppu8Plane[1].write(0, imagedata, inputImg.pi32Pitch[0] * inputImg.i32Height, inputImg.pi32Pitch[1] * inputImg.i32Height / 2);
            inputImg.ppu8Plane[2] = Pointer.NULL;
            inputImg.ppu8Plane[3] = Pointer.NULL;
        } else if (ASVL_COLOR_FORMAT.ASVL_PAF_YUYV == inputImg.u32PixelArrayFormat) {
            inputImg.ppu8Plane[0] = new Memory(inputImg.pi32Pitch[0] * inputImg.i32Height);
            inputImg.ppu8Plane[0].write(0, imagedata, 0, inputImg.pi32Pitch[0] * inputImg.i32Height);
            inputImg.ppu8Plane[1] = Pointer.NULL;
            inputImg.ppu8Plane[2] = Pointer.NULL;
            inputImg.ppu8Plane[3] = Pointer.NULL;
        } else if (ASVL_COLOR_FORMAT.ASVL_PAF_RGB24_B8G8R8 == inputImg.u32PixelArrayFormat) {
            inputImg.ppu8Plane[0] = new Memory(imagedata.length);
            inputImg.ppu8Plane[0].write(0, imagedata, 0, imagedata.length);
            inputImg.ppu8Plane[1] = Pointer.NULL;
            inputImg.ppu8Plane[2] = Pointer.NULL;
            inputImg.ppu8Plane[3] = Pointer.NULL;
        } else {
            throw new Exception("unsupported your format");
        }
        inputImg.setAutoRead(false);
        return inputImg;
    }

    public static Pointer getAgeEngine() throws Exception {

        if (!isExist(pAgeWorkMem)) {
            pAgeWorkMem = CLibrary.INSTANCE.malloc(AGE_WORKBUF_SIZE);
        }

        PointerByReference pEngine = new PointerByReference();

        NativeLong ret = ASGE_FSDKAgeLibrary.INSTANCE.ASAE_FSDK_InitAgeEngine(
                Config.ARCSOFT_APPID,
                Config.ARCSOFT_AGE_SDKKEY,
                pAgeWorkMem,
                AGE_WORKBUF_SIZE,
                pEngine
        );
        throwExceptionIfNotOK(ret, "ASAE_FSDK_InitAgeEngine");
        return pEngine.getValue();
    }

    public static void freeAgeEngine(Pointer pEngine) throws Exception {
        if (isExist(pEngine)) {
            NativeLong ret = ASGE_FSDKAgeLibrary.INSTANCE.ASAE_FSDK_UninitAgeEngine(pEngine);
            throwExceptionIfNotOK(ret, "ASAE_FSDK_UninitAgeEngine");
        }

        if (isExist(pAgeWorkMem)) {
            CLibrary.INSTANCE.free(pAgeWorkMem);
        }
    }

    public static Optional<ASGE_FSDKAge_Version> getAgeVersion(Pointer pEngine) {
        if (isExist(pEngine)) {
            return Optional.of(ASGE_FSDKAgeLibrary.INSTANCE.ASAE_FSDK_GetVersion(pEngine));
        }
        return Optional.empty();
    }

    private static Optional<Integer[]> innerAgeDetection(Pointer ageEngine, Pointer fdEngine, ASVLOFFSCREEN input, boolean preview) throws Exception {
        Optional<ASAE_FSDK_AGEFACEINPUT> ageInput = doFaceDetectionOfAgeInput(fdEngine, input);
        if (ageInput.isPresent()) {
            ASAE_FSDK_AGERESULT result = new ASAE_FSDK_AGERESULT();
            NativeLong ret = preview ?
                    ASGE_FSDKAgeLibrary.INSTANCE.ASAE_FSDK_AgeEstimation_Preview(ageEngine, input, ageInput.get(), result)
                    : ASGE_FSDKAgeLibrary.INSTANCE.ASAE_FSDK_AgeEstimation_StaticImage(ageEngine, input, ageInput.get(), result);
            throwExceptionIfNotOK(ret, preview ? "ASAE_FSDK_AgeEstimation_Preview" : "ASAE_FSDK_AgeEstimation_StaticImage");
            if (!isExist(result.pAgeResultArray)) {
                return Optional.empty();
            }

            int[] ages = result.pAgeResultArray.getIntArray(0, result.lFaceNumber);
            return Optional.of(Arrays.stream(ages).boxed().toArray(Integer[]::new));
        }
        return Optional.empty();
    }

    public static Optional<Integer[]> doAgeDetectionStaticImage(Pointer ageEngine, Pointer fdEngine, ASVLOFFSCREEN input) throws Exception {
        return innerAgeDetection(ageEngine, fdEngine, input, false);
    }

    public static Optional<Integer[]> innerAgeDetectionPreview(Pointer ageEngine, Pointer fdEngine, ASVLOFFSCREEN input) throws Exception {
        return innerAgeDetection(ageEngine, fdEngine, input, true);
    }

    public static Pointer getGenderEngine() throws Exception {
        PointerByReference pGenderEngine = new PointerByReference();
        if (!isExist(pGenderWorkMem)) {
            pGenderWorkMem = CLibrary.INSTANCE.malloc(GENDER_WORKBUF_SIZE);
        }
        NativeLong ret = ASGE_FSDKGenderLibrary.INSTANCE.ASGE_FSDK_InitGenderEngine(Config.ARCSOFT_APPID, Config.ARCSOFT_GENDER_SDKKEY, pGenderWorkMem, GENDER_WORKBUF_SIZE, pGenderEngine);
        throwExceptionIfNotOK(ret, "ASGE_FSDK_InitGenderEngine");
        return pGenderEngine.getValue();
    }

    public static void freeGenderEngine(Pointer hEngine) throws Exception {
        if (isExist(hEngine)) {
            NativeLong ret = ASGE_FSDKGenderLibrary.INSTANCE.ASGE_FSDK_UninitGenderEngine(hEngine);
            throwExceptionIfNotOK(ret, "ASGE_FSDK_UninitGenderEngine");
        }

        if (isExist(pGenderWorkMem)) {
            CLibrary.INSTANCE.free(pGenderWorkMem);
            pGenderWorkMem = null;
        }
    }

    public static Optional<ASGE_FSDKGender_Version> getGenderVersion(Pointer hEngine) throws Exception {
        if (isExist(hEngine)) {
            ASGE_FSDKGender_Version version = ASGE_FSDKGenderLibrary.INSTANCE.ASGE_FSDK_GetVersion(hEngine);
            if (version != null) {
                return Optional.of(version);
            }
        }
        return Optional.empty();
    }
}
