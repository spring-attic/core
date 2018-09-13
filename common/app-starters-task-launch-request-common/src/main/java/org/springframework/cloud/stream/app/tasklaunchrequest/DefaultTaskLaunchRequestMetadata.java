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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author David Turanski
 **/
public class DefaultTaskLaunchRequestMetadata implements TaskLaunchRequestMetadata {

	private Map<String, String> environment = new HashMap();

	private List<String> commandLineArgs = new ArrayList();

	public Map<String, String> getEnvironment() {
		return this.environment;
	}

	public List<String> getCommandLineArgs() {
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
}
