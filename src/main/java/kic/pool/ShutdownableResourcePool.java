package kic.pool;

import kic.lppl.Shutdownable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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