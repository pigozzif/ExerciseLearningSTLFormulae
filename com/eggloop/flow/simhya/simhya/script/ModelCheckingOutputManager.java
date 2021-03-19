/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.script;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import com.eggloop.flow.simhya.simhya.dataprocessing.chart.Line2DChart;
import com.eggloop.flow.simhya.simhya.dataprocessing.chart.LineChart;
import com.eggloop.flow.simhya.simhya.dataprocessing.chart.Plot2DTrajectory;
import com.eggloop.flow.simhya.simhya.dataprocessing.chart.PlotFileType;
import com.eggloop.flow.simhya.simhya.modelchecking.SMCoutput;

/**
 *
 * @author luca
 */
public class ModelCheckingOutputManager implements Plotter, Saver{
    ArrayList<SMCoutput> outlist;
    CommandManager cmd;
    
    public ArrayList<String> formulas = null;
    
    public boolean plotSignals = false;
    public boolean allSignals = false;
    
    public boolean saveSignals = false;
    public boolean saveSummary = true;
    
    public String filename;
    
    public boolean plotToFile = false;
    
    public PlotFileType fileType = PlotFileType.PNG;

    
    
    public ModelCheckingOutputManager(String outVar, CommandManager cmd)  throws ScriptException {
        if (!cmd.containsVariable(outVar))
            throw new ScriptException("Variable $" + outVar + " does not exist.");
        if (cmd.isSMCoutputVariable(outVar)) {
            this.outlist = cmd.getSMCoutput(outVar);
        } else
            throw new ScriptException("Variable $" + outVar + " is not a model checking data variable.");
        this.cmd = cmd;
    }
    
 

    public void saveSignals() {
        this.saveSignals = true;
        this.saveSummary = false;
    }

    public void saveSummary() {
        this.saveSummary = true;
        this.saveSignals = false;
    }
    
    
    
    
    public String save() throws ScriptException {
        String s = "";
        if (filename == null)
            throw new ScriptException("Filename not provided. Cannot save MC data!");
        if (formulas == null)
            throw new ScriptException("Don't know what formulae to save, please specify it!");
        if (filename.endsWith(".txt")) {
            filename = filename.substring(0, filename.length() - 4);
        }
        if (this.saveSummary) {
            try {   
                PrintWriter p = new PrintWriter( filename + ".txt" );
                for (String f : this.formulas) {
                     SMCoutput out = this.getSMCoutput(f); 
                     if (out.isTrajectory())
                        p.print(out.shortPrint());
                     else
                         p.print(out.extendedPrint());
                }
                p.close();
            } catch (FileNotFoundException e) {
                throw new ScriptException( "Error while saving MC data: " + e.getMessage());
            }
            return "Summary of MC data saved correctly ";
        }
        for (String f : this.formulas) {
            SMCoutput out = this.getSMCoutput(f); 
            try {
                if ((saveSignals && out.hasSignals()) || out.isTrajectory() ) {
                    PrintWriter p = new PrintWriter(filename + "_" +  f + ".txt");
                    if (saveSignals) {
                       p.print(out.signalsToString(allSignals));
                    } else {
                        if (out.isTrajectory())
                            p.print(out.extendedPrint());
                    }
                    p.close();
                }
            } catch (Exception e) {
                throw new ScriptException( "Error while saving MC data: " + e.getMessage());
            }
        }
        return "MC data saved succesfully ";
    }
    
    
    public String plot()  throws ScriptException  {
        if (formulas == null)
            throw new ScriptException("Don't know what formulae to plot, please specify it!");
        LineChart chart = null; 
        String title = "";
        if (this.plotSignals && this.formulas.size() > 1) {
            ArrayList<Plot2DTrajectory> list = new ArrayList<Plot2DTrajectory>();
            for (String f : this.formulas) {
                SMCoutput out = this.getSMCoutput(f);
                Plot2DTrajectory T = out.getTopSignal();
                if (T!=null) 
                    list.add(T);
            }
            if (!list.isEmpty())
                chart = new LineChart(list,"time","truth value");
            title = "SimHyA: Top signals of MiTL formulae";
             if (chart == null)
                throw new ScriptException("Selected formulae have no signal to plot!");
                
        } else if (this.plotSignals && this.formulas.size() == 1) {
            SMCoutput out = this.getSMCoutput(this.formulas.get(0));
            chart = out.plotSignals(allSignals);
            if (chart == null)
                throw new ScriptException("Formula " + this.formulas.get(0) + " has no signal to plot!");
            title = "SimHyA: Satisfaction of subformulae of MiTL formula " + this.formulas.get(0);
        } else {
           ArrayList<Plot2DTrajectory> list = new ArrayList<Plot2DTrajectory>();
           for (String f : this.formulas) {
               SMCoutput out = this.getSMCoutput(f);
               if (out.isTrajectory())
                   list.addAll(out.getTrajectory());    
           }
           if (!list.isEmpty())
                chart = new LineChart(list,"time","satisfaction");
           title = "SimHyA: Satisfaction of exploration of MiTL formulae";
           if (chart == null)
                throw new ScriptException("Selected formulae have no trajectory to plot!");
        }
        if (chart == null)
            throw new ScriptException("I cannot plot the MC output!");
        if (this.plotToFile && this.filename != null) {
            chart.saveToFile(filename, fileType);
            return "Plot MC saved as " + fileType.name() + " to file " + filename;
        }
        else if (cmd.isEmbeddedInGUI())  {
            JPanel panel = chart.getPanel();
            cmd.setPanelOutput(panel);
        }
        else {
            JFrame frame = chart.show();
            frame.setTitle(title);
        }
        return "Plotted MC data to new window ";   
    }
    
    
    public void finalise() {
        if (this.formulas == null)
            this.getAllFormulas();
    }
  
    
    public void getAllFormulas() {
        ArrayList<String> list = new ArrayList<String>();
        for (SMCoutput o : this.outlist) {
            list.add(o.getFormula());
        }
        this.formulas = list;
    }
    
    
    private SMCoutput getSMCoutput(String formula)  throws ScriptException {
        for (SMCoutput o : this.outlist) {
            if (o.isFormula(formula))
                return o;
        }
        throw new ScriptException("Formula " + formula + " has not been model checked!");
    }
    
    
}
