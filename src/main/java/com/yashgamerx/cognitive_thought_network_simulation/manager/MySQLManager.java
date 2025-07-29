package com.yashgamerx.cognitive_thought_network_simulation.manager;

import com.yashgamerx.cognitive_thought_network_simulation.controller.CircleController;
import com.yashgamerx.cognitive_thought_network_simulation.exception.DataAccessException;
import com.yashgamerx.cognitive_thought_network_simulation.individuals.Arrow;
import com.yashgamerx.cognitive_thought_network_simulation.storage.MySQLDatabase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

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

    private static final Logger log = Logger.getLogger(MySQLManager.class.getName());

    /**
     * Inserts a new circle node record into the database.
     *
     * <p>This method extracts the label text and layout coordinates from the
     * provided {@link CircleController} and persists them in the
     * `circle_nodes` table. It returns true if one record was inserted.</p>
     *
     * @param circleController the controller containing node data to persist
     * @throws DataAccessException if a database access error occurs
     */
    public static void createCircleNode(CircleController circleController) {
        String sql = "INSERT INTO circle_node(label_text, layout_x, layout_y) VALUES (?, ?, ?)";
        var connection = MySQLDatabase.getInstance().getConnection();
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, circleController.getLabel().getText());
            stmt.setDouble(2, circleController.getStackPane().getLayoutX());
            stmt.setDouble(3, circleController.getStackPane().getLayoutY());

            int rowsInserted = stmt.executeUpdate();
            log.info("Inserted "+rowsInserted+" circle node(s)");
            if(rowsInserted != 1 && rowsInserted != 0){
                throw new DataAccessException("Failed to create circle node");
            }
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
     * @throws DataAccessException if a database access error occurs
     */
    public static void deleteCircleNode(CircleController circleController) {
        String sql = "DELETE FROM circle_node WHERE label_text = ?";
        var connection = MySQLDatabase.getInstance().getConnection();
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, circleController.getLabel().getText());
            int rowsDeleted = stmt.executeUpdate();
            log.info("Deleted "+rowsDeleted+" circle node(s): "+circleController.getLabel().getText());
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
     * @throws DataAccessException if a database access error occurs
     */
    public static void updateCircleNode(CircleController circleController) {
        String sql = "UPDATE circle_node SET layout_x = ?, layout_y = ? WHERE label_text = ?";
        var connection = MySQLDatabase.getInstance().getConnection();
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, circleController.getStackPane().getLayoutX());
            stmt.setDouble(2, circleController.getStackPane().getLayoutY());
            stmt.setString(3, circleController.getLabel().getText());

            int rowsUpdated = stmt.executeUpdate();
            log.info("Updated "+rowsUpdated+" circle node(s): "+circleController.getLabel().getText());
            if(rowsUpdated != 1){
                throw new DataAccessException(
                        "Failed to update circle node with label: "
                                + circleController.getLabel().getText()
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Failed to update circle node with label: "
                            + circleController.getLabel().getText(), e
            );
        }
    }

    /**
     * Deletes all circle node records from the database.
     *
     * <p>Executes a DELETE statement on the `circle_nodes` table without a
     * WHERE clause, removing every record. Returns true if one or more rows
     * were deleted; returns false if the table was already empty.</p>
     *
     * @throws DataAccessException if a database access error occurs
     */
    public static void deleteAllCircleNodes() {
        String sql = "DELETE FROM circle_node";
        var connection = MySQLDatabase.getInstance().getConnection();
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
            log.info("Deleted all circle nodes");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete circle nodes", e);
        }
    }

    public static ResultSet getCircleNodeResultSet(){
        var sql = "SELECT * FROM circle_node";
        var connection = MySQLDatabase.getInstance().getConnection();
        PreparedStatement stmt;
        try {
            stmt = connection.prepareStatement(sql);
            log.info("Executing query: "+sql);
            return stmt.executeQuery();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get circle node ResultSet", e);
        }
    }

    public static void createArrow(Arrow arrow) {
        String sql = "INSERT INTO arrow(id, source_node_label, target_node_label) VALUES (?, ?, ?)";
        var connection = MySQLDatabase.getInstance().getConnection();
        try (var stmt = connection.prepareStatement(sql)) {
            var circleA = arrow.getStartNode().getLabel().getText();
            var circleB = arrow.getEndNode().getLabel().getText();
            stmt.setString(1, circleA + "->" + circleB);
            stmt.setString(2, circleA);
            stmt.setString(3, circleB);

            int rowsInserted = stmt.executeUpdate();
            log.info("Inserted "+rowsInserted+" arrow(s)");
            if(rowsInserted != 1 && rowsInserted != 0){
                throw new DataAccessException("Failed to create arrow");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to create circle node", e);
        }
    }

    public static void deleteArrow(Arrow arrow){
        var sql = "DELETE FROM arrow WHERE id = ?";
        var connection = MySQLDatabase.getInstance().getConnection();
        try (var stmt = connection.prepareStatement(sql)) {
            var id = arrow.getStartNode().getLabel().getText()+"->"+arrow.getEndNode().getLabel().getText();
            stmt.setString(1, id);
            int rowsDeleted = stmt.executeUpdate();
            log.info("Deleted "+rowsDeleted+" arrow(s)");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete arrow", e);
        }
    }

    public static ResultSet getArrowResultSet(){
        var sql = "SELECT * FROM arrow";
        var connection = MySQLDatabase.getInstance().getConnection();
        PreparedStatement stmt;
        try {
            stmt = connection.prepareStatement(sql);
            log.info("Executing query: "+sql);
            return stmt.executeQuery();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get arrow ResultSet", e);
        }
    }

    public static void deleteAllArrows(){
        var sql = "DELETE FROM arrow";
        var connection = MySQLDatabase.getInstance().getConnection();
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
            log.info("Deleted all arrows");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete arrows", e);
        }
    }

}