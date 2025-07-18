package com.yashgamerx.cognitive_thought_network_simulation.individuals;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a node in the cognitive thought network.
 *
 * <p>Each ThoughtNode holds an activation energy value and a dynamic
 * threshold. When activated above its threshold, the node fires:
 * it propagates energy to connected nodes through AssociationEdges,
 * reinforces those edges, and resets its own energy. The node also
 * supports passive decay of energy and threshold recovery over time.
 */
@Getter
public class ThoughtNode {

    /** Unique identifier for this thought node. */
    private final String name;

    /** Current activation energy stored by this node. */
    @Setter
    private double energy;

    /** Minimum threshold value used during recovery (base floor). */
    private final double baseThreshold;

    /** Dynamic threshold that input energy must meet or exceed to fire. */
    @Setter
    private double threshold;

    /** Outgoing connections from this node to others with associated edges. */
    private final Map<ThoughtNode, AssociationEdge> connections;

    /**
     * Constructs a ThoughtNode with the given name.
     * Uses default threshold = 0.5 and baseThreshold = 0.3.
     *
     * @param name unique identifier for the node
     */
    public ThoughtNode(String name) {
        this.name = name;
        this.energy = 0.0;
        this.baseThreshold = 0.3;
        this.threshold = 0.5;
        this.connections = new HashMap<>();
    }

    /**
     * Constructs a ThoughtNode with the given name and custom threshold.
     * Uses default baseThreshold = 0.3.
     *
     * @param name      unique identifier for the node
     * @param threshold initial firing threshold for activation
     */
    public ThoughtNode(String name, double threshold) {
        this.name = name;
        this.energy = 0.0;
        this.baseThreshold = 0.3;
        this.threshold = threshold;
        this.connections = new HashMap<>();
    }

    /**
     * Activates the node by adding input energy and, if the total
     * exceeds the threshold, fires the node:
     * <ol>
     *   <li>Resets this energy to zero</li>
     *   <li>Propagates energy to each connected node:
     *       propagatedEnergy = inputEnergy * edge.weight * edge.decay</li>
     *   <li>Reinforces each AssociationEdge based on pre/post energy</li>
     * </ol>
     *
     * @param inputEnergy the amount of energy to add and potentially fire with
     */
    public void activate(double inputEnergy) {
        this.energy += inputEnergy;
        System.out.println("ThoughtNode " + this.name + " is now activated");

        if (this.energy >= this.threshold) {
            // Reset energy before propagation
            this.energy = 0.0;

            // Propagate activation through each outgoing connection
            for (Map.Entry<ThoughtNode, AssociationEdge> entry : connections.entrySet()) {
                ThoughtNode target = entry.getKey();
                AssociationEdge edge = entry.getValue();

                double propagatedEnergy = inputEnergy * edge.getWeight() * edge.getDecay();
                target.activate(propagatedEnergy);

                // Strengthen the association based on activation levels
                edge.reinforce(inputEnergy, propagatedEnergy);
            }
        }
    }

    /**
     * Applies passive energy decay and threshold recovery:
     * <ul>
     *   <li>Reduces energy by 10% if above 0.01, otherwise sets to 0</li>
     *   <li>Reduces threshold by 5% each call, not dropping below baseThreshold</li>
     * </ul>
     * Logs the updated state to the console.
     */
    public void update() {
        double decayFactor = 0.9;

        if (energy > 0.01) {
            energy *= decayFactor;
        } else {
            energy = 0.0;
        }

        // Recover threshold gradually toward baseThreshold
        threshold = Math.max(baseThreshold, threshold * 0.95);

        System.out.println("ThoughtNode " + this.name
                + " updated energy: " + energy
                + " threshold: " + threshold);
    }
}