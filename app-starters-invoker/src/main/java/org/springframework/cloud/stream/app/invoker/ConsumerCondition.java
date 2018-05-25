package org.springframework.cloud.stream.app.invoker;

import java.util.function.Consumer;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ConsumerCondition extends SpringBootCondition {

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