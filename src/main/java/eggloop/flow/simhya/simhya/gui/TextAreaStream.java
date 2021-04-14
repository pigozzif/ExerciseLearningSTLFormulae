/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.gui;

import java.io.PrintStream;
import java.util.Locale;
import javax.swing.JTextArea;


/**
 *
 * @author luca
 */
public class TextAreaStream extends PrintStream {
    final JTextArea pane;
    private String toAppend;
   


    public TextAreaStream(final JTextArea pane) {
        super(System.out,true);
        this.pane = pane;
        pane.setText("");
    }

    @Override
    public PrintStream format(String format, Object... args) {
        String s = String.format(format, args);
        append(s);
        return this;
    }

    @Override
    public PrintStream format(Locale l, String format, Object... args) {
        String s = String.format(l,format, args);
        append(s);
        return this;
    }

    @Override
    public void print(boolean b) {
        String s = "" + b;
        append(s);
    }

    @Override
    public void print(char c) {
        String s = "" + c;
        append(s);
    }

    @Override
    public void print(int i) {
        String s = "" + i;
        append(s);
    }

    @Override
    public void print(long l) {
        String s = "" + l;
        append(s);
    }

    @Override
    public void print(float f) {
        String s = "" + f;
        append(s);
    }

    @Override
    public void print(double d) {
        String s = "" + d;
        append(s);
    }

    @Override
    public void print(char[] c) {
        String s = "" + java.util.Arrays.toString(c);
        append(s);
    }

    @Override
    public void print(String s1) {
        String s = "" + s1;
        append(s);
    }

    @Override
    public void print(Object obj) {
        String s = "" + obj.toString();
        append(s);
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        String s = String.format(format, args);
        append(s);
        return this;
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        String s = String.format(l,format, args);
        append(s);
        return this;
    }

    @Override
    public void println() {
        String s = "" + "\n";
        append(s);
    }

    @Override
    public void println(boolean x) {
        String s = "" + x + "\n";
        append(s);
    }

    @Override
    public void println(char x) {
        String s = "" + x + "\n";
        append(s);
    }

    @Override
    public void println(int x) {
        String s = "" + x + "\n";
        append(s);
    }

    @Override
    public void println(long x) {
        String s = "" + x + "\n";
        append(s);
    }

    @Override
    public void println(float x) {
        String s = "" + x + "\n";
        append(s);
    }

    @Override
    public void println(double x) {
        String s = "" + x + "\n";
        append(s);
    }

    @Override
    public void println(char[] x) {
        String s = "" + java.util.Arrays.toString(x) + "\n";
        append(s);
    }

    @Override
    public void println(String x) {
        String s = "" + x + "\n";
        append(s);
    }

    @Override
    public void println(Object x) {
        String s = "" + x.toString() + "\n";
        append(s);
    }



    public void saveToFile(String filename) {
        try {
            java.io.PrintWriter w = new java.io.PrintWriter(filename);
            w.print(pane.getText());
            w.close();
        } catch (java.io.IOException e) {
            String s = "###ERROR: Cannot save log to file " + filename + ": " + e.getMessage() + " \n";
            append(s);
        }

    }


    private void append(String s) {
        //int position = pane.getCaretPosition();

        pane.append(s);

//        this.toAppend = s;
//        worker.execute();

        //pane.setText(pane.getText() + s);
        //pane.setCaretPosition(position + s.length());
    }
    


}
