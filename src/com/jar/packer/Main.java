package com.jar.packer;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
	        System.out.println("Missing argument: directory of source");
	        System.out.println("Usage: java -jar JarPacker.jar [Directory of Source]");
            return;
        }
	    new JarTask(args[0]);
    }
}
