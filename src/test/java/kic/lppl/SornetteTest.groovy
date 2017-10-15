package kic.lppl

import spock.lang.Specification

class SornetteTest extends Specification {
    def "test fit"() {
        given:
        def time = DATA.time
        def prices = DATA.price
        def shrinkWindowBy = 2
        def nrOfShrinks = 20
        def nrOfSovlers = 10

        when:
        def solution = Sornette.fit(time, prices, shrinkWindowBy, nrOfShrinks, nrOfSovlers, null);

        then:
        solution.size() > 0
        println(solution) // [416883.0:3, 416885.0:4, 416886.0:6, 416887.0:4, 416888.0:1, 416889.0:1, 416890.0:1]
        solution.toString() == "[416883.0:3, 416885.0:4, 416886.0:6, 416887.0:4, 416888.0:1, 416889.0:1, 416890.0:1]"
    }
}
