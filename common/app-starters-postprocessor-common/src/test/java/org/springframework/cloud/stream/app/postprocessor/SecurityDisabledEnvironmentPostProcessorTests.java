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

import org.junit.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.cloud.stream.app.postprocessor.SecurityDisabledEnvironmentPostProcessor.AUTO_CONFIGURE_EXCLUDE_PROPERTY;
import static org.springframework.cloud.stream.app.postprocessor.SecurityDisabledEnvironmentPostProcessor.PROPERTY_SOURCE_KEY_NAME;
import static org.springframework.cloud.stream.app.postprocessor.SecurityDisabledEnvironmentPostProcessor.SECURITY_AUTO_CONFIGURATION_CLASS;
import static org.springframework.cloud.stream.app.postprocessor.SecurityDisabledEnvironmentPostProcessor.SECURITY_DISABLED_PROPERTY;

/**
 * @author Christian Tzolov
 */
public class SecurityDisabledEnvironmentPostProcessorTests {


	@Test
	public void testNoPropertySet() {

		PropertiesPropertySource testProperties = new PropertiesPropertySource("test-properties", new Properties());
		ConfigurableEnvironment configurableEnvironment = getEnvironment(testProperties);

		assertFalse("The " + SECURITY_DISABLED_PROPERTY + " should not be set", configurableEnvironment.containsProperty(SECURITY_DISABLED_PROPERTY));
		assertFalse("The " + AUTO_CONFIGURE_EXCLUDE_PROPERTY + " should not be set", configurableEnvironment.containsProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY));
	}

	@Test
	public void testSecurityDisabledPropertyTrue() {

		Properties properties = new Properties();
		properties.setProperty(SECURITY_DISABLED_PROPERTY, "true");
		PropertiesPropertySource testProperties = new PropertiesPropertySource("test-properties", properties);

		ConfigurableEnvironment configurableEnvironment = getEnvironment(testProperties);


		assertTrue("The " + SECURITY_DISABLED_PROPERTY + " should be set", configurableEnvironment.containsProperty(SECURITY_DISABLED_PROPERTY));
		assertThat("The " + SECURITY_DISABLED_PROPERTY + " should be set to TRUE",
				configurableEnvironment.getProperty(SECURITY_DISABLED_PROPERTY), equalTo("true"));

		assertTrue("The " + AUTO_CONFIGURE_EXCLUDE_PROPERTY + " should be set", configurableEnvironment.containsProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY));
		assertThat("The " + AUTO_CONFIGURE_EXCLUDE_PROPERTY + " should be set to TRUE",
				configurableEnvironment.getProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY), equalTo(SECURITY_AUTO_CONFIGURATION_CLASS));
	}


	@Test
	public void testSecurityDisabledPropertySet() {

		Properties properties = new Properties();
		properties.setProperty(SECURITY_DISABLED_PROPERTY, "");
		PropertiesPropertySource testProperties = new PropertiesPropertySource("test-properties", properties);

		ConfigurableEnvironment configurableEnvironment = getEnvironment(testProperties);


		assertTrue("The " + SECURITY_DISABLED_PROPERTY + " should be set", configurableEnvironment.containsProperty(SECURITY_DISABLED_PROPERTY));
		assertThat("The " + SECURITY_DISABLED_PROPERTY + " should be set",
				configurableEnvironment.getProperty(SECURITY_DISABLED_PROPERTY), equalTo(""));

		assertTrue("The " + AUTO_CONFIGURE_EXCLUDE_PROPERTY + " should be set", configurableEnvironment.containsProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY));
		assertThat("The " + AUTO_CONFIGURE_EXCLUDE_PROPERTY + " should be set to TRUE",
				configurableEnvironment.getProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY), equalTo(SECURITY_AUTO_CONFIGURATION_CLASS));

	}


	@Test
	public void testSecurityDisabledPropertyFalse() {

		Properties properties = new Properties();
		properties.setProperty(SECURITY_DISABLED_PROPERTY, "false");
		PropertiesPropertySource testProperties = new PropertiesPropertySource("test-properties", properties);

		ConfigurableEnvironment configurableEnvironment = getEnvironment(testProperties);


		assertTrue("The " + SECURITY_DISABLED_PROPERTY + " should be set", configurableEnvironment.containsProperty(SECURITY_DISABLED_PROPERTY));
		assertThat("The " + SECURITY_DISABLED_PROPERTY + " should be set",
				configurableEnvironment.getProperty(SECURITY_DISABLED_PROPERTY), equalTo("false"));

		assertFalse("The " + AUTO_CONFIGURE_EXCLUDE_PROPERTY + " should not be set", configurableEnvironment.containsProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY));
	}

	@Test
	public void enablePreviouslyDisabledSecurity() {

		Properties properties = new Properties();
		properties.setProperty(SECURITY_DISABLED_PROPERTY, "false"); // e.g. enable security

		Properties securityDisabledProperties = new Properties();
		securityDisabledProperties.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY, SECURITY_AUTO_CONFIGURATION_CLASS);

		ConfigurableEnvironment configurableEnvironment = getEnvironment(
				new PropertiesPropertySource("test_properties", properties),
				new PropertiesPropertySource(PROPERTY_SOURCE_KEY_NAME, securityDisabledProperties)); // note that the security is disabled in the context of the post-processor.

		assertTrue("The " + SECURITY_DISABLED_PROPERTY + " should be set", configurableEnvironment.containsProperty(SECURITY_DISABLED_PROPERTY));
		assertThat("The " + SECURITY_DISABLED_PROPERTY + " should be set",
				configurableEnvironment.getProperty(SECURITY_DISABLED_PROPERTY), equalTo("false"));

		assertFalse("The " + AUTO_CONFIGURE_EXCLUDE_PROPERTY + " should removed", configurableEnvironment.containsProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY));
	}

	@Test
	public void ignoreEnableForExternallyDisabledSecurity() {

		Properties properties = new Properties();
		properties.setProperty(SECURITY_DISABLED_PROPERTY, "false"); // e.g. enable security
		// The SecurityAutoConfiguration has been excluded outside the post-processor
		properties.setProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY, SECURITY_AUTO_CONFIGURATION_CLASS);

		ConfigurableEnvironment configurableEnvironment = getEnvironment(
				new PropertiesPropertySource("test_properties", properties));


		assertTrue("The " + SECURITY_DISABLED_PROPERTY + " should be set", configurableEnvironment.containsProperty(SECURITY_DISABLED_PROPERTY));
		assertThat("The " + SECURITY_DISABLED_PROPERTY + " should be set",
				configurableEnvironment.getProperty(SECURITY_DISABLED_PROPERTY), equalTo("false"));

		assertTrue("The " + AUTO_CONFIGURE_EXCLUDE_PROPERTY + " should removed", configurableEnvironment.containsProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY));
		assertThat("The " + AUTO_CONFIGURE_EXCLUDE_PROPERTY + " should not be managed by the post processor",
				configurableEnvironment.getProperty(AUTO_CONFIGURE_EXCLUDE_PROPERTY), equalTo(SECURITY_AUTO_CONFIGURATION_CLASS));
	}

	private ConfigurableEnvironment getEnvironment(PropertiesPropertySource... propertiesPropertySources) {

		SpringApplication springApplication = new SpringApplicationBuilder()
				.sources(SecurityDisabledEnvironmentPostProcessorTests.class)
				.web(WebApplicationType.NONE).build();

		ConfigurableApplicationContext context = springApplication.run();

		if (propertiesPropertySources != null) {
			for (PropertiesPropertySource propertySource : propertiesPropertySources) {
				context.getEnvironment().getPropertySources().addFirst(propertySource);
			}
		}

		new SecurityDisabledEnvironmentPostProcessor().postProcessEnvironment(context.getEnvironment(), springApplication);

		ConfigurableEnvironment configurableEnvironment = context.getEnvironment();
		context.close();

		return configurableEnvironment;
	}

}
