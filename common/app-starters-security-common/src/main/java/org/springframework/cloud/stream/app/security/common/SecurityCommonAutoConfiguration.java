package org.springframework.cloud.stream.app.security.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author Christian Tzolov
 * @author Artem Bilan
 */
@Configuration
public class SecurityCommonAutoConfiguration {

	/**
	 * The custom {@link WebSecurityConfigurerAdapter} to disable security in the application
	 * if {@code spring.cloud.security.enabled = false} (default).
	 * When {@code spring.cloud.security.enabled = true} then this configuration falls back to the default
	 * Spring Security configuration.
	 * @see org.springframework.boot.autoconfigure.security.servlet.SpringBootWebSecurityConfiguration
	 */
	@Configuration
	protected static class HttpSourceSecurityConfiguration extends WebSecurityConfigurerAdapter {

		@Value("${spring.cloud.security.enabled:false}")
		private Boolean enableSecurity;

		@Override
		protected void configure(HttpSecurity httpSecurity) throws Exception {
			if (!this.enableSecurity) {
				httpSecurity.requestMatcher(request -> false);
			}
			else {
				super.configure(httpSecurity);
			}
		}
	}
}
