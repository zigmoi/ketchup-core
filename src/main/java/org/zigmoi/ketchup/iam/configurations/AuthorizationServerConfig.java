package org.zigmoi.ketchup.iam.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

	@Qualifier("authenticationManagerBean")
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.authenticationManager(authenticationManager).tokenStore(tokenStore())
				.accessTokenConverter(jwtTokenConverter());
	}

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory()
				.withClient("client-id-1").secret(passwordEncoder.encode("client-id-1-secret"))
				.authorizedGrantTypes("client_credentials", "password").scopes("all").accessTokenValiditySeconds(36000)
				.and()
				.withClient("client-id-forever-active").secret(passwordEncoder.encode("client-secret-forever-active"))
				.authorizedGrantTypes("client_credentials", "password").scopes("git-webhook", "tekton-event").accessTokenValiditySeconds(-1);
	}

	@Autowired
	private Environment environment;

	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(jwtTokenConverter());
	}

	@Bean
	protected JwtAccessTokenConverter jwtTokenConverter() {
//		String keystoreLocation = environment.getProperty("keystore.location");
//		String keystorePassword = environment.getProperty("keystore.password");
//		KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new FileSystemResource(keystoreLocation),
//				keystorePassword.toCharArray());
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
//		converter.setKeyPair(keyStoreKeyFactory.getKeyPair("ketchupjwt"));
		converter.setSigningKey("test");
		return converter;
	}

}
