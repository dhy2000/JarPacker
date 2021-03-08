package com.jar.packer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class JarTask {

    private final File dir;
    private String mainClass;
    private File maniFest;

    private final Collection<File> sourceList = new ArrayList<>();

    private void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    private void writeManifest() {
        if (Objects.isNull(mainClass)) {
            System.out.println("Failed to find Main Class");
            System.exit(0);
        }
        String metaInf = dir.getAbsolutePath() +
            File.separator + "META-INF";
        File metaInfDir = new File(metaInf);
        if (!metaInfDir.exists()) {
            boolean flg = new File(metaInf).mkdirs();
            if (!flg) {
                System.out.println("Writing Manifest: Failed to create META-INF");
                System.exit(0);
            }
        }
        String manifest = metaInf + File.separator + "MANIFEST.MF";
        this.maniFest = new File(manifest);
        String content = "Manifest-Version: 1.0\nMain-Class: " + mainClass + "\n";
        try {
            FileOutputStream fos = new FileOutputStream(manifest);
            BufferedOutputStream fout = new BufferedOutputStream(fos);
            fout.write(content.getBytes());
            fout.flush();
            fout.close();
        } catch (IOException e) {
            System.out.println("Failed to write Manifest.");
            System.exit(0);
        }
    }

    private void packJar() {
        System.out.println("Packing jar...");
        String jarName = dir.getName() + ".jar";
        String command = "cmd /c cd " + ClassDetector.getPackageRoot() + " && " +
            "jar.exe cvfm " +
            jarName + " " + maniFest.getAbsolutePath() + " .";
        // System.out.println(command);
        try {
            Process proc = Runtime.getRuntime().exec(command);
            int status = proc.waitFor();
            if (status != 0) {
                System.out.println("Failed to pack jar");
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to pack jar");
            System.exit(0);
        }
        // Move jar to here
        try {
            Files.deleteIfExists(Paths.get(jarName));
            Files.move(Paths.get(
                ClassDetector.getPackageRoot().getAbsolutePath() + File.separator + jarName)
                , Paths.get("." + File.separator + jarName));
        } catch (IOException e) {
            System.out.println("Failed to move jar to current position.");
        }
    }

    public JarTask(String dir) {
        this.dir = new File(dir);
        DirectoryVisit.visit(this.dir, file -> {
            if (file.isFile()) {
                if (file.getName().endsWith(".java")) {
                    System.out.println("Java source found: " + file.getAbsolutePath());
                    sourceList.add(file);
                } else if (file.getName().endsWith(".class")) {
                    System.out.println("Delete pre-compiled class: " + file.getAbsolutePath());
                    boolean ign = file.delete();
                }
            }
        });
        Compiler.compile(sourceList);
        DirectoryVisit.visit(this.dir, file -> {
            if (file.isDirectory()) {
                ClassDetector.addClassLoaderPath(file);
            }
        });
        DirectoryVisit.visit(this.dir, file -> {
            if (file.isFile()) {
                if (file.getName().endsWith(".java")) {
                    if (Objects.isNull(mainClass)) {
                        setMainClass(ClassDetector.getMainClass(file));
                    }
                }
            }
        });
        writeManifest();
        packJar();
    }

}
