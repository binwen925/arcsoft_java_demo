package com.zbw.arcsoft;

import com.sun.jna.Native;
import com.sun.jna.Platform;

public class LoadUtils {

    public static enum Function {
        FD("libarcsoft_fsdk_face_detection"),
        FR("libarcsoft_fsdk_face_recognition"),
        AGE("libarcsoft_fsdk_age_estimation"),
        GENDER("libarcsoft_fsdk_gender_estimation");

        private String name;

        Function(String name) {
            this.name = name;
        }
    }

    public static <T> T loadLibrary(Function function, Class<T> interfaceClass) {
        String path = null;
        String format = "lib/%s/%s.%s";
        if (Platform.isWindows()) {
            if (Platform.is64Bit()) {
                path = String.format(format, "win64", function.name, "dll");
            } else {
                path = String.format(format, "win32", function.name, "dll");
            }
        } else if (Platform.is64Bit() && Platform.isLinux()) {
            path = String.format(format, "linux-x64", function.name, "so");
        } else {
            throw new UnsupportedOperationException("unsupported platform");
        }

        return loadLibrary(path, interfaceClass);
    }

    public static <T> T loadLibrary(String filePath, Class<T> interfaceClass) {
        return Native.loadLibrary(filePath, interfaceClass);
    }
}
