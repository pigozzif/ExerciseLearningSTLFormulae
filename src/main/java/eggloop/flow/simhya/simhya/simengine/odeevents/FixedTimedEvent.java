package eggloop.flow.simhya.simhya.simengine.odeevents;

import org.apache.commons.math.ode.events.EventException;
//import org.apache.commons.math.ode.events.TimedEventHandler;
import eggloop.flow.simhya.simhya.dataprocessing.EventDataCollector;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import eggloop.flow.simhya.simhya.model.store.Function;
import eggloop.flow.simhya.simhya.model.store.Predicate;
import eggloop.flow.simhya.simhya.model.transition.AtomicReset;
import eggloop.flow.simhya.simhya.simengine.GuardChecker;
import eggloop.flow.simhya.simhya.simengine.ProgressMonitor;
import org.apache.commons.math.ode.events.EventHandler;

import java.util.Collection;

public class FixedTimedEvent implements EventHandler,PrintableEvent {
    int id;
    private Function firingTime;
    private Predicate guard;
    private SymbolArray vars;
    private AtomicReset[] resets;
    private boolean isStopping;
    private boolean isGuarded;
    private int dimension;
    private boolean useCache = false;
    private EventDataCollector collector;
    String name = "Fixed Time event";
    private boolean printEvent;
    private ProgressMonitor monitor;
    private Collection<Integer> listOfModifiedTriggers;
    private Collection<Integer> listOfModifiedPriorities;
    private GuardChecker odeFunction;

    public FixedTimedEvent(int id, int dimension, Function firingTime, GuardChecker ode) {
        this.id = id;
        this.odeFunction = ode;
        this.firingTime = firingTime;
        this.dimension = dimension;
        this.guard = null;
        this.isGuarded = false;
        this.isStopping = false;
        this.resets = new AtomicReset[0];
        this.vars = SymbolArray.getNewFastEvalSymbolArray(dimension);
        this.collector = null;
        this.printEvent = false;
        this.monitor = null;
        this.listOfModifiedTriggers = null;
        this.listOfModifiedPriorities = null;
    }

    public void setPrintEvent(ProgressMonitor monitor) {
        if (monitor != null) {
            this.monitor = monitor;
            this.printEvent = true;
        } else {
            this.monitor = null;
            this.printEvent = false;
        }

    }

    public void setCollectorInfo(String name, EventDataCollector collector) {
        this.collector = collector;
        this.name = name;
    }

    public void setGuard(Predicate guard) {
        this.guard = guard;
        this.isGuarded = guard != null && !guard.isTautology();
    }

    public void setIsStopping(boolean isStopping) {
        this.isStopping = isStopping;
    }

    public void setReset(AtomicReset[] resets) {
        this.resets = resets != null ? resets : new AtomicReset[0];
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    public double getNextEventTime(double t, double[] y, boolean forward) {
        this.vars.setValuesReference(y);
        double et;
        if (this.isGuarded) {
            boolean g = this.useCache ? this.guard.evaluateCache(this.vars) : this.guard.evaluate(this.vars);
            if (g) {
                et = this.useCache ? this.firingTime.computeCache(this.vars) : this.firingTime.compute(this.vars);
            } else {
                et = forward ? 1.0D / 0.0 : -1.0D / 0.0;
            }
        } else {
            et = this.useCache ? this.firingTime.computeCache(this.vars) : this.firingTime.compute(this.vars);
        }

        if (forward && et < t) {
            et = 1.0D / 0.0;
        }

        if (!forward && et > t) {
            et = -1.0D / 0.0;
        }

        return et;
    }

    public int eventOccurred(double t, double[] y, boolean increasing) throws EventException {
        if (this.printEvent) {
            this.monitor.signalEvent(this.name, t);
        }

        if (this.collector != null) {
            this.collector.putNextTimedEvent(t, y, this.name);
        }

        return this.isStopping ? 0 : 1;
    }


    public double g(double t, double[] y) throws EventException {
        this.vars.setValuesReference(y);
        double x;
        if (this.isGuarded) {
            boolean g = this.useCache ? this.guard.evaluateCache(this.vars) : this.guard.evaluate(this.vars);
            if (g) {
                x = this.useCache ? this.firingTime.computeCache(this.vars) : this.firingTime.compute(this.vars);
            } else {
                x = 1.0D / 0.0;
            }
        } else {
            x = this.useCache ? this.firingTime.computeCache(this.vars) : this.firingTime.compute(this.vars);
        }

        return t - x;
    }

    public void resetState(double t, double[] y) throws EventException {
        this.vars.setValuesReference(y);
        AtomicReset[] arr$ = this.resets;
        int len$ = arr$.length;

        int i$;
        AtomicReset r;
        for (i$ = 0; i$ < len$; ++i$) {
            r = arr$[i$];
            if (this.useCache) {
                r.computeNewValuesCache(this.vars);
            } else {
                r.computeNewValues(this.vars);
            }
        }

        arr$ = this.resets;
        len$ = arr$.length;

        for (i$ = 0; i$ < len$; ++i$) {
            r = arr$[i$];
            r.updateStoreVariables(y);
        }

        this.odeFunction.updateGuardStatus(this.id, t, y);
    }

    public double getPriority(double t, double[] y) throws EventException {
        return 1.0D;
    }

    public String toString() {
        return "FixedTimedEvent{name=" + this.name + '}';
    }

    public Integer getId() {
        return this.id;
    }

    public Collection<Integer> getIdsOfEventsWithModifiedPriority() {
        return this.listOfModifiedPriorities;
    }

    public Collection<Integer> getIdsOfEventsWithModifiedTrigger() {
        return this.listOfModifiedTriggers;
    }

    public boolean isEnabled(double t, double[] y) {
        if (this.isGuarded) {
            this.vars.setValuesReference(y);
            return this.useCache ? this.guard.evaluateCache(this.vars) : this.guard.evaluate(this.vars);
        } else {
            return true;
        }
    }

    public void setIdsOfEventsWithModifiedPriority(Collection<Integer> list) {
        this.listOfModifiedPriorities = list;
    }

    public void setIdsOfEventsWithModifiedTrigger(Collection<Integer> list) {
        this.listOfModifiedTriggers = list;
    }
}
