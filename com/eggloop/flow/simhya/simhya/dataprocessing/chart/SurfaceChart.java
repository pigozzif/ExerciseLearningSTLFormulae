/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.dataprocessing.chart;

import java.awt.Color;
import com.eggloop.flow.simhya.simhya.utils.ColorManager;
import com.eggloop.flow.simhya.simhya.GlobalOptions;
import java.awt.Font;
import org.math.plot.*;



/**
 *
 * @author Luca
 */
public class SurfaceChart extends Chart {
    Plot3DPanel chart;
 
    int labelFontSize = 20;
    int thickFontSize = 15;


    public SurfaceChart(Plot2DSurface traj,String xlabel, String ylabel) {
        super(ChartType.SURFACE);
        Font f = new Font(Font.SANS_SERIF, Font.PLAIN, labelFontSize);
        Font f1 = new Font(Font.SANS_SERIF, Font.PLAIN, thickFontSize);

        chart = new Plot3DPanel();
        chart.setFont(f);

        /*
         * WARNING:
         * for some weird reason, I has to invert x and y axis, both in the plot constructor
         * and in the labels.
         * Maybe using a different library, we can avoid this?
         */
        chart.addLegend(Plot3DPanel.EAST);
        chart.addGridPlot(traj.name, ColorManager.getColor(0), traj.y, traj.x, traj.z);
        chart.getAxis(0).setLabelText(ylabel);
        chart.getAxis(0).setColor(Color.black);
        chart.getAxis(0).setLabelFont(f);
        chart.getAxis(0).setLightLabelFont(f1);
        chart.getAxis(0).setLabelPosition(1.1,-0.1,0);


        chart.getAxis(1).setLabelText(xlabel);
        chart.getAxis(1).setColor(Color.black);
        chart.getAxis(1).setLabelFont(f);
        chart.getAxis(1).setLightLabelFont(f1);
        chart.getAxis(1).setLabelPosition(-0.1,1.1,0);

        chart.getAxis(2).setLabelText(traj.name);
        chart.getAxis(2).setColor(Color.black);
        chart.getAxis(2).setLabelFont(f);
        chart.getAxis(2).setLightLabelFont(f1);
        chart.getAxis(2).setLabelPosition(0,0,1.15);

        chart.setSize(GlobalOptions.ChartFileWidth, GlobalOptions.ChartFileHeight);
        chart.removePlotToolBar();
//        chart.addPopupMenu();
        this.panel = chart;
        //installPopupMenu();
    }

   
    
    @Override
    public void saveToFile(String filename, PlotFileType type) {
        switch(type) {
            case PNG:
//                    chart.toPNGGraphicFile(new java.io.File(filename));
                break;
            case JPG:
//                    chart.toJPGGraphicFile(new java.io.File(filename));
                break;
            case PDF:
                throw new UnsupportedOperationException("File type not supported "
                        + "by this kind of plot.\n Try PNG or JPG");
            case TEX:
                throw new UnsupportedOperationException("File type not supported "
                        + "by this kind of plot.\n Try PNG or JPG");
            default:
                throw new PlotException("Unknown file type.");
        }

    }

    @Override
    public void setTitle(String title) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setXLabel(String label) {
        chart.getAxis(0).setLabelText(label);
    }

    @Override
    public void setYLabel(String label) {
        chart.getAxis(1).setLabelText(label);
    }

    @Override
    public void setZLabel(String label) {
        chart.getAxis(2).setLabelText(label);
    }


    /*
     * TO DO:
     * ingrandire legenda
     * ingrandire ticks su asse
     * capire se si riesce a plottare superficie con colormap
     * cloud?
     */

    
    
}
