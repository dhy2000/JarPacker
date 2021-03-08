package com.jar.packer;

import java.io.File;
import java.util.function.Consumer;

public class DirectoryVisit {

    public static void visit(File dir, Consumer<File> action) {
        File[] list = dir.listFiles();
        if (null == list) {
            return;
        }
        for (File file : list) {
            if (file.isDirectory()) {
                action.accept(file);
                visit(file, action);
            }
            else {
                action.accept(file);
            }
        }
    }

}
