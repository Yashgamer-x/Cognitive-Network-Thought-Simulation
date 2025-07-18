package com.yashgamerx.cognitive_thought_network_simulation.manager;

import com.yashgamerx.cognitive_thought_network_simulation.individuals.ThoughtNode;
import com.yashgamerx.cognitive_thought_network_simulation.storage.MemoryStorageArea;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThoughtUpdater {
    private static ScheduledExecutorService scheduledExecutorService;

    public static void startService(){
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(()-> {
            var thoughtNodes = MemoryStorageArea.getInstance().getThoughtNodeMap().values();
            var associatedEdges = MemoryStorageArea.getInstance().getAssociationEdgeList();
            thoughtNodes.forEach(ThoughtNode::update);
            associatedEdges.forEach(associatedEdge->associatedEdge.decay(0.995));
        },0,1, TimeUnit.SECONDS);
        System.out.println("ThoughtUpdater started");
    }

    public static void stopService(){
        scheduledExecutorService.close();
        System.out.println("ThoughtUpdater stopped");
    }
}
