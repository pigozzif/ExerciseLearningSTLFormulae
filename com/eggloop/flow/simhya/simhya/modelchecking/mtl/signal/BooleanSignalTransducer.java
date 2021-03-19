/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.modelchecking.mtl.signal;

import com.eggloop.flow.simhya.simhya.modelchecking.mtl.Truth;

/**
 * @author luca
 */
public class BooleanSignalTransducer {


    public static BooleanSignal and(BooleanSignal s1, BooleanSignal s2) {
        if (s1.undefined || s2.undefined) {
            BooleanSignal s0 = new BooleanSignal(0, 0);
            s0.finalise();
            return s0;
        }
        double t0 = Math.max(s1.initial_time, s2.initial_time);
        double tf = Math.min(s1.final_time, s2.final_time);
        BooleanSignal s0 = new BooleanSignal(t0, tf);


        TimeInterval I1, I2;
        I1 = s1.getNextPositive();
        I2 = s2.getNextPositive();

        while (I1 != null && I2 != null) {
            TimeInterval I = I1.intersection(I2);
            if (I != null)// && !I.isPoint())
                s0.addNextPositive(I.lower, I.upper, I.lowerClosed, I.upperClosed);
            if (I1.upper < I2.upper)
                I1 = s1.getNextPositive();
            else
                I2 = s2.getNextPositive();
        }
        if (s0.signal.isEmpty())
            s0.addNext(tf, true, false);
        s0.finalise();
        s1.reset();
        s2.reset();
        return s0;
    }

    public static BooleanSignal or(BooleanSignal s1, BooleanSignal s2) {
        if (s1.undefined || s2.undefined) {
            BooleanSignal s0 = new BooleanSignal(0, 0);
            s0.finalise();
            return s0;
        }
        s1.quickComplement();
        s2.quickComplement();
        BooleanSignal s0 = and(s1, s2);
        s0.complement();
        s1.quickComplement();
        s2.quickComplement();
        return s0;
    }

    public static BooleanSignal imply(BooleanSignal s1, BooleanSignal s2) {
        if (s1.undefined || s2.undefined) {
            BooleanSignal s0 = new BooleanSignal(0, 0);
            s0.finalise();
            return s0;
        }
        s2.quickComplement();
        BooleanSignal s0 = and(s1, s2);
        s0.complement();
        s2.quickComplement();
        return s0;
    }

    public static BooleanSignal not(BooleanSignal s) {
        if (s.undefined) {
            BooleanSignal s0 = new BooleanSignal(0, 0);
            s0.finalise();
            return s0;
        }
        BooleanSignal s0 = s.copy();
        s0.complement();
        return s0;
    }

    public static BooleanSignal until(BooleanSignal s1, BooleanSignal s2, double t1, double t2) {
        /*
         * WARNING: this implementation has to be checked.
         */

        if (s1.undefined || s2.undefined) {
            BooleanSignal s0 = new BooleanSignal(0, 0);
            s0.finalise();
            return s0;
        }
        double t0 = Math.max(s1.initial_time, s2.initial_time);
        double tf = Math.min(s1.final_time, s2.final_time);
        BooleanSignal s0 = new BooleanSignal(t0, tf);

        TimeInterval I1, I2, I0, I;
        I1 = s1.getNextPositive();
        I2 = s2.getNextPositive();


        while (I1 != null && I2 != null) {
            if (I1.intersects(I2)) {   //(I1.intersectsNonemptyInterior(I2)) {
                I0 = I1.intersection(I2);
                I0 = I0.backshift(t1, t2);
                I = I0.intersection(I1);
                if (I != null)// && !I.isPoint())
                    s0.addNextPositive(I.lower, I.upper, I.lowerClosed, I.upperClosed);
            }
            if (I1.upper < I2.upper)
                I1 = s1.getNextPositive();
            else
                I2 = s2.getNextPositive();
        }
        //try to extend the final false region as much as possible
        double tt;
        if (s0.signal.isEmpty()) {
            tt = Math.max(tf - t2, t0);
        } else {
            double t = s0.getLastSignalEndTime();
            tt = Math.max(t, tf - t2);
        }
        s1.reset();
        I1 = s1.getNextPositive();
        if (I1 == null)
            tt = s1.final_time;
        while (I1 != null) {
            if (I1.contains(tt))
                if (I1.upper - tt < t1)
                    tt = I1.upper;
                else
                    break;
            else if (I1.isOnTheRightOf(tt)) {
                if (I1.upper - I1.lower < t1)
                    tt = I1.upper;
                else {
                    tt = I1.lower;
                    break;
                }
            }
            I1 = s1.getNextPositive();
        }
        if (tt > t0) {
            s0.final_time = tt;
            s0.addNext(tt, true, false);
        }
        s0.finalise();
        s1.reset();
        s2.reset();
        return s0;
    }


    public static Truth untilZero(BooleanSignal s1, BooleanSignal s2, double t1, double t2) {
        if (s1.undefined || s2.undefined)
            return Truth.UNDEFINED;

        double t0 = Math.max(s1.initial_time, s2.initial_time);
        TimeInterval I1, I2, I0;
        I1 = s1.getNextPositive();
        I2 = s2.getNextPositive();

        if (I1 == null || I1.lower > t0 || (I1.upper < s1.final_time && I1.upper < t0 + t1)) {
            s1.reset();
            s2.reset();
            return Truth.FALSE;
        }
        while (I2 != null) {
            I0 = I1.intersection(I2);
            if (I0 != null)
                I0 = I0.backshift(t1, t2);
            else
                break;
            if (I0 != null && I0.contains(t0)) {
                s1.reset();
                s2.reset();
                return Truth.TRUE;
            }
        }
        s1.reset();
        s2.reset();
        if (I1.upper < s1.final_time || s1.final_time >= t0 + t2)
            return Truth.FALSE;
        else
            return Truth.UNDEFINED;
    }


    public static BooleanSignal always(BooleanSignal s, double t1, double t2) {
        if (s.undefined) {
            BooleanSignal s0 = new BooleanSignal(0, 0);
            s0.finalise();
            return s0;
        }
        s.quickComplement();
        BooleanSignal s0 = eventually(s, t1, t2);
        s0.complement();
        s.quickComplement();
        return s0;
    }

    public static Truth alwaysZero(BooleanSignal s, double t1, double t2) {
        if (s.undefined) {
            return Truth.UNDEFINED;
        }
        s.quickComplement();
        Truth T = eventuallyZero(s, t1, t2);
        T = Truth.not(T);
        s.quickComplement();
        return T;
    }

    public static BooleanSignal eventually(BooleanSignal s, double t1, double t2) {
        if (s.undefined) {
            BooleanSignal s0 = new BooleanSignal(0, 0);
            s0.finalise();
            return s0;
        }
        double t0 = s.initial_time;
        double tf = s.final_time;
        BooleanSignal s0 = new BooleanSignal(t0, tf);

        TimeInterval I1;
        I1 = s.getNextPositive();
        while (I1 != null) {
            TimeInterval I = I1.backshift(t1, t2);
            if (I != null)  // && !I.isPoint())
                s0.addNextPositive(I.lower, I.upper, I.lowerClosed, I.upperClosed);
            I1 = s.getNextPositive();
        }
        double t = s0.getLastSignalEndTime();
        tf = Math.max(t, s.final_time - t2);
        if (s0.signal.isEmpty() && tf > t)
            s0.addNext(tf, true, false);
        s0.final_time = tf;
        s0.finalise();
        s.reset();
        return s0;
    }


    public static Truth eventuallyZero(BooleanSignal s, double t1, double t2) {
        if (s.undefined)
            return Truth.UNDEFINED;
        double t0 = s.initial_time;
        TimeInterval I1, I0 = new TimeInterval(t0 + t1, t0 + t2, true, true, true);
        I1 = s.getNextPositive();
        while (I1 != null) {
            if (I1.intersects(I0)) {
                s.reset();
                return Truth.TRUE;
            } else if (I0.upper < I1.lower) {
                s.reset();
                return Truth.FALSE;
            }
            I1 = s.getNextPositive();
        }
        s.reset();
        if (s.final_time >= t0 + t2)
            return Truth.FALSE;
        else
            return Truth.UNDEFINED;
    }


    public static BooleanSignal convertPWLinearSignalToBoolean(double[] time, double[] signal, boolean zeroIsTrue) {
        if (time == null || signal == null || time.length < 2 || signal.length < 2 || time.length != signal.length)
            throw new RuntimeException("Error in the input of the PWL signal.");
//        int n = 1;
//        while (n < time.length && time[n] > 0.0)
//            n++;
        int n = time.length;
        BooleanSignal s = new BooleanSignal(time[0], time[n - 1]);
        boolean current;
        int j;
        double t0 = time[0];
        if (signal[0] < 0)
            current = false;
        else if (signal[0] > 0)
            current = true;
        else {
            if (signal[1] < 0)
                current = false;
            else if (signal[1] > 0)
                current = true;
            else
                current = zeroIsTrue;
        }
        j = 1;
        while (j < n) {
            if ((current && (signal[j] < 0 || (!zeroIsTrue && signal[j] == 0))) ||
                    (!current && (signal[j] > 0 || (zeroIsTrue && signal[j] == 0)))) {
                double t = zeroCrossing(time[j - 1], time[j], signal[j - 1], signal[j]);
                s.addNext(t, !(current ^ zeroIsTrue), current);
                current = !current;
                t0 = t;
            }
            j++;
        }
        if (s.signal.isEmpty()) {
            //the signal has never changed truth value, hence it is always true or always false
            s.addNext(time[n - 1], true, current);
        }
        s.finalise();
        return s;
    }

    public static BooleanSignal convertPWConstantSignalToBoolean(double[] time, double[] signal, boolean zeroIsTrue) {
        if (time == null || signal == null || time.length < 2 || signal.length < 2 || time.length != signal.length)
            throw new RuntimeException("Error in the input of the PWL signal.");
        //find effective length of signal.
//        int n = 1;
//        while (n < time.length && time[n] > 0.0)
//            n++;
        int n = time.length;
        BooleanSignal s = new BooleanSignal(time[0], time[n - 1]);
        boolean current;
        int j;
        double t0 = time[0];
        if (signal[0] < 0)
            current = false;
        else if (signal[0] > 0)
            current = true;
        else
            current = zeroIsTrue;
        j = 1;
        while (j < n - 1) { //in n-1 we have the final time, and signal[n-1] == signal[n-1].
            if ((current && (signal[j] < 0 || (!zeroIsTrue && signal[j] == 0))) ||
                    (!current && (signal[j] > 0 || (zeroIsTrue && signal[j] == 0)))) {
                s.addNext(time[j], false, current);
                current = !current;
                t0 = time[j];
            }
            j++;
        }
        if (s.signal.isEmpty()) {
            //the signal has never changed truth value, hence it is always true or always false
            s.addNext(time[n - 1], true, current);
        }
        s.finalise();
        return s;
    }


    private static double zeroCrossing(double t1, double t2, double v1, double v2) {
        if (t1 >= t2)
            throw new RuntimeException("Times are in inverted order");
        if (v1 == v2) //default
            return t2;
        double t = t1 + (t2 - t1) / (v2 - v1) * v1;
        return t;
    }


}
