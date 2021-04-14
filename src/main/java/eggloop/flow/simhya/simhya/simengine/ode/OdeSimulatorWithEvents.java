package eggloop.flow.simhya.simhya.simengine.ode;///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package eggloop.flow.simhya.simhya.simengine.ode;
//
//import eggloop.flow.simhya.simhya.dataprocessing.OdeDataCollector;
//import eggloop.flow.simhya.simhya.model.flat.FlatModel;
//import eggloop.flow.simhya.simhya.simengine.SimulationException;
//import eggloop.flow.simhya.simhya.model.store.Function;
//import eggloop.flow.simhya.simhya.model.store.Predicate;
//import eggloop.flow.simhya.simhya.model.transition.Transition;
//import java.util.ArrayList;
//import eggloop.flow.simhya.simhya.simengine.*;
//import java.util.HashSet;
//
//import eggloop.flow.simhya.simhya.simengine.odeevents.FixedTimedEvent;
//import eggloop.flow.simhya.simhya.simengine.odeevents.PrintableEvent;
//import org.apache.commons.math.ode.events.EventHandler;
//
///**
// *
// * @author luca
// */
//public class OdeSimulatorWithEvents extends AbstractOdeSimulator {
//    ArrayList<PrintableEvent> eventsForPrinting = null;
//    ArrayList<FixedTimedEvent> events = null;
//    HashSet<Integer> eventIds;
//
//    public OdeSimulatorWithEvents(FlatModel model, OdeDataCollector collector) {
//        super(model, collector);
//        if (model.containsNonContinuouslyApproximableStochasticTransitions())
//            throw new SimulationException("There are stochastic transitions not continously approximable, use hybrid simulator");
//        if (model.isLinearNoiseApproximation())
//            throw new SimulationException("There are instantaneous or guarded events. Cannot run linear noise analysis. Please remove these events.");
//        if (model.containsContinuousGuardedTransitions(true))
//            this.function = new GuardedOdeFunction(model);
//        else
//            this.function = new PureOdeFunction(model);
//    }
//
//
//
//    private void initializeDependencyGraphs() {
//        for (FixedTimedEvent e : this.events) {
//            int id = e.getId();
//            HashSet<Integer> updated = new HashSet<Integer>();
//            if (id >= 0) {
//                //updated triggers. check guards of all transitions plus firint times.
//                //add switched depending on updated variables.
//                Integer[] upGuard = model.getListOfUpdatedGuards(id);
//                for (Integer i : upGuard)
//                    if (this.eventIds.contains(i))
//                        updated.add(i);
//                Integer[] upFT = model.getListOfUpdatedFiringTimes(id);
//                for (Integer i : upFT)
//                    if (this.eventIds.contains(i))
//                        updated.add(i);
//                e.setIdsOfEventsWithModifiedTrigger(updated);
//                //updated priorities of inst transitions.
//                updated = new HashSet<Integer>();
//                model.getListOfUpdatedRates(id);
//                HashSet<Integer> instTr = new HashSet<Integer>();
//                for (Integer j : model.getListOfInstantaneousTransitionsID())
//                    instTr.add(j);
//                for (Integer i : model.getListOfUpdatedRates(id))
//                    if (instTr.contains(i))
//                        updated.add(i);
//                e.setIdsOfEventsWithModifiedPriority(updated);
//            } else {
//                e.setIdsOfEventsWithModifiedTrigger(new ArrayList<Integer>());
//                e.setIdsOfEventsWithModifiedPriority(new ArrayList<Integer>());
//            }
//        }
//    }
//
//
//
//    private void addEventHandlers() {
//        this.eventsForPrinting = new ArrayList<>();
//        this.events = new ArrayList<> ();
//        this.eventIds = new HashSet<Integer>();
//
//        for (Integer j : model.getListOfInstantaneousTransitionsID()) {
//            Transition t = model.getTransition(j);
//            Predicate g = t.getGuardPredicate();
//            if (g.canBeConvertedToFunction()) {
//                Function f = g.convertToFunction();
//                InstantaneousEvent e = new InstantaneousEvent(j,function.dimension,g,f,t.getRateFunction(),function);
//                e.setReset(t.getResetList());
//                e.setIsStopping(t.isStopping());
//                e.setUseCache(useCache);
//                e.setCollectorInfo(t.getEvent(), collector);
//                events.add(e);
//                eventsForPrinting.add(e);
//                eventIds.add(j);
//            } else {
//                throw new SimulationException("Guard of instantaneous transition " +
//                        t.getEvent() + " cannot be converted to a function. " +
//                        "It probably contains equality or inequality atomic predicates.");
//            }
//        }
//        for (Integer j : model.getListOfTimedTransitionsID()) {
//            Transition t = model.getTransition(j);
//            Predicate g = t.getGuardPredicate();
//            Function f = t.getTimedActivationFunction();
//            if (function.evolvesContinuously(g) || function.evolvesContinuously(f)) {
//                TimedEvent e = new TimedEvent(j,function.dimension,f,function);
//                e.setGuard(g);
//                e.setReset(t.getResetList());
//                e.setIsStopping(t.isStopping());
//                e.setUseCache(useCache);
//                e.setCollectorInfo(t.getEvent(), collector);
//                events.add(e);
//                eventsForPrinting.add(e);
//                eventIds.add(j);
//            } else {
//                FixedTimedEvent e = new FixedTimedEvent(j,function.dimension,f,function);
//                e.setGuard(g);
//                e.setReset(t.getResetList());
//                e.setIsStopping(t.isStopping());
//                e.setUseCache(useCache);
//                e.setCollectorInfo(t.getEvent(), collector);
//                events.add(e);
//                eventsForPrinting.add(e);
//                eventIds.add(j);
//
//            }
//        }
//        initializeDependencyGraphs();
//        //add events to the integrator
//        for (EventHandler e : this.events)
//            integrator.addEventHandler(e, this.maxStepIncrementForEvents,
//                        this.maxErrorForEvents, this.maxIterationforEvents);
//    }
//
//
//    public void initialize() {
//        integrator = super.newIntegrator();
//        integrator.addStepHandler(this);
//        //add event handlers.
//        addEventHandlers();
//        initialized = true;
//    }
//
//    public void reinitialize() {
//        integrator = super.newIntegrator();
//        integrator.addStepHandler(this);
//        //add event handlers.
//        addEventHandlers();
//        initialized = true;
//    }
//
//       /**
//     * Activate or deactivates the mechanism to print events.
//     * @param print
//     */
//    @Override
//    public void printEvents(boolean print) {
//        for (PrintableEvent e : this.eventsForPrinting) {
//           e.setPrintEvent((print ? monitor : null));
//        }
//    }
//
//}
