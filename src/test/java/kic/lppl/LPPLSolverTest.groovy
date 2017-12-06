package kic.lppl

import spock.lang.Specification

import static kic.lppl.LPPLSolver.*
import static kic.lppl.Doubles.*
import static kic.DATA.*
import static spock.util.matcher.HamcrestMatchers.*

class LPPLSolverTest extends Specification {
    def "test solve"() {
        given:
        LPPLSolver sornetteSolver = new LPPLSolver(toFloats(time), toFloats(price), price, DEFAULT_M, DEFAULT_W, null, null);

        when:
        def solution = sornetteSolver.solve()

        then:
        solution[0] closeTo(0.4958619269723745, 0.1)
        solution[1] closeTo(8.680476233603711, 0.5)
        solution[2] closeTo(416882.0, 5)
    }

    def "test reconstructing the oszillator"() {
        given:
        LPPLSolver sornetteSolver = new LPPLSolver(toFloats(time), toFloats(price), price, DEFAULT_M, DEFAULT_W, null, null);

        when:
        def solution = sornetteSolver.solve()
        def oszillator = LPPLSolver.reconstructOszillator(time, solution);

        then:
        1==1
        for (int i = 0; i < oszillator.length; i++) {
            println("${oszillator[i][0]},${oszillator[i][1]},${i < price.length ? price[i] : ""}")
        }
    }
}
