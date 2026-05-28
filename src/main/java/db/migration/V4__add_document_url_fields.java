package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Adds ead_url and driving_license_url columns to candidates.
 * Uses INFORMATION_SCHEMA to stay idempotent — MySQL 8.0 does not support
 * ALTER TABLE ... ADD COLUMN IF NOT EXISTS.
 */
public class V4__add_document_url_fields extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        String schema = connection.getCatalog();

        Map<String, String> columns = new LinkedHashMap<>();
        columns.put("ead_url",              "VARCHAR(500) NULL AFTER `resume_url`");
        columns.put("driving_license_url",  "VARCHAR(500) NULL AFTER `ead_url`");

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
