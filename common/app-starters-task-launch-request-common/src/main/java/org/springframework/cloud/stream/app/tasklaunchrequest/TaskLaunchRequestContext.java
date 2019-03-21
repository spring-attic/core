/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author David Turanski
 **/
public class TaskLaunchRequestContext {

	public static final String HEADER_NAME = TaskLaunchRequestContext.class.getName();

	private Set<String> commandLineArgs = new HashSet<>();

	public Collection<String> getCommandLineArgs() {
		return this.commandLineArgs;
	}

	public void addCommandLineArg(String arg) {
		this.commandLineArgs.add(arg);
	}

	public void addCommandLineArgs(Collection<String> args) {
		this.commandLineArgs.addAll(args);
	}

	/**
	 * Merge command line args with any configured command line args.
	 *
	 * @param taskLaunchRequestProperties the configuration properties.
	 * @return the args.
	 */
	List<String> mergeCommandLineArgs(DataflowTaskLaunchRequestProperties taskLaunchRequestProperties) {
		this.commandLineArgs.addAll(taskLaunchRequestProperties.getArgs());
		return Collections.unmodifiableList(new ArrayList<>(commandLineArgs));
	}

}
