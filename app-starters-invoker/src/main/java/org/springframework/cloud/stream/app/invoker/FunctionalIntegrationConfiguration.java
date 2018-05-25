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
	class FunctionInvoker {

		@Autowired
		Function<Object, Object> functionToInvoke;

		@ServiceActivator(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
		public Object invokeProcessor(Object o) {
			return functionToInvoke.apply(o);
		}
	}

	@Conditional(ConsumerCondition.class)
	@Configuration
	@EnableBinding(Sink.class)
	class ConsumerInvoker {

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
	class PollableSourceInvoker {

		@Autowired
		Supplier<?> supplierToInvoke;

		@PollableSource
		public Object invokePollableSource() {
			return supplierToInvoke.get();
		}
	}

	//Need to have another kind of Source invoker that uses IntegrationFlow in many app starters like ftp, file, jdbc etc.

	class FunctionCondition extends SpringBootCondition {

		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
			try {
				Function function = context.getBeanFactory().getBean(Function.class);
			} catch (NoSuchBeanDefinitionException nsbde) {
				return ConditionOutcome.noMatch("Function bean is not found");
			}
			return ConditionOutcome.match("Function bean is found");
		}
	}

	class ConsumerCondition extends SpringBootCondition {

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


	class PollableSourceCondition extends SpringBootCondition {

		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
			try {
				Map<String, Object> pollableBeans = context.getBeanFactory().getBeansWithAnnotation(PollableSupplier.class);

				Object next = pollableBeans.values().iterator().next();
				if (next.getClass().isAssignableFrom(Supplier.class)) {
					return ConditionOutcome.match("Pollable source bean is found");
				} else {
					return ConditionOutcome.noMatch("Pollable source bean is not found");
				}
			} catch (NoSuchBeanDefinitionException nsbde) {
				return ConditionOutcome.noMatch("Pollable source bean is not found");
			}
		}
	}


}
