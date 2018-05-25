package org.springframework.cloud.stream.app.invoker;

import java.util.function.Function;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class FunctionCondition extends SpringBootCondition {

	public FunctionCondition() {
	}

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
