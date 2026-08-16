package com.portfolio.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Migration ponctuelle et idempotente : sur les bases existantes, la colonne
 * {@code event.category} a été créée par Hibernate 6.3 comme un type ENUM natif
 * MySQL figé sur l'ancienne liste de catégories. {@code ddl-auto=update} ne la
 * met pas à jour, si bien que les nouvelles valeurs (BAPTEME, HENNE, …)
 * provoquent une erreur « Data truncated for column 'category' ».
 * <p>
 * Ce runner convertit la colonne en {@code VARCHAR(50)} — les valeurs textuelles
 * existantes sont préservées. Une fois convertie, il ne fait plus rien.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryColumnMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            String dataType = jdbcTemplate.queryForObject(
                "SELECT DATA_TYPE FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'event' AND COLUMN_NAME = 'category'",
                String.class);

            if (dataType != null && dataType.equalsIgnoreCase("enum")) {
                jdbcTemplate.execute("ALTER TABLE event MODIFY COLUMN category VARCHAR(50)");
                log.info("Migration : colonne event.category convertie de ENUM vers VARCHAR(50)");
            }
        } catch (Exception e) {
            // Ne bloque pas le démarrage : on log l'avertissement pour investigation.
            log.warn("Migration event.category ignorée ({})", e.getMessage());
        }
    }
}
