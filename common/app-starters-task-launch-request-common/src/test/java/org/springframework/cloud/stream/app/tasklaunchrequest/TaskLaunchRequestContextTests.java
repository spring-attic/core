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

package org.springframework.cloud.stream.app.tasklaunchrequest;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.entry;

/**
 * @author David Turanski
 **/
public class TaskLaunchRequestContextTests {
	@Test
	public void testMergeCommandLineArgs() {
		TaskLaunchRequestProperties taskLaunchRequestProperties = new TaskLaunchRequestProperties();
		taskLaunchRequestProperties.setParameters(Arrays.asList("abc", "klm", "xyz", "pqr"));

		TaskLaunchRequestContext taskLaunchRequestContext = new TaskLaunchRequestContext();
		taskLaunchRequestContext.addCommandLineArgs(Arrays.asList("foo", "bar"));
		assertThat(
			taskLaunchRequestContext.mergeCommandLineArgs(taskLaunchRequestProperties)).containsExactlyInAnyOrder("abc",
			"klm", "xyz", "pqr", "foo", "bar");
	}

	@Test
	public void testMergeEnvironment() {
		TaskLaunchRequestProperties taskLaunchRequestProperties = new TaskLaunchRequestProperties();
		TaskLaunchRequestContext taskLaunchRequestContext = new TaskLaunchRequestContext();

		taskLaunchRequestProperties.setEnvironmentProperties("foo=bar,boo=baz");
		taskLaunchRequestContext.addEnvironmentVariable("cat", "caz");
		taskLaunchRequestContext.addEnvironmentVariable("car", "cdr");

		Map<String, String> environmenent = taskLaunchRequestContext.mergeEnvironmentProperties
			(taskLaunchRequestProperties);

		assertThat(environmenent).containsOnly(
			entry("foo", "bar"),
			entry("boo", "baz"),
			entry("cat", "caz"),
			entry("car", "cdr"),
			entry(TaskLaunchRequestProperties.SPRING_DATASOURCE_PASSWORD_PROPERTY_KEY, taskLaunchRequestProperties
				.getDataSourcePassword()),
			entry(TaskLaunchRequestProperties.SPRING_DATASOURCE_USERNAME_PROPERTY_KEY, taskLaunchRequestProperties
				.getDataSourceUserName()),
			entry(TaskLaunchRequestProperties.SPRING_DATASOURCE_URL_PROPERTY_KEY, taskLaunchRequestProperties
				.getDataSourceUrl()));

	}

}
