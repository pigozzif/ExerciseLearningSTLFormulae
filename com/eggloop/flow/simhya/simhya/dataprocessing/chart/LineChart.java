/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.dataprocessing.chart;

import java.util.ArrayList;
import java.util.HashMap;
import umontreal.iro.lecuyer.charts.*;
import java.awt.*;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemSource;
import com.eggloop.flow.simhya.simhya.utils.ColorManager;
import com.eggloop.flow.simhya.simhya.GlobalOptions;
import org.jfree.chart.ChartPanel;

/**
 *
 * @author Luca
 */
public class LineChart extends Chart {
    ArrayList<String> varNames;
    HashMap<String,Integer> colorCode;
    int fontSize = 20;
    int legendSize = 25;
    public boolean fullLatexDocument = false;
    XYLineChart chart  = null;
    int currentColor = 0;
    boolean legendAtBottom = true;


    /**
     * Initializes an empty LineChart
     */
    public LineChart() {
        super(ChartType.LINE);
        chart = new XYLineChart();
        this.varNames = new ArrayList<String>();
        this.colorCode = new HashMap<String,Integer>();
        this.setTitle("");
        this.panel = new ChartPanel(chart.getJFreeChart());
    }
    
    public LineChart(Plot2DTrajectory trajectory, String XLabel, String YLabel) {
        super(ChartType.LINE);
        ArrayList<Plot2DTrajectory> trajectories = new ArrayList<Plot2DTrajectory>();
        trajectories.add(trajectory);
        init(trajectories,XLabel,YLabel);
    }
    

    /**
     * Generates a line chart from a list of 2D trajectories.
     * @param trajectories a list of {@link Plot2DTrajectory}
     * @param XLabel
     * @param YLabel
     */
    public LineChart(ArrayList<Plot2DTrajectory> trajectories, String XLabel, String YLabel) {
        super(ChartType.LINE);
        init(trajectories,XLabel,YLabel);
    }
    
    
    private void init(ArrayList<Plot2DTrajectory> trajectories, String XLabel, String YLabel) {
        chart = new XYLineChart();
        this.varNames = new ArrayList<String>();
        this.colorCode = new HashMap<String,Integer>();
        for (Plot2DTrajectory traj : trajectories) {
            int color;
            if (!varNames.contains(traj.name)) {
                varNames.add(traj.name);
                color = currentColor++;
                colorCode.put(traj.name, color);
            } else
                color = this.colorCode.get(traj.name); 
            int k = chart.add(traj.x, traj.y);
            chart.getSeriesCollection().setName(k, traj.name);
            chart.getSeriesCollection().setColor(k, ColorManager.getColor(color));
        }
        this.setTitle("");
        this.setXLabel(XLabel);
        this.setYLabel(YLabel);
        this.setChartAspect();
        this.setLegend();
        if (trajectories.size() == this.varNames.size())
            setLineAspect(2.0f, false);
        else
            setLineAspect(1.0f, false);
        this.panel = new ChartPanel(chart.getJFreeChart());
    }


    public void addTrajectories(ArrayList<Plot2DTrajectory> trajectories) {
        for (Plot2DTrajectory traj : trajectories) {
            int color;
            if (!varNames.contains(traj.name)) {
                varNames.add(traj.name);
                color = currentColor++;
                colorCode.put(traj.name, color);
            } else
                color = this.colorCode.get(traj.name);
            int k = chart.add(traj.x, traj.y);
            chart.getSeriesCollection().setName(k, traj.name);
            chart.getSeriesCollection().setColor(k, ColorManager.getColor(color));
        }
        this.setChartAspect();
        this.setLegend();
        if (trajectories.size() == this.varNames.size())
            setLineAspect(2.0f, false);
        else
            setLineAspect(1.0f, false);
    }


   

    private void setChartAspect() {
        Font f = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize);

        chart.getJFreeChart().setPadding(new RectangleInsets(10,10,10,10));
        chart.getJFreeChart().getXYPlot().getDomainAxis().setAutoTickUnitSelection(true);
        chart.getJFreeChart().getXYPlot().getRangeAxis().setAutoTickUnitSelection(true);
        chart.getJFreeChart().setBackgroundPaint(Color.white);
        
        chart.getJFreeChart().getXYPlot().getDomainAxis().setLabelFont(f);
        chart.getJFreeChart().getXYPlot().getDomainAxis().setTickLabelFont(f);
        chart.getJFreeChart().getXYPlot().getRangeAxis().setLabelFont(f);
        chart.getJFreeChart().getXYPlot().getRangeAxis().setTickLabelFont(f);
    }

    public final void setLegend() {
        Font f = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize);
        LegendTitle legend = new LegendTitle(new XYPlotLegend(varNames));
        if (this.legendAtBottom)
            legend.setPosition(RectangleEdge.BOTTOM);
        else
            legend.setPosition(RectangleEdge.RIGHT);
        legend.setItemFont(f);
        chart.getJFreeChart().removeLegend();
        chart.getJFreeChart().addLegend(legend);
    }
    
    
    public void removeLegend() {
        chart.getJFreeChart().removeLegend();
    }
    
    public final void setLineAspect(float lineWidth, boolean useDashAndSymbols){
        Stroke s = new BasicStroke(lineWidth);
        if (lineWidth != 1.0)
            for (int i=0;i<chart.getJFreeChart().getXYPlot().getDatasetCount();i++)
                chart.getJFreeChart().getXYPlot().getRenderer(i).setBaseStroke(s);
        //TO DO
        //DASHED AND SIMBOLS LINES!!!
    }


    @Override
    public void saveToFile(String filename, PlotFileType type) {
        try {
            switch(type)  {
                case PDF:
                    PDFChartExport.saveChartAsPDF(filename, chart.getJFreeChart(), GlobalOptions.XYchartFileWidth, GlobalOptions.XYchartFileHeight);
                    break;
                case TEX:
                    chart.setLatexDocFlag(fullLatexDocument);
                    chart.toLatexFile(filename, GlobalOptions.XYchartFileWidth, GlobalOptions.XYchartFileHeight);
                    break;
                case PNG:
                    ChartUtilities.saveChartAsPNG(new java.io.File(filename), chart.getJFreeChart(), GlobalOptions.XYchartFileWidth, GlobalOptions.XYchartFileHeight);
                    break;
                case JPG:
                    ChartUtilities.saveChartAsJPEG(new java.io.File(filename), chart.getJFreeChart(), GlobalOptions.XYchartFileWidth, GlobalOptions.XYchartFileHeight);
                    break;
            }
        }
        catch(java.io.IOException e) { throw new PlotException("Cannot save chart to file " + filename); }
    }

    @Override
    public void setTitle(String title) {
        chart.setTitle(title);

    }

    @Override
    public void setXLabel(String label) {
        chart.getXAxis().setLabel(label);
    }

    @Override
    public void setYLabel(String label) {
        chart.getYAxis().setLabel(label);
    }

    @Override
    public void setZLabel(String label) {
        throw new UnsupportedOperationException("This chart has 2 dimensions");
    }



    private class XYPlotLegend implements LegendItemSource {
        private LegendItemCollection legend;
        private Dimension dim = new Dimension(legendSize,4);

        private XYPlotLegend(ArrayList<String> names) {
            legend = new LegendItemCollection();
            for (int j=0;j<names.size();j++) {
                String n = names.get(j);
                LegendItem item = new org.jfree.chart.LegendItem(" " + n, "", "", "",
                            new Rectangle(dim), new BasicStroke(5), ColorManager.getColor(j));
                legend.add(item);
            }

        }

        public LegendItemCollection getLegendItems() {
            return legend;
        }
    }

}
