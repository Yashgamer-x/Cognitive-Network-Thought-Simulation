package com.yashgamerx.cognitive_thought_network_simulation.manager;

import com.yashgamerx.cognitive_thought_network_simulation.individuals.ThoughtNode;
import com.yashgamerx.cognitive_thought_network_simulation.storage.MemoryStorageArea;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages a scheduled background service that periodically updates all ThoughtNodes
 * and decays all AssociationEdges in the MemoryStorageArea.
 *
 * <p>This class uses a single-threaded {@link ScheduledExecutorService} to:
 * <ol>
 *   <li>Invoke {@link ThoughtNode#update()} on each thought node</li>
 *   <li>Invoke {@link com.yashgamerx.cognitive_thought_network_simulation.individuals.AssociationEdge#decay(double)}
 *       on each association edge</li>
 * </ol>
 * The interval is configurable (default: every 10 seconds).
 */
public class ThoughtUpdater {

    /** Single-threaded scheduler for periodic updates. */
    private static ScheduledExecutorService scheduledExecutorService;

    /**
     * Starts the background update service.
     * If already started, this will create a new executor and schedule tasks.
     */
    public static void startService() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            var storage = MemoryStorageArea.getInstance();
            // Update each thought node's state
            storage.getThoughtNodeMap().values().forEach(ThoughtNode::update);
            // Decay each association edge's weight
            storage.getAssociationEdgeList().forEach(edge -> edge.decay(0.995));
        }, 0, 10, TimeUnit.SECONDS);
        System.out.println("ThoughtUpdater started");
    }

    /**
     * Stops the background update service, shutting down its executor.
     * Note: calls to this method on an already-shutdown executor will throw an exception.
     */
    public static void stopService() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
        System.out.println("ThoughtUpdater stopped");
    }
}