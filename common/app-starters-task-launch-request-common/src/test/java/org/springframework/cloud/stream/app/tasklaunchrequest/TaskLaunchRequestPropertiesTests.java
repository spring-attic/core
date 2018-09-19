/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.tasklaunchrequest;

import java.util.List;

import org.junit.Test;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cloud.stream.config.SpelExpressionConverterConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.config.EnableIntegration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * @author David Turanski
 **/
public class TaskLaunchRequestPropertiesTests {
	@Test
	public void batchUriCanBeCustomized() {
		TaskLaunchRequestProperties properties = getBatchProperties("task.launch.request.resourceUri:uri:/somewhere");
		assertThat(properties.getResourceUri()).isEqualTo("uri:/somewhere");
	}

	@Test(expected = AssertionError.class)
	public void batchUriIsRequired() {
		validateRequiredProperty("sftp.task.resourceUri");
	}

	@Test
	public void dataSourceUrlCanBeCustomized() {
		TaskLaunchRequestProperties properties = getBatchProperties(
			"task.launch.request..dataSourceUrl:jdbc:h2:tcp://localhost/mem:df");
		assertThat(properties.getDataSourceUrl()).isEqualTo("jdbc:h2:tcp://localhost/mem:df");
	}

	@Test(expected = AssertionError.class)
	public void dataSourceUrlIsRequired() {
		validateRequiredProperty("task.launch.request.dataSourceUrl");
	}

	@Test
	public void dataSourceUsernameCanBeCustomized() {
		TaskLaunchRequestProperties properties = getBatchProperties("task.launch.request.dataSourceUserName:user");
		assertThat(properties.getDataSourceUserName()).isEqualTo("user");
	}

	@Test(expected = AssertionError.class)
	public void dataSourceUsernameIsRequired() {
		validateRequiredProperty("task.launch.request.dataSourceUserName");
	}

	@Test
	public void dataSourcePasswordCanBeCustomized() {
		TaskLaunchRequestProperties properties = getBatchProperties("task.launch.request.dataSourcePassword:pass");
		assertThat(properties.getDataSourcePassword()).isEqualTo("pass");
	}

	@Test
	public void deploymentPropertiesCanBeCustomized() {
		TaskLaunchRequestProperties properties = getBatchProperties(
			"task.launch.request.deploymentProperties:prop1=val1,prop2=val2");
		assertThat(properties.getDeploymentProperties()).isEqualTo("prop1=val1,prop2=val2");
	}

	@Test
	public void environmentPropertiesCanBeCustomized() {
		TaskLaunchRequestProperties properties = getBatchProperties(
			"task.launch.request.environmentProperties:prop1=val1,prop2=val2");
		assertThat(properties.getEnvironmentProperties()).isEqualTo("prop1=val1,prop2=val2");
	}

	private TaskLaunchRequestProperties getBatchProperties(String var) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

		if (var != null) {
			TestPropertyValues.of(var).applyTo(context);
		}

		context.register(Conf.class);
		context.refresh();

		return context.getBean(TaskLaunchRequestProperties.class);
	}

	@Test
	public void parametersCanBeCustomized() {
		TaskLaunchRequestProperties properties = getBatchProperties("task.launch.request.parameters:jp1=jpv1,"
			+ "jp2=jpv2");
		List<String> jobParameters = properties.getParameters();

		assertThat(jobParameters).isNotNull();
		assertThat(jobParameters).hasSize(2);
		assertThat(jobParameters.get(0)).isEqualTo("jp1=jpv1");
		assertThat(jobParameters.get(1)).isEqualTo("jp2=jpv2");
	}

	private void validateRequiredProperty(String property) {
		try {
			getBatchProperties(property + ":");
		}
		catch (Exception e) {
		}

		fail(property + " is required");
	}

	@Configuration
	@EnableIntegration
	@EnableConfigurationProperties(TaskLaunchRequestProperties.class)
	@Import(SpelExpressionConverterConfiguration.class)
	static class Conf {
	}
}
