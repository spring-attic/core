/*
 * Copyright 2018 the original author or authors.
 *
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

package org.springframework.cloud.stream.app.function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.stream.function.IntegrationFlowFunctionSupport;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.ChannelInterceptor;

/**
 * Auto configuration class that provides necessary beans and configuration for adding
 * Function composition support to the stream apps.
 *
 * @author Ilayaperumal Gopinathan
 */
@Configuration
@ConditionalOnBean(IntegrationFlowFunctionSupport.class)
public class StreamAppFunctionAutoConfiguration {

	@Autowired(required = false)
	private Source channel;

	@Autowired
	private IntegrationFlowFunctionSupport functionSupport;

	@SuppressWarnings("resource")
	@Bean
	public MessageChannel processOutput() {
		return new FluxMessageChannel();
	}

	@Bean
	public IntegrationFlow httpProcessOutput() {
		MessageChannel processOutput = processOutput();
		//todo: check multiple function names and add interceptor for each function
		//todo: this in fact needs to come from SCSt
		QueueChannel queueChannel  = new QueueChannel();((AbstractMessageChannel) this.channel.output()).addInterceptor(new FunctionInvokerInterceptor(processOutput, queueChannel));
		IntegrationFlowBuilder flowBuilder = IntegrationFlows.from(processOutput).bridge();
		this.functionSupport.andThenFunction(flowBuilder, queueChannel);
		return flowBuilder.get();
	}

	public class FunctionInvokerInterceptor implements ChannelInterceptor {

		private final MessageChannel processOutput;

		private final PollableChannel pollableChannel;

		public FunctionInvokerInterceptor(MessageChannel processOutput, PollableChannel pollableChannel) {
			this.processOutput = processOutput;
			this.pollableChannel = pollableChannel;
		}

		@Override
		public Message<?> preSend(Message<?> message, MessageChannel channel) {
			this.processOutput.send(message);
			return pollableChannel.receive();
		}
	}
}
