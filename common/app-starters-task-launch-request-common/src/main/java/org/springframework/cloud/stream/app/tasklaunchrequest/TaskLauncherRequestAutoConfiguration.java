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
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.cloud.task.launcher.TaskLaunchRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.MimeTypeUtils;

/**
 * @author David Turanski
 **/
@Configuration
public class TaskLauncherRequestAutoConfiguration {

	private final TaskLaunchRequestProperties taskLaunchRequestProperties;

	public TaskLauncherRequestAutoConfiguration(TaskLaunchRequestProperties taskLaunchRequestProperties) {
		this.taskLaunchRequestProperties = taskLaunchRequestProperties;
	}

	@Bean
	public TaskLaunchRequestTransformer taskLaunchRequestTransformer(
		TaskLaunchRequestTypeProvider taskLaunchRequestTypeProvider) {
		return message -> {
			switch (taskLaunchRequestTypeProvider.taskLaunchRequestType()) {
			case DATAFLOW:
				return dataflowTaskLaunchRequest().apply(message);
			case STANDALONE:
				return standaloneTaskLaunchRequest().apply(message);
			default:
				return message;
			}
		};
	}

	private Function<Message<?>, Message<?>> standaloneTaskLaunchRequest() {
		return message -> {
			TaskLaunchRequestContext taskLaunchRequestContext = taskLaunchRequestContext(message);

			TaskLaunchRequest outboundPayload = new TaskLaunchRequest(taskLaunchRequestProperties.getResourceUri(),
				taskLaunchRequestContext.mergeCommandLineArgs(taskLaunchRequestProperties),
				taskLaunchRequestContext.mergeEnvironmentProperties(taskLaunchRequestProperties),
				DeploymentPropertiesParser.parseDeploymentProperties(taskLaunchRequestProperties), null);

			MessageBuilder<?> builder = MessageBuilder.withPayload(outboundPayload).copyHeaders(message.getHeaders());

			return adjustHeaders(builder, message.getHeaders()).build();
		};
	}

	private Function<Message<?>, Message<?>> dataflowTaskLaunchRequest() {
		return message -> {
			Assert.hasText(taskLaunchRequestProperties.getApplicationName(), "'applicationName' is required");

			TaskLaunchRequestContext taskLaunchRequestContext = taskLaunchRequestContext(message);

			DataFlowTaskLaunchRequest taskLaunchRequest = new DataFlowTaskLaunchRequest();
			taskLaunchRequest.setCommandlineArguments(
				taskLaunchRequestContext.mergeCommandLineArgs(taskLaunchRequestProperties));
			taskLaunchRequest.setDeploymentProperties(
				DeploymentPropertiesParser.parseDeploymentProperties(taskLaunchRequestProperties));
			taskLaunchRequest.setApplicationName(taskLaunchRequestProperties.getApplicationName());
			MessageBuilder<?> builder = MessageBuilder.withPayload(taskLaunchRequest).copyHeaders(message.getHeaders());

			return adjustHeaders(builder, message.getHeaders()).build();
		};
	}

	private MessageBuilder<?> adjustHeaders(MessageBuilder<?> builder, MessageHeaders messageHeaders) {
		builder.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);
		if (messageHeaders.containsKey(TaskLaunchRequestContext.HEADER_NAME)) {
			builder.removeHeader(TaskLaunchRequestContext.HEADER_NAME);
		}
		return builder;
	}

	@Bean
	public TaskLaunchRequestTypeProvider taskLaunchRequestTypeProvider() {
		return () -> taskLaunchRequestProperties.getTaskLauncherOutput();
	}

	private TaskLaunchRequestContext taskLaunchRequestContext(Message<?> message) {
		TaskLaunchRequestContext taskLaunchRequestContext = (TaskLaunchRequestContext) message.getHeaders()
			.get(TaskLaunchRequestContext.HEADER_NAME);

		return taskLaunchRequestContext != null ? taskLaunchRequestContext : new TaskLaunchRequestContext();
	}

	public static class DataFlowTaskLaunchRequest {
		@JsonProperty("args")
		private List<String> commandlineArguments = new ArrayList<>();
		@JsonProperty("deploymentProps")
		private Map<String, String> deploymentProperties = new HashMap<>();
		@JsonProperty("name")
		private String applicationName;

		public void setCommandlineArguments(List<String> commandlineArguments) {
			Assert.notNull(commandlineArguments, "'commandLineArguments' cannot be null.");
			this.commandlineArguments = commandlineArguments;
		}

		public List<String> getCommandlineArguments() {
			return this.commandlineArguments;
		}

		public void setDeploymentProperties(Map<String, String> deploymentProperties) {
			Assert.notNull(commandlineArguments, "'deploymentProperties' cannot be null.");
			this.deploymentProperties = deploymentProperties;
		}

		public Map<String, String> getDeploymentProperties() {
			return this.deploymentProperties;
		}

		public void setApplicationName(String applicationName) {
			Assert.hasText(applicationName, "'applicationName' cannot be blank.");
			this.applicationName = applicationName;
		}

		public String getApplicationName() {
			return this.applicationName;
		}
	}
}
