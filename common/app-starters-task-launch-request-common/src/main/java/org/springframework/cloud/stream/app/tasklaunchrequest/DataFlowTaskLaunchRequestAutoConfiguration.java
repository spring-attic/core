/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.tasklaunchrequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
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
@EnableConfigurationProperties(DataflowTaskLaunchRequestProperties.class)
public class DataFlowTaskLaunchRequestAutoConfiguration {

	private static final Log log = LogFactory.getLog(DataFlowTaskLaunchRequestAutoConfiguration.class);

	private final DataflowTaskLaunchRequestProperties taskLaunchRequestProperties;

	private final Map<String,Expression> argExpressionsMap;
	private final Map<String,String> deploymentProperties;

	public DataFlowTaskLaunchRequestAutoConfiguration(DataflowTaskLaunchRequestProperties taskLaunchRequestProperties) {
		this.taskLaunchRequestProperties = taskLaunchRequestProperties;
		this.deploymentProperties = KeyValueListParser.parseCommaDelimitedKeyValuePairs(
			taskLaunchRequestProperties.getDeploymentProperties());

		argExpressionsMap = new HashMap<>();
		if (StringUtils.hasText(taskLaunchRequestProperties.getArgExpressions())) {
			SpelExpressionParser expressionParser = new SpelExpressionParser();

			KeyValueListParser.parseCommaDelimitedKeyValuePairs(taskLaunchRequestProperties.getArgExpressions()).forEach(
				(k,v)-> argExpressionsMap.put(k, expressionParser.parseExpression(v)));
		}
	}

	@Bean
	public TaskLaunchRequestFunction taskLaunchRequest() {
		return message -> dataflowTaskLaunchRequest(message);
	}

	private Message dataflowTaskLaunchRequest(Message message) {

		Assert.hasText(taskLaunchRequestProperties.getTaskName(), "'taskName' is required");
		log.info(String.format("creating a task launch request for task %s", taskLaunchRequestProperties
			.getTaskName()));
		TaskLaunchRequestContext taskLaunchRequestContext = taskLaunchRequestContext(message);

		DataFlowTaskLaunchRequest taskLaunchRequest = new DataFlowTaskLaunchRequest();

		List<String> evaluatedArgs = evaluateArgExpressions(message,
			KeyValueListParser.parseCommaDelimitedKeyValuePairs(taskLaunchRequestProperties.getArgExpressions()));

		taskLaunchRequest
			.addCommmandLineArguments(taskLaunchRequestProperties.getArgs())
			.addCommmandLineArguments(evaluatedArgs)
			.addCommmandLineArguments(taskLaunchRequestContext.getCommandLineArgs());

		taskLaunchRequest.setDeploymentProperties(deploymentProperties);
		taskLaunchRequest.setTaskName(taskLaunchRequestProperties.getTaskName());

		MessageBuilder<?> builder = MessageBuilder.withPayload(taskLaunchRequest).copyHeaders(message.getHeaders());

		return adjustHeaders(builder, message.getHeaders()).build();
	}

	private List<String> evaluateArgExpressions(Message message, Map<String, String> argExpressions) {
		List<String> results = new LinkedList<>();
		argExpressions.forEach((k, v) -> {
			Expression expression = argExpressionsMap.get(k);
			Assert.notNull(expression, String.format("expression %s cannot be null!", v));
			Object val = expression.getValue(message);
			results.add(String.format("%s=%s", k, val));
		});
		return results;
	}

	private MessageBuilder<?> adjustHeaders(MessageBuilder<?> builder, MessageHeaders messageHeaders) {
		builder.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);
		if (messageHeaders.containsKey(TaskLaunchRequestContext.HEADER_NAME)) {
			builder.removeHeader(TaskLaunchRequestContext.HEADER_NAME);
		}
		return builder;
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
		private String taskName;

		public void setCommandlineArguments(List<String> commandlineArguments) {
			this.commandlineArguments = new ArrayList<>(commandlineArguments);
		}

		public List<String> getCommandlineArguments() {
			return this.commandlineArguments;
		}

		public void setDeploymentProperties(Map<String, String> deploymentProperties) {
			this.deploymentProperties = deploymentProperties;
		}

		public Map<String, String> getDeploymentProperties() {
			return this.deploymentProperties;
		}

		public void setTaskName(String taskName) {
			this.taskName = taskName;
		}

		public String getTaskName() {
			return this.taskName;
		}

		DataFlowTaskLaunchRequest addCommmandLineArguments(Collection<String> args) {
			this.commandlineArguments.addAll(args);
			return this;
		}
	}
}
