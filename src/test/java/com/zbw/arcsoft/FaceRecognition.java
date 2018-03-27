package com.zbw.arcsoft;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.zbw.arcsoft.age.ASAE_FSDK_AGEFACEINPUT;
import com.zbw.arcsoft.age.ASAE_FSDK_AGERESULT;
import com.zbw.arcsoft.age.ASGE_FSDKAgeLibrary;
import com.zbw.arcsoft.age.ASGE_FSDKAge_Version;
import com.zbw.arcsoft.fd.AFD_FSDK_Version;
import com.zbw.arcsoft.fr.AFR_FSDK_FACEMODEL;
import com.zbw.arcsoft.fr.AFR_FSDK_Version;
import com.zbw.arcsoft.gender.ASGE_FSDKGender_Version;
import com.zbw.arcsoft.utils.ArcsoftUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class FaceRecognition {

    private static final Logger LOGGER = LoggerFactory.getLogger(FaceRecognition.class);

    @Test
    public void pintFDEngineVersion() throws Exception {
        Pointer fdEngine = ArcsoftUtil.getFDEngine();
        Optional<AFD_FSDK_Version> version = ArcsoftUtil.getFDVersion(fdEngine);
        if (version.isPresent()) {
            System.out.println(version.get());
        }
        ArcsoftUtil.freeFDEngine(fdEngine);
    }

    @Test
    public void printFREngineVersion() throws Exception {
        Pointer frEngine = ArcsoftUtil.getFREngine();
        Optional<AFR_FSDK_Version> version = ArcsoftUtil.getFRVersion(frEngine);
        if (version.isPresent()) {
            System.out.println(version.get());
        }
        ArcsoftUtil.freeFREngine(frEngine);
    }

    @Test
    public void testFaceDetection() throws Exception {
        Pointer fdEngine = ArcsoftUtil.getFDEngine();
        ASVLOFFSCREEN input = ArcsoftUtil.loadImage("lena.bmp", true);
        Optional<FaceInfo[]> faces = ArcsoftUtil.doFaceDetection(fdEngine, input);
        if (faces.isPresent()) {
            Stream.of(faces.get()).forEach(face -> {
                LOGGER.info(String.format("left:%d,top:%d,bottom:%d,right:%d,orient:%d", face.left, face.top, face.bottom, face.right, face.orient));
            });
        }
        ArcsoftUtil.freeFREngine(fdEngine);
    }

    @Test
    public void testExtractFaceFeature() throws Exception {
        Pointer frEngine = ArcsoftUtil.getFREngine();
        Pointer fdEngine = ArcsoftUtil.getFDEngine();
        ASVLOFFSCREEN input = ArcsoftUtil.loadImage("lena.bmp", true);

        Optional<FaceInfo[]> faces = ArcsoftUtil.doFaceDetection(fdEngine, input);
        if (faces.isPresent()) {
            FaceInfo faceInfo = faces.get()[0];
            Optional<AFR_FSDK_FACEMODEL> model = ArcsoftUtil.extractFRFeature(frEngine, input, faceInfo);
            if (model.isPresent()) {
                byte[] data = model.get().toByteArray();
                try (OutputStream out = new FileOutputStream("lena.data")) {
                    out.write(data);
                }
            }
        }
        ArcsoftUtil.freeFDEngine(fdEngine);
        ArcsoftUtil.freeFREngine(frEngine);
    }

    @Test
    public void testFaceFeatureMatching() throws Exception {
        Pointer frEngine = ArcsoftUtil.getFREngine();
        Pointer fdEngine = ArcsoftUtil.getFDEngine();
        ASVLOFFSCREEN input = ArcsoftUtil.loadImage("lena.bmp", true);
        byte[] lenaFeature = fromClasspathFile("lena.data");
        AFR_FSDK_FACEMODEL lena = AFR_FSDK_FACEMODEL.fromByteArray(lenaFeature);

        Optional<FaceInfo[]> faces = ArcsoftUtil.doFaceDetection(fdEngine, input);
        if (faces.isPresent()) {
            FaceInfo faceInfo = faces.get()[0];
            Optional<AFR_FSDK_FACEMODEL> model = ArcsoftUtil.extractFRFeature(frEngine, input, faceInfo);
            if (model.isPresent()) {
                Optional<Float> score = ArcsoftUtil.facePairMatching(frEngine, lena, model.get());
                if (score.isPresent()) {
                    LOGGER.debug("similar score is {}", score.get());
                }
            }
        }

        ArcsoftUtil.freeFDEngine(fdEngine);
        ArcsoftUtil.freeFREngine(frEngine);
    }

    private byte[] fromClasspathFile(String file) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(file)) {
            byte[] buffer = new byte[in.available()];
            in.read(buffer);
            return buffer;
        }
    }

    @Test
    public void printAgeEngineVersion() throws Exception {
        Pointer ageEngine = ArcsoftUtil.getAgeEngine();
        Optional<ASGE_FSDKAge_Version> version = ArcsoftUtil.getAgeVersion(ageEngine);
        if (version.isPresent()) {
            LOGGER.debug("version info:\r\n{}", version.get());
        }
        ArcsoftUtil.freeAgeEngine(ageEngine);
    }

    @Test
    public void testDoAgeEstimation() throws Exception {
        Pointer ageEngine = ArcsoftUtil.getAgeEngine();
        Pointer fdEngine = ArcsoftUtil.getFDEngine();

        ASVLOFFSCREEN input = ArcsoftUtil.loadImage("lena.bmp", true);
        Optional<ASAE_FSDK_AGEFACEINPUT> ageInput = ArcsoftUtil.doFaceDetectionOfAgeInput(fdEngine, input);
        if (ageInput.isPresent()) {
            ASAE_FSDK_AGERESULT result = new ASAE_FSDK_AGERESULT();
            NativeLong ret = ASGE_FSDKAgeLibrary.INSTANCE.ASAE_FSDK_AgeEstimation_StaticImage(
                    ageEngine,
                    input,
                    ageInput.get(),
                    result
            );
            if (ret.longValue() != 0) {
                LOGGER.warn(String.format("ASGE_FSDK_GenderEstimation_StaticImage ret 0x%x", ret.longValue()));
            } else {
                LOGGER.debug("face numbers:{}", result.lFaceNumber);
                int[] ages = result.pAgeResultArray.getIntArray(0, result.lFaceNumber);
                Stream.of(ages).forEach(age -> {
                    LOGGER.debug("age is {}", age);
                });
            }
        }

        ArcsoftUtil.freeFDEngine(fdEngine);
        ArcsoftUtil.freeAgeEngine(ageEngine);
    }

    @Test
    public void doAgeDetectionStaticImage() throws Exception {
        Pointer ageEngine = ArcsoftUtil.getAgeEngine();
        Pointer fdEngine = ArcsoftUtil.getFDEngine();

        ASVLOFFSCREEN input = ArcsoftUtil.loadImage("C:\\文档\\vivo手机照片\\IMG_20170127_142120.jpg", false);
        Optional<Integer[]> ages = ArcsoftUtil.doAgeDetectionStaticImage(ageEngine, fdEngine, input);
        if (ages.isPresent()) {
            LOGGER.debug("ages is {}", Arrays.toString(ages.get()));
        }

        ArcsoftUtil.freeFDEngine(fdEngine);
        ArcsoftUtil.freeAgeEngine(ageEngine);
    }

    @Test
    public void printGenderEngineVersion() throws Exception {
        Pointer genderEngine = ArcsoftUtil.getGenderEngine();
        Optional<ASGE_FSDKGender_Version> version = ArcsoftUtil.getGenderVersion(genderEngine);
        if (version.isPresent()) {
            LOGGER.debug("version is :\r\n{}", version.get());
        }
        ArcsoftUtil.freeGenderEngine(genderEngine);
    }
}
