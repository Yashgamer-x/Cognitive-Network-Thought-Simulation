package com.yashgamerx.cognitive_thought_network_simulation.manager;

import com.yashgamerx.cognitive_thought_network_simulation.controller.CircleController;
import com.yashgamerx.cognitive_thought_network_simulation.individuals.ThoughtNode;
import com.yashgamerx.cognitive_thought_network_simulation.individuals.AssociationEdge;
import com.yashgamerx.cognitive_thought_network_simulation.storage.MemoryStorageArea;

/**
 * Central static manager for ThoughtNode lifecycle and network topology.
 *
 * <p>Provides convenience methods for:
 * <ul>
 *   <li>Creating and registering new thoughts</li>
 *   <li>Checking existence of thoughts</li>
 *   <li>Connecting and disconnecting thoughts via AssociationEdges</li>
 *   <li>Removing thoughts and associated edges</li>
 *   <li>Activating thoughts by name with default energy</li>
 * </ul>
 *
 * <p>All interactions are performed via the global {@link MemoryStorageArea} singleton.
 */
public class ThoughtManager {

    /**
     * Checks whether a thought with the given name exists in memory.
     *
     * @param thoughtName the name identifier to look up
     * @return true if a ThoughtNode is mapped; false otherwise
     */
    public static boolean thoughtExists(String thoughtName) {
        return MemoryStorageArea
                .getInstance()
                .getThoughtNodeMap()
                .containsKey(thoughtName);
    }

    /**
     * Retrieves the ThoughtNode associated with the given name from the global MemoryStorageArea.
     * This is a convenience lookup into the underlying ThoughtNode map.
     *
     * @param thoughtName the name/key of the ThoughtNode to fetch
     * @return the matching ThoughtNode, or null if no entry exists for the given name
     */

    public static ThoughtNode getThoughtNode(String thoughtName) {
        return MemoryStorageArea
                .getInstance()
                .getThoughtNodeMap()
                .get(thoughtName);
    }

    /**
     * Connects two thoughts by creating a directed {@link AssociationEdge}
     * from the first to the second.
     *
     * <p>Does nothing if:
     * <ul>
     *   <li>Either node does not exist</li>
     *   <li>The two nodes are already connected</li>
     * </ul>
     *
     * @param thoughtNames an array with [0]=source and [1]=target thought names
     */
    public static void connectThought(String... thoughtNames) {
        var storage = MemoryStorageArea.getInstance();
        var thought1 = storage.getThoughtNodeMap().get(thoughtNames[0]);
        var thought2 = storage.getThoughtNodeMap().get(thoughtNames[1]);

        if (thought1 == null || thought2 == null ||
                thought1.getConnections().containsKey(thought2)) {
            return;
        }

        var edge = new AssociationEdge();
        thought1.getConnections().put(thought2, edge);
        storage.getAssociationEdgeList().add(edge);
    }

    /**
     * Removes the association between two named thoughts if present.
     * Also removes the {@link AssociationEdge} from the global list.
     *
     * @param thoughtNameA the source thought name
     * @param thoughtNameB the target thought name
     */
    public static void disconnectThought(String thoughtNameA, String thoughtNameB) {
        var storage = MemoryStorageArea.getInstance();
        var thought1 = storage.getThoughtNodeMap().get(thoughtNameA);
        var thought2 = storage.getThoughtNodeMap().get(thoughtNameB);

        if (thought1 != null && thought2 != null) {
            var edge = thought1.getConnections().remove(thought2);
            if (edge != null) {
                storage.getAssociationEdgeList().remove(edge);
            }
        }
    }

    /**
     * Removes a thought by name. Also removes all edges outbound from it.
     * Inbound connections pointing to this node are not removed.
     *
     * @param thoughtName the name of the thought to delete
     */
    public static void removeThought(String thoughtName) {
        var storage = MemoryStorageArea.getInstance();
        var thoughtNode = storage.getThoughtNodeMap().remove(thoughtName);

        if (thoughtNode != null) {
            thoughtNode.getConnections()
                    .values()
                    .parallelStream()
                    .forEach(edge -> storage.getAssociationEdgeList().remove(edge));
        }
    }

    /**
     * Registers a new {@link ThoughtNode} and associates it with a UI controller.
     * If a node with this name already exists, no action is taken.
     *
     * @param thoughtName      unique name for the new thought
     * @param circleController associated UI component
     */
    public static void createThought(String thoughtName, CircleController circleController) {
        var storage = MemoryStorageArea.getInstance();
        if (storage.getThoughtNodeMap().containsKey(thoughtName)) return;

        var node = new ThoughtNode(thoughtName, circleController);
        storage.getThoughtNodeMap().put(thoughtName, node);
    }

    /**
     * Triggers activation of a thought by name using a default energy value.
     * This causes potential visual feedback and propagation logic.
     *
     * @param thoughtName the name of the thought to activate
     */
    public static void activateThought(String thoughtName) {
        var node = MemoryStorageArea.getInstance()
                .getThoughtNodeMap()
                .get(thoughtName);
        if (node != null) {
            node.activate(1.0);
        }
    }
}