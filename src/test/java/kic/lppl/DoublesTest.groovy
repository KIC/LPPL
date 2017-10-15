package kic.lppl

import spock.lang.Specification
import static kic.lppl.Doubles.*;

class DoublesTest extends Specification {
    def "test toFloats"() {
        given:
        def d = [1,2] as double[]

        when:
        def f = toFloats(d)

        then:
        f == [1f, 2f] as float[]
    }

    def "test toFloats1"() {
        given:
        def d = [1,2,3,4,5,6,7,8] as double[]

        when:
        def f = toFloats(d, from)

        then:
        f == expected

        where:
        from    | expected
        -3      | [6,7,8] as float[]
        3       | [4,5,6,7,8] as float[]
    }

    def "test fromIndex"() {
        given:
        def d = [1,2,3,4,5,6,7,8] as double[]

        when:
        def d2 = fromIndex(d, from)

        then:
        d2 == expected

        where:
        from    | expected
        -3      | [6,7,8] as double[]
        3       | [4,5,6,7,8] as double[]
    }
}
