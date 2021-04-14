/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.dataprocessing;

import eggloop.flow.simhya.simhya.dataprocessing.chart.PDFChartExport;
import eggloop.flow.simhya.simhya.dataprocessing.chart.PlotFileType;
import umontreal.iro.lecuyer.charts.*;
import java.util.ArrayList;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

import java.awt.*;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemSource;
import java.util.ArrayList;
import eggloop.flow.simhya.simhya.utils.ColorManager;
import org.jfree.data.statistics.HistogramBin;
import org.jfree.data.Range;

import eggloop.flow.simhya.simhya.GlobalOptions;

/**
 *
 * @author Luca
 */
public class DistributionPlotter {
    ArrayList<String> varNames;
    int fontSize = 20;
    int legendSize = 25;
    
    DistributionPlotter(ArrayList<String> varNames) {
        this.varNames = varNames;
    }

    HistogramChart generateChart(ArrayList<DoubleArrayList> data) {
        CustomHistogramDataset dataset = new CustomHistogramDataset();
        dataset.setType(GlobalOptions.histogramType);
        if (data.size() != this.varNames.size())
            throw new DataException("Number of datasets dimension mismatch");
        for (int i=0;i<data.size();i++) {
            DoubleArrayList x = data.get(i).copy();
            x.quickSort();
            
            double q1 = Descriptive.quantile(x, 0.25);
            double q2 = Descriptive.quantile(x, 0.75);
            double step = (q2-q1)/(x.size()/2/GlobalOptions.averageBinSizeInInterquantileRange);
            if (step == 0.0) {
                step = (x.getQuick(x.size()-1) - x.getQuick(0))/(x.size()/GlobalOptions.averageBinSizeInInterquantileRange);
            }
            int binNumber;
            if (step == 0.0)
                binNumber = 1;
            else
                binNumber = (int)Math.ceil((x.getQuick(x.size()-1) - x.getQuick(0))/step);

             
            x.trimToSize();
            dataset.addSeries(this.varNames.get(i), x.elements(), binNumber);
        }

        HistogramChart chart = new HistogramChart("","value",GlobalOptions.histogramType.toString(),dataset);
        //control if there is a deterministic distribution
        
        double minB = Double.POSITIVE_INFINITY;
        for (int i=0;i<data.size();i++) {
            java.util.List l = chart.getSeriesCollection().getBins(i);
            HistogramBin b = ((HistogramBin)l.get(0));
            if (b.getBinWidth() > 0.0 && b.getBinWidth() < minB)
                minB = b.getBinWidth();
        }
        if (minB == Double.POSITIVE_INFINITY) minB = 0.001;
        for (int i=0;i<data.size();i++) {
            java.util.List l = chart.getSeriesCollection().getBins(i);
            HistogramBin b = ((HistogramBin)l.get(0));
            if (l.size() == 1 && b.getBinWidth() == 0.0) {
                b = new HistogramBin(b.getStartBoundary(),b.getStartBoundary() + minB);
                //                for (int k=0;k<data.get(i).size();k++)
//                    b.incrementCount();
//                l.set(0, b);
                HistogramBin[] bs = new HistogramBin[1];
                bs[0] = b;
                chart.getSeriesCollection().getSeriesCollection().setBins(i,bs);
            }
        }

        setHistChartAttributes(chart,data.size());
        return chart;
    }

    HistogramChart generateChart(ArrayList<DoubleArrayList> data, int bins) {
        CustomHistogramDataset dataset = new CustomHistogramDataset();

        if (data.size() != this.varNames.size())
            throw new DataException("Number of datasets dimension mismatch");
        for (int i=0;i<data.size();i++) {
            DoubleArrayList x = data.get(i).copy();
            x.quickSort();
            if (x.getQuick(x.size()-1) - x.getQuick(0) == 0.0)
                bins = 1;
            x.trimToSize();
            dataset.addSeries(this.varNames.get(i), x.elements(), bins);
        }
        HistogramChart chart = new HistogramChart("","value",GlobalOptions.histogramType.toString(),dataset);
         double minB = Double.POSITIVE_INFINITY;
        for (int i=0;i<data.size();i++) {
            java.util.List l = chart.getSeriesCollection().getBins(i);
            HistogramBin b = ((HistogramBin)l.get(0));
            if (b.getBinWidth() > 0.0 && b.getBinWidth() < minB)
                minB = b.getBinWidth();
        }
        if (minB == Double.POSITIVE_INFINITY) minB = 0.001;
        for (int i=0;i<data.size();i++) {
            java.util.List l = chart.getSeriesCollection().getBins(i);
            HistogramBin b = ((HistogramBin)l.get(0));
            if (l.size() == 1 && b.getBinWidth() == 0.0) {
                b = new HistogramBin(b.getStartBoundary(),b.getStartBoundary() + minB);
//                for (int k=0;k<data.get(i).size();k++)
//                    b.incrementCount();
//                l.set(0, b);
                HistogramBin[] bs = new HistogramBin[1];
                bs[0] = b;
                chart.getSeriesCollection().getSeriesCollection().setBins(i,bs);
            }
        }

        setHistChartAttributes(chart,data.size());
        return chart;
    }

    private void setHistChartAttributes(HistogramChart chart, int series) {
        
        chart.getJFreeChart().getXYPlot().getDomainAxis().setAutoRange(true);
        chart.getJFreeChart().getXYPlot().getRangeAxis().setAutoRange(true);

        chart.getJFreeChart().getXYPlot().getDomainAxis().resizeRange(1.05);
        chart.getJFreeChart().getXYPlot().getRangeAxis().resizeRange(1.05);

        

        chart.getSeriesCollection().getSeriesCollection().setType(GlobalOptions.histogramType);
        chart.setTitle("");
        chart.getXAxis().setLabel("value");
        chart.getYAxis().setLabel(GlobalOptions.histogramType.toString());
        chart.getJFreeChart().setPadding(new RectangleInsets(10,10,10,10));
        chart.getJFreeChart().getXYPlot().getDomainAxis().setAutoTickUnitSelection(true);
        chart.getJFreeChart().getXYPlot().getRangeAxis().setAutoTickUnitSelection(true);
        chart.getJFreeChart().setBackgroundPaint(Color.white);
        Font f = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize);
        chart.getJFreeChart().getXYPlot().getDomainAxis().setLabelFont(f);
        chart.getJFreeChart().getXYPlot().getDomainAxis().setTickLabelFont(f);
        chart.getJFreeChart().getXYPlot().getRangeAxis().setLabelFont(f);
        chart.getJFreeChart().getXYPlot().getRangeAxis().setTickLabelFont(f);

        for (int i=0;i<series;i++) {
            chart.getSeriesCollection().setColor(i, ColorManager.getColor(i));
            chart.getSeriesCollection().setOutlineWidth(i, 2.0);
        }

       
        LegendTitle legend = new LegendTitle(new XYPlotLegend(varNames));
        legend.setPosition(RectangleEdge.BOTTOM);
        legend.setItemFont(f);
        chart.getJFreeChart().removeLegend();
        chart.getJFreeChart().addLegend(legend);
        
//        for (int i=0;i<chart.getJFreeChart().getXYPlot().getDatasetCount();i++) {
//                chart.getJFreeChart().getXYPlot().getRenderer(i).setBaseOutlineStroke(new java.awt.BasicStroke(1.0f));
//                chart.getJFreeChart().getXYPlot().getRenderer(i).setBaseOutlinePaint(Color.black);
//        }
    }


    void saveToFile(HistogramChart chart, String filename, PlotFileType target,
            boolean fullLatexDocument) {
        try {
            switch(target)  {
                case PDF:
                    PDFChartExport.saveChartAsPDF(filename, chart.getJFreeChart(), GlobalOptions.HistChartFileWidth, GlobalOptions.HistChartFileHeight);
                    break;
                case TEX:
                    chart.setLatexDocFlag(fullLatexDocument);
                    chart.toLatexFile(filename, GlobalOptions.HistChartFileWidth, GlobalOptions.HistChartFileHeight);
                    break;
                case PNG:
                    ChartUtilities.saveChartAsPNG(new java.io.File(filename), chart.getJFreeChart(), GlobalOptions.HistChartFileWidth, GlobalOptions.HistChartFileHeight);
                    break;
                case JPG:
                    ChartUtilities.saveChartAsJPEG(new java.io.File(filename), chart.getJFreeChart(), GlobalOptions.HistChartFileWidth, GlobalOptions.HistChartFileHeight);
                    break;
            }
        }
        catch(java.io.IOException e) { throw new DataException("Cannot save to file " + filename); }
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
