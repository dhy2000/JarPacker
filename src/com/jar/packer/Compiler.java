package com.jar.packer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

public class Compiler {
    public static void compile(Collection<File> javaList) {
        // Write Source List
        File srcList = new File("temp_srclist.tmp");
        String listContent = javaList.stream().map(file -> file.getAbsolutePath() + "\n")
            .collect(Collectors.joining());
        try {
            FileOutputStream fos = new FileOutputStream(srcList);
            BufferedOutputStream fout = new BufferedOutputStream(fos);
            fout.write(listContent.getBytes());
            fout.flush();
            fout.close();
        } catch (IOException e) {
            System.out.println("Failed to write source list");
            boolean ign = srcList.delete();
            System.exit(0);
        }

        try {
            System.out.println("Compiling all java files");
            String command = "javac -encoding UTF-8 @" + srcList.getName();
            // System.out.println(command);
            Process proc = Runtime.getRuntime().exec(command);
            int status = proc.waitFor();
            if (status != 0) {
                System.out.println("Error on compiling java files");
                final int maxBufferSize = 10240;
                byte[] buffer = new byte[maxBufferSize];
                int len = proc.getInputStream().read(buffer);
                System.out.println("javac stdout: ");
                System.out.println(len > 0 ? new String(buffer, 0, len) : "");
                len = proc.getErrorStream().read(buffer);
                System.out.println("javac stderr: ");
                System.out.println(len > 0 ? new String(buffer, 0, len) : "");
                System.exit(0);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error on compiling java files");
            System.exit(0);
        }

        boolean ign = srcList.delete();
    }
}
