package kic.lppl;

import net.finmath.optimizer.SolverException;
import static kic.lppl.Doubles.*;

// FIXME this shoule become some sort of builder to run multiple LPPL's and count the quantiles
public class Sornette {

    // defaults = time[time.length - 1] + 1d}

    /**
     *
     * @param time  an array of decimal time. The timestap format is perfectly linear, so to use i.e. hours just do
     *              timestamp / 1000 / 60 / 60 and to convert back use tc * 60 * 60 * 1000
     * @param prices    an array of prices, from the book it should actually be the log of each price
     * @param initGuessedM  default is 0.5 but you may use the results of a previous fit
     * @param initGuessedW  default is 9 but you may use the results of a previous fit
     * @param initGuessedTC just use last time value plus some threshold > 0(i.e. 1)
     *                       but you may use the results of a previous fit
     * @return an array of doubles with the parameters [m, w, tc] we pay specifically attention on the tc parameter
     *         as this is the decimal time expectation for change in the regime. Don't forget to convert the tc back
     *         to a timestamp like you did for the time array i.e. tc * 60 * 60 * 1000
     * @throws SolverException
     */
    public static double[] fit(double[] time, double[] prices, double initGuessedM, double initGuessedW, double initGuessedTC) throws SolverException {
        LPPLSolver sornetteSolver = new LPPLSolver(toFloats(time), toFloats(prices), prices, initGuessedM, initGuessedW, initGuessedTC);
        return sornetteSolver.solve();
    }

}
