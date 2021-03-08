# JarPacker

Usage: 

    java -jar JarPacker.jar [sourcedir]

For instance, if there is a java project directory named `Project` , then you can put this tool outside the directory, which can be described as directory-tree below:

    ./
    ../
    Project/
        [files inside the directory]
    JarPacker.jar

then run `java -jar JarPacker.jar Project` , and you will get a runnable jar file named `Project.jar` beside `JarPacker.jar` .

## Warnings

The behavior is unpredictable if:
- There is no Main class or more than one with method `public static void main(String args[])` 
- the `package` declarations in Java source code do not match the project directory tree.

