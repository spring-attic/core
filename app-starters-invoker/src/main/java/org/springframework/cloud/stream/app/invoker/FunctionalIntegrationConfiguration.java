package org.springframework.cloud.stream.app.invoker;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.app.annotation.PollableSource;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.integration.annotation.ServiceActivator;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Soby Chacko
 * @author Ilayaperumal Gopinathan
 */
@Configuration
public class FunctionalIntegrationConfiguration {

	@Conditional(FunctionCondition.class)
	@Configuration
	@EnableBinding(Processor.class)
	public static class FunctionInvoker {

		@Autowired
		Function<?, ?> functionToInvoke;

		@ServiceActivator(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
		public Object invokeProcessor(Object o) {
			return functionToInvoke.apply(o);
		}
	}

	@Conditional(ConsumerCondition.class)
	@Configuration
	@EnableBinding(Sink.class)
	public static class ConsumerInvoker {

		@Autowired
		Consumer<Object> consumerToInvoke;

		@StreamListener(target = Sink.INPUT)
		public void invokeSink(Object o) {
			consumerToInvoke.accept(o);
		}
	}

	@Conditional(PollableSourceCondition.class)
	@Configuration
	@EnableBinding(Source.class)
	public static class PollableSourceInvoker {

		@Autowired
		Supplier<?> supplierToInvoke;

		@PollableSource
		public Object invokePollableSource() {
			return supplierToInvoke.get();
		}
	}

	//Need to have another kind of Source invoker that uses IntegrationFlow in many app starters like ftp, file, jdbc etc.


}
