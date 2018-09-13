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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.task.launcher.TaskLaunchRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;

/**
 * @author David Turanski
 **/
@Configuration
public class TaskLauncherRequestConfiguration {

	static final String DATASOURCE_URL_PROPERTY_KEY = "spring.datasource.url";

	static final String DATASOURCE_USERNAME_PROPERTY_KEY = "spring.datasource.username";

	static final String DATASOURCE_PASSWORD_PROPERTY_KEY = "spring.datasource.password";

	private final TaskLaunchRequestMetadata taskLaunchRequestMetadata;
	private final TaskLaunchRequestProperties taskLaunchRequestProperties;



	public TaskLauncherRequestConfiguration(TaskLaunchRequestMetadata taskLaunchRequestMetadata,
		TaskLaunchRequestProperties taskLaunchRequestProperties) {

		this.taskLaunchRequestMetadata = taskLaunchRequestMetadata;
		this.taskLaunchRequestProperties = taskLaunchRequestProperties;
	}

	@Bean
	public Function<Message<?>, Message<?>> standaloneTaskLaunchRequest() {
		return message -> {
			TaskLaunchRequest outboundPayload = new TaskLaunchRequest(taskLaunchRequestProperties.getResourceUri(),
				mergeCommandLineArgs(), mergeEnvironmentProperties(), getDeploymentProperties(), null);

			MessageBuilder<TaskLaunchRequest> builder = MessageBuilder.withPayload(outboundPayload)
				.copyHeaders(message.getHeaders())
				.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);
			return builder.build();
		};
	}

	@Bean
	public Function<Message<?>, Message<?>> dataflowTaskLauchRequest() {
		return message -> {
			Assert.hasText(taskLaunchRequestProperties.getApplicationName(), "'applicationName' is required");

			DataFlowTaskLaunchRequest taskLaunchRequest = new DataFlowTaskLaunchRequest();
			taskLaunchRequest.setCommandlineArguments(mergeCommandLineArgs());
			taskLaunchRequest.setDeploymentProperties(getDeploymentProperties());
			taskLaunchRequest.setApplicationName(taskLaunchRequestProperties.getApplicationName());
			return MessageBuilder.withPayload(taskLaunchRequest)
				.copyHeaders(message.getHeaders())
				.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
				.build();
		};
	}

	@Bean
	public TaskLaunchRequestTypeProvider taskLaunchRequestTypeProvider(@Value("${spring.cloud.stream.function.definition:}")
	String functionDefinition) {
		return () -> {
			if (functionDefinition.contains("dataflowTaskLaunchRequest")) {
				return TaskLaunchRequestType.DATAFLOW;
			}

			if (functionDefinition.contains("standaloneTaskLaunchRequest")) {
				return TaskLaunchRequestType.STANDALONE;
			}

			return TaskLaunchRequestType.NONE;
		};
	}

	/*
	 * Merge user provided environment variables with any internally provided in TaskLaunchRequestMetadata
	 *
	 */
	Map<String, String> mergeEnvironmentProperties() {
		Map<String, String> environmentProperties = taskLaunchRequestMetadata.getEnvironment();

		environmentProperties.putIfAbsent(DATASOURCE_URL_PROPERTY_KEY, taskLaunchRequestProperties.getDataSourceUrl());
		environmentProperties.putIfAbsent(DATASOURCE_USERNAME_PROPERTY_KEY, taskLaunchRequestProperties
			.getDataSourceUserName());
		environmentProperties.putIfAbsent(DATASOURCE_PASSWORD_PROPERTY_KEY, taskLaunchRequestProperties.getDataSourcePassword());

		String providedProperties = taskLaunchRequestProperties.getEnvironmentProperties();

		if (StringUtils.hasText(providedProperties)) {
			String[] splitProperties = StringUtils.split(providedProperties, ",");
			Properties properties = StringUtils.splitArrayElementsIntoProperties(splitProperties, "=");

			for (String key : properties.stringPropertyNames()) {
				environmentProperties.put(key, properties.getProperty(key));
			}
		}

		return environmentProperties;
	}

	 Map<String, String> getDeploymentProperties() {
		ArrayList<String> pairs = new ArrayList<>();
		Map<String, String> deploymentProperties = new HashMap<>();

		String properties = taskLaunchRequestProperties.getDeploymentProperties();
		String[] candidates = StringUtils.commaDelimitedListToStringArray(properties);

		for (int i = 0; i < candidates.length; i++) {
			if (i > 0 && !candidates[i].contains("=")) {
				pairs.set(pairs.size() - 1, pairs.get(pairs.size() - 1) + "," + candidates[i]);
			}
			else {
				pairs.add(candidates[i]);
			}
		}

		for (String pair : pairs) {
			addKeyValuePairAsProperty(pair, deploymentProperties);
		}

		return deploymentProperties;
	}

	private void addKeyValuePairAsProperty(String pair, Map<String, String> properties) {
		int firstEquals = pair.indexOf('=');
		if (firstEquals != -1) {
			properties.put(pair.substring(0, firstEquals).trim(), pair.substring(firstEquals + 1).trim());
		}
	}

	/*
	 * Merge user provided commandLine args with any internally provided in TaskLaunchRequestMetadata
 	*
 	*/
	List<String> mergeCommandLineArgs() {
		List<String> commandLineArgs = taskLaunchRequestMetadata.getCommandLineArgs();
		commandLineArgs.addAll(taskLaunchRequestProperties.getParameters());
		return commandLineArgs;
	}

	static class DataFlowTaskLaunchRequest {
		@JsonProperty("args")
		private List<String> commandlineArguments = new ArrayList<>();
		@JsonProperty("deploymentProps")
		private Map<String, String> deploymentProperties = new HashMap<>();
		@JsonProperty("name")
		private String applicationName;

		public List<String> getCommandlineArguments() {
			return commandlineArguments;
		}

		public void setCommandlineArguments(List<String> commandlineArguments) {
			Assert.notNull(commandlineArguments, "'commandLineArguments' cannot be null.");
			this.commandlineArguments = commandlineArguments;
		}

		public Map<String, String> getDeploymentProperties() {
			return deploymentProperties;
		}

		public void setDeploymentProperties(Map<String, String> deploymentProperties) {
			Assert.notNull(commandlineArguments, "'deploymentProperties' cannot be null.");
			this.deploymentProperties = deploymentProperties;
		}

		public String getApplicationName() {
			return applicationName;
		}

		public void setApplicationName(String applicationName) {
			Assert.hasText(applicationName, "'applicationName' cannot be blank.");
			this.applicationName = applicationName;
		}
	}
}
