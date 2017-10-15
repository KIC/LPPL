package kic.lppl;

import kic.pool.ResourcePool;
import kic.pool.ShutdownableResourcePool;
import net.finmath.optimizer.SolverException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import java.util.stream.IntStream;
import static java.util.stream.Collectors.*;
import static kic.lppl.Doubles.*;
import static kic.lppl.LPPLSolver.*;

// FIXME this shoule become some sort of builder to run multiple LPPL's and count the quantiles
public class Sornette {

    /**
     * Use this Method do just make one single fit of the Log Periodic Power Law of the form:
     *   A + B * a + C1 * a * cos(w * log(tc - t)) + C2 * a * sin(w * log(tc - t))
     *
     * @param time  an array of decimal time. The timestap format is perfectly linear, so to use i.e. hours just do
     *              timestamp / 1000 / 60 / 60 and to convert back use tc * 60 * 60 * 1000
     * @param prices    an array of prices, from the book it should actually be the log of each price
     * @return an array of doubles with the parameters [m, w, tc] we pay specifically attention on the tc parameter
     *         as this is the decimal time expectation for change in the regime. Don't forget to convert the tc back
     *         to a timestamp like you did for the time array i.e. tc * 60 * 60 * 1000
     * @throws SolverException
     */
    public static double[] fit(double[] time, double[] prices) throws SolverException {
        LPPLSolver sornetteSolver = newDefaultSolver(time, prices);
        double[] solution = sornetteSolver.solve();
        sornetteSolver.shutDown();
        return solution;
    }

    /**
     * Make a fit on a single window with a number of smaller subwindwos. The Data has to be sorted in ASC order.
     *
     * @param time  an array of decimal time. The timestap format is perfectly linear, so to use i.e. hours just do
     *              timestamp / 1000 / 60 / 60 and to convert back use tc * 60 * 60 * 1000
     * @param prices    an array of prices, from the book it should actually be the log of each price
     * @param shrinkWindowBy    nr of datapoints each subwindow is smaller then the previous one
     * @param nrOfShrinks       nr number of subwindows
     * @param nrOfSolvers       nr of openCL solvers, this parameter depends on the memory size of you GPUs
     * @param service           optionally one could pass an executor service by default we create our own threadpool
     *                          like so: Executors.newFixedThreadPool(nrOfSolvers);
     * @return sornette crash indicator returns the number of esitmated TCs (time of crash). We can expect the TCs
     *                                  form some kind of distribution where one can read how likely a cange of regime
     *                                  is at a given time
     * @throws SolverException
     * @throws InterruptedException
     */
    public static Map<Double, Integer> fit(double[] time,
                                           double[] prices,
                                           int shrinkWindowBy,
                                           int nrOfShrinks,
                                           int nrOfSolvers,
                                           ExecutorService service) throws SolverException, InterruptedException {

        if (shrinkWindowBy * nrOfShrinks >= time.length) throw new IllegalArgumentException("nrOfShrinks * shrinkWindowBy > time.length");

        ExecutorService executors = service != null
                ? service
                : Executors.newFixedThreadPool(nrOfSolvers);

        ShutdownableResourcePool<LPPLSolver> lpplSolverResourcePool =
                new ShutdownableResourcePool<>(IntStream.range(0, nrOfSolvers)
                                                        .mapToObj(i -> newDefaultSolver(time, prices))
                                                        .collect(toList()));

        final CountDownLatch signalDone = new CountDownLatch(nrOfShrinks);
        final Map<Double, Integer> sornette = new ConcurrentSkipListMap<>();
        for (int i=0; i<nrOfShrinks; i++) {
            final int index = i * shrinkWindowBy;
            final float[] windowedTime = toFloats(time, index);
            final float[] windowedPrices = toFloats(prices, index);
            final double[] windowedDoublePrices = fromIndex(prices, index);

            final LPPLSolver solver = lpplSolverResourcePool.acquire();
            CompletableFuture.supplyAsync(() -> { double[] solution = solver.withNewTarget(windowedTime, windowedPrices, windowedDoublePrices, false).solve();
                                                  lpplSolverResourcePool.recycle(solver);
                                                  return solution;
                                                 }, executors)
                             .thenAccept(params -> sornette.put(params[TC], sornette.getOrDefault(params[TC], 0) + 1))
                             .thenAccept(v -> signalDone.countDown());
        }

        // shutdown all solvers this should also wait until the last solver is returned
        signalDone.await();
        lpplSolverResourcePool.shutdown();
        return sornette;
    }


    public static Map<Double, Integer> fit(double[] time,
                                           double[] prices,
                                           int windowSize,
                                           int shrinkWindowBy,
                                           int nrOfShrinks,
                                           int nrOfSolvers,
                                           ExecutorService service) throws SolverException, InterruptedException {
        // here we will then move the window over the whole time series.
        // for every window we also apply the widow shrinkage to collect all the TCs.
        // this is like a backtest and could be plotted like a heatmap
        return null;
    }

    protected static LPPLSolver newDefaultSolver(double[] time, double[] prices) {
        return new LPPLSolver(toFloats(time),
                              toFloats(prices),
                              prices,
                              DEFAULT_M,
                              DEFAULT_W,
                              null,
                              null);
    }

}
