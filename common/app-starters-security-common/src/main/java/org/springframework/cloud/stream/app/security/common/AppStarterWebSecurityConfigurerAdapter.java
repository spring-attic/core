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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.endpoint.BindingsEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 *
 * App Starter default configuration for web security when the actuator dependency is on the
 * classpath.
 * By default it allows unauthenticated access to the {@link HealthEndpoint}, {@link LogFileWebEndpoint}
 * {@link InfoEndpoint} and {@link BindingsEndpoint}.
 *
 * The {@code spring.cloud.stream.security.enabled} and {@code spring.cloud.stream.security.csrf-enabled}
 * can be used to customize adapter's behavior
 * Setting {@code spring.cloud.stream.security.enabled = false} would surpass the security for the entire application.
 * For secured application setting {@code spring.cloud.stream.security.csrf-enabled = false} disables CSRF.
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
		http.authorizeRequests()
				.requestMatchers(
						EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class,
								LogFileWebEndpoint.class, BindingsEndpoint.class))
				.permitAll().anyRequest().authenticated()
				.and().formLogin()
				.and().httpBasic();

		if (this.securityProperties.isCsrfEnabled() == false) {
			http.csrf().disable();
		}
	}

	@Override
	public void configure(WebSecurity builder) {
		if (this.securityProperties.isEnabled() == false) {
			builder.ignoring().antMatchers("/**");
		}
	}
}
