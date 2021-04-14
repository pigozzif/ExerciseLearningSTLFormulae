/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.utils;
import java.awt.Color;

/**
 *
 * @author Luca
 */
public class ColorManager {

    private static final int setNumber = 2;
    private static final int setSize = 45;
    private static float[][] depths;
    private static float[][] shades;


    static {
        depths = new float[setNumber][];
        depths[0] = new float[3];
        depths[0][0] = 1.0f; depths[0][1] = 0.33f; depths[0][2] = 0.66f;
        depths[1] = new float[3];
        depths[1][0] = 0.75f; depths[1][1] = 0.5f; depths[1][2] = 0.25f;

        shades = new float[7][];
        shades[0] = new float[3];
        shades[0][0] = 1.0f; shades[0][1] = 0.0f; shades[0][2] = 0.0f;
        shades[1] = new float[3];
        shades[1][0] = 0.0f; shades[1][1] = 1.0f; shades[1][2] = 0.0f;
        shades[2] = new float[3];
        shades[2][0] = 0.0f; shades[2][1] = 0.0f; shades[2][2] = 1.0f;
        shades[3] = new float[3];
        shades[3][0] = 1.0f; shades[3][1] = 1.0f; shades[3][2] = 0.0f;
        shades[4] = new float[3];
        shades[4][0] = 1.0f; shades[4][1] = 0.0f; shades[4][2] = 1.0f;
        shades[5] = new float[3];
        shades[5][0] = 0.0f; shades[5][1] = 1.0f; shades[5][2] = 1.0f;
        shades[6] = new float[3];
        shades[6][0] = 1.0f; shades[6][1] = 1.0f; shades[6][2] = 1.0f;
    }


    public static Color getColor(int id) {
        int depth;
        int shade;
        int depth0,depth1,depth2;
        float r,g,b;
        int k;

        depth = id/setSize;
        if (depth >= setNumber)
            return randomColor();
        k = id % setSize;
        if (0 <= k && k <= 6) {
            shade = k - 0;
            depth0 = depth1 = depth2 = 0;
        }
        else if (7 <= k && k <= 13) {
            shade = k - 7;
            depth0 = depth1 = depth2 = 1;
        }
        else if (14 <= k && k <= 20) {
            shade = k - 14;
            depth0 = depth1 = depth2 = 2;
        }
        else if (21 <= k && k <= 24) {
            shade = 3 + k - 21;
            depth0 = 0;
            depth1 = 1;
            depth2 = 2;
        }
        else if (25 <= k && k <= 28) {
            shade = 3 + k - 25;
            depth0 = 0;
            depth1 = 2;
            depth2 = 1;
        }
        else if (29 <= k && k <= 32) {
            shade = 3 + k - 29;
            depth0 = 1;
            depth1 = 0;
            depth2 = 2;
        }
        else if (33 <= k && k <= 36) {
            shade = 3 + k - 33;
            depth0 = 1;
            depth1 = 2;
            depth2 = 0;
        }
        else if (37 <= k && k <= 40) {
            shade = 3 + k - 37;
            depth0 = 2;
            depth1 = 0;
            depth2 = 1;
        }
        else if (41 <= k && k <= 44) {
            shade = 3 + k - 41;
            depth0 = 2;
            depth1 = 1;
            depth2 = 0;
        }
        else {
            throw new RuntimeException("If we are here something very weird should have happened...");
        }
        r = depths[depth][depth0] * shades[shade][0];
        g = depths[depth][depth1] * shades[shade][1];
        b = depths[depth][depth2] * shades[shade][2];
        //correct the white bug
        if (r == 1.0f && g == 1.0f && b == 1.0f )
            return new Color(0.0f,0.0f,0.0f);
        return new Color(r,g,b);
    }


    private static Color randomColor() {
        float r = (float)RandomGenerator.nextDouble();
        float g = (float)RandomGenerator.nextDouble();
        float b = (float)RandomGenerator.nextDouble();
        return new Color(r,g,b);
    }

}
