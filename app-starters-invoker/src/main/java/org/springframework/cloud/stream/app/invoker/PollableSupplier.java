package org.springframework.cloud.stream.app.invoker;

import java.lang.annotation.*;

/**
 * Marker annotation.
 *
 * @author Soby Chacko
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface PollableSupplier {
}
