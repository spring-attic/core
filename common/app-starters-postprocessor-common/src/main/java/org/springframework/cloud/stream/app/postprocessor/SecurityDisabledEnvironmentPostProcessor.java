/*
 * Copyright 2018 the original author or authors.
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

package org.springframework.cloud.stream.app.postprocessor;

import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.StringUtils;

/**
 * With SpringBoot 2+ the security is enabled by default. To disable it one can exclude the
 * {@link org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration} using the
 * `spring.autoconfigure.exclude` property. For example:
 * (--spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration)
 *
 * Conversely if the SecurityAutoConfiguration is already excluded, the security can be re-enabled by overriding and
 * removing the SecurityAutoConfiguration form the spring.autoconfigure.exclude.
 *
 * {@link SecurityDisabledEnvironmentPostProcessor} allows controlling the security enabled/disabled behavior with the help of a
 * dedicated boolean property: `spring.cloud.security.disabled`.
 *
 *  - If the `spring.cloud.security.disabled` property is not used the {@link SecurityDisabledEnvironmentPostProcessor} is ignored.
 *
 *  - If the `spring.cloud.security.disabled` (or `spring.cloud.security.disabled=true`) is set then the result behavior is equivalent
 * to: spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
 *
 *  - If the `spring.cloud.security.disabled=false` is used the result behavior is equivalent to removing spring.autoconfigure.exclude
 *  from the property source.
 *
 *
 *  Note: the PostProcessor will not alter any spring.autoconfigure.exclude set outside the scope of the post-processor.
 *
 * @author Christian Tzolov
 */
public class SecurityDisabledEnvironmentPostProcessor implements EnvironmentPostProcessor {

	public static final String SECURITY_DISABLED_PROPERTY = "spring.cloud.security.disabled";
	protected static final String PROPERTY_SOURCE_KEY_NAME = SecurityDisabledEnvironmentPostProcessor.class.getName();
	protected static final String AUTO_CONFIGURE_EXCLUDE_PROPERTY = "spring.autoconfigure.exclude";
	protected static final String SECURITY_AUTO_CONFIGURATION_CLASS = "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration";

	public SecurityDisabledEnvironmentPostProcessor() {
		super();
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication springApplication) {

		// If the SECURITY_DISABLED_PROPERTY is not used, ignore the post processor al together.
		if (!environment.containsProperty(SECURITY_DISABLED_PROPERTY)) {
			environment.getPropertySources().remove(PROPERTY_SOURCE_KEY_NAME); // clean any previous settings
		}
		else if (isSecurityAutoConfigurationExcluded(environment)) {
			if (!isSecurityDisabled(environment)) {
				// Remove the exclusion if it was done by this post-processor.
				environment.getPropertySources().remove(PROPERTY_SOURCE_KEY_NAME);
			}
		}
		else if (isSecurityDisabled(environment)) {
			excludeSecurityAutoConfiguration(environment);
		}
	}

	private void excludeSecurityAutoConfiguration(ConfigurableEnvironment environment) {
		Properties properties = new Properties();
		properties.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY, SECURITY_AUTO_CONFIGURATION_CLASS);
		PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource(PROPERTY_SOURCE_KEY_NAME, properties);
		environment.getPropertySources().addLast(propertiesPropertySource);
	}

	private boolean isSecurityAutoConfigurationExcluded(ConfigurableEnvironment environment) {
		return environment.containsProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY) &&
				environment.getProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY).contains(SECURITY_AUTO_CONFIGURATION_CLASS);
	}

	private boolean isSecurityDisabled(ConfigurableEnvironment environment) {
		String propertyValue = environment.getProperty(SECURITY_DISABLED_PROPERTY);
		return StringUtils.hasText(propertyValue) ? Boolean.valueOf(propertyValue) : true;
	}
}
