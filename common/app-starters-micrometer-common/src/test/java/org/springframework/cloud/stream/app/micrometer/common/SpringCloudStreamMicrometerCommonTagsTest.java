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

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Christian Tzolov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(
		classes = SpringCloudStreamMicrometerCommonTagsTest.AutoconfigurationApplication.class)
@TestPropertySource(properties = { "spring.cloud.dataflow.cluster.name=myCluster",
		"spring.cloud.dataflow.stream.name=myStream",
		"spring.cloud.dataflow.stream.app.label=myApp",
		"instance.index=666",
		"spring.cloud.application.guid=666guid",
		"spring.cloud.dataflow.stream.app.type=source" })
public class SpringCloudStreamMicrometerCommonTagsTest {

	@Autowired
	private SimpleMeterRegistry simpleMeterRegistry;

	@Test
	public void testCommonTagsPropertiesWired() {
		assertNotNull(simpleMeterRegistry);
		Meter m = simpleMeterRegistry.find("jvm.memory.committed").meter();
		assertNotNull("The jvm.memory.committed meter mast be present in SpringBoot apps!", m);

		assertThat(m.getId().getTag("clusterName"), is("myCluster"));
		assertThat(m.getId().getTag("streamName"), is("myStream"));
		assertThat(m.getId().getTag("applicationName"), is("myApp"));
		assertThat(m.getId().getTag("instanceIndex"), is("666"));
		assertThat(m.getId().getTag("applicationType"), is("source"));
		assertThat(m.getId().getTag("applicationGuid"), is("666guid"));
	}

	@SpringBootApplication
	public static class AutoconfigurationApplication {
		public static void main(String[] args) {
			SpringApplication.run(AutoconfigurationApplication.class, args);
		}
	}
}
