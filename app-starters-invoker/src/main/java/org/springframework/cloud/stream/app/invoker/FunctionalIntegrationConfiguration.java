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
import org.springframework.messaging.Message;

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

	//Need to explore ordering for autoconfiguration so that FunctionConidtion waits for
	//any downstream function beans to be registered.

	//@Conditional(FunctionCondition.class)
	@Configuration
	@EnableBinding(Processor.class)
	public static class FunctionInvoker {

		@Autowired
		Function<Message<?>, Object> functionToInvoke;

		@ServiceActivator(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
		public Object invokeProcessor(Message<?> o) {
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

	private static class FunctionCondition extends SpringBootCondition {

		public FunctionCondition() {
		}

		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
			try {
				Function function = context.getBeanFactory().getBean(Function.class);
				//check function is qualified
			} catch (NoSuchBeanDefinitionException nsbde) {
				return ConditionOutcome.noMatch("Function bean is not found");
			}
			return ConditionOutcome.match("Function bean is found");
		}
	}

	private static class ConsumerCondition extends SpringBootCondition {

		public ConsumerCondition() {}

		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
			try {
				Consumer consumer = context.getBeanFactory().getBean(Consumer.class);
			} catch (NoSuchBeanDefinitionException nsbde) {
				return ConditionOutcome.noMatch("Consumer bean is not found");
			}
			return ConditionOutcome.match("Consumer bean is found");
		}
	}

	private static class PollableSourceCondition extends SpringBootCondition {

		public PollableSourceCondition() {}

		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
			try {
				Map<String, Object> pollableBeans = context.getBeanFactory().getBeansWithAnnotation(PollableSupplier.class);

				if (!pollableBeans.isEmpty()) {
					Object next = pollableBeans.values().iterator().next();
					if (next.getClass().isAssignableFrom(Supplier.class)) {
						return ConditionOutcome.match("Pollable source bean is found");
					}
					else {
						return ConditionOutcome.noMatch("Pollable source bean is not found");
					}
				}
				else {
					return ConditionOutcome.noMatch("Pollable source bean is not found");
				}
			} catch (NoSuchBeanDefinitionException nsbde) {
				return ConditionOutcome.noMatch("Pollable source bean is not found");
			}
		}
	}

}
