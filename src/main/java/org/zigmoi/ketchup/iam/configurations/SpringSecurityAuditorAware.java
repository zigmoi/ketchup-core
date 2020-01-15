package org.zigmoi.ketchup.iam.configurations;

import org.springframework.data.domain.AuditorAware;
import org.zigmoi.ketchup.iam.commons.AuthUtils;

import java.util.Optional;

class SpringSecurityAuditorAware implements AuditorAware<String> {

  @Override
  public Optional<String> getCurrentAuditor() {
    return Optional.of( AuthUtils.getCurrentQualifiedUsername());
  }
}