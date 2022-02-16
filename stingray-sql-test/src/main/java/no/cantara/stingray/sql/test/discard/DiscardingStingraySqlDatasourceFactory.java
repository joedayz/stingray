package no.cantara.stingray.sql.test.discard;

import no.cantara.config.ApplicationProperties;
import no.cantara.stingray.sql.StingraySqlDatasourceFactory;

public class DiscardingStingraySqlDatasourceFactory implements StingraySqlDatasourceFactory {
    @Override
    public Class<?> providerClass() {
        return DiscardingStingraySqlDatasource.class;
    }

    @Override
    public String alias() {
        return "discard";
    }

    @Override
    public DiscardingStingraySqlDatasource create(ApplicationProperties config) {
        return new DiscardingStingraySqlDatasource();
    }
}
