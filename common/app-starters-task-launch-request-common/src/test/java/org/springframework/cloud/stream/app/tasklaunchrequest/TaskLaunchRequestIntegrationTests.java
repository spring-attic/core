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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollectorAutoConfiguration;
import org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.springframework.cloud.stream.app.tasklaunchrequest.DataFlowTaskLaunchRequestAutoConfiguration.DataFlowTaskLaunchRequest;

/**
 * @author David Turanski
 **/
public class TaskLaunchRequestIntegrationTests {

	@Test
	public void simpleDataflowTaskLaunchRequest() throws IOException {

		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
			TestChannelBinderConfiguration.getCompleteConfiguration(TestApp.class)).web(WebApplicationType.NONE)
			.run( "--spring.jmx.enabled=false",
				"---task.launch.request.task-name=foo")) {

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
			.run("--spring.jmx.enabled=false",
				"---task.launch.request.task-name=foo", "---task.launch.request.args=foo=bar,baz=boo",
				"---task.launch.request.deploymentProperties=count=3")) {

			DataFlowTaskLaunchRequest dataFlowTaskLaunchRequest = verifyAndreceiveDataFlowTaskLaunchRequest(context,
				DataFlowTaskLaunchRequest.class);

			assertThat(dataFlowTaskLaunchRequest.getApplicationName()).isEqualTo("foo");
			assertThat(dataFlowTaskLaunchRequest.getCommandlineArguments()).containsExactlyInAnyOrder("foo=bar",
				"baz=boo");
			assertThat(dataFlowTaskLaunchRequest.getDeploymentProperties()).containsOnly(entry("count", "3"));
		}
	}

	@Test
	public void dataflowTaskLaunchRequestWithTaskLaunchRequestContext() throws IOException {

		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
			TestChannelBinderConfiguration.getCompleteConfiguration(TestApp.class)).web(WebApplicationType.NONE)
			.run( "--spring.jmx.enabled=false",
				"---task.launch.request.task-name=foo", "---task.launch.request.args=foo=bar,baz=boo")) {

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
			.run("--spring.jmx.enabled=false",
				"---task.launch.request.task-name=foo", "--enhanceTLRContext=true")) {

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

		MessageChannel input = context.getBean("input", MessageChannel.class);

		OutputDestination outputDestination = context.getBean(OutputDestination.class);
		ObjectMapper objectMapper = context.getBean(ObjectMapper.class);

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
	@EnableBinding(Source.class)
	static class TestApp {
		@Bean
		public TaskLaunchRequestContextProvider taskLaunchRequestContextProvider(@Value("${enhanceTLRContext:false}")
			boolean enhance) {
				return new TaskLaunchRequestContextProvider(enhance);
		}

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


		static class TaskLaunchRequestContextProvider implements MessageProcessor<Message> {

			private final boolean enhance;

			TaskLaunchRequestContextProvider(boolean enhance) {
				this.enhance = enhance;
			}

			@Override
			public Message<?> processMessage(Message<?> message) {
				if (enhance) {

					TaskLaunchRequestContext taskLaunchRequestContext = new TaskLaunchRequestContext();
					taskLaunchRequestContext.addCommandLineArg("runtimeArg");

					message = MessageBuilder.fromMessage(message).setHeader(TaskLaunchRequestContext.HEADER_NAME,
						taskLaunchRequestContext).build();
				}

				return message;
			}
		}
	}
}
