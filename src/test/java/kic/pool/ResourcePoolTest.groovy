package kic.pool

import spock.lang.Specification

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ResourcePoolTest extends Specification {
    def "test acquire/recycle"() {
        given:
        def pool = new ResourcePool<int[]>(Arrays.asList(new int[1], new int[1]))
        def ex = Executors.newFixedThreadPool(100)

        when:
        for (int i=0; i<1000; i++) {
            ex.execute {
                def res = pool.acquire()
                res[0]++
                pool.recycle(res)
            }
        }

        ex.shutdown()
        ex.awaitTermination(1, TimeUnit.MINUTES)

        then:
        def res = pool.getResources()
        res[0][0] + res[1][0]  == 1000
    }
}
