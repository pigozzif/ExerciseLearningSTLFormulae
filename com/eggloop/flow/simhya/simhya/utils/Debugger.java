/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.utils;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 *
 * @author Luca
 */
public class Debugger {
    private static File debugFile;
    private static PrintWriter p;
    private static boolean debugToFile;
    private static boolean active;

    static {
        active = false;
        debugFile = null;
        debugToFile = false;
    }

    public static void setDebugFile(String filename) {
        debugFile = new File(filename);
        debugToFile = true;
        try { p = new PrintWriter(new FileOutputStream(filename)); }
        catch(IOException e) {System.err.println(e); }
    }

    public static void put(String s) {
        if (active)
            if (debugToFile)
               p.println(s);
            else
                System.out.println(s);
    }

    public static void activate() {
        active = true;
    }

    public static void deActivate() {
        active = false;
    }

}
