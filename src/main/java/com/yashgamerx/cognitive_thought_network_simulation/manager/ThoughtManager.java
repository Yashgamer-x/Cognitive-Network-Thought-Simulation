package com.yashgamerx.cognitive_thought_network_simulation.manager;

import com.yashgamerx.cognitive_thought_network_simulation.individuals.ThoughtNode;
import com.yashgamerx.cognitive_thought_network_simulation.individuals.AssociationEdge;
import com.yashgamerx.cognitive_thought_network_simulation.storage.MemoryStorageArea;

/**
 * Utility class for managing ThoughtNode instances and their associations
 * within the MemoryStorageArea. Provides methods to create, remove,
 * query, and connect thoughts by name.
 */
public class ThoughtManager {

    /**
     * Checks whether a thought with the given name exists in storage.
     *
     * @param thoughtName the unique identifier of the thought
     * @return true if a ThoughtNode with the specified name is present; false otherwise
     */
    public static boolean thoughtExists(String thoughtName) {
        return MemoryStorageArea
                .getInstance()
                .getThoughtNodeMap()
                .containsKey(thoughtName);
    }

    /**
     * Connects two existing thoughts by creating an AssociationEdge
     * from the first thought to the second thought.
     * <p>
     * If either thought is missing or they are already connected,
     * this method returns without side effects.
     *
     * @param thoughtNames a two-element array where:
     *                     [0] is the source thought name,
     *                     [1] is the target thought name
     */
    public static void connectThought(String... thoughtNames) {
        var storage = MemoryStorageArea.getInstance();
        ThoughtNode thought1 = storage.getThoughtNodeMap().get(thoughtNames[0]);
        ThoughtNode thought2 = storage.getThoughtNodeMap().get(thoughtNames[1]);

        // Skip if either thought is null or already connected
        if (thought1 == null || thought2 == null
                || thought1.getConnections().containsKey(thought2)) {
            return;
        }

        // Create new association and register it
        AssociationEdge associationEdge = new AssociationEdge();
        thought1.getConnections().put(thought2, associationEdge);
        storage.getAssociationEdgeList().add(associationEdge);
    }

    /**
     * Removes the thought with the specified name from storage.
     * Any existing associations to or from this thought remain in the list,
     * but the node itself is no longer retrievable.
     *
     * @param thoughtName the unique identifier of the thought to remove
     */
    public static void removeThought(String thoughtName) {
        MemoryStorageArea
                .getInstance()
                .getThoughtNodeMap()
                .remove(thoughtName);
    }

    /**
     * Creates a new ThoughtNode with the given name and stores it.
     * If a thought with the same name already exists, no action is taken.
     *
     * @param thoughtName the unique identifier for the new thought
     */
    public static void createThought(String thoughtName) {
        var storage = MemoryStorageArea.getInstance();
        if (storage.getThoughtNodeMap().containsKey(thoughtName)) {
            return;
        }
        ThoughtNode thoughtNode = new ThoughtNode(thoughtName);
        storage.getThoughtNodeMap().put(thoughtName, thoughtNode);
    }

    /**
     * Activates the thought by name with a default intensity.
     * This method is public and is used internally to trigger
     * activation logic on a ThoughtNode.
     *
     * @param thoughtName the unique identifier of the thought to activate
     */
    public static void activateThought(String thoughtName) {
        ThoughtNode thoughtNode =
                MemoryStorageArea.getInstance()
                        .getThoughtNodeMap()
                        .get(thoughtName);
        if (thoughtNode != null) {
            thoughtNode.activate(1.0);
        }
    }
}