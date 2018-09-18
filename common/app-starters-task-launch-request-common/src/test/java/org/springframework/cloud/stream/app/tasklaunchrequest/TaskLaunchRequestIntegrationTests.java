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

import java.io.IOException;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollectorAutoConfiguration;
import org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration;
import org.springframework.cloud.task.launcher.TaskLaunchRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.springframework.cloud.stream.app.tasklaunchrequest.TaskLauncherRequestAutoConfiguration.DataFlowTaskLaunchRequest;

/**
 * @author David Turanski
 **/
public class TaskLaunchRequestIntegrationTests {

	@Test
	public void simpleStandaloneLaunchRequest() throws IOException {

		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
			TestChannelBinderConfiguration.getCompleteConfiguration(TestApp.class)).web(WebApplicationType.NONE)
			.run("--task.task-launcher-output=STANDALONE", "--spring.jmx.enabled=false",
				"--task.resource-uri=file:///some.jar", "--task.data-source-password=pass")) {

			TaskLaunchRequest taskLaunchRequest = verifyAndreceiveDataFlowTaskLaunchRequest(context,
				TaskLaunchRequest.class);

			assertThat(taskLaunchRequest.getApplicationName()).startsWith("Task");
			assertThat(taskLaunchRequest.getEnvironmentProperties()).containsKeys(
				TaskLaunchRequestProperties.SPRING_DATASOURCE_URL_PROPERTY_KEY,
				TaskLaunchRequestProperties.SPRING_DATASOURCE_USERNAME_PROPERTY_KEY,
				TaskLaunchRequestProperties.SPRING_DATASOURCE_PASSWORD_PROPERTY_KEY);

			assertThat(taskLaunchRequest.getEnvironmentProperties()
				.get(TaskLaunchRequestProperties.SPRING_DATASOURCE_PASSWORD_PROPERTY_KEY)).isEqualTo("pass");
			assertThat(taskLaunchRequest.getCommandlineArguments()).hasSize(0);
			assertThat(taskLaunchRequest.getDeploymentProperties()).hasSize(0);
		}
	}

	@Test
	public void simpleDataflowTaskLaunchRequest() throws IOException {

		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
			TestChannelBinderConfiguration.getCompleteConfiguration(TestApp.class)).web(WebApplicationType.NONE)
			.run("--task.task-launcher-output=DATAFLOW", "--spring.jmx.enabled=false",
				"--task.application-name=foo")) {

			DataFlowTaskLaunchRequest dataFlowTaskLaunchRequest = verifyAndreceiveDataFlowTaskLaunchRequest(context,
				DataFlowTaskLaunchRequest.class);

			assertThat(dataFlowTaskLaunchRequest.getApplicationName()).isEqualTo("foo");
			assertThat(dataFlowTaskLaunchRequest.getCommandlineArguments()).hasSize(0);
			assertThat(dataFlowTaskLaunchRequest.getDeploymentProperties()).hasSize(0);
		}
	}

	@Test
	public void dataflowTaskLaunchRequestWithArgsAndDeploymentProperties() throws IOException {

		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
			TestChannelBinderConfiguration.getCompleteConfiguration(TestApp.class)).web(WebApplicationType.NONE)
			.run("--task.task-launcher-output=DATAFLOW", "--spring.jmx.enabled=false",
				"--task.application-name=foo", "--task.parameters=foo=bar,baz=boo",
				"--task" + ".deploymentProperties=count=3")) {

			DataFlowTaskLaunchRequest dataFlowTaskLaunchRequest = verifyAndreceiveDataFlowTaskLaunchRequest(context,
				DataFlowTaskLaunchRequest.class);

			assertThat(dataFlowTaskLaunchRequest.getApplicationName()).isEqualTo("foo");
			assertThat(dataFlowTaskLaunchRequest.getCommandlineArguments()).containsExactlyInAnyOrder("foo=bar",
				"baz=boo");
			assertThat(dataFlowTaskLaunchRequest.getDeploymentProperties()).containsOnly(entry("count", "3"));
		}
	}

	@Test
	public void simpleStandaloneLaunchRequestWithTaskLaunchRequest() throws IOException {

		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
			TestChannelBinderConfiguration.getCompleteConfiguration(TestApp.class)).web(WebApplicationType.NONE)
			.run("--task.task-launcher-output=STANDALONE", "--spring.jmx.enabled=false",
				"--task.resource-uri=file:///some.jar")) {

			TaskLaunchRequestContext taskLaunchRequestContext = new TaskLaunchRequestContext();
			taskLaunchRequestContext.addCommandLineArg("localFile=/some/file/path");
			taskLaunchRequestContext.addEnvironmentVariable("foo", "bar");

			TaskLaunchRequest taskLaunchRequest = verifyAndreceiveDataFlowTaskLaunchRequest(context,
				taskLaunchRequestContext, TaskLaunchRequest.class);

			assertThat(taskLaunchRequest.getApplicationName()).startsWith("Task");
			assertThat(taskLaunchRequest.getEnvironmentProperties()).containsKeys(
				TaskLaunchRequestProperties.SPRING_DATASOURCE_URL_PROPERTY_KEY,
				TaskLaunchRequestProperties.SPRING_DATASOURCE_USERNAME_PROPERTY_KEY,
				TaskLaunchRequestProperties.SPRING_DATASOURCE_PASSWORD_PROPERTY_KEY);

			assertThat(taskLaunchRequest.getCommandlineArguments()).containsExactly("localFile=/some/file/path");
			assertThat(taskLaunchRequest.getEnvironmentProperties()).contains(entry("foo", "bar"));

			assertThat(taskLaunchRequest.getDeploymentProperties()).hasSize(0);
		}
	}

	@Test
	public void simpleStandaloneLaunchRequestWithTaskLaunchRequestContextProvider() throws IOException {

		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
			TestChannelBinderConfiguration.getCompleteConfiguration(TestApp.class)).web(WebApplicationType.NONE)
			.run("--task.task-launcher-output=STANDALONE", "--spring.jmx.enabled=false",
				"--task.resource-uri=file:///some.jar", "--enhanceTLRContext=true")) {

			TaskLaunchRequestContext taskLaunchRequestContext = new TaskLaunchRequestContext();

			TaskLaunchRequest taskLaunchRequest = verifyAndreceiveDataFlowTaskLaunchRequest(context,
				taskLaunchRequestContext, TaskLaunchRequest.class);

			assertThat(taskLaunchRequest.getApplicationName()).startsWith("Task");
			assertThat(taskLaunchRequest.getEnvironmentProperties()).containsKeys(
				TaskLaunchRequestProperties.SPRING_DATASOURCE_URL_PROPERTY_KEY,
				TaskLaunchRequestProperties.SPRING_DATASOURCE_USERNAME_PROPERTY_KEY,
				TaskLaunchRequestProperties.SPRING_DATASOURCE_PASSWORD_PROPERTY_KEY);

			assertThat(taskLaunchRequest.getCommandlineArguments()).containsExactly("runtimeArg");
			assertThat(taskLaunchRequest.getEnvironmentProperties()).contains(entry("runtimeKey", "runtimeValue"));

			assertThat(taskLaunchRequest.getDeploymentProperties()).hasSize(0);
		}
	}

	@Test
	public void dataflowTaskLaunchRequestWithTaskLaunchRequestContext() throws IOException {

		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
			TestChannelBinderConfiguration.getCompleteConfiguration(TestApp.class)).web(WebApplicationType.NONE)
			.run("--task.task-launcher-output=DATAFLOW", "--spring.jmx.enabled=false",
				"--task.application-name=foo", "--task.parameters=foo=bar,baz=boo")) {

			TaskLaunchRequestContext taskLaunchRequestContext = new TaskLaunchRequestContext();

			taskLaunchRequestContext.addCommandLineArg("localFile=/some/file/path");
			taskLaunchRequestContext.addCommandLineArg("process=true");

			DataFlowTaskLaunchRequest dataFlowTaskLaunchRequest = verifyAndreceiveDataFlowTaskLaunchRequest(context,
				taskLaunchRequestContext, DataFlowTaskLaunchRequest.class);

			assertThat(dataFlowTaskLaunchRequest.getApplicationName()).isEqualTo("foo");
			assertThat(dataFlowTaskLaunchRequest.getCommandlineArguments()).containsExactlyInAnyOrder("foo=bar",
				"baz=boo", "localFile=/some/file/path", "process=true");
		}
	}

	@Test
	public void dataflowTaskLaunchRequestWithTaskLaunchRequestContextProvider() throws IOException {

		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
			TestChannelBinderConfiguration.getCompleteConfiguration(TestApp.class)).web(WebApplicationType.NONE)
			.run("--task.task-launcher-output=DATAFLOW", "--spring.jmx.enabled=false",
				"--task.application-name=foo", "--enhanceTLRContext=true")) {

			DataFlowTaskLaunchRequest dataFlowTaskLaunchRequest = verifyAndreceiveDataFlowTaskLaunchRequest(context,
				DataFlowTaskLaunchRequest.class);

			assertThat(dataFlowTaskLaunchRequest.getApplicationName()).isEqualTo("foo");
			assertThat(dataFlowTaskLaunchRequest.getCommandlineArguments()).hasSize(1);
			assertThat(dataFlowTaskLaunchRequest.getCommandlineArguments()).containsExactly("runtimeArg");
		}
	}

	private <T> T verifyAndreceiveDataFlowTaskLaunchRequest(ApplicationContext context, Class<T> returnType)
		throws IOException {
		return this.verifyAndreceiveDataFlowTaskLaunchRequest(context, null, returnType);
	}

	private <T> T verifyAndreceiveDataFlowTaskLaunchRequest(ApplicationContext context,
		TaskLaunchRequestContext taskLaunchRequestContext, Class<T> returnType) throws IOException {
		TaskLaunchRequestTypeProvider launchRequestTypeProvider = context.getBean(TaskLaunchRequestTypeProvider.class);

		assertThat(launchRequestTypeProvider.taskLaunchRequestType()).isEqualTo(
			returnType == DataFlowTaskLaunchRequest.class ? TaskLaunchRequestType.DATAFLOW : TaskLaunchRequestType
				.STANDALONE);

		MessageChannel input = context.getBean("input", MessageChannel.class);

		OutputDestination outputDestination = context.getBean(OutputDestination.class);
		ObjectMapper objectMapper = context.getBean( ObjectMapper.class);

		MessageBuilder<byte[]> builder = MessageBuilder.withPayload(new byte[] {});

		if (taskLaunchRequestContext != null) {
			builder.setHeader(TaskLaunchRequestContext.HEADER_NAME, taskLaunchRequestContext);
		}

		input.send(builder.build());

		Message<byte[]> message = outputDestination.receive(1000);

		assertThat(message).isNotNull();

		return (T) objectMapper.readValue(message.getPayload(), returnType);
	}

	@EnableAutoConfiguration(exclude = { TestSupportBinderAutoConfiguration.class,
		MessageCollectorAutoConfiguration.class })
	@EnableConfigurationProperties(TestApp.MyTaskLaunchRequestProperties.class)
	@EnableBinding(Source.class)
	static class TestApp {

		@Bean
		public MessageChannel input() {
			return new DirectChannel();
		}

		@Autowired
		TaskLaunchRequestTransformer taskLaunchRequestTransformer;

		@Bean
		public IntegrationFlow flow(TaskLaunchRequestContextProvider provider) {

			return IntegrationFlows.from(input()).transform(provider).transform(taskLaunchRequestTransformer)
				.channel(Source.OUTPUT).get();
		}

		@Bean
		public TaskLaunchRequestContextProvider taskLaunchRequestContextProvider(TaskLaunchRequestTypeProvider
			taskLaunchRequestTypeProvider, @Value("${enhanceTLRContext:false}") boolean enhance) {
			return new TaskLaunchRequestContextProvider(taskLaunchRequestTypeProvider, enhance);
		}

		@ConfigurationProperties(prefix = "task")
		static class MyTaskLaunchRequestProperties extends TaskLaunchRequestProperties {
		}

		static class TaskLaunchRequestContextProvider implements MessageProcessor<Message> {

			private final TaskLaunchRequestTypeProvider taskLaunchRequestTypeProvider;
			private final boolean enhance;

			TaskLaunchRequestContextProvider(TaskLaunchRequestTypeProvider taskLaunchRequestTypeProvider,
				boolean enhance) {
				this.taskLaunchRequestTypeProvider = taskLaunchRequestTypeProvider;
				this.enhance = enhance;
			}

			@Override
			public Message<?> processMessage(Message<?> message) {
				if (taskLaunchRequestTypeProvider.taskLaunchRequestType() != TaskLaunchRequestType.NONE && enhance) {

					TaskLaunchRequestContext taskLaunchRequestContext = new TaskLaunchRequestContext();
					taskLaunchRequestContext.addCommandLineArg("runtimeArg");
					taskLaunchRequestContext.addEnvironmentVariable("runtimeKey", "runtimeValue");

					message = MessageBuilder.fromMessage(message).setHeader(TaskLaunchRequestContext.HEADER_NAME,
						taskLaunchRequestContext).build();
				}

				return message;
			}
		}
	}
}
