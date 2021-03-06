package eggloop.flow.numeric.rsm.gaussianprocesses.kernel;

/**
 * Created by ssilvetti on 05/01/17.
 */
public interface Kernel {

    void setHyp (double[] hyp);

    double[] getHyp ();

    double dot(double[] x, double[] y);

    double dotEuclideanDistance(double distance);

    double dotDistances(double[] distances);

}
