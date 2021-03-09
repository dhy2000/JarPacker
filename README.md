# JarPacker

An automatic tool to pack runnable jar from a Java source directory with **only one Main class**. 

Usage: 

    java -jar JarPacker.jar [sourcedir]

For instance, if there is a java project directory named `Project` , then you can put this tool outside the directory, which can be described as directory-tree below:

    ./
    ../
    Project/
        [files inside the directory]
    JarPacker.jar

then run `java -jar JarPacker.jar Project` , and you will get a runnable jar file named `Project.jar` beside `JarPacker.jar` .

## Feature

This tool can find the only Main class **automatically** and build a java project into a runnable jar file.

## Warnings

The behavior will be unpredictable if:
- There is no Main class or more than one with method `public static void main(String args[])` 

  

