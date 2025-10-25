package com.flex.interpre.global.config;
import com.pgvector.PGvector;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
public class PgvectorConfig {

    // 앱 시작 시 DataSource에 pgvector의 vector 타입 등록
    @Bean
    ApplicationRunner applicationRunner(DataSource dataSource) {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {
                PGvector.addVectorType(connection);
            } catch (SQLException e) {
                throw new RuntimeException("pgvector 타입 등록에 실패했습니다.", e);
            }
        };
    }
}