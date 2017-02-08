package org.springframework.cloud.stream.app.test.redis;

import org.springframework.cloud.stream.test.junit.AbstractExternalResourceTestSupport;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

/**
 * @author Soby Chacko
 */
public class RedisTestSupport extends AbstractExternalResourceTestSupport<JedisConnectionFactory> {

	public RedisTestSupport() {
		super("REDIS");
	}
	@Override
	protected void cleanupResource() throws Exception {
		resource.destroy();
	}

	@Override
	protected void obtainResource() throws Exception {
		resource = new JedisConnectionFactory();
		resource.afterPropertiesSet();
		resource.getConnection().close();
	}
}
