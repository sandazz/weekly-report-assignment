package com.weeklyreport.backend.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

// This Spring Boot version has no built-in Flyway auto-configuration, so it's wired manually here.
// Skipped under the "test" profile: tests run against H2, and this Postgres-flavored SQL doesn't
// need to (and can't reliably) run there - Hibernate's ddl-auto=create-drop builds the test schema.
@Configuration
@Profile("!test")
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .load();
    }

    // Forces the "flyway" bean (and thus the migration) to run before
    // entityManagerFactory validates the schema.
    @Bean
    public static BeanFactoryPostProcessor flywayDependsOnEntityManagerFactoryPostProcessor() {
        return (ConfigurableListableBeanFactory beanFactory) -> {
            if (beanFactory.containsBeanDefinition("entityManagerFactory")) {
                BeanDefinition definition = beanFactory.getBeanDefinition("entityManagerFactory");
                definition.setDependsOn("flyway");
            }
        };
    }
}
