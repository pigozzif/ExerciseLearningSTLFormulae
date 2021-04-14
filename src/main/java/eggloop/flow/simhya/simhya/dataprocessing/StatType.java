/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.dataprocessing;

/**
 *
 * @author Luca
 */
public enum StatType {
        AVERAGE, VARIANCE, STDDEV, CV, FANO, SKEW, KURTOSIS, STDERR;

    @Override
    public String toString() {
        switch(this) {
            case AVERAGE:
                return "average";
            case VARIANCE:
                return "variance";
            case STDDEV:
                return "standard deviation";
            case CV:
                return "coefficient of variation";
            case FANO:
                return "Fano factor";
            case SKEW:
                return "skew";
            case KURTOSIS:
                return "kurtosis";
            case STDERR:
                return "standard error";
            default:
                return "";
        }
    }
}
