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

import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
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

		try (ConfigurableApplicationContext context =
				 new SpringApplicationBuilder(TestApp.class).web(WebApplicationType.NONE)
					 .run("--spring.jmx.enabled=false",
						 "---task.launch.request.task-name=foo")) {

			DataFlowTaskLaunchRequest dataFlowTaskLaunchRequest = verifyAndreceiveDataFlowTaskLaunchRequest(context,
				null);

			assertThat(dataFlowTaskLaunchRequest.getTaskName()).isEqualTo("foo");
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

			DataFlowTaskLaunchRequest dataFlowTaskLaunchRequest = verifyAndreceiveDataFlowTaskLaunchRequest(context);

			assertThat(dataFlowTaskLaunchRequest.getTaskName()).isEqualTo("foo");
			assertThat(dataFlowTaskLaunchRequest.getCommandlineArguments()).containsExactlyInAnyOrder("foo=bar",
				"baz=boo");
			assertThat(dataFlowTaskLaunchRequest.getDeploymentProperties()).containsOnly(entry("count", "3"));
		}
	}

	@Test
	public void dataflowTaskLaunchRequestWithTaskLaunchRequestContext() throws IOException {

		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
			TestChannelBinderConfiguration.getCompleteConfiguration(TestApp.class)).web(WebApplicationType.NONE)
			.run("--spring.jmx.enabled=false",
				"---task.launch.request.task-name=foo", "---task.launch.request.args=foo=bar,baz=boo")) {

			TaskLaunchRequestContext taskLaunchRequestContext = new TaskLaunchRequestContext();

			taskLaunchRequestContext.addCommandLineArg("localFile=/some/file/path");
			taskLaunchRequestContext.addCommandLineArg("process=true");

			DataFlowTaskLaunchRequest dataFlowTaskLaunchRequest = verifyAndreceiveDataFlowTaskLaunchRequest(context,
				taskLaunchRequestContext);

			assertThat(dataFlowTaskLaunchRequest.getTaskName()).isEqualTo("foo");
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

			DataFlowTaskLaunchRequest dataFlowTaskLaunchRequest = verifyAndreceiveDataFlowTaskLaunchRequest(context);

			assertThat(dataFlowTaskLaunchRequest.getTaskName()).isEqualTo("foo");
			assertThat(dataFlowTaskLaunchRequest.getCommandlineArguments()).hasSize(1);
			assertThat(dataFlowTaskLaunchRequest.getCommandlineArguments()).containsExactly("runtimeArg");
		}
	}

	private DataFlowTaskLaunchRequest verifyAndreceiveDataFlowTaskLaunchRequest(ApplicationContext context)
		throws IOException {
		return this.verifyAndreceiveDataFlowTaskLaunchRequest(context, null);
	}

	private DataFlowTaskLaunchRequest verifyAndreceiveDataFlowTaskLaunchRequest(ApplicationContext context,
		TaskLaunchRequestContext taskLaunchRequestContext) throws IOException {

		MessageChannel input = context.getBean("input", MessageChannel.class);

		PollableChannel output = context.getBean("output", PollableChannel.class);

		MessageBuilder<byte[]> builder = MessageBuilder.withPayload(new byte[] {});

		if (taskLaunchRequestContext != null) {
			builder.setHeader(TaskLaunchRequestContext.HEADER_NAME, taskLaunchRequestContext);
		}

		input.send(builder.build());

		Message<?> message = output.receive(1000);

		assertThat(message).isNotNull();

		return (DataFlowTaskLaunchRequest) message.getPayload();
	}

	@Configuration
	@EnableAutoConfiguration
	static class TestApp {

		@Value("${enhanceTLRContext:false}") boolean enhance;

		@Bean
		public MessageChannel input() {
			return new DirectChannel();
		}

		@Bean
		MessageChannel output() {
			return new QueueChannel();
		}

		@Autowired
		TaskLaunchRequestTransformer taskLaunchRequestTransformer;

		@Bean
		public IntegrationFlow flow() {

			return IntegrationFlows.from(input())
				.enrichHeaders(
					h -> h.headerFunction(TaskLaunchRequestContext.HEADER_NAME,
						m -> {
							if (enhance) {
								TaskLaunchRequestContext taskLaunchRequestContext = new TaskLaunchRequestContext();
								taskLaunchRequestContext.addCommandLineArg("runtimeArg");
								return taskLaunchRequestContext;
							}
							return null;
						}
					))
				.transform(taskLaunchRequestTransformer)
				.channel(output()).get();
		}
	}
}
