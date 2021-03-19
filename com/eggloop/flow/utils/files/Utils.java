package com.eggloop.flow.utils.files;

import java.io.*;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.function.UnaryOperator;

/**
 * Created by simone on 30/12/16.
 */
public class Utils {
    public static void savetofile(String file, boolean[][] data)
            throws FileNotFoundException {
        final PrintWriter pw = new PrintWriter(file);
        for (boolean[] aData : data) {
            final int last_j = aData.length - 1;
            for (int j = 0; j < last_j; j++) {
                pw.print(aData[j] ? '1' : '0');
                pw.print(", ");
            }
            pw.print(aData[last_j] ? '1' : '0');
            pw.println();
        }
        pw.close();
    }

    public static void savetofile(String file, double[][] data)
            throws FileNotFoundException {
        final PrintWriter pw = new PrintWriter(file);
        for (double[] aData : data) {
            final int last_j = aData.length - 1;
            for (int j = 0; j < last_j; j++) {
                pw.print(aData[j]);
                pw.print(", ");
            }
            pw.print(aData[last_j]);
            pw.println();
        }
        pw.close();
    }


    private static String designToString(double[] design) {
        StringBuilder build = new StringBuilder();
        for (double v : design) {
            build.append(v);
            build.append(",");
        }

        return build.substring(0, build.length() - 1);

    }

    public static void writeDesignToFile(String filepath, List<double[]> design) {
        try (PrintWriter writer = new PrintWriter(filepath, "UTF-8")) {
            for (double[] doubles : design) {
                writer.println(designToString(doubles));
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void writeDoubleToFile(String filepath, double[] values) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(filepath, "UTF-8");
            writer.println(designToString(values));
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static  UnaryOperator<String> getFilePath(Class instance) {
        return fileName -> instance.getClassLoader().getResource(fileName).getPath();
    }

    public static double[][] readMatrixFromFile(String filePath) {
        int rows = 1;
        int cols = 0;
        Scanner scanner = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();
            scanner = new Scanner(line);
            scanner.useDelimiter(",");
            scanner.useLocale(Locale.US);
            while (scanner.hasNext()) {
                scanner.nextDouble();
                cols++;
            }
            while (reader.readLine() != null) {
                rows++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }

        }
        double[][] x = new double[rows][cols];
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));) {

            for (int i = 0; i < rows; i++) {
                try (Scanner scanner1 = new Scanner(reader.readLine())) {
                    scanner1.useDelimiter(",");
                    scanner1.useLocale(Locale.US);
                    int j = 0;
                    while (scanner1.hasNext() && j < cols) {
                        x[i][j++] = scanner1.nextDouble();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return x;
    }

    public static double[] readVectorFromFile(String filePath) {
        int cols = 0;
        try {
            String line;

            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                line = reader.readLine();
            }
            try (Scanner scanner = new Scanner(line)) {
                scanner.useDelimiter(",");
                scanner.useLocale(Locale.US);
                while (scanner.hasNext()) {
                    scanner.nextDouble();
                    cols++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        double[] x = new double[cols];
        try {
            String line;

            int j;
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

                line = reader.readLine();
            }
            try (Scanner scanner = new Scanner(line)) {
                scanner.useDelimiter(",");
                scanner.useLocale(Locale.US);
                j = 0;
                while (scanner.hasNext() && j < cols) {
                    x[j++] = scanner.nextDouble();
                }
            }
            return x;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static double[][][] readMatrixMultiFromFile(int n, String filePath) {
        int rows = 1;
        int cols = 0;
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line = reader.readLine();
                try (Scanner scanner = new Scanner(line)) {
                    scanner.useDelimiter(",");
                    scanner.useLocale(Locale.US);
                    while (scanner.hasNext()) {
                        scanner.nextDouble();
                        cols++;
                    }
                }
                while (reader.readLine() != null) {
                    rows++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int m = cols / n;
        double[][][] x = new double[rows][m][n];
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                for (int i = 0; i < rows; i++) {
                    String line = reader.readLine();
                    try (Scanner scanner = new Scanner(line)) {
                        scanner.useDelimiter(",");
                        scanner.useLocale(Locale.US);

                        for (int k = 0; k < m; k += m) {
                            int j = 0;
                            while (scanner.hasNext()) {
                                for (int l = 0; l < m; l++) {
                                    x[i][l][j] = scanner.nextDouble();
                                }
                                j = j + 1;
                            }
                        }
                    }

                }
            }
            return x;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}

