package no.cantara.stingray.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public interface StingraySqlDatasource extends StingraySqlTransactionFactory {

    /**
     * Get the datasource. This should be a connection-pool configured to use the applications configured postgres database.
     *
     * @return
     */
    DataSource getDataSource();

    /**
     * @return Information about the datasource configuration. Used for debugging and health-info.
     */
    String info();

    /**
     * Close connection-pool and/or data-sources as returned by getDataSource().
     */
    void close();

    @Override
    default StingraySqlTransaction createTransaction() throws StingraySqlException {
        DataSource dataSource = getDataSource();
        Connection connection;
        try {
            connection = dataSource.getConnection();
            if (!connection.getAutoCommit()) {
                try {
                    connection.rollback(); // ensure a clean connection before starting transaction
                } catch (SQLException e) {
                    final Logger log = LoggerFactory.getLogger(StingraySqlDatasource.class);
                    log.warn("While preparing transaction by rolling back any outstanding uncommitted work.", e);
                }
            }
        } catch (SQLException e) {
            throw new StingraySqlException(e);
        }
        return new DefaultStingraySqlTransaction(connection);
    }
}
