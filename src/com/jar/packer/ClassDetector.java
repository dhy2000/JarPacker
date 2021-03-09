package com.jar.packer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;

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
        InnerClassLoader classLoader = new InnerClassLoader();
        String classFullName = getFullClassName(classFile, classRoot);
        String classAbsName = classRoot.getAbsolutePath() + File.separator + classFullName;
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
