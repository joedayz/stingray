package no.cantara.stingray.sql.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import no.cantara.config.ApplicationProperties;
import no.cantara.stingray.sql.StingraySqlDatasourceFactory;

import java.util.Properties;

public class HikariStingraySqlDatasourceFactory implements StingraySqlDatasourceFactory {
    @Override
    public Class<?> providerClass() {
        return HikariStingraySqlDatasource.class;
    }

    @Override
    public String alias() {
        return "hikari";
    }

    @Override
    public HikariStingraySqlDatasource create(ApplicationProperties ap) {
        Properties props = new Properties();
        props.putAll(ap.map());
        HikariConfig config = new HikariConfig(props);
        HikariDataSource hikariDataSource = new HikariDataSource(config);
        HikariStingraySqlDatasource hikariStingraySqlDatasource = new HikariStingraySqlDatasource(hikariDataSource);
        return hikariStingraySqlDatasource;
    }
}
