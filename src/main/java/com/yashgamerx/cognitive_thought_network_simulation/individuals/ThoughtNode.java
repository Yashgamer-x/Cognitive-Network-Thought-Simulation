package com.yashgamerx.cognitive_thought_network_simulation.individuals;

import com.yashgamerx.cognitive_thought_network_simulation.controller.CircleController;
import com.yashgamerx.cognitive_thought_network_simulation.manager.FlashManager;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a node in the cognitive thought network with thread-safe state
 * updates and UI feedback.
 *
 * <p>Each ThoughtNode holds:
 * <ul>
 *   <li>an activation energy value and a dynamic firing threshold</li>
 *   <li>a map of outgoing {@link AssociationEdge} connections</li>
 *   <li>a {@link CircleController} for UI interaction and visual flashes</li>
 *   <li>a {@link Lock} to synchronize concurrent activate/update calls</li>
 * </ul>
 *
 * <p>When activated above its threshold, the node:
 * <ol>
 *   <li>resets or accumulates energy</li>
 *   <li>propagates energy to connected nodes after a short delay</li>
 *   <li>reinforces each outgoing edge based on pre/post energy</li>
 * </ol>
 *
 * <p>Additionally, calling {@link #update()} applies passive decay to
 * its energy and recovers its threshold over time.
 */
@Getter
public class ThoughtNode {

    /** Guards updates to energy (and any future shared state). */
    private final Lock stateLock;

    /** Unique identifier for this thought node. */
    private final String name;

    /** Current activation energy stored by this node. */
    @Setter
    private double energy;

    /** Base threshold floor used during threshold recovery. */
    private final double baseThreshold;

    /** Dynamic threshold that must be reached to fire. */
    @Setter
    private double threshold;

    /** Outgoing connections from this node to others with associated edges. */
    private final Map<ThoughtNode, AssociationEdge> connections;

    /** Controller for the UI element representing this thought. */
    private final CircleController circleController;

    /**
     * Constructs a ThoughtNode with default threshold = 0.5 and baseThreshold = 0.3.
     *
     * @param name             the unique name of this node
     * @param circleController the UI controller tied to this node's ellipse
     */
    public ThoughtNode(String name, CircleController circleController) {
        this.name = name;
        this.energy = 0.0;
        this.baseThreshold = 0.3;
        this.threshold = 0.5;
        this.connections = new HashMap<>();
        this.circleController = circleController;
        this.stateLock = new ReentrantLock();
    }

    /**
     * Constructs a ThoughtNode with a custom initial threshold.
     * baseThreshold remains at 0.3.
     *
     * @param name             the unique name of this node
     * @param threshold        the initial firing threshold
     * @param circleController the UI controller tied to this node's ellipse
     */
    public ThoughtNode(String name, double threshold, CircleController circleController) {
        this.name = name;
        this.energy = 0.0;
        this.baseThreshold = 0.3;
        this.threshold = threshold;
        this.connections = new HashMap<>();
        this.circleController = circleController;
        this.stateLock = new ReentrantLock();
    }

    /**
     * Activates the node by adding input energy. Immediately flashes
     * the associated ellipse green, then after 0.6 seconds propagates
     * activation to connected nodes if the threshold is exceeded.
     * Reinforces each edge based on pre/post energy.
     *
     * @param inputEnergy the amount of energy to add and possibly fire with
     */
    public void activate(double inputEnergy) {
        // Thread-safe energy accumulation
        stateLock.lock();
        try {
            energy += inputEnergy;
        } finally {
            stateLock.unlock();
        }

        System.out.println("ThoughtNode " + this.name + " is now activated");
        FlashManager.flashGreen(circleController.getEllipse());

        // Delay propagation to simulate a processing pause
        PauseTransition delay = new PauseTransition(Duration.seconds(0.6));
        delay.setOnFinished(_ -> {
            if (energy >= threshold) {
                // Propagate through each outgoing connection
                for (Map.Entry<ThoughtNode, AssociationEdge> entry : connections.entrySet()) {
                    ThoughtNode target = entry.getKey();
                    AssociationEdge edge = entry.getValue();

                    double propagatedEnergy = inputEnergy * edge.getWeight() * edge.getDecay();
                    target.activate(propagatedEnergy);

                    edge.reinforce(inputEnergy, propagatedEnergy);
                }
            }
        });
        delay.play();
    }

    /**
     * Applies passive decay to the node's energy and gradual recovery
     * of its threshold:
     * <ul>
     *   <li>If energy &gt; 0.01, reduce by 10%; otherwise zero it out</li>
     *   <li>Lower threshold by 5% but not below baseThreshold</li>
     * </ul>
     * This method is thread-safe for energy updates.
     */
    public void update() {
        double decayFactor = 0.9;

        stateLock.lock();
        try {
            if (energy > 0.01) {
                energy *= decayFactor;
            } else {
                energy = 0.0;
            }
        } finally {
            stateLock.unlock();
        }

        // Threshold recovery outside the lock since threshold is single-writer
        threshold = Math.max(baseThreshold, threshold * 0.95);

        System.out.println("ThoughtNode " + this.name
                + " updated energy: " + energy
                + " threshold: " + threshold);
    }
}