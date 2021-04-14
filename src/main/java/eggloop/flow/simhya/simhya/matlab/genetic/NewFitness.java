package eggloop.flow.simhya.simhya.matlab.genetic;

public class NewFitness  implements FitnessFunction  {
    @Override
    public double compute(double p1, double p2, int size, double undef1, double undef2, int runs) {
//        double a = 0;
//        double b = 0;
//        double res =0;
//        if (p1<p2){
//            res = -(p1+undef1)+p2-undef2;
//        }else{
//            res = -(p2+undef2)+p1-undef1;
//        }
//        return -Math.abs(((p1+p2)/2));
////        if((p1-3*undef1)*(p1-3*undef1)<0){
////            a =  Math.min(Math.abs(p1-3*undef1),Math.abs(p1+3*undef1));
////        }
////        if((p2-3*undef2)*(p2-3*undef2)<0){
////            b =  Math.min(Math.abs(p2-3*undef2),Math.abs(p2+3*undef2));
////        }
////        return a/undef1+b/undef2;

       //double a= CNDF(-p1/Math.sqrt(undef1))+1-(CNDF(-p2/Math.sqrt(undef2)));
       //double b= CNDF(-p2/Math.sqrt(undef2))+1-(CNDF(-p1/Math.sqrt(undef1)));
//        if(p1>0 & p2<0){
//            return p1-p2;
//        }
//       else return -Math.abs(p1+p2);


        return (Math.abs(p1 - p2) / Math.abs(3 * (undef1 + undef2)))- (Math.abs(p1 - p2) / Math.abs(3 * (undef1 + undef2)))*0.05*(GeneticOptions.size_penalty_coefficient*size);
    }

    double CNDF(double x)
    {
        int neg = (x < 0d) ? 1 : 0;
        if ( neg == 1)
            x *= -1d;

        double k = (1d / ( 1d + 0.2316419 * x));
        double y = (((( 1.330274429 * k - 1.821255978) * k + 1.781477937) *
                k - 0.356563782) * k + 0.319381530) * k;
        y = 1.0 - 0.398942280401 * Math.exp(-0.5 * x * x) * y;

        return (1d - neg) * y + neg * (1d - y);
    }
}
