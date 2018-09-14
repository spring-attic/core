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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.util.StringUtils;

/**
 * @author David Turanski
 **/
public class TaskLaunchRequestContext {

	public static final String HEADER_NAME = TaskLaunchRequestContext.class.getName();

	private Map<String, String> environment = new HashMap();

	private Set<String> commandLineArgs = new HashSet<>();

	public Map<String, String> getEnvironment() {
		return this.environment;
	}

	public Collection<String> getCommandLineArgs() {
		return this.commandLineArgs;
	}

	public void addCommandLineArg(String arg) {
		this.commandLineArgs.add(arg);
	}

	public void addCommandLineArgs(Collection<String> args) {
		this.commandLineArgs.addAll(args);
	}

	public void addEnvironmentVariable(String key, String value) {
		this.environment.put(key, value);
	}

	public void addEnvironment(Map<String, String> env) {
		this.environment.putAll(env);
	}

	/**
	 * Merge environment variables with default and any configured environment variables.
	 *
	 * @param taskLaunchRequestProperties the configuration properties.
	 * @return the environment.
	 */
	Map<String, String> mergeEnvironmentProperties(TaskLaunchRequestProperties taskLaunchRequestProperties) {
		Map<String, String> environmentProperties = getEnvironment();

		environmentProperties.putAll(taskLaunchRequestProperties.springDataSourceConnectionProperties());

		String providedProperties = taskLaunchRequestProperties.getEnvironmentProperties();

		if (StringUtils.hasText(providedProperties)) {
			String[] splitProperties = StringUtils.split(providedProperties, ",");
			Properties properties = StringUtils.splitArrayElementsIntoProperties(splitProperties, "=");

			for (String key : properties.stringPropertyNames()) {
				environmentProperties.put(key, properties.getProperty(key));
			}
		}

		return Collections.unmodifiableMap(environmentProperties);
	}

	/**
	 * Merge command line args with any configured command line args.
	 *
	 * @param taskLaunchRequestProperties the configuration properties.
	 * @return the args.
	 */
	List<String> mergeCommandLineArgs(TaskLaunchRequestProperties taskLaunchRequestProperties) {
		this.commandLineArgs.addAll(taskLaunchRequestProperties.getParameters());
		return Collections.unmodifiableList(new ArrayList<>(commandLineArgs));
	}

}
