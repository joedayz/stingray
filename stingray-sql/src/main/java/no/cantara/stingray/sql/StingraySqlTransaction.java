package no.cantara.stingray.sql;

import java.sql.Connection;

public interface StingraySqlTransaction extends AutoCloseable {

    void commit();

    void rollback();

    Connection getConnection();

    /**
     * Closing the transaction will roll back any uncommitted work and close all related transaction resources
     * including the underlying connection.
     */
    @Override
    void close();
}
