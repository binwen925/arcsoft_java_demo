package com.zbw.arcsoft;

import com.sun.jna.Pointer;
import com.zbw.arcsoft.utils.ArcsoftUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.ch.IOUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.stream.Stream;

public class FaceRecognition {

    private static final Logger LOGGER = LoggerFactory.getLogger(FaceRecognition.class);

    @Test
    public void pintFDEngineVersion() throws Exception {
        Pointer fdEngine = ArcsoftUtil.getFDEngine();
        AFD_FSDK_Version version = ArcsoftUtil.getFDVersion(fdEngine);
        if (version != null) {
            System.out.println(version);
        }
        ArcsoftUtil.freeFDEngine(fdEngine);
    }

    @Test
    public void printFREngineVersion() throws Exception {
        Pointer frEngine = ArcsoftUtil.getFREngine();
        AFR_FSDK_Version version = ArcsoftUtil.getFRVersion(frEngine);
        if (version != null) {
            System.out.println(version);
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
}
