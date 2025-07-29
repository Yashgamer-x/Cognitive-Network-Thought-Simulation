package com.yashgamerx.cognitive_thought_network_simulation.manager;

import com.yashgamerx.cognitive_thought_network_simulation.controller.CircleController;
import com.yashgamerx.cognitive_thought_network_simulation.exception.DataAccessException;
import com.yashgamerx.cognitive_thought_network_simulation.storage.MySQLDatabase;

import java.sql.SQLException;

/**
 * Provides CRUD operations for circle nodes in the MySQL database.
 *
 * <p>This utility class centralizes all SQL interactions related to the
 * `circle_nodes` table. Each method obtains a Connection from the
 * {@link MySQLDatabase} singleton, executes the appropriate SQL statement
 * within a try-with-resources block, and wraps any {@link SQLException}
 * in a custom unchecked {@link DataAccessException} for higher-level handling.</p>
 *
 * <p>All operations identify records by the node's label text, which
 * serves as the primary key in the database schema. Methods return a boolean
 * indicating whether exactly one record was affected.</p>
 */
public class MySQLManager {

    /**
     * Inserts a new circle node record into the database.
     *
     * <p>This method extracts the label text and layout coordinates from the
     * provided {@link CircleController} and persists them in the
     * `circle_nodes` table. It returns true if one record was inserted.</p>
     *
     * @param circleController the controller containing node data to persist
     * @return true if the record was inserted successfully; false otherwise
     * @throws DataAccessException if a database access error occurs
     */
    public static boolean createCircleNode(CircleController circleController) {
        String sql = "INSERT INTO circle_nodes(label_text, layout_x, layout_y) VALUES (?, ?, ?)";

        try (
                var connection = MySQLDatabase.getInstance().getConnection();
                var stmt = connection.prepareStatement(sql)
        ) {
            stmt.setString(1, circleController.getLabel().getText());
            stmt.setDouble(2, circleController.getStackPane().getLayoutX());
            stmt.setDouble(3, circleController.getStackPane().getLayoutY());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted == 1;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to create circle node", e);
        }
    }

    /**
     * Deletes a circle node record from the database by its label text.
     *
     * <p>This method uses the label text from the provided
     * {@link CircleController} to execute a DELETE on the
     * `circle_nodes` table. It returns true if one record was deleted.</p>
     *
     * @param circleController the controller whose label identifies the node
     * @return true if exactly one record was deleted; false otherwise
     * @throws DataAccessException if a database access error occurs
     */
    public static boolean deleteCircleNode(CircleController circleController) {
        String sql = "DELETE FROM circle_nodes WHERE label_text = ?";

        try (
                var connection = MySQLDatabase.getInstance().getConnection();
                var stmt = connection.prepareStatement(sql)
        ) {
            stmt.setString(1, circleController.getLabel().getText());
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted == 1;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete circle node", e);
        }
    }

    /**
     * Updates the layout position of an existing circle node in the database.
     *
     * <p>This method sets new X and Y layout coordinates for the circle node
     * identified by its label text. It returns true if one record was updated.</p>
     *
     * @param circleController the controller containing the node’s updated state
     * @return true if exactly one row was updated; false if no matching node was found
     * @throws DataAccessException if a database access error occurs
     */
    public static boolean updateCircleNode(CircleController circleController) {
        String sql = "UPDATE circle_nodes SET layout_x = ?, layout_y = ? WHERE label_text = ?";

        try (
                var connection = MySQLDatabase.getInstance().getConnection();
                var stmt = connection.prepareStatement(sql)
        ) {
            stmt.setDouble(1, circleController.getStackPane().getLayoutX());
            stmt.setDouble(2, circleController.getStackPane().getLayoutY());
            stmt.setString(3, circleController.getLabel().getText());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated == 1;

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Failed to update circle node with label: "
                            + circleController.getLabel().getText(), e
            );
        }
    }
}