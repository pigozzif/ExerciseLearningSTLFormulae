/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.dataprocessing.chart;

/**
 *
 * @author Luca
 */
public enum ChartType {
    LINE, LINE3D, SURFACE, COLORMAP, HISTOGRAM, HISTOGRAM3D, SCATTER, SCATTER3D;

    @Override
    public String toString() {
        switch(this) {
            case LINE:
                return "line";
            case LINE3D:
                return "3D line";
            case SURFACE:
                return "surface";
            case COLORMAP:
                return "colormap";
            case HISTOGRAM:
                return "histogram";
            case HISTOGRAM3D:
                return "3D histogram";
            case SCATTER:
                return "scatter plot";
            case SCATTER3D:
                return "3D scatter plot";
            default:
                return "";
        }
    }

    public int getDimensions() {
        switch(this) {
            case LINE:
                return 2;
            case LINE3D:
                return 3;
            case SURFACE:
                return 3;
            case COLORMAP:
                return 2;
            case HISTOGRAM:
                return 2;
            case HISTOGRAM3D:
                return 3;
            case SCATTER:
                return 2;
            case SCATTER3D:
                return 3;
            default:
                return 0;
        }
    }
}
