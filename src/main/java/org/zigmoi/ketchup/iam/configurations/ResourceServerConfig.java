package org.zigmoi.ketchup.iam.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                //  .requestMatcher(new RequestHeaderRequestMatcher("Authorization"))
                .authorizeRequests()
                .antMatchers("/v1/release/pipeline/tekton-events**")
                .access("#oauth2.hasScope('tekton-event')")
                .antMatchers("/v1/release/git-webhook/*/listener-url/**")
                .access("#oauth2.hasScope('git-webhook')")
                .antMatchers("/**")
                .access("#oauth2.hasScope('all')");
    }
}