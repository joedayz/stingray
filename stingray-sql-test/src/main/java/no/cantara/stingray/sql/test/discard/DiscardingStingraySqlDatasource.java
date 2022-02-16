package no.cantara.stingray.sql.test.discard;

import no.cantara.stingray.sql.StingraySqlDatasource;

import javax.sql.DataSource;

public class DiscardingStingraySqlDatasource implements StingraySqlDatasource {

    private final DiscardingDataSource dataSource;

    public DiscardingStingraySqlDatasource() {
        dataSource = new DiscardingDataSource();
    }

    public DataSource getDataSource() {
        return new DiscardingDataSource();
    }

    @Override
    public String info() {
        return "discarding-postgres-to-be-used-only-for-testing";
    }

    @Override
    public void close() {
    }
}
