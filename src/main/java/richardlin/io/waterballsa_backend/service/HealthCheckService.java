package richardlin.io.waterballsa_backend.service;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class HealthCheckService {

    private final DataSource dataSource;

    public HealthCheckService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Object> checkHealth() {
        Map<String, Object> health = new HashMap<>();
        boolean databaseHealthy = checkDatabase();

        health.put("status", databaseHealthy ? "UP" : "DOWN");
        health.put("database", databaseHealthy ? "UP" : "DOWN");

        return health;
    }

    private boolean checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}
