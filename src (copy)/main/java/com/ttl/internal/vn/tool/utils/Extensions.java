package com.ttl.internal.vn.tool.utils;

import java.io.File;

public class Extensions {
    public static boolean haveFileExtension(File file, String extension) {
        if (!file.isFile()) {
            return false;
        }
        String filename = file.getName();
        extension = extension.trim();
        extension = extension.startsWith(".") ? extension : "." + extension;
        return filename.toLowerCase().endsWith(extension.toLowerCase());
    }

    public static boolean isLogFile(File file) {
        return haveFileExtension(file, "log");
    }
}
