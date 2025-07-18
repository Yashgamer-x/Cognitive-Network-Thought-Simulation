package com.yashgamerx.cognitive_thought_network_simulation.individuals;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a modifiable association between two thought nodes.
 *
 * <p>The association maintains a {@code weight} that reflects the
 * strength of the connection, and provides methods to reinforce
 * (strengthen) or decay (weaken) that weight over time. Weight
 * changes are clamped within configurable bounds to prevent runaway
 * growth or complete nullification.
 */
@Getter
@Setter
public class AssociationEdge {

    /** Current strength of the association (clamped between minWeight and maxWeight). */
    private double weight;

    /** Base decay factor applied when decaying the weight. */
    private double decay;

    /** Minimum allowable weight after clamping. */
    private double minWeight;

    /** Maximum allowable weight after clamping. */
    private double maxWeight;

    /** Learning rate factor used during reinforcement. */
    private double learningRate;

    /**
     * Constructs an AssociationEdge with default parameters:
     * <ul>
     *   <li>weight = 0.5</li>
     *   <li>decay = 0.9</li>
     *   <li>minWeight = 0.05</li>
     *   <li>maxWeight = 1.0</li>
     *   <li>learningRate = 0.05</li>
     * </ul>
     */
    public AssociationEdge() {
        this.weight = 0.5;
        this.decay = 0.9;
        this.minWeight = 0.05;
        this.maxWeight = 1.0;
        this.learningRate = 0.05;
    }

    /**
     * Constructs an AssociationEdge with custom initial weight and decay factor.
     * Other parameters (minWeight, maxWeight, learningRate) remain at their defaults.
     *
     * @param weight the starting strength of the association
     * @param decay  the base decay factor to apply in {@link #decay(double)}
     */
    public AssociationEdge(double weight, double decay) {
        this.weight = weight;
        this.decay = decay;
    }

    /**
     * Reinforces the association by increasing its weight proportionally
     * to the product of pre- and post-synaptic energy signals and the learning rate.
     *
     * <p>New weight is computed as:
     * <pre>weight += learningRate * preEnergy * postEnergy;</pre>
     * and then clamped to remain within {@code [minWeight, maxWeight]}.
     *
     * @param preEnergy  activation level of the source node
     * @param postEnergy activation level of the target node
     */
    public void reinforce(double preEnergy, double postEnergy) {
        weight += learningRate * preEnergy * postEnergy;
        // Clamp to [minWeight, maxWeight]
        weight = Math.max(minWeight, Math.min(weight, maxWeight));
    }

    /**
     * Applies exponential decay to the current weight by multiplying it
     * by the provided decay rate, then ensures it does not fall below
     * {@code minWeight}.
     *
     * <p>If {@code decayRate = 0.99}, the weight will be reduced by 1% each call.
     *
     * @param decayRate factor by which to reduce the weight (e.g., 0.99)
     */
    public void decay(double decayRate) {
        weight *= decayRate;
        // Prevent weight from dropping below minimum
        weight = Math.max(minWeight, weight);
        System.out.println("Updated Weight: " + weight + "\n");
    }
}