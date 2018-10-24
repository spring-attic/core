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
public class SecurityCommonConfiguration {


	/**
	 * The custom {@link WebSecurityConfigurerAdapter} to disable security in the application
	 * if {@code http.enableSecurity = false} (default).
	 * When {@code http.enableSecurity = true} and {@code http.enableCsrf = false} (default),
	 * the CSRF protection is disabled in the application.
	 * If both options are {@code true}, then this configuration falls back to the default
	 * Spring Security configuration.
	 * @see org.springframework.boot.autoconfigure.security.servlet.SpringBootWebSecurityConfiguration
	 */
	@Configuration
	protected static class HttpSourceSecurityConfiguration extends WebSecurityConfigurerAdapter {

		@Value("${spring.cloud.security.enabled:false}")
		private Boolean enableSecurity;

		//@Value("${spring.cloud.security.csrf.enabled:false}")
		//private Boolean enableCsrf;

		@Override
		protected void configure(HttpSecurity http) throws Exception {

			if (!this.enableSecurity) {
				http.requestMatcher(request -> false);
			}
			//else if (!this.enableCsrf) {
			//	super.configure(http);
			//	http.csrf().disable();
			//}
			else {
				super.configure(http);
			}
		}
	}
}
