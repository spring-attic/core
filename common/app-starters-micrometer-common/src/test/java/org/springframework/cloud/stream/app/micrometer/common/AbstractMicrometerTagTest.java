/*
 * Copyright 2018-2019 the original author or authors.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.pivotal.cfenv.test.CfEnvTestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleProperties;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimplePropertiesConfigAdapter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

import static org.junit.Assert.assertNotNull;

/**
 * @author Christian Tzolov
 * @author Soby Chacko
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AbstractMicrometerTagTest.AutoConfigurationApplication.class)
public class AbstractMicrometerTagTest {

	private static ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	protected SimpleMeterRegistry simpleMeterRegistry;

	@Autowired
	protected ConfigurableApplicationContext context;

	protected Meter meter;

	@Before
	public void before() {
		assertNotNull(simpleMeterRegistry);
		meter = simpleMeterRegistry.find("jvm.memory.committed").meter();
		assertNotNull("The jvm.memory.committed meter mast be present in SpringBoot apps!", meter);
	}

	@BeforeClass
	public static void setup() {
		CfEnvTestUtils.mockVcapServicesFromString(getServicesPayload(getMockVcap("cloud-test-info.json")));
	}

	private static String getMockVcap(String filename) {
		return readTestDataFile(filename);
	}

	private static String readTestDataFile(String fileName) {
		Scanner scanner = null;
		try {
			Reader fileReader = new InputStreamReader(
					AbstractMicrometerTagTest.class.getResourceAsStream(fileName));
			scanner = new Scanner(fileReader);
			return scanner.useDelimiter("\\Z").next();
		}
		finally {
			if (scanner != null) {
				scanner.close();
			}
		}
	}

	private static String getServicesPayload(String... servicePayloads) {
		Map<String, List<String>> labelPayloadMap = new HashMap<>();

		for (String payload : servicePayloads) {
			String label = getServiceLabel(payload);

			List<String> payloadsForLabel = labelPayloadMap.computeIfAbsent(label, k -> new ArrayList<>());
			payloadsForLabel.add(payload);
		}

		StringBuilder result = new StringBuilder("{\n");
		int labelSize = labelPayloadMap.size();
		int i = 0;

		for (Map.Entry<String, List<String>> entry : labelPayloadMap.entrySet()) {
			result.append(quote(entry.getKey())).append(":");
			result.append(getServicePayload(entry.getValue()));
			if (i++ != labelSize - 1) {
				result.append(",\n");
			}
		}
		result.append("}");
		return result.toString();
	}

	private static String getServicePayload(List<String> servicePayloads) {
		StringBuilder payload = new StringBuilder("[");

		for (int i = 0; i < servicePayloads.size(); i++) {
			payload.append(servicePayloads.get(i));
			if (i != servicePayloads.size() - 1) {
				payload.append(",");
			}
		}
		payload.append("]");

		return payload.toString();
	}

	private static String quote(String str) {
		return "\"" + str + "\"";
	}

	@SuppressWarnings("unchecked")
	private static String getServiceLabel(String servicePayload) {
		try {
			Map<String, Object> serviceMap = objectMapper.readValue(servicePayload,
					Map.class);
			return serviceMap.get("label").toString();
		}
		catch (Exception e) {
			return null;
		}
	}

	@SpringBootApplication
	@EnableConfigurationProperties(SimpleProperties.class)
	public static class AutoConfigurationApplication {

		public static void main(String[] args) {
			SpringApplication.run(AutoConfigurationApplication.class, args);
		}

		@Bean
		public SimpleMeterRegistry simpleMeterRegistry(SimpleConfig config, Clock clock) {
			return new SimpleMeterRegistry(config, clock);
		}

		@Bean
		@ConditionalOnMissingBean
		public SimpleConfig simpleConfig(SimpleProperties simpleProperties) {
			return new SimplePropertiesConfigAdapter(simpleProperties);
		}
	}
}
