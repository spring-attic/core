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
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Base Properties to create a {@link org.springframework.cloud.task.launcher.TaskLaunchRequest}.
 *
 * @author Chris Schaefer
 * @author David Turanski
 */
@Validated
@ConfigurationProperties("task.launch.request")
public class DataflowTaskLaunchRequestProperties {

	/**
	 * Comma separated list of optional args in key=value format.
	 */
	private List<String> args = new ArrayList<>();

	/**
	 * Comma separated list of option args as SpEL expressions in key=value format.
	 */
	private String argExpressions = "";

	/**
	 * Comma delimited list of deployment properties to be applied to the
	 * TaskLaunchRequest.
	 */
	private String deploymentProperties = "";

	/**
	 * The Data Flow task name.
	 */
	private String taskName;

	@NotNull
	public List<String> getArgs() {
		return this.args;
	}

	public void setArgs(List<String> args) {
		this.args = new ArrayList<>(args);
	}

	@NotNull
	public String getDeploymentProperties() {
		return this.deploymentProperties;
	}

	public void setDeploymentProperties(String deploymentProperties) {
		this.deploymentProperties = deploymentProperties;
	}

	@NotBlank
	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getArgExpressions() {
		return argExpressions;
	}

	public void setArgExpressions(String argExpressions) {
		this.argExpressions = argExpressions;
	}

}
