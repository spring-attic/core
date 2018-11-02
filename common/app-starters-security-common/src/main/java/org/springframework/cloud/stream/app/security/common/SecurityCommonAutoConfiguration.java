/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.stream.app.security.common;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author Christian Tzolov
 * @author Artem Bilan
 */
@Configuration
@AutoConfigureBefore(name = SecurityCommonAutoConfiguration.MANAGEMENT_WEB_SECURITY_AUTO_CONFIGURATION_CLASS)
public class SecurityCommonAutoConfiguration {

	public final static String MANAGEMENT_WEB_SECURITY_AUTO_CONFIGURATION_CLASS =
			"org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration";

	/**
	 * The custom {@link WebSecurityConfigurerAdapter} to disable security in the application
	 * if {@code spring.cloud.security.enabled = false}.
	 * When {@code spring.cloud.security.enabled = true} (default) then this configuration falls back to the default
	 * Spring Security configuration.
	 * @see org.springframework.boot.autoconfigure.security.servlet.SpringBootWebSecurityConfiguration
	 */
	@Configuration
	@ConditionalOnProperty(name = "spring.cloud.security.enabled", havingValue = "false", matchIfMissing = true)
	protected static class DisableSecurityConfiguration extends WebSecurityConfigurerAdapter {

		@Override
		public void configure(WebSecurity builder) {
			builder.ignoring().antMatchers("/**");
		}
	}

	//@Configuration
	//@Conditional(OnHttpCsrfSecurityDisabled.class)
	//protected static class DisableHttpCsrfSecurityConfiguration extends WebSecurityConfigurerAdapter {
	//
	//	@Override
	//	protected void configure(HttpSecurity http) throws Exception {
	//		super.configure(http);
	//		http.csrf().disable();
	//	}
	//}
	//
	//public static class OnHttpCsrfSecurityDisabled extends AllNestedConditions {
	//	public OnHttpCsrfSecurityDisabled() {
	//		super(ConfigurationPhase.REGISTER_BEAN);
	//	}
	//
	//	@ConditionalOnProperty(name = "spring.cloud.security.enabled", havingValue = "true", matchIfMissing = true)
	//	static class SecurityEnabled {
	//	}
	//
	//	@ConditionalOnProperty(name = "spring.cloud.security.csrf.enabled", havingValue = "false")
	//	static class HttpCsrfDisabled {
	//	}
	//}
}
