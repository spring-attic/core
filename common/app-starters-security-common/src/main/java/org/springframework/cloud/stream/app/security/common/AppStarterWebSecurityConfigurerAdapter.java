/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.security.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * App Starter default configuration for managing the apps web security.
 *
 * The {@code spring.cloud.streamapp.security.enabled} and
 * {@code spring.cloud.streamapp.security.csrf-enabled} can be used to customize adapter's behavior
 *
 * If the security is enabled (e.g. spring.cloud.streamapp.security.enabled = true) and the
 *  actuator dependency is on the classpath it falls back to {@link ManagementWebSecurityAutoConfiguration}
 * allowing unauthenticated access to the HealthEndpoint and InfoEndpoint.
 *
 * Setting {@code spring.cloud.streamapp.security.enabled = false} would surpass the security
 * for the entire application.
 *
 * Setting {@code spring.cloud.streamapp.security.csrf-enabled = false} disables the CSRF.
 *
 * If the user specifies their own {@link WebSecurityConfigurerAdapter}, this will back-off completely
 * and the user should specify all the bits that they want to configure as part of the custom security
 * configuration.
 *
 * @author Christian Tzolov
 * @author Artem Bilan
 *
 * @since 2.1
 */
@Configuration
@EnableConfigurationProperties(AppStarterWebSecurityAutoConfigurationProperties.class)
public class AppStarterWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

	@Autowired
	private AppStarterWebSecurityAutoConfigurationProperties securityProperties;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
		if (!this.securityProperties.isCsrfEnabled()) {
			http.csrf().disable();
		}
	}

	@Override
	public void configure(WebSecurity builder) {
		if (!this.securityProperties.isEnabled()) {
			builder.ignoring().antMatchers("/**");
		}
	}
}
