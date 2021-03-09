package com.jar.packer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.function.Consumer;

public class FileUtils {

    public static boolean mkdir(File dir) {
        if (dir.exists()) {
            return false;
        } else {
            return dir.mkdirs();
        }
    }

    public static void rmdir(File dir) {
        if (!dir.exists()) {
            return;
        }
        if (dir.isFile()) {
            boolean ign = dir.delete();
            return;
        }
        File[] list = dir.listFiles();
        if (Objects.isNull(list)) {
            return;
        }
        for (File file : list) {
            if (file.isDirectory()) {
                rmdir(file);
            } else {
                boolean ign = file.delete();
            }
        }
        boolean ign = dir.delete();
    }

    public static File createTempDir() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");
        String dirName = "temp_" + df.format(new Date());
        File dir;
        int count = 0;
        final int maxRetryCount = 5;
        while ((dir = new File(dirName)).exists() && (dir.isDirectory())) {
            dirName = "temp_" + df.format(new Date());
            count++;
            if (count >= maxRetryCount) {
                return null;
            }
        }
        return dir.mkdirs() ? dir : null;
    }

    public static void forEach(File dir, Consumer<File> action) {
        File[] list = dir.listFiles();
        if (Objects.isNull(list)) {
            return;
        }
        for (File file : list) {
            if (file.isDirectory()) {
                action.accept(file);
                forEach(file, action);
            }
            else {
                action.accept(file);
            }
        }
    }

}
