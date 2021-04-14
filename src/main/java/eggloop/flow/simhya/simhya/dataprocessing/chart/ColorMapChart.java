package eggloop.flow.simhya.simhya.dataprocessing.chart;///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package eggloop.flow.simhya.simhya.dataprocessing.chart;
//
///**
// *
// * @author Luca
// */
//
//
//
//import java.awt.Color;
//import java.awt.event.ActionEvent;
//import org.math.plot.canvas.*;
//import eggloop.flow.simhya.simhya.utils.ColorManager;
//import eggloop.flow.simhya.simhya.GlobalOptions;
//
//import javax.swing.JOptionPane;
//import javax.swing.JMenuItem;
//import javax.swing.JFileChooser;
//import javax.swing.filechooser.FileFilter;
//import java.io.File;
//import java.security.AccessControlException;
//import java.awt.Font;
//
//import org.math.plot.*;
//import org.math.plot.plots.*;
//import org.math.plot.plotObjects.ColorMap;
//
//
//
///**
// *
// * @author Luca
// */
//public class ColorMapChart extends Chart {
//
//    Plot2DPanel chart;
//    private JMenuItem rotate;
//    private JMenuItem zoom;
//    private JMenuItem translate;
//    private JFileChooser pngFileChooser;
//    private boolean denySaveSecurity = false;
//
//    int labelFontSize = 20;
//    int thickFontSize = 15;
//
//    ColorMap cmap;
//
//
//    public ColorMapChart(Plot2DSurface traj,String xlabel, String ylabel) {
//        super(ChartType.SURFACE);
//        cmap = ColorMap.getRedColorMap();
//        init(traj,xlabel,ylabel);
//    }
//
//    public ColorMapChart(Plot2DSurface traj,String xlabel, String ylabel, ColorMap map) {
//        super(ChartType.SURFACE);
//        cmap = map;
//        init(traj,xlabel,ylabel);
//    }
//
//
//    private void init(Plot2DSurface traj,String xlabel, String ylabel) {
//        Font f = new Font(Font.SANS_SERIF, Font.PLAIN, labelFontSize);
//        Font f1 = new Font(Font.SANS_SERIF, Font.PLAIN, thickFontSize);
//
//        chart = new Plot2DPanel();
//        chart.setFont(f);
//
//        chart.addColorMapPlot(traj.name, cmap, traj.x, traj.y, traj.z);
//        chart.addColorMapLegend(Plot2DPanel.EAST);
//
//        chart.getAxis(0).setLabelText(xlabel);
//        chart.getAxis(0).setColor(Color.black);
//        chart.getAxis(0).setLabelFont(f);
//        chart.getAxis(0).setLightLabelFont(f1);
//        chart.getAxis(0).setLabelPosition(1.1,-0.1);
//
//
//        chart.getAxis(1).setLabelText(ylabel);
//        chart.getAxis(1).setColor(Color.black);
//        chart.getAxis(1).setLabelFont(f);
//        chart.getAxis(1).setLightLabelFont(f1);
//        chart.getAxis(1).setLabelPosition(-0.1,1.1);
//
//        //very ugly code to fix axes bounds... Could not find a better way!
//        Plot p = chart.plotCanvas.getPlot(0);
//        if(p instanceof ColorMapPlot) {
//            ColorMapPlot p1 = (ColorMapPlot)p;
//            double[] x = p1.getXBounds();
//            double xoff = 0.1*(x[1]-x[0]);
//            double[] y = p1.getYBounds();
//            double yoff = 0.1*(y[1]-y[0]);
//            //System.out.println("Precision units " + java.util.Arrays.toString(chart.plotCanvas.base.getPrecisionUnit()));
//            chart.plotCanvas.base.setFixedPrecision(new double[] {xoff, yoff});
//        }
//
//        chart.setSize(GlobalOptions.ChartFileWidth, GlobalOptions.ChartFileHeight);
//        chart.removePlotToolBar();
//        chart.addPopupMenu();
//        this.panel = chart;
//    }
//
//
//
//    @Override
//    public void saveToFile(String filename, PlotFileType type) {
//        try {
//            switch(type) {
//                case PNG:
//                    chart.toPNGGraphicFile(new File(filename));
//                    break;
//                case JPG:
//                    chart.toJPGGraphicFile(new File(filename));
//                    break;
//                case PDF:
//                    throw new UnsupportedOperationException("File type not supported "
//                            + "by this kind of plot.\n Try PNG or JPG");
//                case TEX:
//                    throw new UnsupportedOperationException("File type not supported "
//                            + "by this kind of plot.\n Try PNG or JPG");
//                default:
//                    throw new PlotException("Unknown file type.");
//            }
//        } catch(java.io.IOException e) {
//            throw new PlotException("Failed to open file " + filename);
//        }
//
//    }
//
//    @Override
//    public void setTitle(String title) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public void setXLabel(String label) {
//        chart.getAxis(0).setLabelText(label);
//    }
//
//    @Override
//    public void setYLabel(String label) {
//        chart.getAxis(1).setLabelText(label);
//    }
//
//    @Override
//    public void setZLabel(String label) {
//
//    }
//
//
//    /*
//     * TO DO:
//     * ingrandire legenda
//     * ingrandire ticks su asse
//     * capire se si riesce a plottare superficie con colormap
//     * cloud?
//     */
//
//
//}
