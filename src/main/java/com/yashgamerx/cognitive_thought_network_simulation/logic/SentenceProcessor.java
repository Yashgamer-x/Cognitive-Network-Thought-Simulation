package com.yashgamerx.cognitive_thought_network_simulation.logic;

import com.yashgamerx.cognitive_thought_network_simulation.individuals.ThoughtNode;
import com.yashgamerx.cognitive_thought_network_simulation.manager.ThoughtManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Processes a user‐supplied sentence by mapping each word to a ThoughtNode,
 * verifying that those nodes form a fully connected clique, and then
 * computing and printing the intersection of their neighbor sets.
 */
public class SentenceProcessor {

    /**
     * Splits the input message on spaces, looks up each token in the ThoughtManager,
     * collects non‐null ThoughtNode instances, validates they form a clique
     * (each node connected to every other), and then:
     * <ul>
     *   <li>Builds a set of neighbors of the first thought</li>
     *   <li>Removes any already included thoughts</li>
     *   <li>Filters down to neighbors common to all thoughts</li>
     *   <li>Prints the resulting set, or "Thoughts not found" if empty</li>
     * </ul>
     *
     * @param message the sentence or series of tokens to process
     * @throws NullPointerException if {@code message} is null
     * @throws IndexOutOfBoundsException if no valid ThoughtNode is found and clique logic is invoked
     */
    public static void compute(String message) {
        // 1. Split the input string
        var messageSplit = message.split(" ");

        // 2. Lookup ThoughtNode for each token in parallel
        List<ThoughtNode> thoughts = new ArrayList<>();
        Arrays.stream(messageSplit)
                .parallel()
                .forEach(split -> {
                    var thoughtNode = ThoughtManager.getThoughtNode(split);
                    if (thoughtNode != null) {
                        thoughts.add(thoughtNode);
                    }
                });

        // 3. Validate clique and, if valid, compute shared neighbors
        if (validateClique(thoughts)) {
            var newThoughts = new HashSet<>(thoughts.getFirst().getConnections().keySet());
            newThoughts.removeIf(thoughts::contains);

            thoughts.parallelStream()
                    .forEach(thought -> newThoughts.removeIf(
                            newThought -> !thought.getConnections().containsKey(newThought)
                    ));

            if (newThoughts.isEmpty()) {
                System.out.println("Thoughts not found");
            } else {
                newThoughts.forEach(System.out::println);
            }
        }
    }

    /**
     * Determines whether the provided list of ThoughtNode instances forms a clique.
     * A clique means every node has a direct connection to every other node in the list.
     *
     * @param thoughts list of nodes to check
     * @return {@code true} if each node is connected to every other, {@code false} otherwise
     */
    private static boolean validateClique(List<ThoughtNode> thoughts) {
        for (ThoughtNode thought : thoughts) {
            if (!validateConnections(thought, thoughts)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks that a single ThoughtNode has connections to all other nodes in the list.
     *
     * @param thoughtNode the node whose connections are being verified
     * @param thoughts    the list of all nodes in the candidate clique
     * @return {@code true} if {@code thoughtNode} is connected to every other node;
     *         {@code false} if any connection is missing
     */
    private static boolean validateConnections(ThoughtNode thoughtNode, List<ThoughtNode> thoughts) {
        for (ThoughtNode thought : thoughts) {
            if (thoughtNode == thought) {
                continue;
            }
            if (!thoughtNode.getConnections().containsKey(thought)) {
                return false;
            }
        }
        return true;
    }
}