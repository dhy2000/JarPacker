package com.jar.packer;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassDetector {

    private ClassDetector() {}

    public static void addClassLoaderPath(File rootPath) {
        Method method;
        try {
            method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            boolean access = method.isAccessible();
            try {
                if (!access) {
                    method.setAccessible(true);
                }
                URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
                method.invoke(classLoader, rootPath.toURI().toURL());
            } catch (Throwable t) {
                System.out.println("Failed to addURL");
                method.setAccessible(access);
                System.exit(0);
            }
            method.setAccessible(access);
        } catch (NoSuchMethodException e) {
            System.out.println("Failed to add Class Loader Path: " + rootPath.getAbsolutePath());
            System.exit(0);
        }

    }

    private static String getFullClassName(File classFile, File classRoot) {
        // classFile: D:\\Files\\project\\out\\com\\package\\main\\Main.class
        // classRoot: D:\\Files\\project\\out
        String relativePath = classFile.getAbsolutePath().replaceAll(
            classRoot.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"),
            ""
        );
        return relativePath.replaceAll("\\.class$", "")
            .replace(File.separator, ".")
            .replaceAll("^\\.", "");
    }

    public static String getMainClass(File classFile, File classRoot) {
        String classFullName = getFullClassName(classFile, classRoot);
        Class<?> cls;
        try {
            cls = Class.forName(classFullName);
        } catch (Throwable e) {
            return null;
        }
        try {
            Method mainMethod = cls.getMethod("main", String[].class);
            return Modifier.isStatic(mainMethod.getModifiers()) ? classFullName : null;
        } catch (NoSuchMethodException | NoClassDefFoundError e) {
            return null;
        }
    }

}
