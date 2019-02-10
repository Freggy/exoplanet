package de.karlsruhe.hhs.exoplanet.station;

import de.karlsruhe.hhs.exoplanet.shared.Measure;
import de.karlsruhe.hhs.exoplanet.shared.Position;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Yannic Rieger
 */
public class DataAccess {

    private static final String INSERT_QUERY = "INSERT INTO data (x, y, ground, temp) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE ground = VALUES(ground), temp = VALUES(temp)";

    private final String host;
    private final String database;
    private final String username;
    private final String password;
    private final int port;
    private Connection connection;

    public DataAccess(final String host, final int port, final String database, final String username, final String password) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    public void connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
        } catch (final SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void writeData(final Measure measure, final Position position) {
        try (final PreparedStatement statement = this.connection.prepareStatement(INSERT_QUERY)) {
            statement.setObject(1, position.getX());
            statement.setObject(2, position.getY());
            statement.setObject(3, measure.getGround().name());
            statement.setObject(4, measure.getTemperature());
            statement.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            this.connection.close();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }
}
