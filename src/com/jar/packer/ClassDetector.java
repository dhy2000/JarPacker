package com.jar.packer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassDetector {
    public static class InnerClassLoader extends ClassLoader {
        // name: class path
        public synchronized Class<?> loadClass(String name, File file) {
            Class<?> cls = findLoadedClass(name);
            if (Objects.nonNull(cls)) {
                return cls;
            }
            FileInputStream fin;
            try {
                fin = new FileInputStream(file);
                byte[] buffer = new byte[(int) file.length()];
                int len = fin.read(buffer);
                fin.close();
                return defineClass(null, buffer, 0, len);
            } catch (IOException e) {
                System.out.println("Fatal error: Failed to read class file " + file);
            }
            return null;
        }
    }

    private static File packageRoot;

    public static File getPackageRoot() {
        return packageRoot;
    }

    public static void addClassLoaderPath(File path) {
        Method method;
        try {
            method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            boolean access = method.isAccessible();
            try {
                if (!access) {
                    method.setAccessible(true);
                }
                URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
                method.invoke(classLoader, path.toURI().toURL());
            } catch (Throwable t) {
                System.out.println("Failed to addURL");
                method.setAccessible(access);
                System.exit(0);
            }
            method.setAccessible(access);
        } catch (NoSuchMethodException e) {
            System.out.println("Failed to add Class Loader Path: " + path.getAbsolutePath());
            System.exit(0);
        }

    }

    public static String packageNameInJava(File javaFile) {
        try {
            Scanner fin = new Scanner(new FileInputStream(javaFile));
            StringBuilder fileContent = new StringBuilder(fin.nextLine());
            while (fin.hasNext()) {
                fileContent.append(fin.nextLine());
            }
            fin.close();
            Matcher matcher = Pattern.compile("^package\\W+(?<package>[A-Za-z0-9.]+);").matcher(fileContent.toString());
            if (matcher.find()) {
                return matcher.group("package");
            }
            return "";
        } catch (FileNotFoundException e) {
            System.out.println("Fatal Error: cannot read " + javaFile);
        }
        return "";
    }

    public static String getMainClass(File javaFile) {
        String packageName = packageNameInJava(javaFile);
        InnerClassLoader classLoader = new InnerClassLoader();
        String className = javaFile.getName().replaceAll("\\.java$", "");
        String classFullName = (packageName.equals("") ? "" : (packageName + ".")) + className;
        String absPath = javaFile.getAbsolutePath().replaceAll("\\.java$", "");
        String classPath = absPath.replaceAll(
            classFullName.replace('.', File.separatorChar).replaceAll("\\\\", "\\\\\\\\"),
            classFullName
        );
        String pkgRoot = absPath.replaceAll(
            classFullName.replace('.', File.separatorChar).replaceAll("\\\\", "\\\\\\\\"),
            ""
        );
        if (Objects.isNull(packageRoot)) {
            packageRoot = new File(pkgRoot);
        }
        File classFile = new File(javaFile.getAbsolutePath().replaceAll("\\.java$", ".class"));
        String classAbsName = classPath + "." + className;
        Class<?> cls;
        try {
            cls = classLoader.loadClass(classAbsName, classFile);
        } catch (Throwable e) {
            return null;
        }
        if (Objects.isNull(cls)) {
            return null;
        }
        try {
            Method mainMethod = cls.getMethod("main", String[].class);
            return Modifier.isStatic(mainMethod.getModifiers()) ? classFullName : null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

}
