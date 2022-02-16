package no.cantara.stingray.sql.test.embedded;

import no.cantara.config.ApplicationProperties;
import no.cantara.config.ProviderLoader;
import no.cantara.stingray.sql.StingrayFlywayMigrationHelper;
import no.cantara.stingray.sql.StingraySqlDatasource;
import no.cantara.stingray.sql.StingraySqlDatasourceFactory;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmbeddedPostgresStingraySqlDatasourceTest {

    private void createDatabase(ApplicationProperties config) {
        StingraySqlDatasource stingraySqlDatasource = ProviderLoader.configure(config.subTree("flyway.creation.config"), "embedded", StingraySqlDatasourceFactory.class);
        StingrayFlywayMigrationHelper flywayMigrationHelper = StingrayFlywayMigrationHelper.defaultCreation(
                "embeddedpgtest", stingraySqlDatasource,
                config.get("flyway.migration.config.dataSource.databaseName"),
                config.get("flyway.migration.config.dataSource.user"),
                config.get("flyway.migration.config.dataSource.password"),
                config.get("database.config.dataSource.databaseName"),
                config.get("database.config.dataSource.user"),
                config.get("database.config.dataSource.password")
        );
        flywayMigrationHelper.upgradeDatabase();
        stingraySqlDatasource.close();
    }

    private void migrateDatabase(ApplicationProperties config) {
        StingraySqlDatasource stingraySqlDatasource = ProviderLoader.configure(config.subTree("flyway.migration.config"), "embedded", StingraySqlDatasourceFactory.class);
        StingrayFlywayMigrationHelper flywayMigrationHelper = StingrayFlywayMigrationHelper.forMigration(
                "db/migration", stingraySqlDatasource,
                config.get("database.config.dataSource.user")
        );
        flywayMigrationHelper.upgradeDatabase();
        stingraySqlDatasource.close();
    }

    @Test
    public void thatEmbeddedDataSourceWorks() throws SQLException {
        ApplicationProperties config = ApplicationProperties.builder()
                .classpathPropertiesFile("embedded.properties")
                .build();
        createDatabase(config);
        migrateDatabase(config);
        StingraySqlDatasource stingraySqlDatasource = ProviderLoader.configure(config.subTree("database.config"), "embedded", StingraySqlDatasourceFactory.class);
        try {
            DataSource dataSource = stingraySqlDatasource.getDataSource();
            String generatedPersonId = UUID.randomUUID().toString();
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement ps = connection.prepareStatement("INSERT INTO Person(person_id, firstname, lastname) VALUES(?,?,?)")) {
                    ps.setString(1, generatedPersonId);
                    ps.setString(2, "John");
                    ps.setString(3, "Smith");
                    int n = ps.executeUpdate();
                    assertEquals(1, n);
                }
                try (PreparedStatement ps = connection.prepareStatement("SELECT person_id, firstname, lastname FROM Person WHERE person_id = ?")) {
                    ps.setString(1, generatedPersonId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String personId = rs.getString(1);
                            String firstname = rs.getString(2);
                            String lastname = rs.getString(3);
                            assertEquals(generatedPersonId, personId);
                            assertEquals("John", firstname);
                            assertEquals("Smith", lastname);
                        }
                    }
                }
            }
        } finally {
            stingraySqlDatasource.close();
        }
    }
}
