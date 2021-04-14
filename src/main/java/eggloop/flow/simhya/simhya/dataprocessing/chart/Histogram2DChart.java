/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.dataprocessing.chart;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import eggloop.flow.simhya.simhya.GlobalOptions;
import eggloop.flow.simhya.simhya.utils.ColorManager;
import org.jfree.chart.*;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.statistics.HistogramBin;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import umontreal.iro.lecuyer.charts.CustomHistogramDataset;
import umontreal.iro.lecuyer.charts.HistogramChart;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author luca
 */
public class Histogram2DChart extends Chart {
    ArrayList<String> varNames;
    int fontSize = 20;
    int legendSize = 25;
    public boolean fullLatexDocument = false;
    HistogramChart chart;

    /**
     * Constructs a Histogram2DChart with the given number of bins for each variable
     *
     * @param data a list of {@link PlotDistributionState} data
     * @param bins the number of bins
     */
    public Histogram2DChart(ArrayList<PlotDistributionState> data, int bins) {
        super(ChartType.HISTOGRAM);
        varNames = new ArrayList<String>();
        CustomHistogramDataset dataset = new CustomHistogramDataset();

        for (int i = 0; i < data.size(); i++) {
            PlotDistributionState dist = data.get(i);
            varNames.add(dist.name);
            DoubleArrayList x = dist.data.copy();
            x.quickSort();
            if (x.getQuick(x.size() - 1) - x.getQuick(0) == 0.0)
                bins = 1;
            x.trimToSize();
            dataset.addSeries(this.varNames.get(i), x.elements(), bins);
        }
        chart = new HistogramChart("", "value", GlobalOptions.histogramType.toString(), dataset);
        double minB = Double.POSITIVE_INFINITY;
        for (int i = 0; i < data.size(); i++) {
            java.util.List l = chart.getSeriesCollection().getBins(i);
            HistogramBin b = ((HistogramBin) l.get(0));
            if (b.getBinWidth() > 0.0 && b.getBinWidth() < minB)
                minB = b.getBinWidth();
        }
        if (minB == Double.POSITIVE_INFINITY) minB = 0.001;
        for (int i = 0; i < data.size(); i++) {
            java.util.List l = chart.getSeriesCollection().getBins(i);
            HistogramBin b = ((HistogramBin) l.get(0));
            if (l.size() == 1 && b.getBinWidth() == 0.0) {
                b = new HistogramBin(b.getStartBoundary(), b.getStartBoundary() + minB);
//                for (int k=0;k<data.get(i).size();k++)
//                    b.incrementCount();
//                l.set(0, b);
                HistogramBin[] bs = new HistogramBin[1];
                bs[0] = b;
                chart.getSeriesCollection().getSeriesCollection().setBins(i, bs);
            }
        }

        setChartAspect();
        setLegend();
        this.panel = new ChartPanel(chart.getJFreeChart());
    }


    /**
     * Constructs a Histogram2DChart automatically setting the number of bins for each variable
     *
     * @param data a list of {@link PlotDistributionState} data
     */
    public Histogram2DChart(ArrayList<PlotDistributionState> data) {
        super(ChartType.HISTOGRAM);
        varNames = new ArrayList<String>();
        CustomHistogramDataset dataset = new CustomHistogramDataset();
        dataset.setType(GlobalOptions.histogramType);
        for (int i = 0; i < data.size(); i++) {
            PlotDistributionState dist = data.get(i);
            varNames.add(dist.name);
            DoubleArrayList x = dist.data.copy();
            x.quickSort();

            double q1 = Descriptive.quantile(x, 0.25);
            double q2 = Descriptive.quantile(x, 0.75);
            double step = (q2 - q1) / ((double) x.size() / (2 * GlobalOptions.averageBinSizeInInterquantileRange));
            if (step == 0.0) {
                step = (x.getQuick(x.size() - 1) - x.getQuick(0)) / ((double) x.size() / GlobalOptions.averageBinSizeInInterquantileRange);
            }
            int binNumber;
            if (step == 0.0)
                binNumber = 1;
            else
                binNumber = (int) Math.ceil((x.getQuick(x.size() - 1) - x.getQuick(0)) / step);


            x.trimToSize();
            dataset.addSeries(this.varNames.get(i), x.elements(), binNumber);
        }

        chart = new HistogramChart("", "value", GlobalOptions.histogramType.toString(), dataset);
        //control if there is a deterministic distribution

        double minB = Double.POSITIVE_INFINITY;
        for (int i = 0; i < data.size(); i++) {
            java.util.List l = chart.getSeriesCollection().getBins(i);
            HistogramBin b = ((HistogramBin) l.get(0));
            if (b.getBinWidth() > 0.0 && b.getBinWidth() < minB)
                minB = b.getBinWidth();
        }
        if (minB == Double.POSITIVE_INFINITY) minB = 0.001;
        for (int i = 0; i < data.size(); i++) {
            java.util.List l = chart.getSeriesCollection().getBins(i);
            HistogramBin b = ((HistogramBin) l.get(0));
            if (l.size() == 1 && b.getBinWidth() == 0.0) {
                b = new HistogramBin(b.getStartBoundary(), b.getStartBoundary() + minB);
                //                for (int k=0;k<data.get(i).size();k++)
//                    b.incrementCount();
//                l.set(0, b);
                HistogramBin[] bs = new HistogramBin[1];
                bs[0] = b;
                chart.getSeriesCollection().getSeriesCollection().setBins(i, bs);
            }
        }

        setChartAspect();
        setLegend();
        this.panel = new ChartPanel(chart.getJFreeChart());

    }


    private void setChartAspect() {
        Font f = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize);

        this.setTitle("");

        chart.getSeriesCollection().getSeriesCollection().setType(GlobalOptions.histogramType);
        chart.getJFreeChart().setPadding(new RectangleInsets(10, 10, 10, 10));
        chart.getJFreeChart().setBackgroundPaint(Color.white);

        chart.getJFreeChart().getXYPlot().getDomainAxis().setAutoRange(true);
        chart.getJFreeChart().getXYPlot().getDomainAxis().resizeRange(1.05);
        chart.getJFreeChart().getXYPlot().getDomainAxis().setAutoTickUnitSelection(true);
        chart.getJFreeChart().getXYPlot().getDomainAxis().setLabelFont(f);
        chart.getJFreeChart().getXYPlot().getDomainAxis().setTickLabelFont(f);


        chart.getJFreeChart().getXYPlot().getRangeAxis().setAutoRange(true);
        chart.getJFreeChart().getXYPlot().getRangeAxis().resizeRange(1.05);
        chart.getJFreeChart().getXYPlot().getRangeAxis().setAutoTickUnitSelection(true);
        chart.getJFreeChart().getXYPlot().getRangeAxis().setLabelFont(f);
        chart.getJFreeChart().getXYPlot().getRangeAxis().setTickLabelFont(f);


        this.setXLabel("");
        this.setYLabel(GlobalOptions.histogramType.toString());


        int series = this.varNames.size();
        for (int i = 0; i < series; i++) {
            chart.getSeriesCollection().setColor(i, ColorManager.getColor(i));
            chart.getSeriesCollection().setOutlineWidth(i, 2.0);
        }


    }


    public final void setLegend() {
        Font f = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize);
        LegendTitle legend = new LegendTitle(new XYPlotLegend(varNames));
        legend.setPosition(RectangleEdge.BOTTOM);
        legend.setItemFont(f);
        chart.getJFreeChart().removeLegend();
        chart.getJFreeChart().addLegend(legend);
    }

    public void removeLegend() {
        chart.getJFreeChart().removeLegend();
    }


    @Override
    public void saveToFile(String filename, PlotFileType type) {
        try {
            switch (type) {
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
        } catch (java.io.IOException e) {
            throw new PlotException("Cannot save to file " + filename);
        }
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

    }

    private class XYPlotLegend implements LegendItemSource {
        private LegendItemCollection legend;
        private Dimension dim = new Dimension(legendSize, 4);

        private XYPlotLegend(ArrayList<String> names) {
            legend = new LegendItemCollection();
            for (int j = 0; j < names.size(); j++) {
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
