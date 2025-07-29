package com.yashgamerx.cognitive_thought_network_simulation.storage;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton for obtaining a JDBC Connection to the MySQL database.
 *
 * <p>This class uses double-checked locking to create a single instance
 * in a thread-safe manner. It dynamically loads the MySQL JDBC driver and
 * establishes a Connection to the specified database URL with credentials.</p>
 */
public class MySQLDatabase {

    /**
     * JDBC URL of the target MySQL database, including host, port, and schema.
     */
    private static final String URL =
            "jdbc:mysql://localhost:3306/cognitive_thought_network_simulation";

    /**
     * Username for authenticating with the MySQL database.
     */
    private static final String USERNAME = "root";

    /**
     * Password for authenticating with the MySQL database.
     */
    private static final String PASSWORD = "root";

    /**
     * Driver class name for the MySQL Connector/J.
     */
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    /**
     * The live JDBC Connection to the database.
     */
    @Getter
    private final Connection connection;

    /**
     * The singleton instance of this class.
     */
    private static volatile MySQLDatabase instance;

    /**
     * Private constructor that loads the JDBC driver and opens the Connection.
     *
     * @throws RuntimeException if the driver class cannot be loaded or the connection fails
     */
    private MySQLDatabase() {
        try {
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(
                    URL + "?user=" + USERNAME + "&password=" + PASSWORD
            );
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Failed to initialize MySQL connection", e);
        }
    }

    /**
     * Returns the singleton instance, creating it if necessary.
     *
     * <p>This method uses double-checked locking for thread safety
     * and minimal synchronization overhead.</p>
     *
     * @return the single MySQLDatabase instance
     */
    public static MySQLDatabase getInstance() {
        if (instance == null) {
            synchronized (MySQLDatabase.class) {
                if (instance == null) {
                    instance = new MySQLDatabase();
                }
            }
        }
        return instance;
    }
}