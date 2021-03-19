/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.utils;
import java.util.Arrays;
import java.util.ArrayList;

/**
 *
 * @author luca
 */
public class ArrayUtils {


    /**
     * Returns true if the generic array contains the given element,
     * uses equals method.
     * @param <T> 
     * @param array
     * @param element
     * @return
     */
    public static <T> boolean contains(T[] array, T element) {
        for (int i=0;i<array.length;i++)
            if (array[i].equals(element))
                return true;
        return false;
    }

    public static boolean contains(int[] array, int element) {
        for (int i=0;i<array.length;i++)
            if (array[i] == element)
                return true;
        return false;
    }

    public static int[] toArray(ArrayList<Integer> list) {
        int [] array = new int[list.size()];
        int j=0;
        for (Integer i : list)
            array[j++] = i;
        return array;
    }

}
