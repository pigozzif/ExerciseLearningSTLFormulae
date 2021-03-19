package com.eggloop.flow.utils.files;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by ssilvetti on 20/05/16.
 */
public class WriteToFile {

    public static void writeDesignToFile(String filepath, List<double[]> design) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(filepath, "UTF-8");
            for (double[] doubles : design) {
                writer.println(designToString(doubles));
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }

    public static void writeDoubleToFile(String filepath, double[] values) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(filepath, "UTF-8");
            writer.println(designToString(values));
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }

    public static String designToString(double[] design) {
        StringBuilder build = new StringBuilder();
        for (double v : design) {
            build.append(v);
            build.append(",");
        }

        return build.substring(0, build.length() - 1);

    }
}
