package com.yashgamerx.cognitive_thought_network_simulation;

import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public class TextInputDialogForCircleClass {

    public static boolean dialog(CircleController circleController){
        // 1. Create a TextInputDialog instance
        Optional<String> result = getString();

        // 4. Process the result
        result.ifPresent(name -> { // This lambda executes only if a value is present (OK was clicked)
            circleController.setLabel(name);
            System.out.println("User entered: " + name);
        });

        // If you need to explicitly check for cancellation:
        if (result.isEmpty()) {
            System.out.println("Dialog was cancelled.");
            return false;
        }
        return true;
    }

    private static Optional<String> getString() {
        var dialog = new TextInputDialog("Default Value"); // Optional: set a default value

        // 2. Set dialog properties (optional but recommended for clarity)
        dialog.setTitle("Input Required");
        dialog.setHeaderText("Please enter Thought Name:"); // Larger text above the input field
        dialog.setContentText("Name:"); // Text next to the input field

        // 3. Show the dialog and wait for user input
        // showAndWait() returns an Optional<String>
        return dialog.showAndWait();
    }
}
