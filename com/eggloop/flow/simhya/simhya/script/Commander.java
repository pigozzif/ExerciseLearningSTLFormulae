/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.script;

import java.util.ArrayList;
import java.io.PrintStream;

/**
 *
 * @author Luca
 */
public interface Commander {
    
    /**
     * This method executes a command passed as a string and 
     * returns an object.
     * @param command the command string
     * @return It can return a JPanel object, if the command requires the visualization of some output
     * or a String object, if the command required the visualization of some text.
     */
    public Object execute(String command);
    
    /**
     * Returns a list of symbol object, containing name and value of variables
     * @param internalVariableName the name of a command variable of a model or of a dataset
     * @return 
     */
    public ArrayList<Symbol> getVariables(String internalVariableName);
        /**
     * Returns a list of symbol object, containing name and value of model parameters
     * @param internalVariableName the name of a command variable of a model 
     * @return 
     */
    public ArrayList<Symbol> getParameters(String internalVariableName);
    /**
     * Returns a list of symbol object, containing name and value of explorable parameters
     * @param internalVariableName the name of a command variable of a model 
     * @return 
     */
    public ArrayList<Symbol> getExplorableParameters(String internalVariableName);
    /**
     * Returns a list of exploredsymbol object, containing name and range of explored parameters
     * @param internalVariableName the name of a command variable of an exploration dataset
     * @return 
     */
    public ArrayList<ExploredSymbol> getExploredParameters(String internalVariableName);
    /**
     * Sets the print stream on which error messages will be written
     * @param err 
     */
    public void setErrorStream(PrintStream err);
    
    /**
     * Sets the print stream on which output messages will be written.
     * @param out 
     */
    public void setOutputStream(PrintStream out);
    
    
    
    /**
     * Returns the list of model variables
     * @return 
     */
    public ArrayList<String> getListOfModelVariables();
    
    /**
     * Returns the list of data variables
     * @return 
     */
    public ArrayList<String> getListOfDataVariables();
    
    /**
     * Returns the list of trajectory data variables
     * @return 
     */
    public ArrayList<String> getListOfTrajectoryDataVariables();
   
    
    /**
     * Returns the list of statistics exploration data variables
     * @return 
     */
    public ArrayList<String> getListOfStatisticsExploratorDataVariables();
    
    /**
     * Returns the list of deterministic exploration data variables
     * @return 
     */
    public ArrayList<String> getListOfDeterministicExploratorDataVariables();
    
    /**
     * Returns true if variable is a model variable
     * @param variable
     * @return 
     */
    public boolean isModelVariable(String variable);
    
    /**
     * Returns true if variable is a model variable
     * @param variable
     * @return 
     */
    public boolean isDataVariable(String variable);
    
    /**
     * Returns true if variable is a data variable
     * @param variable
     * @return 
     */
    public boolean isTrajectoryDataVariable(String variable);
    
    
    /**
     * Returns true if variable is a statistics exploration data variable
     * @param variable
     * @return 
     */
    public boolean isStatisticsExploratorVariable(String variable);
    
    /**
     * Returns true if variable is a deterministic exploration data variable
     * @param variable
     * @return 
     */
    public boolean isDeterministicExploratorVariable(String variable);
    
    
    /**
     * returns true if the variable is an explorator variable which contains data about the 
     * whole trajectories.
     * @param variable
     * @return 
     */
    public boolean exploratorVariableContainsTrajectoryData(String variable);
    
    /**
     * returns true if the variable is an explorator variable which contains data about the 
     * statistics of the whole trajectories. 
     * @param variable
     * @return 
     */
    public boolean exploratorVariableContainsTrajectoryStatistics(String variable);
    /**
     * returns true if the variable is an explorator variable which contains data about the 
     * distribution of the final state of the trajectory 
     * @param variable
     * @return 
     */
    public boolean exploratorVariableContainsFinalStateData(String variable);
    /**
     * returns true if the variable is an explorator variable which contains data about the 
     * statistics of the final state of the trajectory 
     * @param variable
     * @return 
     */
    public boolean exploratorVariableContainsFinalStateStatistics(String variable);

    /**
     * returns true if variable is a trajectory variable containing single trajectory data.
     * @param variable
     * @return 
     */
    public boolean trajectoryVariableContainsTrajectoryData(String variable);
    
    /**
     * returns true if variable is a trajectory variable containing statistics data
     * @param variable
     * @return 
     */
    public boolean trajectoryVariableContainsStatistics(String variable);
    /**
     * returns true if variable is a trajectory variable containing statistics on skew and 
     * kurtosis for trajectories
     * @param variable
     * @return 
     */
    public boolean trajectoryVariableContainsTrajectoryHigherOrderMoments(String variable);
    /**
     * returns true if variable is a trajectory variable containing data about the final state
     * @param variable
     * @return 
     */
    public boolean trajectoryVariableContainsFinalStateData(String variable);
    /**
     * returns true if variable is a trajectory variable containing statistics on skew and 
     * kurtosis on the final state.
     * @param variable
     * @return 
     */
    public boolean trajectoryVariableContainsFinalStateHigherOrderMoments(String variable);
    
    /**
     * returns the number of runs of trajectory data stored in a trajectory variable.
     * @param variable
     * @return 
     */
    public int getNumberOfRunsInTrajectoryVariable(String variable);
    
    
    /**
     * Returns the list of codes of available integrators
     * @return 
     */
    public ArrayList<String> getIntegratorCodeList();
    /**
     * Returns a list of description of available integrators
     * @return 
     */
    public ArrayList<String> getIntegratorDescriptionList();
    
    
    /**
     * returns the default atol
     * @return 
     */
    public double getDefaultAtol();
    /**
     * returns the default rtol
     * @return 
     */
    public double getDefaultRtol();
    /**
     * returns the default minstepsize
     * @return 
     */
    public double getDefaultMinStepSize();
    /**
     * returns the default maxstepsize
     * @return 
     */
    public double getDefaultMaxStepSize();
    /**
    * returns the default maxeventstep
    * @return 
    */     
    public double getDefaultMaxEventStep();
    /**
     * returns the defaul maxeventerror
     * @return 
     */
    public double getDefaultMaxEventError();
    /**
     * returns the default maxeventiter
     * @return 
     */
    public int getDefaultMaxEventIter();
    /**
     * returns the default sample points
     * @return 
     */        
    public int getDefaultSamplePoints();
    
    
    /**
     * Returns the name of a model, if variable is a model variable, or raises an exception otherwise.
     * @param variable
     * @return 
     */
    public String getModelName(String variable);
    
    /**
     * Sets the working directory of simhya
     * @param directory 
     */
    public void setWorkingDirectory(java.io.File directory);
    
    /**
     * gets the current working directory.
     * @return 
     */
    public java.io.File getWorkingDirectory(); 
    
    
}
