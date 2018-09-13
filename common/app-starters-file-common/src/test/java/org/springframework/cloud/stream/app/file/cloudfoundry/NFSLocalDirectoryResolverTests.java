/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.file.cloudfoundry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.app.file.LocalDirectoryResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author David Turanski
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class NFSLocalDirectoryResolverTests {

	@Autowired
	private LocalDirectoryResolver localDirectoryResolver;

	@Test
	public void setLocalDirectoryResolver() throws IOException {
		assertThat(localDirectoryResolver.resolve("temp").getPath()).isEqualTo("/vcap/foo/bar/temp");
		assertThat(localDirectoryResolver.resolve("/temp").getPath()).isEqualTo("/vcap/foo/bar/temp");
	}

	@SpringBootApplication
	static class MyApplication {
		@Bean
		LocalDirectoryResolver localDirectoryResolver() {
			return new LocalDirectoryResolver();
		}
	}

	//Registered in src/test/resources/META-INF/spring.factories
	public static class TestEnvironment implements EnvironmentPostProcessor {

		/*
		 * Provide a VCAP_SERVICES entry to the environment and then invoke the
		 * CloudFoundryVcapEnvironmentPostProcessor to generate "vcap.service.*" properties.
		 * The presence of "VCAP_SERVICES" also satisfies @ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
		 */
		@Override
		public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
			environment.getPropertySources().addLast(new PropertySource<String>("vcap_test") {
				@Nullable
				@Override
				public Object getProperty(String s) {
					String vcapServices = null;
					if (s.equals("VCAP_SERVICES")) {
						try {
							vcapServices = IOUtils.toString(
								new ClassPathResource("vcap-service-nfs.json").getInputStream(),
								StandardCharsets.UTF_8);

						}
						catch (IOException e) {
							e.printStackTrace();
						}

					}
					return vcapServices;
				}

			});
		}
	}
}
