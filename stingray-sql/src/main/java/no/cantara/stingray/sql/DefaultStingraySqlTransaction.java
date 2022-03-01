package no.cantara.stingray.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class DefaultStingraySqlTransaction implements StingraySqlTransaction {

    private static final Logger log = LoggerFactory.getLogger(DefaultStingraySqlTransaction.class);

    private final Connection connection;
    private final boolean previousAutoCommit;

    public DefaultStingraySqlTransaction(Connection connection) throws StingraySqlException {
        Objects.requireNonNull(connection);
        this.connection = connection;
        try {
            previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new StingraySqlException(e);
        }
    }

    public void commit() throws StingraySqlException {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new StingraySqlException(e);
        }
    }

    public void rollback() throws StingraySqlException {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new StingraySqlException(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            log.error("Rollback failed! While closing transaction. Transaction in unknown state.", e);
        } finally {
            try {
                connection.setAutoCommit(previousAutoCommit); // restore original connection autocommit state
            } catch (SQLException e) {
                log.error("Failed to restore previous autocommit state. While closing transaction.", e);
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("Connection close failed! While closing transaction.", e);
                }
            }
        }
    }
}
