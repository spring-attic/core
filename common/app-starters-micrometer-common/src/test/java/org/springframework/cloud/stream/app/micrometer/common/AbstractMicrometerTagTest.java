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
package org.springframework.cloud.stream.app.micrometer.common;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Before;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

/**
 * @author Christian Tzolov
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AbstractMicrometerTagTest.AutoConfigurationApplication.class)
public class AbstractMicrometerTagTest {

	@Autowired
	protected SimpleMeterRegistry simpleMeterRegistry;

	protected Meter meter;

	@Before
	public void before() {
		assertNotNull(simpleMeterRegistry);
		meter = simpleMeterRegistry.find("jvm.memory.committed").meter();
		assertNotNull("The jvm.memory.committed meter mast be present in SpringBoot apps!", meter);
	}

	@SpringBootApplication
	public static class AutoConfigurationApplication {
		public static void main(String[] args) {
			SpringApplication.run(AutoConfigurationApplication.class, args);
		}
	}

	// Hack to set the VCAP_APPLICATION for tests. Requited by
	// https://cloud.spring.io/spring-cloud-connectors/spring-cloud-cloud-foundry-connector.html#_cloud_detection
	static {
		try {
			AbstractMicrometerTagTest.setVcapApplicationEnv();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Based on https://stackoverflow.com/a/7201825/4430769
	protected static void setVcapApplicationEnv() throws Exception {
		try {
			Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
			Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
			theEnvironmentField.setAccessible(true);
			Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
			env.put("VCAP_APPLICATION", "foo");
			Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
			theCaseInsensitiveEnvironmentField.setAccessible(true);
			Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
			cienv.put("VCAP_APPLICATION", "foo");
		}
		catch (NoSuchFieldException e) {
			Class[] classes = Collections.class.getDeclaredClasses();
			Map<String, String> env = System.getenv();
			for (Class cl : classes) {
				if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
					Field field = cl.getDeclaredField("m");
					field.setAccessible(true);
					Object obj = field.get(env);
					Map<String, String> map = (Map<String, String>) obj;
					map.clear();
					map.put("VCAP_APPLICATION", "foo");
				}
			}
		}
	}
}
