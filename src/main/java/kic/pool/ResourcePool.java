package kic.pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ResourcePool<Resource> {
    protected final BlockingQueue<Resource> pool;
    private boolean running = true;

    public ResourcePool(Collection<Resource> poolResources) {
        // Enable the fairness; otherwise, some threads may wait forever.
        pool = new ArrayBlockingQueue<>(poolResources.size(), true, poolResources);
    }

    public Resource acquire() {
        try {
            if (!running) throw new IllegalStateException("Pool stopped!");
            return pool.take();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public void recycle(Resource resource) {
        if (!running) throw new IllegalStateException("Pool stopped!");
        pool.add(resource);
    }

    public List<Resource> getResources() {
        return new ArrayList<>(pool);
    }

    public int size() {
        return pool.size();
    }

    public synchronized void shutdown() {
        running = false;
    }
}