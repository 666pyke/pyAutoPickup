package org.me.pyke.pyautopickup.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class Database {
    private final String filePath;
    private final String url;
    private Connection conn;

    public Database(String filePath) {
        this.filePath = filePath;
        this.url = "jdbc:sqlite:" + filePath;
    }

    public void open() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver is missing from the plugin jar", e);
        }

        File parent = new File(filePath).getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new SQLException("Could not create plugin data folder: " + parent.getAbsolutePath());
        }

        conn = DriverManager.getConnection(url);
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS toggles (" +
                            "uuid TEXT PRIMARY KEY," +
                            "enabled INTEGER NOT NULL DEFAULT 1," +
                            "notify_enabled INTEGER NOT NULL DEFAULT 1" +
                            ")"
            );
        }

        try (Statement st = conn.createStatement()) {
            st.executeUpdate("ALTER TABLE toggles ADD COLUMN notify_enabled INTEGER NOT NULL DEFAULT 1");
        } catch (SQLException ignored) {
            // Existing installations already have this column.
        }
    }

    public void close() {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException ignored) {
        }
    }

    public boolean isOpen() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException ignored) {
            return false;
        }
    }

    public boolean getEnabled(UUID uuid, boolean defaultValue) throws SQLException {
        if (!isOpen()) return defaultValue;
        try (PreparedStatement ps = conn.prepareStatement("SELECT enabled FROM toggles WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("enabled") == 1;
            }
        }
        return defaultValue;
    }

    public void setEnabled(UUID uuid, boolean enabled) throws SQLException {
        if (!isOpen()) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO toggles(uuid, enabled, notify_enabled) VALUES(?,?,COALESCE((SELECT notify_enabled FROM toggles WHERE uuid=?),1)) " +
                        "ON CONFLICT(uuid) DO UPDATE SET enabled=excluded.enabled")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, enabled ? 1 : 0);
            ps.setString(3, uuid.toString());
            ps.executeUpdate();
        }
    }

    public boolean getNotifyEnabled(UUID uuid) throws SQLException {
        if (!isOpen()) return true;
        try (PreparedStatement ps = conn.prepareStatement("SELECT notify_enabled FROM toggles WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("notify_enabled") == 1;
            }
        }
        return true;
    }

    public void setNotifyEnabled(UUID uuid, boolean notifyEnabled, boolean defaultPickupEnabled) throws SQLException {
        if (!isOpen()) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO toggles(uuid, enabled, notify_enabled) VALUES(?, COALESCE((SELECT enabled FROM toggles WHERE uuid=?),?), ?) " +
                        "ON CONFLICT(uuid) DO UPDATE SET notify_enabled=excluded.notify_enabled")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, uuid.toString());
            ps.setInt(3, defaultPickupEnabled ? 1 : 0);
            ps.setInt(4, notifyEnabled ? 1 : 0);
            ps.executeUpdate();
        }
    }
}
