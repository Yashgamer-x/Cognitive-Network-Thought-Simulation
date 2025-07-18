package com.yashgamerx.cognitive_thought_network_simulation;

import com.yashgamerx.cognitive_thought_network_simulation.individuals.CircleController;
import com.yashgamerx.cognitive_thought_network_simulation.manager.ThoughtManager;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

/**
 * Utility class for prompting the user to enter a thought name via a dialog,
 * validating it, and applying it to a CircleController label.
 */
public class TextInputDialogForCircleClass {

    /**
     * Displays an input dialog for the user to enter a thought name,
     * checks for cancellation or duplicate thoughts, and then sets the label
     * on the provided CircleController if valid.
     *
     * @param circleController the controller whose label will be updated
     * @return true if a new, non‐duplicate name was entered and applied;
     *         false if the dialog was cancelled or the thought already exists
     */
    public static boolean dialog(CircleController circleController) {
        // 1. Show the text input dialog and get the user's input
        Optional<String> result = getString();

        // 2. If the user clicked Cancel or entered a duplicate thought name, abort
        //    - result.isEmpty(): user cancelled
        //    - result.isPresent() && ThoughtManager.thoughtExists(...): duplicate
        if (result.isEmpty() || ThoughtManager.thoughtExists(result.get())) {
            System.out.println("Dialog was cancelled or thought already exists.");
            return false;
        }

        // 3. If we have a valid new name, update the CircleController's label
        result.ifPresent(name -> {
            circleController.setLabel(name);
            System.out.println("User entered: " + name);
        });

        return true;
    }

    /**
     * Builds and displays a TextInputDialog configured for entering a thought name.
     * Uses a default value in the input field and sets title, header, and content text.
     *
     * @return an Optional containing the entered string if OK was clicked;
     *         an empty Optional if Cancel was clicked
     */
    private static Optional<String> getString() {
        TextInputDialog dialog = new TextInputDialog("Default Value");
        dialog.setTitle("Input Required");
        dialog.setHeaderText("Please enter Thought Name:");
        dialog.setContentText("Name:");

        // showAndWait() blocks until the user closes the dialog
        return dialog.showAndWait();
    }
}