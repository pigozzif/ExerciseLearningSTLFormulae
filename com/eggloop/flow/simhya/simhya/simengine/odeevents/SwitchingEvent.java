package com.eggloop.flow.simhya.simhya.simengine.odeevents;//package com.eggloop.flow.simhya.simhya.simengine.odeevents;
//
////
//// Source code recreated from a .class file by IntelliJ IDEA
//// (powered by Fernflower decompiler)
////
//
//import org.apache.commons.math.ode.events.EventException;
//import org.apache.commons.math.ode.events.EventHandler;
//import com.eggloop.flow.simhya.simhya.dataprocessing.EventDataCollector;
//import com.eggloop.flow.simhya.simhya.matheval.SymbolArray;
//import com.eggloop.flow.simhya.simhya.model.store.Function;
//import com.eggloop.flow.simhya.simhya.model.store.Predicate;
//import com.eggloop.flow.simhya.simhya.simengine.ProgressMonitor;
//import com.eggloop.flow.simhya.simhya.simengine.hybrid.DynamicSwitcher;
//
//import java.util.Collection;
//
//public class SwitchingEvent implements EventHandler, PrintableEvent {
//    private Function eventConditionWhileTrue;
//    private Function eventConditionWhileFalse;
//    private Function currentEventCondition;
//    private Predicate guardWhileTrue;
//    private Predicate guardWhileFalse;
//    private Predicate currentGuard;
//    private boolean status;
//    private DynamicSwitcher switcher;
//    private boolean isLocalSwitching;
//    private int id;
//    private int multiplicator;
//    private SymbolArray vars;
//    private boolean useCache;
//    private int dimension;
//    private EventDataCollector collector;
//    String name = "Switching event";
//    private boolean printEvent;
//    private ProgressMonitor monitor;
//    private Collection<Integer> listOfModifiedTriggers;
//    private Collection<Integer> listOfModifiedPriorities;
//
//    public SwitchingEvent(int id, int multiplicator, Function eventConditionWhileTrue, Function eventConditionWhileFalse, Predicate guardWhileTrue, Predicate guardWhileFalse, DynamicSwitcher switcher, boolean isLocalSwitching, int dimension) {
//        this.id = id;
//        this.multiplicator = multiplicator;
//        this.guardWhileFalse = guardWhileFalse;
//        this.guardWhileTrue = guardWhileTrue;
//        this.eventConditionWhileTrue = eventConditionWhileTrue;
//        this.eventConditionWhileFalse = eventConditionWhileFalse;
//        this.isLocalSwitching = isLocalSwitching;
//        this.switcher = switcher;
//        this.dimension = dimension;
//        this.useCache = false;
//        this.vars = SymbolArray.getNewFastEvalSymbolArray(dimension);
//        this.collector = null;
//        this.printEvent = false;
//        this.monitor = null;
//        this.listOfModifiedPriorities = null;
//        this.listOfModifiedTriggers = null;
//    }
//
//    public SwitchingEvent(int id, Predicate guard, Function eventCondition, DynamicSwitcher switcher, boolean isLocalSwitching, int dimension) {
//        this.id = id;
//        this.guardWhileFalse = guard;
//        this.guardWhileTrue = guard;
//        this.eventConditionWhileTrue = eventCondition;
//        this.eventConditionWhileFalse = eventCondition;
//        this.switcher = switcher;
//        this.isLocalSwitching = isLocalSwitching;
//        this.dimension = dimension;
//        this.useCache = false;
//        this.vars = SymbolArray.getNewFastEvalSymbolArray(dimension);
//        this.collector = null;
//    }
//
//    public void setPrintEvent(ProgressMonitor monitor) {
//        if (monitor != null) {
//            this.monitor = monitor;
//            this.printEvent = true;
//        } else {
//            this.monitor = null;
//            this.printEvent = false;
//        }
//
//    }
//
//    public void setCollectorInfo(String name, EventDataCollector collector) {
//        this.collector = collector;
//        this.name = name;
//    }
//
//    public void setUseCache(boolean useCache) {
//        this.useCache = useCache;
//    }
//
//    public void initSwitchingCondition() {
//        this.status = this.useCache ? this.eventConditionWhileFalse.computeCache() >= 0.0D : this.eventConditionWhileFalse.compute() >= 0.0D;
//        if (this.status) {
//            this.currentEventCondition = this.eventConditionWhileTrue;
//            this.currentGuard = this.guardWhileTrue;
//        } else {
//            this.currentEventCondition = this.eventConditionWhileFalse;
//            this.currentGuard = this.guardWhileFalse;
//        }
//
//        if (this.isLocalSwitching) {
//            this.switcher.setLocalContinuityStatus(this.id, this.status);
//        } else {
//            this.switcher.setGlobalContinuityStatus(this.id, this.status);
//        }
//
//    }
//
//    public int eventOccurred(double t, double[] y, boolean increasing) throws EventException {
//        if (this.printEvent) {
//            this.monitor.signalEvent(this.name, t);
//        }
//
//        this.status = !this.status;
//        if (this.status) {
//            this.currentEventCondition = this.eventConditionWhileTrue;
//            this.currentGuard = this.guardWhileTrue;
//        } else {
//            this.currentEventCondition = this.eventConditionWhileFalse;
//            this.currentGuard = this.guardWhileFalse;
//        }
//
//        if (this.isLocalSwitching) {
//            this.switcher.setLocalContinuityStatus(this.id, this.status);
//        } else {
//            this.switcher.setGlobalContinuityStatus(this.id, this.status);
//        }
//
//        if (this.collector != null) {
//            this.collector.putNextInstantaneousEvent(t, y, this.name);
//        }
//
//        return 1;
//    }
//
//    public double g(double t, double[] y) throws EventException {
//        this.vars.setValuesReference(y);
//        double x = this.useCache ? this.currentEventCondition.computeCache(this.vars) : this.currentEventCondition.compute(this.vars);
//        return x;
//    }
//
//    public void resetState(double t, double[] y) throws EventException {
//    }
//
//    public double getPriority(double t, double[] y) throws EventException {
//        return 1.0D;
//    }
//
//    public String toString() {
//        return "SwitchingEvent{name=" + this.name + '}';
//    }
//
//    public Integer getId() {
//        return -(this.multiplicator + 1 + this.id);
//    }
//
//    public Collection<Integer> getIdsOfEventsWithModifiedPriority() {
//        return this.listOfModifiedPriorities;
//    }
//
//    public Collection<Integer> getIdsOfEventsWithModifiedTrigger() {
//        return this.listOfModifiedTriggers;
//    }
//
//    public void setIdsOfEventsWithModifiedPriority(Collection<Integer> list) {
//        this.listOfModifiedPriorities = list;
//    }
//
//    public void setIdsOfEventsWithModifiedTrigger(Collection<Integer> list) {
//        this.listOfModifiedTriggers = list;
//    }
//
//    public boolean isEnabled(double t, double[] y) {
//        boolean var10000;
//        label18:
//        {
//            this.vars.setValuesReference(y);
//            if (this.useCache) {
//                if (this.currentGuard.evaluateCache(this.vars)) {
//                    break label18;
//                }
//            } else if (this.currentGuard.evaluate(this.vars)) {
//                break label18;
//            }
//
//            if (!this.currentGuard.changeContinuously()) {
//                var10000 = false;
//                return var10000;
//            }
//        }
//
//        var10000 = true;
//        return var10000;
//    }
//}
