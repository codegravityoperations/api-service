package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Idempotent migration: adds profile fields to the candidates table only if they
 * are not already present. Uses INFORMATION_SCHEMA to check existence because
 * MySQL 8.0 does not support ALTER TABLE ... DROP COLUMN IF EXISTS (MariaDB only).
 */
public class V4__add_candidate_profile_fields extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        String schema = connection.getCatalog();

        // Ordered map preserves AFTER positioning
        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("highest_education",  "VARCHAR(150)  NULL AFTER `notes`");
        columns.put("field_of_study",     "VARCHAR(150)  NULL AFTER `highest_education`");
        columns.put("work_authorization", "VARCHAR(100)  NULL AFTER `field_of_study`");
        columns.put("tools_technologies", "TEXT          NULL AFTER `work_authorization`");
        columns.put("accommodation_needed", "VARCHAR(255) NULL AFTER `tools_technologies`");

        for (Map.Entry<String, String> entry : columns.entrySet()) {
            if (!columnExists(connection, schema, entry.getKey())) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(
                        "ALTER TABLE `candidates` ADD COLUMN `" + entry.getKey() + "` " + entry.getValue()
                    );
                }
            }
        }
    }

    private boolean columnExists(Connection connection, String schema, String columnName)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                     "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'candidates' AND COLUMN_NAME = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schema);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
