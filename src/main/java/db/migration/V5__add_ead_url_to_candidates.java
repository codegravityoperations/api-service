package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.*;

public class V5__add_ead_url_to_candidates extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        String schema = connection.getCatalog();

        if (!columnExists(connection, schema, "ead_url")) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("ALTER TABLE `candidates` ADD COLUMN `ead_url` VARCHAR(500) NULL AFTER `resume_url`");
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
