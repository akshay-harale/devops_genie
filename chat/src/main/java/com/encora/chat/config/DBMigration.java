//package com.encora.chat.config;
//
//import org.flywaydb.core.Flyway;
//import org.flywaydb.core.api.MigrationVersion;
//import org.flywaydb.core.api.output.MigrateResult;
//import org.hibernate.exception.SQLGrammarException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.scheduling.annotation.EnableAsync;
//
//import javax.annotation.PostConstruct;
//
//@Configuration
//@EnableAsync
//public class DBMigration {
//    private static final Logger logger = LoggerFactory.getLogger(DBMigration.class);
//
//
//    private final JdbcTemplate jdbcTemplate;
//
//    public DBMigration(JdbcTemplate jdbcTemplate) {
//        this.jdbcTemplate = jdbcTemplate;
//    }
//
//    @PostConstruct
//    public void migrateCommonScriptsIntoSnowflake() {
//        try {
//            logger.info("Apply flyway migration");
//            MigrateResult flyway = Flyway.configure()
//                    .dataSource(jdbcTemplate.getDataSource())
//                    .schemas("public")
//                    .locations("classpath:db/migration/")
//                    .target(MigrationVersion.LATEST)
//                    .load()
//                    .migrate();
//            logger.info(flyway.toString());
//        } catch (SQLGrammarException sqlGrammarException) {
//            logger.error("Error while migrating common script into snowflake: {}", sqlGrammarException.getMessage());
//        }
//    }
//
//}
