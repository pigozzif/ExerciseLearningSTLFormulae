/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.dataprocessing;
import java.util.Locale;

/**
 *
 * @author luca
 */
class EventInfo {
    public static final int INSTANTANEOUS = 0;
    public static final int TIMED = 1;
    public static final int STOCHASTIC = 2;

    public String name;
    public double time;
    public int type;


    public EventInfo(String name, double time, int type) {
        this.name = name;
        this.time = time;
        this.type = type;
    }

    public String toString(int id) {
        String s = id + "\t\t" +  String.format(Locale.UK, "%.5f", time);
        s += "\t\t" + name + "\t\t";
        switch(type) {
            case INSTANTANEOUS:
                s += "I";
                break;
            case TIMED:
                s += "T";
                break;
            case STOCHASTIC:
                s+="S";
        }
        return s;
    }



}
