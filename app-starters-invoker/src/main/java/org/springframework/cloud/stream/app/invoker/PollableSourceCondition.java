package org.springframework.cloud.stream.app.invoker;

import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class PollableSourceCondition extends SpringBootCondition {

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
