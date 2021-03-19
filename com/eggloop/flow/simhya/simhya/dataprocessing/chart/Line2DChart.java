/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.dataprocessing.chart;
import java.awt.Color;
import com.eggloop.flow.simhya.simhya.utils.ColorManager;
import com.eggloop.flow.simhya.simhya.GlobalOptions;
import java.awt.Font;
import java.io.IOException;
import org.math.plot.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Luca
 */
public class Line2DChart extends Chart  {
    Plot2DPanel chart;
    ArrayList<String> varNames;
    HashMap<String,Integer> colorCode;
    int currentColor = 0;

    int labelFontSize = 20;
    int thickFontSize = 15;




    public Line2DChart(ArrayList<Plot2DTrajectory> trajectories, String xlabel, String ylabel) {
        super(ChartType.LINE);
        initialise(trajectories,xlabel,ylabel);
    }


    public Line2DChart(Plot2DTrajectory trajectory, String xlabel, String ylabel) {
        super(ChartType.LINE);
        ArrayList<Plot2DTrajectory> list = new ArrayList<Plot2DTrajectory>();
        list.add(trajectory);
        initialise(list,xlabel,ylabel);
    }


    private void initialise(ArrayList<Plot2DTrajectory> trajectories, String xlabel, String ylabel) {
        this.varNames = new ArrayList<String>();
        this.colorCode = new HashMap<String,Integer>();
        Font f = new Font(Font.SANS_SERIF, Font.PLAIN, labelFontSize);
        Font f1 = new Font(Font.SANS_SERIF, Font.PLAIN, thickFontSize);

        chart = new Plot2DPanel();
        chart.setFont(f);

        for (Plot2DTrajectory traj : trajectories) {
            int color;
            if (!varNames.contains(traj.name)) {
                varNames.add(traj.name);
                color = currentColor++;
                colorCode.put(traj.name, color);
            } else
                color = this.colorCode.get(traj.name);
            chart.addLinePlot(traj.name, ColorManager.getColor(color), traj.x, traj.y);
        }

        chart.addLegend(Plot2DPanel.EAST);

        chart.getAxis(0).setLabelText(xlabel);
        chart.getAxis(0).setColor(Color.black);
        chart.getAxis(0).setLabelFont(f);
        chart.getAxis(0).setLightLabelFont(f1);
        chart.getAxis(0).setLabelPosition(0.5,-0.1,0);


        chart.getAxis(1).setLabelText(ylabel);
        chart.getAxis(1).setColor(Color.black);
        chart.getAxis(1).setLabelFont(f);
        chart.getAxis(1).setLightLabelFont(f1);
        chart.getAxis(1).setLabelPosition(-0.13,0.5,0);
        chart.getAxis(1).setLabelAngle(-Math.PI/2);

        chart.setSize(GlobalOptions.ChartFileWidth, GlobalOptions.ChartFileHeight);
        chart.removePlotToolBar();
//        chart.addPopupMenu();
        this.panel = chart;
    }


   public void removeLegend() {
       chart.removeLegend();
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

    }
}
