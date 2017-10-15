package kic.pool;

import java.util.Collection;

public class ShutdownableResourcePool<Resource extends Shutdownable> extends ResourcePool<Resource> {

    public ShutdownableResourcePool(Collection<Resource> poolResources) {
        super(poolResources);
    }

    @Override
    public synchronized void shutdown() {
        super.shutdown();
        for (int i=0; i<size(); i++) {
            try {
                pool.take().shutDown();
            } catch (Exception e) {}
        }
    }
}