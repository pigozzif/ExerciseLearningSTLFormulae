/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.hype;
import java.util.ArrayList;
import java.util.HashMap;
import eggloop.flow.simhya.simhya.matheval.Evaluator;
import eggloop.flow.simhya.simhya.matheval.Expression;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import eggloop.flow.simhya.simhya.matheval.function.FunctionDefinition;

/**
 *
 * @author Luca
 */
public class HypeModel {
    String modelName;
    Evaluator eval;
    //global lookup table
    public SymbolTable symbols;
    //lookup tables for definitions.
    public HashMap<String,Variable> variables;
    HashMap<String,Parameter> parameters;
    HashMap<String,ExpressionVariable> expressions;
    HashMap<String,Function> guards;
    HashMap<String,Function> functions;
    HashMap<String,ResetDefinition> resets;
    ArrayList<Object> definitions;
    //mappings
    HashMap<String,String> influenceMap;
    public HashMap<String,Event> eventMap;
    //components
    HashMap<String,Subcomponent> subcomponents;
    HashMap<String,Component> components;
    HashMap<String,SubcomponentTemplate> subcomponentTemplates;
    HashMap<String,ComponentTemplate> componentTemplates;
    Component uncontrolledSystem;
    //controllers
    HashMap<String,SequentialController> sequentialControllers;
    HashMap<String,Controller> controllers;
    Controller systemController;
    

    public HypeModel(String modelName) {
        this.modelName = modelName;
        this.eval = new Evaluator();
        symbols = new SymbolTable();
        variables = new HashMap<String,Variable>();
        parameters = new HashMap<String,Parameter>();
        expressions = new HashMap<String,ExpressionVariable>();
        guards = new HashMap<String,Function>();
        functions = new HashMap<String,Function>();
        resets = new HashMap<String,ResetDefinition>();
        definitions = new ArrayList<Object>();
        influenceMap = new HashMap<String,String>();
        eventMap = new HashMap<String,Event>();
        subcomponents = new HashMap<String,Subcomponent>();
        components = new HashMap<String,Component>();
        subcomponentTemplates = new HashMap<String,SubcomponentTemplate>();
        componentTemplates = new HashMap<String,ComponentTemplate>();
        uncontrolledSystem = null;
        sequentialControllers = new HashMap<String,SequentialController>();
        controllers = new HashMap<String,Controller>();
        systemController = null;
        //adds the deadlocked seq controller nil
        this.sequentialControllers.put("nil", new SequentialController("nil"));
    }
    
   

    /**
     * Adds a variable definition
     * @param name
     * @param value
     */
    public void addVariable(String name, String value) {
        if (variables.containsKey(name) || parameters.containsKey(name) || expressions.containsKey(name))
            throw new HypeException("Symbol " + name + " already defined");
        Expression exp;
        try { 
            int id = eval.parse(value);
            exp = eval.getExpression(id);
        } catch (Exception e) {
            throw new HypeException("Cannot parse initial value definition " + value + " of variable " + name + ": " + e.getMessage());
        } catch (Error e) {
            throw new HypeException("Cannot parse initial value definition " + value + " of variable " + name + ": " + e.getMessage());
        }
        eval.addVariable(name, exp.computeValue(), exp);
        Variable v = new Variable(name,value);
        this.variables.put(name, v);
        this.symbols.addSymbol(name);
        this.definitions.add(v);
    }
    
    public boolean isVariableDefined(String name) {
    	if (variables.containsKey(name) || parameters.containsKey(name) || expressions.containsKey(name))
    		return true;
    	else
    		return false;
    }

    /**
     * Adds a parameter definition
     * @param name
     * @param value
     */
    public void addParameter(String name, String value) {
        if (variables.containsKey(name) || parameters.containsKey(name) || expressions.containsKey(name))
            throw new HypeException("Symbol " + name + " already defined");
        Expression exp;
        try {
            int id = eval.parse(value);
            exp = eval.getExpression(id);
        } catch (Exception e) {
            throw new HypeException("Cannot parse initial value definition " + value + " of parameter " + name + ": " + e.getMessage());
        } catch (Error e) {
            throw new HypeException("Cannot parse initial value definition " + value + " of parameter " + name + ": " + e.getMessage());
        }
        eval.addConstant(name, exp.computeValue(), exp);
        Parameter p = new Parameter(name,value);
        this.parameters.put(name, p);
        this.symbols.addSymbol(name);
        this.definitions.add(p);
    }

    /**
     * Adds a new expression variable definition
     * @param name
     * @param value
     */
    public void addExpressionVariable(String name, String value) {
        if (variables.containsKey(name) || parameters.containsKey(name) || expressions.containsKey(name))
            throw new HypeException("Symbol " + name + " already defined");
        Expression exp;
        try {
            int id = eval.parse(value);
            exp = eval.getExpression(id);
        } catch (Exception e) {
            throw new HypeException("Cannot parse initial value definition " + value + " of expression variable " + name + ": " + e.getMessage());
        } catch (Error e) {
            throw new HypeException("Cannot parse initial value definition " + value + " of expression variable " + name + ": " + e.getMessage());
        }
        eval.addExpressionVariable(name, exp);
        ExpressionVariable v = new ExpressionVariable(name,value);
        this.expressions.put(name, v);
        this.symbols.addSymbol(name);
        this.definitions.add(v);
    }

    /**
     * Adds a guard definition
     * @param name
     * @param parameters
     * @param definition
     */
    public void addGuard(String name, ArrayList<String> parameters, String definition) {
        if (this.guards.containsKey(name) || this.functions.containsKey(name))
            throw new HypeException("Guard or function " + name + " already defined");
        Function def = new Function(name,parameters,definition);
        String fullDef = def.getFullDefinition();
        FunctionDefinition exp;
        try {
            eval.parse(fullDef);
            exp = eval.getFunctionDefinition(name);
        } catch (Exception e) {
            throw new HypeException("Cannot parse guard definition " + fullDef + ": " + e.getMessage());
        } catch (Error e) {
            throw new HypeException("Cannot parse guard definition " + fullDef + ": " + e.getMessage());
        }
        if (!exp.isLogicalFunction())
            throw new HypeException("Defined guard " + name + "is not a logical predicate");
        this.guards.put(name, def);
        this.definitions.add(def);
    }

    /**
     * Adds a function definition
     * @param name
     * @param parameters
     * @param definition
     */
    public void addFunction(String name, ArrayList<String> parameters, String definition) {
        if (this.guards.containsKey(name) || this.functions.containsKey(name))
            throw new HypeException("Guard or function " + name + " already defined");
        Function def = new Function(name,parameters,definition);
        String fullDef = def.getFullDefinition();
        FunctionDefinition exp;
        try {
            eval.parse(fullDef);
            exp = eval.getFunctionDefinition(name);
        } catch (Exception e) {
            throw new HypeException("Cannot parse function definition " + fullDef + ": " + e.getMessage());
        
        } catch (Error e) {
            throw new HypeException("Cannot parse function definition " + fullDef + ": " + e.getMessage());
        }
        if (exp.isLogicalFunction())
            throw new HypeException("Defined function " + name + "is a logical predicate and not a function");
        this.functions.put(name, def);
        this.definitions.add(def);
    }

    /**
     * Adds a reset definiton. The reset list must be parsed correctly in advance
     * @param name
     * @param parameters
     * @param resets
     */
    public void addResetDefinition(String name, ArrayList<String> parameters, ResetList resets) {
        if (this.resets.containsKey(name))
            throw new HypeException("Reset " + name + " already defined");
        ResetDefinition r = new ResetDefinition(name,parameters,resets);
        this.resets.put(name, r);
    }


    public void addInfluenceMap(String influence, String variable) {
        if (!isVariable(variable))
            throw new HypeException("Variable " + variable + " is not defined");
        if (isInfluence(influence))
            throw new HypeException("Influence " + influence + " already defined");
        this.symbols.addInfluence(influence);
        this.influenceMap.put(influence, variable);
    }


    /**
     * Adds a non stochastic event to the event list, checks for correctness of guard.
     * @param name
     * @param guard
     * @param reset
     */
    public void addEvent(String name, String guard, ResetList reset) {
        if (this.isEvent(name))
            throw new HypeException("Event " + name + " already defined");
        if (!guard.isEmpty() && !guard.contains("$") && !guard.contains("#"))
            this.checkGuard(guard, name);
        Event e = new Event(name,guard,reset);
        /*System.out.println("add event");
        System.out.println("name:" + name);
        System.out.println("guard:" + guard);
        System.out.println("ResetList:" + reset.toString());*/
        this.symbols.addEvent(name);
        this.eventMap.put(name, e);
    }

    /**
     * Adds a stochastic event to the event list, checks for correctness of guard and rate
     * @param name
     * @param guard
     * @param reset
     * @param rate
     */
    public void addEvent(String name, String guard, ResetList reset, String rate) {
        if (this.isEvent(name))
            throw new HypeException("Event " + name + " already defined");
        if (!guard.isEmpty() && !guard.contains("$") && !guard.contains("#"))
            checkGuard(guard, name);
        checkRate(rate,name);
        Event e = new Event(name,guard,reset,rate);
        this.symbols.addEvent(name);
        this.eventMap.put(name, e);
    }
    
    
    /**
     * Adds a subcomponent to the model, checking that its name is unique
     * @param comp the subcomponent
     */
    public void addSubcomponent(Subcomponent comp) {
        String n = comp.name;
        if (this.subcomponents.containsKey(n) || this.components.containsKey(n)
                || this.subcomponentTemplates.containsKey(n) 
                || this.componentTemplates.containsKey(n))
            throw new HypeException("Subcomponent " + n + " already defined");
        for (String e : comp.eventToFlow.keySet()) {
            if (!this.isEvent(e))
                throw new HypeException("Event " + e + " for subcomponent " + comp.name + " not defined!");
        }
        this.subcomponents.put(n, comp);
    }
    
    /**
     * Adds a component to the model, checking that its name is unique
     * @param comp the component
     */
    public void addComponent(Component comp) {
        String n = comp.name;
        if (this.subcomponents.containsKey(n) || this.components.containsKey(n)
                || this.subcomponentTemplates.containsKey(n) 
                || this.componentTemplates.containsKey(n))
            throw new HypeException("Component " + n + " already defined");
        this.components.put(n, comp);
    }
    
    /**
     * Adds a subcomponent tempate to the model, checking that its name is unique
     * @param comp the subcomponent
     */
    public void addSubcomponentTemplate(SubcomponentTemplate comp) {
        String n = comp.name;
        if (this.subcomponents.containsKey(n) || this.components.containsKey(n)
                || this.subcomponentTemplates.containsKey(n) 
                || this.componentTemplates.containsKey(n))
            throw new HypeException("Subcomponent " + n + " already defined");
        this.subcomponentTemplates.put(n, comp);
    }
    
    /**
     * Adds a component to the model, checking that its name is unique
     * @param comp the component
     */
    public void addComponentTemplate(ComponentTemplate comp) {
        String n = comp.name;
        if (this.subcomponents.containsKey(n) || this.components.containsKey(n)
                || this.subcomponentTemplates.containsKey(n) 
                || this.componentTemplates.containsKey(n))
            throw new HypeException("Component " + n + " already defined");
        this.componentTemplates.put(n, comp);
    }
    
    /**
     * Sets the uncontrolled system. It must be a defined component.
     * @param compName 
     */
    public void setUncontrolledSystem(String compName) {
        if (!this.components.containsKey(compName))
            throw new HypeException("Component " + compName + " is not defined");
        this.uncontrolledSystem = this.components.get(compName);
    }
    
    
    /**
     * Adds a sequential controller to the model
     * @param controller 
     */
    public void addSequentialController(SequentialController controller) {
        if (this.sequentialControllers.containsKey(controller.name) ||
                this.controllers.containsKey(controller.name))
            throw new HypeException("Sequential controller " + controller.name + " already defined");
        this.sequentialControllers.put(controller.name, controller);
    }
    
    /**
     * Adds a controller to the model
     * @param controller 
     */
    public void addController(Controller controller) {
        if (this.sequentialControllers.containsKey(controller.name) ||
                this.controllers.containsKey(controller.name))
            throw new HypeException("Controller " + controller.name + " already defined");
        this.controllers.put(controller.name, controller);
    }
    
    /**
     * adds the system controller
     * @param compName 
     */
    public void setSystemController(String compName) {
        if (!this.controllers.containsKey(compName))
            throw new HypeException("Controller " + compName + " is not defined");
        this.systemController = this.controllers.get(compName);
    }
    
    public Variable getVariableValue(String name) {
    	return this.variables.get(name);
    }
    
    //checks
    public boolean isVariable(String name) {
        return this.variables.containsKey(name);
    }

    public boolean isParameter(String name) {
        return this.parameters.containsKey(name);
    }

    public boolean isExpressionVariable(String name) {
        return this.expressions.containsKey(name);
    }

    public boolean isGuardDefinition(String name) {
        return this.guards.containsKey(name);
    }

    public boolean isFunctionDefinition(String name) {
        return this.functions.containsKey(name);
    }

    public boolean isResetDefinition(String name) {
        return this.resets.containsKey(name);
    }

    public boolean isInfluence(String influenceName) {
        return this.influenceMap.containsKey(influenceName);
    }

    public boolean isEvent(String event) {
        return this.eventMap.containsKey(event);
    }
    
    public boolean isComponent(String name) {
        return this.components.containsKey(name);
    }
    
    public boolean isSubcomponent(String name) {
        return this.subcomponents.containsKey(name);
    }
    
    public boolean isComponentTemplate(String name) {
        return this.componentTemplates.containsKey(name);
    }
    
    public boolean isSubcomponentTemplate(String name) {
        return this.subcomponentTemplates.containsKey(name);
    }
    
    public boolean isSequentialController(String name) {
        return this.sequentialControllers.containsKey(name);
    }
    
    public boolean isController(String name) {
        return this.controllers.containsKey(name);
    }
    
    //////////////////////////////////

    public String getInfluencedVariable(String influenceName) {
        if (!this.isInfluence(influenceName))
            throw new HypeException("Influence " + influenceName + " is not defined");
        return this.influenceMap.get(influenceName);
    }

    public HashMap<String, String> getInfluenceMap() {
        return influenceMap;
    }

    public Event getEvent(String eventName) {
        if (!this.isEvent(eventName))
            throw new HypeException("Event " + eventName + " is not defined");
        return this.eventMap.get(eventName);
    }
    
    public Subcomponent getSubcomponent(String name) {
        if (!this.subcomponents.containsKey(name))
            throw new HypeException("Subcomponent " + name + " is not defined");
        return this.subcomponents.get(name);
    }
    
    public SubcomponentTemplate getSubcomponentTemplate(String name) {
        if (!this.subcomponentTemplates.containsKey(name))
            throw new HypeException("Subcomponent template " + name + " is not defined");
        return this.subcomponentTemplates.get(name);
    }
    
    public Component getComponent(String name) {
        if (!this.components.containsKey(name))
            throw new HypeException("Component " + name + " is not defined");
        return this.components.get(name);
    }
    
    public ComponentTemplate getComponentTemplate(String name) {
        if (!this.componentTemplates.containsKey(name))
            throw new HypeException("Component template " + name + " is not defined");
        return this.componentTemplates.get(name);
    }
    
    public Controller getController(String name) {
        if (!this.controllers.containsKey(name))
            throw new HypeException("Controller " + name + " is not defined");
        return this.controllers.get(name);
    }
    
    public SequentialController getSequentialController(String name) {
        if (!this.sequentialControllers.containsKey(name))
            throw new HypeException("Sequential Controller " + name + " is not defined");
        return this.sequentialControllers.get(name);
    }
    
    public SymbolTable getSymbolTable() {
        return this.symbols;
    }
    
    ///////////////////////////////////////////////////////////

    /**
     * parses an expression and returns it
     * @param expression
     * @return
     */
    public Expression parseExpression(String expression) {
        Expression exp;
        try {
            int id = eval.parse(expression);
            exp = eval.getExpression(id);
        } catch (Exception e) {
            throw new HypeException("Cannot parse expression " + expression + ": " + e.getMessage());
        }
        return exp;
    }

    /**
     * parses an expression and returns it
     * @param expression
     * @param boundVariables a list of parameters to be treated as bounded local variables during parsing.
     * @return
     */
    public Expression parseExpression(String expression, ArrayList<String> boundVariables) {
        Expression exp;
        SymbolArray local = new SymbolArray();
        for (String s : boundVariables)
            local.addSymbol(s, 0);
        try {
            int id = eval.parse(expression, local);
            exp = eval.getExpression(id);
        } catch (Exception e) {
            throw new HypeException("Cannot parse expression " + expression + ": " + e.getMessage());
        }
        return exp;
    }

    /**
     * Instantiates the required reset definition
     * @param name
     * @param globalSymbols
     * @return
     */
    public ResetList instantiateResetDefinition(String name, ArrayList<String> globalSymbols) {
        if (!this.isResetDefinition(name))
            throw new HypeException("Reset definition " + name + " is not defined");
        ResetDefinition r = this.resets.get(name);
        return r.instantiate(eval, globalSymbols);
    }

    
    private void checkGuard(String guard, String name) {
        Expression exp;
        try {
            int id = eval.parse(guard);
            exp = eval.getExpression(id);
        } catch (Exception e) {
            throw new HypeException("Cannot parse guard " + guard + " for event " + name + ": " + e.getMessage());
        } catch (Error e) {
            throw new HypeException("Cannot parse guard " + guard + " for event " + name + ": " + e.getMessage());
        }
        if (!exp.isLogicalExpression())
            throw new HypeException("Guard " + guard + " for event " + name + "is not a logical predicate");
    }
    
    
    private void checkRate(String function, String name) {
        Expression exp;
        try {
            int id = eval.parse(function);
            exp = eval.getExpression(id);
        } catch (Exception e) {
            throw new HypeException("Cannot parse rate " + function + " for event " + name + ": " + e.getMessage());
        } catch (Error e) {
            throw new HypeException("Cannot parse rate " + function + " for event " + name + ": " + e.getMessage());
        }
        if (exp.isLogicalExpression())
            throw new HypeException("Rate " + function + " for event " + name + "is a logical predicate and not a function");
    }
    
    

    /**
     * Generates a String object containing the equivalent flat model definition 
     * of the hype model!!!!
     * @return 
     */
    public String generateFlatModelString() {
        ArrayList<Variable> stateVariables; 
        ArrayList<FlowTransition> flowTransitions;
        ArrayList<EventTransition> eventTransitions = new ArrayList<EventTransition>();
        HashMap<String,HashMap<String,Integer>> eventToFlowState;
        
        /*
         * Uncontrolled system:
         * 1. generare variabili flow state
         * 2. generare le event flow map e update eventi
         * 3. generare le flow transition
         */ 
        stateVariables = this.uncontrolledSystem.generateStateVariables();
        eventToFlowState = this.uncontrolledSystem.generateEventToFlowStateMapping(this.eventMap.keySet());
        for (String ev : this.eventMap.keySet())
            this.eventMap.get(ev).setFlowReset(eventToFlowState.get(ev));
        flowTransitions = this.uncontrolledSystem.generateFlowTransitions();
        
         /* Controller
         * 1. grounding
         * 2. derivative setp
         * 3. state variables
         * 4. generate transitions for each event 
         */
        SynchronizationNode sync = this.systemController.synchRoot.constructGroundTree();
        sync.computeDerivativeSetsOfSequentialControllers(sequentialControllers);
        stateVariables.addAll(sync.generateStateVariables());
        for (String ev : this.eventMap.keySet()) {
            ArrayList<GuardResetPair> l = sync.generateTransitionsForEvent(ev);
            for (GuardResetPair p : l)  {
 //           	System.out.println(p.toString());
   //         	System.out.println(ev.toString());
                eventTransitions.add(this.eventMap.get(ev).getTransition(p));
            }	
        }
        
        //generate model string;
        String model = "model hype_" + this.modelName + " {\n";
        for (Object x : this.definitions)
            model += "\t" + x.toString() + "\n";
        model += "\n";
        for (Variable x : stateVariables)
            model += "\t" + x.toString() + "\n";
        model += "\n";
        for (FlowTransition x : flowTransitions)
            model += "\t" + x.toString() + "\n";
        model += "\n";
        for (EventTransition x : eventTransitions) 
            model += "\t" + x.toString() + "\n";
        model += "}";
        return model;
    }

}
