package com.yashgamerx.cognitive_thought_network_simulation;

import com.yashgamerx.cognitive_thought_network_simulation.controller.CircleController;
import com.yashgamerx.cognitive_thought_network_simulation.logic.SentenceProcessor;
import com.yashgamerx.cognitive_thought_network_simulation.manager.ThoughtManager;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

/**
 * Utility class for prompting the user to enter a thought name via a dialog,
 * validating it, and applying it to a CircleController label, or processing
 * a query through the SentenceProcessor.
 */
public class TextInputDialogClass {

    /**
     * Displays an input dialog for the user to enter a thought name,
     * checks for cancellation or duplicate thoughts, and then sets the label
     * on the provided CircleController if valid.
     *
     * @param circleController the controller whose label will be updated
     * @return true if a new, non‐duplicate name was entered and applied;
     *         false if the dialog was cancelled or the thought already exists
     */
    public static boolean circleDialog(CircleController circleController) {
        Optional<String> result = getCircleString();

        if (result.isEmpty() || ThoughtManager.thoughtExists(result.get())) {
            System.out.println("Dialog was cancelled or thought already exists.");
            return false;
        }

        result.ifPresent(name -> {
            circleController.setLabel(name);
            System.out.println("User entered: " + name);
        });

        return true;
    }

    /**
     * Displays an input dialog for the user to enter a query or statement,
     * checks for cancellation or duplicate thoughts, and if valid, sends the
     * input to the SentenceProcessor for computation.
     */
    public static void queryDialog() {
        Optional<String> result = getQueryString();

        if (result.isEmpty() || ThoughtManager.thoughtExists(result.get())) {
            System.out.println("Dialog was cancelled or thought already exists.");
            return;
        }

        result.ifPresent(message -> {
            System.out.println("User entered: " + message);
            SentenceProcessor.compute(message);
        });
    }

    /**
     * Builds and displays a TextInputDialog configured for entering a thought name.
     * Uses a default value in the input field and sets title, header, and content text.
     *
     * @return an Optional containing the entered string if OK was clicked;
     *         an empty Optional if Cancel was clicked
     */
    private static Optional<String> getCircleString() {
        TextInputDialog dialog = new TextInputDialog("Default Value");
        dialog.setTitle("Input Required");
        dialog.setHeaderText("Please enter Thought Name:");
        dialog.setContentText("Name:");

        return dialog.showAndWait();
    }

    /**
     * Builds and displays a TextInputDialog configured for entering a query or statement.
     * Uses a default value in the input field and sets title, header, and content text.
     *
     * @return an Optional containing the entered string if OK was clicked;
     *         an empty Optional if Cancel was clicked
     */
    private static Optional<String> getQueryString() {
        TextInputDialog dialog = new TextInputDialog("Default Value");
        dialog.setTitle("Input Required");
        dialog.setHeaderText("Please enter query/statement:");
        dialog.setContentText("Query:");

        return dialog.showAndWait();
    }
}