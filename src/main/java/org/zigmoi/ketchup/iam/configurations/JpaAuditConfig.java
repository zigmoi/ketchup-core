package org.zigmoi.ketchup.iam.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
class JpaAuditConfig {

  @Bean
  public AuditorAware<String> auditorProvider() {
    return new SpringSecurityAuditorAware();
  }
}