/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.utils;

import org.sbml.jsbml.ASTNode;
import com.eggloop.flow.simhya.simhya.matheval.Expression;
import com.eggloop.flow.simhya.simhya.model.store.faststore.*;
import com.eggloop.flow.simhya.simhya.model.store.*;
import java.util.*;
import com.eggloop.flow.simhya.simhya.model.flat.*;
import com.eggloop.flow.simhya.simhya.model.transition.*;
import com.eggloop.flow.simhya.simhya.matheval.SymbolArray;


/**
 *
 * @author Luca
 */
public class Examples {


    public  FlatModel randomWalkWithBarrierAndScheduledReset() {
        FlatModel model = new FlatModel("random_walk");
        FastStore store = new FastStore();
        Reset r,dr; Guard g,dg; Rate f; TimedActivation ft; Delay d;
        Transition t;

        store.addVariable("X", 0);
        store.addVariable("RT", 1);
        store.addParameter("b", 10.0);
        store.addParameter("d", 5.0);
        store.addParameter("k", 2.0);
        store.addParameter("res", 100.0);

        store.finalizeVariableInitialization();
        model.setStore(store);

        //add increase transition
        t = new Transition("increase",TType.STOCHASTIC);
        g = new Guard(new ConstantPredicate(true));
        f = new Rate(store.addFunction("k"));
        r = new Reset();
        //r.addAtomicReset(store.getVariableID("X"), store.addFunction("X+1"));
        r.addAtomicReset(store.getVariableID("X"),"X", new ConstantIncrementFunction(store,store.getVariableID("X"),1));
        t.setGuard(g);
        t.setRate(f);
        t.setReset(r);
        model.addTransition(t);

        //add decrease transition
        t = new Transition("decrease",TType.STOCHASTIC);
        g = new Guard(new ConstantPredicate(true));
        f = new Rate(store.addFunction("k"));
        r = new Reset();
        //r.addAtomicReset(store.getVariableID("X"), store.addFunction("X-1"));
        r.addAtomicReset(store.getVariableID("X"),"X", new ConstantIncrementFunction(store,store.getVariableID("X"),-1));
        t.setGuard(g);
        t.setRate(f);
        t.setReset(r);
        model.addTransition(t);


        //add barrier transition
        t = new Transition("upper_barrier",TType.INSTANTANEOUS);
        g = new Guard(store.addPredicate("X >= b"));
        f = new Rate(new ConstantFunction(1));
        r = new Reset();
        t.setGuard(g);
        t.setRate(f);
        t.setReset(r);
        t.setStopping(true);
        model.addTransition(t);

         //add lower barrier delayed transition
        t = new Transition("lower_barrier",TType.INSTANTANEOUS);
        g = new Guard(store.addPredicate("X == -b"));
        f = new Rate(new ConstantFunction(1));
        r = new Reset();
        r.addAtomicReset(store.getVariableID("X"),"X", new ConstantFunction(-1000));
        d = new Delay(store.addFunction("d"));
        dg =  new Guard(new ConstantPredicate(true));
        dr = new Reset();
        dr.addAtomicReset(store.getVariableID("X"),"X", store.addFunction("1000"));
        t.setGuard(g);
        t.setRate(f);
        t.setReset(r);
        t.setDelay(d, dg, dr);
        t.setStoppingAfterDelay(true);
        model.addTransition(t);

        //add timed reset to zero
        t = new Transition("zero_reset",TType.TIMED);
        g = new Guard(new ConstantPredicate(true));
        ft = new TimedActivation(store.addFunction("res*RT"));
        r = new Reset();
        r.addAtomicReset(store.getVariableID("X"),"X", new ConstantFunction(0));
        r.addAtomicReset(store.getVariableID("RT"),"RT", new ConstantIncrementFunction(store,store.getVariableID("RT"),1));
        t.setGuard(g);
        t.setTimedActivation(ft);
        t.setReset(r);
        model.addTransition(t);

        model.finalizeInitialization();
        return model;
    }


    public FlatModel simpleSelfNegDelayGeneNetwork(double initTrascriptionRate,
            double transcriptionDelay, double degRate, double feedbackStrength, double hillExponent) {
        
        FlatModel model = new FlatModel("delayed feedback loop");
        FastStore store = new FastStore();
        Reset r,dr; Guard g,dg; Rate f; TimedActivation ft; Delay d;
        Transition t;

        int X = store.addVariable("X", 0);
        store.addParameter("kp", initTrascriptionRate);
        store.addParameter("d", transcriptionDelay);
        int kd = store.addParameter("kd", degRate);
        store.addParameter("alpha", feedbackStrength);
        store.addParameter("n", hillExponent);
        
        store.finalizeVariableInitialization();
        model.setStore(store);

          //add lower barrier delayed transition
        t = new Transition("delayed production",TType.STOCHASTIC);
        g = new Guard(new ConstantPredicate(true));
        f = new Rate(store.addFunction("kp/(1+alpha*X^n)"));
        r = new Reset();
        d = new Delay(store.addFunction("d"));
        dg =  new Guard(new ConstantPredicate(true));
        dr = new Reset();
        dr.addAtomicReset(store.getVariableID("X"),"X", new ConstantIncrementFunction(store,X,1));
        t.setGuard(g);
        t.setRate(f);
        t.setReset(r);
        t.setDelay(d, dg, dr);
        model.addTransition(t);

        //add decrease transition
        t = new Transition("degradation",TType.STOCHASTIC);
        g = new Guard(new ConstantPredicate(true));
        f = new Rate(new LinearFunction(store,kd,X));
        r = new Reset();
        r.addAtomicReset(X,"X", new ConstantIncrementFunction(store,X,-1));
        t.setGuard(g);
        t.setRate(f);
        t.setReset(r);
        model.addTransition(t);

        model.finalizeInitialization();

       return model;
    }

//    public  FlatModel ProstateCancer(double N0,  boolean intermittent, boolean randomP, boolean randomRate, boolean hidden) {
//        //PARAMETRI DEL MODELLO
//        double V = 1e5; //volume times avogadro number
//        double omega = 1e5; 
//        
//        //INITIAL VALUE OF VARIABLES
//        double X = 15*N0;
//        double Y = 0.1*N0;
//        double Z = 12*V;
//        //drug administration
//        int U = 1;
//        //INITIAL PSA level, to be computed as X/N0 + Y/N0
//        double P = (X/N0 + Y/N0) * omega;
//        //hidden factor, can be zero or one
//        int H = 0;
//
//        //parameters
//        double alfax  = 0.0204;
//        double alfay = 0.0242;
//        double betax = 0.0076;
//        double betay = 0.0168;
//
//        double k1 = 0;
//        double k2 = 2*V;
//        double k3 = 8;
//        double k4 = 0.5*V;
//
//        double d = 1;
//        double m1 = 5e-5;
//        double Z0 = 20 * V;
//        double tau = 63.5;
//
//        double alfaz = Z0 / tau; // muSt be multiplied by (1-U)
//        double betaz = 1/tau;
//        double alfap = omega/N0;
//        double betap = 1;
//
//        
//        double deactivationThreshold = 4;
//        double activationThreshold = 10;
//        double controlTime = 28;
//
//        //size of random interval, standard multiplicative factor is 1,
//        //which must be contained in the interval.
//        double lowerBound = 0.5;
//        double upperBound = 1.5;
//
//       //hidden mechanism fluctuation
//        double hiddenOnValue = 2;
//        double hiddenRate = 2;
//
//
//        FlatModel model = new FlatModel("Prostate Cancer " + (intermittent ? "IAS " : "CAS ") +
//                (randomRate ? "random production " : "") + (hidden ? "hidden factor" : ""));
//        FastStore store = new FastStore();
//        Reset r; Guard g; Rate f; TimedActivation ft;
//        Transition t;
//
//        int idX = store.addVariable("X", X);
//        int idY = store.addVariable("Y", Y);
//        int idZ = store.addVariable("Z", Z);
//        int idU = store.addVariable("U", U);
//        int idP = store.addVariable("P", P);
//        int idH = store.addVariable("H", H);
//        int idC = store.addVariable("CONTROL", 1);
//
//        int idalfax = store.addParameter("alfax", alfax);
//        int idalfay = store.addParameter("alfay", alfay);
//        int idalfaz = store.addParameter("alfaz", alfaz);
//        int idalfap = store.addParameter("alfap", alfap);
//        int idbetax = store.addParameter("betax", betax);
//        int idbetay = store.addParameter("betay", betay);
//        int idbetaz = store.addParameter("betaz", betaz);
//        int idbetap = store.addParameter("betap", betap);
//        int idk1 = store.addParameter("k1", k1);
//        int idk2 = store.addParameter("k2", k2);
//        int idk3 = store.addParameter("k3", k3);
//        int idk4 = store.addParameter("k4", k4);
//        int idd = store.addParameter("d", d);
//        int iddm = store.addParameter("dm", 1);
//        int idm1 = store.addParameter("m1", m1);
//        int idZ0 = store.addParameter("Z0", Z0);
//
//        store.addParameter("deThr", deactivationThreshold);
//        store.addParameter("acThr", activationThreshold);
//        store.addParameter("cTime", controlTime);
//        int idrl = store.addParameter("randLB", lowerBound);
//        int idru = store.addParameter("randUB", upperBound);
//        store.addParameter("khidden", hiddenRate);
//        int idhon = store.addParameter("hiddenOn", hiddenOnValue);
//        
//        store.finalizeVariableInitialization();
//
//        model.setStore(store);
//        Function func;
//        //adding basic transitions
//
//
//
//        //growth of X
//        t = new Transition("growthX",TType.STOCHASTIC);
//        g = new Guard(new ConstantPredicate(true));
//        //f = new Rate(store.addFunction("alfax*(k1 + (1-k1)* Z/(Z+k2)) * X"));
//        func = new GFunction(store,idalfax,idk1,idk2,idX,idZ);
//        f = new Rate(func);
//        r = new Reset();
//        r.addAtomicReset(idX,"X", new ConstantIncrementFunction(store,idX,1));
//        if (!randomP)
//            r.addAtomicReset(idP,"P", new ConstantIncrementFunction(store,idP,alfap));
//        t.setGuard(g);
//        t.setRate(f);
//        t.setReset(r);
//        model.addTransition(t);
//
//        //death of X
//        t = new Transition("deathX",TType.STOCHASTIC);
//        g = new Guard(new ConstantPredicate(true));
//        //f = new Rate(store.addFunction("betax * (k3 + (1-k3) * Z / (Z+k4)) * X"));
//        func = new GFunction(store,idbetax,idk3,idk4,idX,idZ);
//        f = new Rate(func);
//        r = new Reset();
//        r.addAtomicReset(idX,"X", new ConstantIncrementFunction(store,idX,-1));
//        if (!randomP)
//            r.addAtomicReset(idP,"P", new ConstantIncrementFunction(store,idP,-alfap));
//        t.setGuard(g);
//        t.setRate(f);
//        t.setReset(r);
//        model.addTransition(t);
//
//        //growth of Y
//        t = new Transition("growthY",TType.STOCHASTIC);
//        g = new Guard(new ConstantPredicate(true));
//        //f = new Rate(store.addFunction("alfay * max(1-d*Z/Z0,0) * Y"));
//        func = new MFunction(store,idalfay,idd,idZ0,idY,idZ);
//        f = new Rate(func);
//        r = new Reset();
//        r.addAtomicReset(idY,"Y", new ConstantIncrementFunction(store,idY,1));
//        if (!randomP)
//            r.addAtomicReset(idP,"P", new ConstantIncrementFunction(store,idP,alfap));
//        t.setGuard(g);
//        t.setRate(f);
//        t.setReset(r);
//        model.addTransition(t);
//
//        //death of Y
//        t = new Transition("deathY",TType.STOCHASTIC);
//        g = new Guard(new ConstantPredicate(true));
//        f = new Rate(new LinearFunction(store,idbetay,idY));
//        r = new Reset();
//        r.addAtomicReset(idY,"Y", new ConstantIncrementFunction(store,idY,-1));
//        if (!randomP)
//            r.addAtomicReset(idP,"P", new ConstantIncrementFunction(store,idP,-alfap));
//        t.setGuard(g);
//        t.setRate(f);
//        t.setReset(r);
//        model.addTransition(t);
//
//        //mutation of X into Y
//        t = new Transition("mutXY",TType.STOCHASTIC);
//        g = new Guard(new ConstantPredicate(true));
//        //f = new Rate(store.addFunction("m1 * max(1-Z/Z0,0) * X"));
//        func = new MFunction(store,idm1,iddm,idZ0,idX,idZ);
//        f = new Rate(func);
//        r = new Reset();
//        r.addAtomicReset(idX,"X", new ConstantIncrementFunction(store,idX,-1));
//        r.addAtomicReset(idY,"Y", new ConstantIncrementFunction(store,idY,1));
//        t.setGuard(g);
//        t.setRate(f);
//        t.setReset(r);
//        model.addTransition(t);
//
//        //producton of Z
//        t = new Transition("prodZ",TType.STOCHASTIC);
//        g = new Guard(store.addPredicate("U == 1"));
//        f = new Rate(new ConstantFunction(alfaz));
//        r = new Reset();
//        r.addAtomicReset(idZ,"Z", new ConstantIncrementFunction(store,idZ,1));
//        t.setGuard(g);
//        t.setRate(f);
//        t.setReset(r);
//        model.addTransition(t);
//
//        //degradation of Z
//        t = new Transition("degZ",TType.STOCHASTIC);
//        g = new Guard(new ConstantPredicate(true));
//        f = new Rate(new LinearFunction(store,idbetaz,idZ));
//        r = new Reset();
//        r.addAtomicReset(idZ,"Z", new ConstantIncrementFunction(store,idZ,-1));
//        t.setGuard(g);
//        t.setRate(f);
//        t.setReset(r);
//        model.addTransition(t);
//
//
//        if (randomP) {
//            //producton of P
//            t = new Transition("prodP",TType.STOCHASTIC);
//            g = new Guard(new ConstantPredicate(true));
//            if (randomRate) {
//                //f = new Rate(store.addFunction("uniform(randLB,randUB) * alfap * (X + Y)"));
//                func = new PFunction(store,idalfap,idrl,idru,idX,idY);
//                f = new Rate(func);
//            } else {
//                //f = new Rate(store.addFunction("alfap * (X + Y)"));
//                func = new PFunction(store,idalfap,idX,idY);
//                f = new Rate(func);
//            }
//            r = new Reset();
//            r.addAtomicReset(idP,"P", new ConstantIncrementFunction(store,idP,1));
//            t.setGuard(g);
//            t.setRate(f);
//            t.setReset(r);
//            model.addTransition(t);
//
//            //degradation of P
//            t = new Transition("degP",TType.STOCHASTIC);
//            g = new Guard(new ConstantPredicate(true));
//            //f = new Rate(store.addFunction("(1+H) P"));
//            func = new PHFunction(store,idP,idH);
//            f = new Rate(func);
//            r = new Reset();
//            r.addAtomicReset(idP,"P", new ConstantIncrementFunction(store,idP,-1));
//            t.setGuard(g);
//            t.setRate(f);
//            t.setReset(r);
//            model.addTransition(t);
//        }
//
//        if (hidden) {
//            //hidden mechanism
//            t = new Transition("hidden",TType.STOCHASTIC);
//            g = new Guard(new ConstantPredicate(true));
//            f = new Rate(new ConstantFunction(hiddenRate));
//            r = new Reset();
//            //r.addAtomicReset(idH, store.addFunction("if(H>0,0,hiddenOn)"));
//            func = new HFunction(store,idhon,idH);
//            r.addAtomicReset(idH,"H", func);
//            t.setGuard(g);
//            t.setRate(f);
//            t.setReset(r);
//            model.addTransition(t);
//        }
//
//        if (intermittent) {
//            t = new Transition("intermittent_check_on",TType.TIMED);
//            g = new Guard(store.addPredicate("U == 1"));
//            ft = new TimedActivation(store.addFunction("cTime CONTROL"));
//            r = new Reset();
//            r.addAtomicReset(idU,"U", store.addFunction("if(P <= deThr,0,1)"));
//            r.addAtomicReset(idC,"C", new ConstantIncrementFunction(store,idC,1));
//            t.setGuard(g);
//            t.setTimedActivation(ft);
//            t.setReset(r);
//            model.addTransition(t);
//            
//            t = new Transition("intermittent_check_off",TType.TIMED);
//            g = new Guard(store.addPredicate("U == 0"));
//            ft = new TimedActivation(store.addFunction("cTime CONTROL"));
//            r = new Reset();
//            r.addAtomicReset(idU,"U", store.addFunction("if(P >= acThr,1,0)"));
//            r.addAtomicReset(idC,"C", new ConstantIncrementFunction(store,idC,1));
//            t.setGuard(g);
//            t.setTimedActivation(ft);
//            t.setReset(r);
//            model.addTransition(t);
//        }
//        model.finalizeInitialization();
//        return model;
//    }

    public  FlatModel SIR(int N, double infectionRate, double recoveryRate, double susceptibleRate) {
        FlatModel model = new FlatModel("SIR");
        FastStore store = new FastStore();
        Reset r; Guard g; Rate f; TimedActivation ft;
        Transition t;

        int S = store.addVariable("S", N-1);
        int I = store.addVariable("I", 1);
        int R = store.addVariable("R", 0);
      
        int ki = store.addParameter("ki", infectionRate);
        int kr = store.addParameter("kr", recoveryRate);
        int ks = store.addParameter("ks", susceptibleRate);

        store.finalizeVariableInitialization();
        model.setStore(store);



        //infection
        t = new Transition("infection",TType.STOCHASTIC);
        g = new Guard(new ConstantPredicate(true));
        f = new Rate(new MassActionFunction(store,ki,S,I));
        r = new Reset();
        r.addAtomicReset(S,"S", new ConstantIncrementFunction(store,S,-1));
        r.addAtomicReset(I,"I", new ConstantIncrementFunction(store,I,1));
        t.setGuard(g);
        t.setRate(f);
        t.setReset(r);
        model.addTransition(t);

         //slow recovery
        t = new Transition("recovery",TType.STOCHASTIC);
        g = new Guard(new ConstantPredicate(true));
        f = new Rate(new LinearFunction(store,kr,I));
        r = new Reset();
        r.addAtomicReset(R,"R", new ConstantIncrementFunction(store,R,1));
        r.addAtomicReset(I,"I", new ConstantIncrementFunction(store,I,-1));
        t.setGuard(g);
        t.setRate(f);
        t.setReset(r);
        model.addTransition(t);

      

        //back to susceptible
        t = new Transition("susceptible",TType.STOCHASTIC);
        g = new Guard(new ConstantPredicate(true));
        f = new Rate(new LinearFunction(store,ks,R));
        r = new Reset();
        r.addAtomicReset(S,"S", new ConstantIncrementFunction(store,S,1));
        r.addAtomicReset(R,"R", new ConstantIncrementFunction(store,R,-1));
        t.setGuard(g);
        t.setRate(f);
        t.setReset(r);
        model.addTransition(t);


        model.finalizeInitialization();
        return model;
    }

    public  FlatModel SIReval(int N, double infectionRate, double recoveryRate, double susceptibleRate, boolean useFastIncrement) {
        FlatModel model = new FlatModel("SIR");
        FastStore store = new FastStore();
        Reset r; Guard g; Rate f; 
        Transition t;

        int S = store.addVariable("S", N-1);
        int I = store.addVariable("I", 1);
        int R = store.addVariable("R", 0);

        int ki = store.addParameter("ki", infectionRate);
        int kr = store.addParameter("kr", recoveryRate);
        int ks = store.addParameter("ks", susceptibleRate);

        store.finalizeVariableInitialization();
        model.setStore(store);



        //infection
        t = new Transition("infection",TType.STOCHASTIC);
        g = new Guard(new ConstantPredicate(true));
        f = new Rate(store.addFunction("ki*S*I"));
        r = new Reset();
        if (useFastIncrement)
            r.addAtomicReset(S,"S", new ConstantIncrementFunction(store,S,-1));
        else
            r.addAtomicReset(S,"S", store.addFunction("S-1"));
        if (useFastIncrement)
            r.addAtomicReset(I,"I", new ConstantIncrementFunction(store,I,1));
        else
            r.addAtomicReset(I,"I", store.addFunction("I+1"));
        t.setGuard(g);
        t.setRate(f);
        t.setReset(r);
        model.addTransition(t);

         //slow recovery
        t = new Transition("recovery",TType.STOCHASTIC);
        g = new Guard(new ConstantPredicate(true));
        f = new Rate( store.addFunction("kr*I"));
        r = new Reset();
        if (useFastIncrement)
            r.addAtomicReset(R,"R", new ConstantIncrementFunction(store,R,1));
        else r.addAtomicReset(R,"R", store.addFunction("R+1"));
        if (useFastIncrement)
            r.addAtomicReset(I,"I", new ConstantIncrementFunction(store,I,-1));
        else r.addAtomicReset(I,"I", store.addFunction("I-1"));
        t.setGuard(g);
        t.setRate(f);
        t.setReset(r);
        model.addTransition(t);



        //back to susceptible
        t = new Transition("susceptible",TType.STOCHASTIC);
        g = new Guard(new ConstantPredicate(true));
        f = new Rate(store.addFunction("ks*R"));
        r = new Reset();
        if (useFastIncrement)
        r.addAtomicReset(S,"S", new ConstantIncrementFunction(store,S,1));
        else r.addAtomicReset(S,"S", store.addFunction("S+1"));
        if (useFastIncrement)
        r.addAtomicReset(R,"R", new ConstantIncrementFunction(store,R,-1));
        else r.addAtomicReset(R,"R", store.addFunction("R-1"));
        t.setGuard(g);
        t.setRate(f);
        t.setReset(r);
        model.addTransition(t);


        model.finalizeInitialization();
        return model;
    }

    public  FlatModel SIRtwoRecoveryRatesHisteresis(int N, double infectionRate, double slowRecoveryRate,
            double fastRecoveryRate, double susceptibleRate, double slowToFastThreshold,
            double fastToSlowThreshold) {
        FlatModel model = new FlatModel("SIR with two recovery rates");
        FastStore store = new FastStore();
        Reset r; Guard g; Rate f; TimedActivation ft;
        int id;
        Transition t;

        store.addVariable("S", N-1);
        store.addVariable("I", 1);
        store.addVariable("R", 0);
        store.addVariable("STATE", 0);

        store.addParameter("ki", infectionRate);
        store.addParameter("krs", slowRecoveryRate);
        store.addParameter("krf", fastRecoveryRate);
        store.addParameter("ks", susceptibleRate);
        store.addParameter("stf", slowToFastThreshold);
        store.addParameter("fts", fastToSlowThreshold);
        store.addParameter("N", N);

         store.finalizeVariableInitialization();
       
        model.setStore(store);

        //infection
        t = new Transition("infection",TType.STOCHASTIC);
        g = new Guard(new ConstantPredicate(true));
        f = new Rate(store.addFunction("ki*S*I"));
        r = new Reset();
        id = store.getVariableID("S");
        r.addAtomicReset(id,"S", new ConstantIncrementFunction(store,id,-1));
        id = store.getVariableID("I");
        r.addAtomicReset(id,"I", new ConstantIncrementFunction(store,id,1));
        t.setGuard(g);
        t.setRate(f);
        t.setReset(r);
        model.addTransition(t);

         //slow recovery
        t = new Transition("slow_recovery",TType.STOCHASTIC);
        g = new Guard(new ConstantPredicate(true));
        f = new Rate(store.addFunction("krs*I*(1-STATE)"));
        r = new Reset();
        id = store.getVariableID("R");
        r.addAtomicReset(id,"R", new ConstantIncrementFunction(store,id,1));
        id = store.getVariableID("I");
        r.addAtomicReset(id,"I", new ConstantIncrementFunction(store,id,-1));
        t.setGuard(g);
        t.setRate(f);
        t.setReset(r);
        model.addTransition(t);

         //fast recovery
        t = new Transition("fast_recovery",TType.STOCHASTIC);
        g = new Guard(new ConstantPredicate(true));
        f = new Rate(store.addFunction("krf*I*STATE"));
        r = new Reset();
        id = store.getVariableID("R");
        r.addAtomicReset(id,"R", new ConstantIncrementFunction(store,id,1));
        id = store.getVariableID("I");
        r.addAtomicReset(id,"I", new ConstantIncrementFunction(store,id,-1));
        t.setGuard(g);
        t.setRate(f);
        t.setReset(r);
        model.addTransition(t);

        //back to susceptible
        t = new Transition("susceptible",TType.STOCHASTIC);
        g = new Guard(new ConstantPredicate(true));
        f = new Rate(store.addFunction("ks*R"));
        r = new Reset();
        id = store.getVariableID("S");
        r.addAtomicReset(id,"S", new ConstantIncrementFunction(store,id,1));
        id = store.getVariableID("R");
        r.addAtomicReset(id,"R", new ConstantIncrementFunction(store,id,-1));
        t.setGuard(g);
        t.setRate(f);
        t.setReset(r);
        model.addTransition(t);

      

        //add slow to fast transition
        t = new Transition("slow_to_fast",TType.INSTANTANEOUS);
        g = new Guard(store.addPredicate("I >= stf*N && STATE == 0"));
        f = new Rate(new ConstantFunction(1));
        r = new Reset();
        id = store.getVariableID("STATE");
        r.addAtomicReset(id,"STATE", new ConstantFunction(1));
        t.setGuard(g);
        t.setRate(f);
        t.setReset(r);
        model.addTransition(t);

        //add fast to slow
        t = new Transition("fast_to_slow",TType.INSTANTANEOUS);
        g = new Guard(store.addPredicate("I <= fts*N && STATE == 1"));
        f = new Rate(new ConstantFunction(1));
        r = new Reset();
        id = store.getVariableID("STATE");
        r.addAtomicReset(id,"STATE", new ConstantFunction(0));
        t.setGuard(g);
        t.setRate(f);
        t.setReset(r);
        model.addTransition(t);

        model.finalizeInitialization();
        return model;
    }



//    private class GFunction implements Function {
//        int alpha, k1, k2, X, Z;
//        private Store store;
//        private double[] variables;
//        private double[] parameters;
//
//        public GFunction(Store store, int alpha, int k1, int k2, int X, int Z) {
//            this.store = store;
//            this.alpha = alpha;
//            this.k1 = k1;
//            this.k2 = k2;
//            this.X = X;
//            this.Z = Z;
//            this.store.addExternalFunction(this);
//        }
//
//    public void initialize() {
//        this.variables = store.getVariablesValues();
//        this.parameters = store.getParametersValues();
//    }
//
//     
//        public double compute() {
//            return parameters[alpha] * ( parameters[k1] +
//                    (1-parameters[k1]) * variables[Z] /
//                    ((variables[Z] + parameters[k2]))) * variables[X];
//        }
//
//        public double compute(SymbolArray vars) {
//           return parameters[alpha] * ( parameters[k1] +
//                    (1-parameters[k1]) * vars.getValue(Z) /
//                    (( vars.getValue(Z) + parameters[k2]))) *  vars.getValue(X);
//        }
//
//        public double computeCache() {
//            return parameters[alpha] * ( parameters[k1] +
//                    (1-parameters[k1]) * variables[Z] /
//                    ((variables[Z] + parameters[k2]))) * variables[X];
//        }
//
//        public double computeCache(SymbolArray vars) {
//           return parameters[alpha] * ( parameters[k1] +
//                    (1-parameters[k1]) * vars.getValue(Z) /
//                    (( vars.getValue(Z) + parameters[k2]))) *  vars.getValue(X);
//        }
//
//        
//        public ArrayList<Integer> getVariableList() {
//            ArrayList<Integer> list = new ArrayList();
//            list.add(X);
//            list.add(Z);
//            return list;
//        }
//
//        public String toModelLanguage() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public double getConstantIncrement() {
//            throw new UnsupportedOperationException("This function is not a constant increment function,"
//                    + "please call isConstantIncrementFunction() before this function");
//        }
//
//        public boolean isConstantIncrementFunction() {
//            return false;
//        }
//
//        public Function substitute(ArrayList<String> varNames, ArrayList<Expression> expressions) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public ASTNode convertToJSBML() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        
//
//
//
//    }
//
//    private class MFunction implements Function {
//        int alpha, d, Z0, X, Z;
//        private Store store;
//       private double[] variables;
//       private double[] parameters;
//
//        public MFunction(Store store, int alpha, int d, int Z0, int X, int Z) {
//            this.store = store;
//            this.store.addExternalFunction(this);
//            this.alpha = alpha;
//            this.d = d;
//            this.Z0 = Z0;
//            this.X = X;
//            this.Z = Z;
//
//        }
//
//        public void initialize() {
//         this.variables = store.getVariablesValues();
//         this.parameters = store.getParametersValues();
//    }
//
//        public double compute(SymbolArray vars) {
//             return parameters[alpha] * Math.max( 1 - parameters[d] *
//                  vars.getValue(Z) /  parameters[Z0],0) * vars.getValue(X);
//        }
//
//        @Override
//        public double compute() {
//            return parameters[alpha] * Math.max( 1 - parameters[d] *
//                  variables[Z] /  parameters[Z0],0) * variables[X];
//        }
//
//        public double computeCache(SymbolArray vars) {
//             return parameters[alpha] * Math.max( 1 - parameters[d] *
//                  vars.getValue(Z) /  parameters[Z0],0) * vars.getValue(X);
//        }
//
//        @Override
//        public double computeCache() {
//            return parameters[alpha] * Math.max( 1 - parameters[d] *
//                  variables[Z] /  parameters[Z0],0) * variables[X];
//        }
//
//        @Override
//        public ArrayList<Integer> getVariableList() {
//            ArrayList<Integer> list = new ArrayList();
//            list.add(X);
//            list.add(Z);
//            return list;
//        }
//
//        public String toModelLanguage() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public double getConstantIncrement() {
//            throw new UnsupportedOperationException("This function is not a constant increment function,"
//                    + "please call isConstantIncrementFunction() before this function");
//        }
//
//        public boolean isConstantIncrementFunction() {
//            return false;
//        }
//
//        public Function substitute(ArrayList<String> varNames, ArrayList<Expression> expressions) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//        public ASTNode convertToJSBML() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//    }
//
//     private class PFunction implements Function {
//        int alpha, b1, b2, X, Y;
//        boolean random;
//        private Store store;
//        private double[] variables;
//        private double[] parameters;
//
//        public PFunction(Store store, int alpha, int b1, int b2, int X, int Y) {
//            this.store = store;
//            this.store.addExternalFunction(this);
//            this.alpha = alpha;
//            this.b1 = b1;
//            this.b2 = b2;
//            this.X = X;
//            this.Y = Y;
//            random = true;
//
//        }
//
//
//        public void initialize() {
//            this.variables = store.getVariablesValues();
//            this.parameters = store.getParametersValues();
//        }
//         public PFunction(Store store, int alpha, int X, int Y) {
//            this.alpha = alpha;
//            this.X = X;
//            this.Y = Y;
//            random = false;
//
//        }
//
//         public double compute(SymbolArray vars) {
//            return parameters[alpha] * (vars.getValue(X) + vars.getValue(Y) ) *
//                    (random ? parameters[b1] + RandomGenerator.nextDouble() *
//                    (parameters[b2]-parameters[b1]) : 1);
//        }
//
//        @Override
//        public double compute() {
//            return parameters[alpha] * (variables[X] + variables[Y] ) *
//                    (random ? parameters[b1] + RandomGenerator.nextDouble() *
//                    (parameters[b2]-parameters[b1]) : 1);
//        }
//
//         public double computeCache(SymbolArray vars) {
//            return parameters[alpha] * (vars.getValue(X) + vars.getValue(Y) ) *
//                    (random ? parameters[b1] + RandomGenerator.nextDouble() *
//                    (parameters[b2]-parameters[b1]) : 1);
//        }
//
//        @Override
//        public double computeCache() {
//            return parameters[alpha] * (variables[X] + variables[Y] ) *
//                    (random ? parameters[b1] + RandomGenerator.nextDouble() *
//                    (parameters[b2]-parameters[b1]) : 1);
//        }
//
//        @Override
//        public ArrayList<Integer> getVariableList() {
//            ArrayList<Integer> list = new ArrayList();
//            list.add(X);
//            list.add(Y);
//            return list;
//        }
//
//        public String toModelLanguage() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public double getConstantIncrement() {
//            throw new UnsupportedOperationException("This function is not a constant increment function,"
//                    + "please call isConstantIncrementFunction() before this function");
//        }
//
//        public boolean isConstantIncrementFunction() {
//            return false;
//        }
//
//        public Function substitute(ArrayList<String> varNames, ArrayList<Expression> expressions) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        
//        public ASTNode convertToJSBML() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//    }
//
//     private class PHFunction implements Function {
//        int P,H;
//        private Store store;
//        private double[] variables;
//        private double[] parameters;
//
//        public PHFunction(Store store, int P, int H) {
//            this.store = store;
//            this.store.addExternalFunction(this);
//            this.P = P;
//            this.H = H;
//        }
//
//        public void initialize() {
//            this.variables = store.getVariablesValues();
//            this.parameters = store.getParametersValues();
//        }
//
//        @Override
//        public double compute() {
//            return ( 1 + variables[H] ) * variables[P];
//        }
//
//        public double compute(SymbolArray vars) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//         @Override
//        public double computeCache() {
//            return ( 1 + variables[H] ) * variables[P];
//        }
//
//        public double computeCache(SymbolArray vars) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public ArrayList<Integer> getVariableList() {
//            ArrayList<Integer> list = new ArrayList();
//            list.add(P);
//            list.add(H);
//            return list;
//        }
//
//        public String toModelLanguage() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public double getConstantIncrement() {
//            throw new UnsupportedOperationException("This function is not a constant increment function,"
//                    + "please call isConstantIncrementFunction() before this function");
//        }
//
//        public boolean isConstantIncrementFunction() {
//            return false;
//        }
//
//
//        public Function substitute(ArrayList<String> varNames, ArrayList<Expression> expressions) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//        public ASTNode convertToJSBML() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//    }
//
//      private class HFunction implements Function {
//        int k,H;
//        Store store;
//        private double[] variables;
//        private double[] parameters;
//
//        public HFunction(Store store, int k, int H) {
//            this.store = store;
//            this.store.addExternalFunction(this);
//            this.k = k;
//            this.H = H;
//        }
//
//        public void initialize() {
//            this.variables = store.getVariablesValues();
//            this.parameters = store.getParametersValues();
//        }
//
//        @Override
//        public double compute() {
//            return ( variables[H] > 0 ? 0 : parameters[k]);
//        }
//
//        public double compute(SymbolArray vars) {
//            return ( vars.getValue(H) > 0 ? 0 : parameters[k]);
//        }
//
//        @Override
//        public double computeCache() {
//            return ( variables[H] > 0 ? 0 : parameters[k]);
//        }
//
//        public double computeCache(SymbolArray vars) {
//            return ( vars.getValue(H) > 0 ? 0 : parameters[k]);
//        }
//
//        @Override
//        public ArrayList<Integer> getVariableList() {
//            ArrayList<Integer> list = new ArrayList();
//            list.add(H);
//            return list;
//        }
//
//        public String toModelLanguage() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public double getConstantIncrement() {
//            throw new UnsupportedOperationException("This function is not a constant increment function,"
//                    + "please call isConstantIncrementFunction() before this function");
//        }
//
//        public boolean isConstantIncrementFunction() {
//            return false;
//        }
//
//        public Function substitute(ArrayList<String> varNames, ArrayList<Expression> expressions) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        
//        public ASTNode convertToJSBML() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//    }

}


