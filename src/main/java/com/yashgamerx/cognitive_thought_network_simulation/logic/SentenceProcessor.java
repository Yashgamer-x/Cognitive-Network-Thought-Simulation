package com.yashgamerx.cognitive_thought_network_simulation.logic;

import com.yashgamerx.cognitive_thought_network_simulation.Whiteboard;
import com.yashgamerx.cognitive_thought_network_simulation.individuals.ThoughtNode;
import com.yashgamerx.cognitive_thought_network_simulation.manager.ThoughtManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Processes user‐supplied sentences by mapping tokens to ThoughtNode instances,
 * validating their interconnections, and either drawing new links on the Whiteboard
 * or computing common neighbors among a clique of thoughts.
 * <p>
 * Behavior depends on the punctuation of the final token:
 * <ul>
 *   <li>Messages ending with a period (".") trigger the "remember" logic,
 *       which draws arrows between all non‐connected ThoughtNodes.</li>
 *   <li>Messages ending with a question mark ("?") trigger the "question" logic,
 *       which validates a clique and prints their shared neighbors.</li>
 * </ul>
 */
public class SentenceProcessor {

    /**
     * Entry point for sentence processing. Splits input on spaces, determines
     * whether to invoke remember or question logic based on the trailing punctuation.
     *
     * @param message the sentence or string of tokens to process; must not be null
     * @throws NullPointerException if {@code message} is null
     * @throws IndexOutOfBoundsException if {@code message} contains no valid tokens
     */
    public static void compute(String message) {
        if (message == null) {
            throw new NullPointerException("Input message cannot be null");
        }

        String[] tokens = message.split(" ");
        if (tokens.length == 0) {
            throw new IndexOutOfBoundsException("No tokens to process");
        }

        String last = tokens[tokens.length - 1];
        if (last.endsWith("?")) {
            computeQuestion(tokens);
        } else if (last.endsWith(".")) {
            computeRemember(tokens);
        }
    }

    /**
     * "Remember" processing: looks up ThoughtNode instances for each token,
     * then draws arrows between every pair of nodes that are not already connected.
     *
     * @param tokens array of sanitized tokens (may contain punctuation)
     */
    private static void computeRemember(String[] tokens) {
        List<ThoughtNode> thoughts = thoughtClarifier(tokens);

        // For every unordered pair, draw a new connection if absent
        for (ThoughtNode t1 : thoughts) {
            for (ThoughtNode t2 : thoughts) {
                if (t1.equals(t2) || t1.getConnections().containsKey(t2)) {
                    continue;
                }
                Whiteboard wb = Whiteboard.getInstance();
                wb.startArrowDraw(t1.getCircleController());
                wb.endArrowDraw(t2.getCircleController());
            }
        }
    }

    /**
     * "Question" processing: looks up ThoughtNode instances, validates they form a clique,
     * then computes and prints the set of nodes connected to all of them (excluding the clique).
     *
     * @param tokens array of sanitized tokens (may contain punctuation)
     */
    private static void computeQuestion(String[] tokens) {
        List<ThoughtNode> thoughts = thoughtClarifier(tokens);

        // Only proceed if every node links to every other
        if (!validateClique(thoughts)) {
            System.out.println("Input does not form a fully connected clique");
            return;
        }

        // Start with neighbors of the first node, exclude the clique itself
        var shared = new HashSet<>(thoughts.getFirst().getConnections().keySet());
        thoughts.forEach(shared::remove);

        // Retain only those present in every other node's connections
        thoughts.parallelStream()
                .forEach(thought ->
                        shared.removeIf(candidate ->
                                !thought.getConnections().containsKey(candidate)
                        )
                );

        if (shared.isEmpty()) {
            System.out.println("Thoughts not found");
        } else {
            shared.forEach(System.out::println);
        }
    }

    /**
     * Converts raw tokens into ThoughtNode instances via the ThoughtManager.
     * Non‐alphanumeric characters are stripped before lookup. Only non‐null
     * results are collected.
     *
     * @param tokens raw input tokens
     * @return list of resolved ThoughtNode instances (never null)
     */
    private static List<ThoughtNode> thoughtClarifier(String[] tokens) {
        List<ThoughtNode> results = new ArrayList<>();

        Arrays.stream(tokens)
                .parallel()
                .map(token -> token.replaceAll("[^a-zA-Z0-9]", ""))
                .map(ThoughtManager::getThoughtNode)
                .forEach(node -> {
                    if (node != null) {
                        results.add(node);
                    }
                });

        return results;
    }

    /**
     * Validates that every node in the list has a direct connection to every other node.
     * This defines a clique in graph terms.
     *
     * @param thoughts list of ThoughtNode instances to check
     * @return true if each node connects to all others; false otherwise
     */
    private static boolean validateClique(List<ThoughtNode> thoughts) {
        for (ThoughtNode t : thoughts) {
            if (!validateConnections(t, thoughts)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks a single ThoughtNode for connections to all other nodes in the candidate clique.
     *
     * @param node     the ThoughtNode whose links are being verified
     * @param allNodes the complete list of nodes in the clique candidate
     * @return true if {@code node} is connected to every other element in {@code allNodes}
     */
    private static boolean validateConnections(ThoughtNode node, List<ThoughtNode> allNodes) {
        for (ThoughtNode other : allNodes) {
            if (node == other) {
                continue;
            }
            if (!node.getConnections().containsKey(other)) {
                return false;
            }
        }
        return true;
    }
}