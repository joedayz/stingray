package no.cantara.stingray.sql;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class StingrayFlywayMigrationHelper {
    private static final Logger log = LoggerFactory.getLogger(StingrayFlywayMigrationHelper.class);

    private final Flyway flyway;
    private final String info;

    private final StingraySqlDatasource stingraySqlDatasource;

    public static StingrayFlywayMigrationHelper defaultCreation(
            String applicationAlias,
            StingraySqlDatasource stingraySqlDatasource,
            String migrationDatabase,
            String migrationUser,
            String migrationPassword,
            String database,
            String user,
            String password) {
        return forCreation(applicationAlias, "db/default-creation", stingraySqlDatasource, migrationDatabase, migrationUser, migrationPassword, database, user, password);
    }

    public static StingrayFlywayMigrationHelper forCreation(
            String applicationAlias,
            String flywayMigrationFolder,
            StingraySqlDatasource stingraySqlDatasource,
            String migrationDatabase,
            String migrationUser,
            String migrationPassword,
            String database,
            String user,
            String password) {
        Map<String, String> placeholders = new LinkedHashMap<>();
        placeholders.put("migration.database", migrationDatabase);
        placeholders.put("migration.user", migrationUser);
        placeholders.put("migration.password", migrationPassword);
        placeholders.put("app.database", database);
        placeholders.put("app.user", user);
        placeholders.put("app.password", password);
        return new StingrayFlywayMigrationHelper(stingraySqlDatasource, flywayMigrationFolder, placeholders, applicationAlias);
    }

    public static StingrayFlywayMigrationHelper forMigration(
            String flywayMigrationFolder,
            StingraySqlDatasource stingraySqlDatasource,
            String user) {
        Map<String, String> placeholders = new LinkedHashMap<>();
        placeholders.put("app.user", user);
        return new StingrayFlywayMigrationHelper(stingraySqlDatasource, flywayMigrationFolder, placeholders);
    }

    public StingrayFlywayMigrationHelper(StingraySqlDatasource stingraySqlDatasource, String flywayMigrationFolder, Map<String, String> placeholders, String applicationAlias) {
        this.stingraySqlDatasource = stingraySqlDatasource;
        this.info = stingraySqlDatasource.info();
        flyway = Flyway.configure()
                .baselineOnMigrate(false)
                .locations(flywayMigrationFolder)
                .table("schema_version")
                .schemas(applicationAlias)
                .createSchemas(true)
                .placeholders(placeholders)
                .dataSource(stingraySqlDatasource.getDataSource()).load();
    }

    public StingrayFlywayMigrationHelper(StingraySqlDatasource stingraySqlDatasource, String flywayMigrationFolder, Map<String, String> placeholders) {
        this.stingraySqlDatasource = stingraySqlDatasource;
        this.info = stingraySqlDatasource.info();
        flyway = Flyway.configure()
                .baselineOnMigrate(false)
                .locations(flywayMigrationFolder)
                .table("schema_version")
                .placeholders(placeholders)
                .dataSource(stingraySqlDatasource.getDataSource()).load();
    }

    public void upgradeDatabase() {
        log.info("Upgrading database {} using migration files from {}", info, flyway.getConfiguration().getLocations());
        try {
            flyway.migrate();
        } catch (FlywayException e) {
            log.error("Database upgrade failed using " + info, e);
        }
    }

    //used by tests
    public void cleanDatabase() {
        try {
            flyway.clean();
        } catch (FlywayException e) {
            throw new RuntimeException("Database cleaning failed.", e);
        }
    }
}