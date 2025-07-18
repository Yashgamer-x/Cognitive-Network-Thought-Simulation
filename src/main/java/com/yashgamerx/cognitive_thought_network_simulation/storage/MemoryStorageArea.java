package com.yashgamerx.cognitive_thought_network_simulation.storage;

import com.yashgamerx.cognitive_thought_network_simulation.individuals.ThoughtNode;
import com.yashgamerx.cognitive_thought_network_simulation.individuals.AssociationEdge;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central in-memory storage for thought nodes and their association edges.
 *
 * <p>This class implements a thread-safe singleton using
 * double-checked locking. It provides global access to:
 * <ul>
 *   <li>a map of {@link ThoughtNode} objects keyed by their unique names</li>
 *   <li>a list of all {@link AssociationEdge} instances in the network</li>
 * </ul>
 */
public class MemoryStorageArea {

    /**
     * Map of thought names to their corresponding ThoughtNode instances.
     */
    @Getter
    private final Map<String, ThoughtNode> thoughtNodeMap;

    /**
     * List of all AssociationEdge objects created between thought nodes.
     */
    @Getter
    private final List<AssociationEdge> associationEdgeList;

    /**
     * The singleton instance of MemoryStorageArea.
     * Declared volatile to ensure safe publication across threads.
     */
    private static volatile MemoryStorageArea instance;

    /**
     * Returns the singleton instance of MemoryStorageArea,
     * creating it if necessary using double-checked locking for thread safety.
     *
     * @return the global MemoryStorageArea instance
     */
    public static MemoryStorageArea getInstance() {
        if (instance == null) {
            synchronized (MemoryStorageArea.class) {
                if (instance == null) {
                    instance = new MemoryStorageArea();
                }
            }
        }
        return instance;
    }

    /**
     * Private constructor to prevent external instantiation.
     * Initializes the internal storage collections.
     */
    private MemoryStorageArea() {
        thoughtNodeMap = new HashMap<>();
        associationEdgeList = new ArrayList<>();
    }
}