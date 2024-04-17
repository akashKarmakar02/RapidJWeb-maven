package com.neo.RapidJWeb.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class ClassPathScanner {

    public List<Class<?>> getClasses(String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        String classPath = getClassPath(packageName);

        if (classPath != null) {
            scanClasses(new File(classPath), packageName, classes);
        }

        return classes;
    }

    private void scanClasses(File directory, String packageName, List<Class<?>> classes) throws ClassNotFoundException {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        scanClasses(file, packageName + "." + file.getName(), classes);
                    } else if (file.getName().endsWith(".class")) {
                        String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                        Class<?> clazz = Class.forName(className);
                        classes.add(clazz);
                    }
                }
            }
        }
    }

    private String getClassPath(String packageName) {
        String classPath = ClassLoader.getSystemClassLoader().getResource(packageName.replace('.', '/')).getPath();
        return new File(classPath).getPath();
    }
}
