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

import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.WebSecurityEnablerConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author Christian Tzolov
 * @author Artem Bilan
 *
 * @since 2.1
 */
@Configuration
@ConditionalOnClass(WebSecurityConfigurerAdapter.class)
@ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.ANY)
@Conditional(AppStarterWebSecurityAutoConfiguration.OnHttpCsrfOrSecurityDisabled.class)
@AutoConfigureBefore(value = { ManagementWebSecurityAutoConfiguration.class, SecurityAutoConfiguration.class })
@Import({ AppStarterWebSecurityConfigurerAdapter.class, WebSecurityEnablerConfiguration.class })
public class AppStarterWebSecurityAutoConfiguration {

	public static class OnHttpCsrfOrSecurityDisabled extends AnyNestedCondition {
		public OnHttpCsrfOrSecurityDisabled() {
			super(ConfigurationCondition.ConfigurationPhase.PARSE_CONFIGURATION);
		}

		@ConditionalOnProperty(name = "spring.cloud.streamapp.security.enabled", havingValue = "false")
		static class SecurityDisabled {
		}

		@ConditionalOnProperty(name = "spring.cloud.streamapp.security.csrf-enabled", havingValue = "false")
		static class HttpCsrfDisabled {
		}
	}
}
