/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.dataprocessing.chart;
import javax.swing.JPanel;
import javax.swing.JFrame;
import eggloop.flow.simhya.simhya.GlobalOptions;

/**
 *
 * @author Luca
 */
public abstract class Chart {
    final ChartType type;
    final int dimensions;
    JPanel panel;

    
    public Chart(ChartType type) {
        this.type = type;
        this.dimensions = type.getDimensions();
    }

    /**
     *
     * @return the dimensions of the chart (2 or 3);
     */
    public int getDimensions() {
        return this.dimensions;
    }

    /**
     *
     * @return the Jpanel of the chart.
     */
    public JPanel getPanel() {
        return panel;
    }

    /**
     * Shows the chart in a new window, with default size
     * @return the JFrame hande
     */
    public JFrame show() {
        JFrame frame = new JFrame();
        frame.setSize(GlobalOptions.ChartFrameWidth, GlobalOptions.ChartFrameHeight);
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return frame;
    }

    /**
     * Shows the chart in a new window, with specified size
     * @param width
     * @param height
     * @return the JFrame hande
     */
    public JFrame show(int width, int height) {
        JFrame frame = new JFrame();
        frame.setSize(width, height);
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return frame;
    }


    /**
     * Sets the title of the chart. Default title is empty
     * @param title
     */
    public abstract void setTitle(String title);

    /**
     * Sets the label of the X axis
     * @param label
     */
    public abstract void setXLabel(String label);
    /**
     * Sets the label of the Y axis
     * @param label
     */
    public abstract void setYLabel(String label);
    /**
     * Sets the label of the Z axis, if any
     * @param label
     */
    public abstract void setZLabel(String label);

//    public abstract void showXThicks(boolean showThicks);
//    public abstract void showYThicks(boolean showThicks);
//    public abstract void showZThicks(boolean showThicks);
//    public abstract void setXThickPoints(int points);
//    public abstract void setYThickPoints(int points);
//    public abstract void setZThickPoints(int points);

    /**
     * Saves the graph to a file, of specified type.
     * @param filename
     * @param type
     */
    public abstract void saveToFile(String filename, PlotFileType type);





}
