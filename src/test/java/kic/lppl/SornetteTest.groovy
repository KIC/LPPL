package kic.lppl

import spock.lang.Specification
import static kic.DATA.*

class SornetteTest extends Specification {
    def "test fit"() {
        given:
        def shrinkWindowBy = 2
        def nrOfShrinks = 20
        def nrOfSovlers = 10

        when:
        def solution = Sornette.fit(time, price, shrinkWindowBy, nrOfShrinks, nrOfSovlers, null);

        then:
        println(solution)
        solution.toString() == "[416883.0:3, 416885.0:4, 416886.0:6, 416887.0:4, 416888.0:1, 416889.0:1, 416890.0:1]"
    }
}
