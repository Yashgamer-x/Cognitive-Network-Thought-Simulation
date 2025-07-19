package com.yashgamerx.cognitive_thought_network_simulation.manager;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Ellipse;
import javafx.util.Duration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides a thread-safe, JavaFX-friendly utility for flashing
 * Ellipse nodes to lime green for a short duration, then
 * reliably reverting them to their original fill color.
 *
 * <p>Uses {@link Platform#runLater} to execute on the JavaFX
 * Application Thread, and tracks flashes per-ellipse to prevent
 * overlapping transitions from “stealing” each other’s revert
 * calls.
 */
public class FlashManager {

    /**
     * Maps each Ellipse to its “true” default fill color,
     * captured the first time it’s flashed.
     */
    private static final Map<Ellipse, Paint> DEFAULT_COLOR = new ConcurrentHashMap<>();

    /**
     * Tracks the currently scheduled revert {@link PauseTransition}
     * for each ellipse, so it can be cancelled if a new flash starts.
     */
    private static final Map<Ellipse, PauseTransition> ACTIVE_REVERT = new ConcurrentHashMap<>();

    /**
     * Flashes the given {@link Ellipse} to lime green for 0.3 seconds,
     * then restores it to the color it had before any flashing began.
     *
     * <p>This method:
     * <ol>
     *   <li>Runs on the JavaFX Application Thread via {@link Platform#runLater}.</li>
     *   <li>Captures the ellipse’s original fill color once and stores it.</li>
     *   <li>Cancels any in-flight revert transition so flashes don’t overlap.</li>
     *   <li>Sets the fill to {@code Color.LIMEGREEN} immediately.</li>
     *   <li>Schedules a {@link PauseTransition} for 0.3 seconds to revert the fill.</li>
     * </ol>
     *
     * @param ellipse the JavaFX Ellipse node to flash
     */
    public static void flashGreen(Ellipse ellipse) {
        Platform.runLater(() -> {
            // Remember the ellipse’s “real” default color only the first time
            DEFAULT_COLOR.putIfAbsent(ellipse, ellipse.getFill());

            // Cancel any pending revert so it won't override this new flash
            PauseTransition oldRevert = ACTIVE_REVERT.remove(ellipse);
            if (oldRevert != null) {
                oldRevert.stop();
            }

            // Flash green immediately
            ellipse.setFill(javafx.scene.paint.Color.LIMEGREEN);

            // Schedule revert to the stored default color after 0.3 seconds
            PauseTransition revert = new PauseTransition(Duration.seconds(0.3));
            revert.setOnFinished(_ -> {
                ellipse.setFill(DEFAULT_COLOR.get(ellipse));
                ACTIVE_REVERT.remove(ellipse);
            });

            // Track and start the new revert transition
            ACTIVE_REVERT.put(ellipse, revert);
            revert.play();
        });
    }
}