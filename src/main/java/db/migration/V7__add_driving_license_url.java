package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.*;

/**
 * Adds driving_license_url column to candidates.
 * Uses INFORMATION_SCHEMA to stay idempotent — MySQL 8.0 does not support
 * ALTER TABLE ... ADD COLUMN IF NOT EXISTS.
 *
 * Note: ead_url was added by V6; this migration covers the driving license field
 * introduced in ICP-37.
 */
public class V7__add_driving_license_url extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        String schema = connection.getCatalog();

        if (!columnExists(connection, schema, "driving_license_url")) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(
                    "ALTER TABLE `candidates` ADD COLUMN `driving_license_url` VARCHAR(500) NULL AFTER `ead_url`"
                );
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
