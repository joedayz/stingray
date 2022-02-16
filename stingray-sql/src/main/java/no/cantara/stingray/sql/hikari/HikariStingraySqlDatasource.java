package no.cantara.stingray.sql.hikari;

import com.zaxxer.hikari.HikariDataSource;
import no.cantara.stingray.sql.StingraySqlDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HikariStingraySqlDatasource implements StingraySqlDatasource {
    private static final Logger log = LoggerFactory.getLogger(HikariStingraySqlDatasource.class);

    final HikariDataSource dataSource;

    public HikariStingraySqlDatasource(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public String info() {
        return dataSource.getJdbcUrl();
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
