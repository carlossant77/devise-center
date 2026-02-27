package br.com.devisecenter.devise_center.infra.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DatabaseKeepAlive {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseKeepAlive.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseKeepAlive(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(cron = "0 0 3 */3 * ?")
    public void keepDatabaseAlive() {
        try {
            jdbcTemplate.execute("SELECT 1");
            logger.info("Keep-alive query executada com sucesso no Supabase.");
        } catch (Exception e) {
            logger.error("Falha ao executar keep-alive no banco de dados: {}", e.getMessage());
        }
    }
}
