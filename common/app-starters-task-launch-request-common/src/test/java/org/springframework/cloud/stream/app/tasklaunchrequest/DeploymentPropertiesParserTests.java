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

import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Chris Schaefer
 * @author David Turanski
 */
public class DeploymentPropertiesParserTests {

	@Test
	public void testParseSimpleDeploymentProperty() {
		TaskLaunchRequestProperties taskLaunchRequestProperties = new TaskLaunchRequestProperties();

		taskLaunchRequestProperties.setDeploymentProperties("app.sftp.param=value");

		Map<String, String> deploymentProperties = DeploymentPropertiesParser.parseDeploymentProperties(
			taskLaunchRequestProperties);
		assertTrue("Invalid number of deployment properties: " + deploymentProperties.size(),
			deploymentProperties.size() == 1);
		assertTrue("Expected deployment key not found", deploymentProperties.containsKey("app.sftp.param"));
		assertEquals("Invalid deployment value", "value", deploymentProperties.get("app.sftp.param"));
	}

	@Test
	public void testParseSimpleDeploymentPropertyMultipleValues() {
		TaskLaunchRequestProperties taskLaunchRequestProperties = new TaskLaunchRequestProperties();
		taskLaunchRequestProperties.setDeploymentProperties("app.sftp.param=value1,value2");

		Map<String, String> deploymentProperties = DeploymentPropertiesParser.parseDeploymentProperties(
			taskLaunchRequestProperties);

		assertTrue("Invalid number of deployment properties: " + deploymentProperties.size(),
			deploymentProperties.size() == 1);
		assertTrue("Expected deployment key not found", deploymentProperties.containsKey("app.sftp.param"));
		assertEquals("Invalid deployment value", "value1,value2", deploymentProperties.get("app.sftp.param"));
	}

	@Test
	public void testParseMultipleDeploymentPropertiesSingleValue() {
		TaskLaunchRequestProperties taskLaunchRequestProperties = new TaskLaunchRequestProperties();
		taskLaunchRequestProperties.setDeploymentProperties("app.sftp.param=value1,app.sftp.other.param=value2");

		Map<String, String> deploymentProperties = DeploymentPropertiesParser.parseDeploymentProperties(
			taskLaunchRequestProperties);

		assertTrue("Invalid number of deployment properties: " + deploymentProperties.size(),
			deploymentProperties.size() == 2);
		assertTrue("Expected deployment key not found", deploymentProperties.containsKey("app.sftp.param"));
		assertEquals("Invalid deployment value", "value1", deploymentProperties.get("app.sftp.param"));
		assertTrue("Expected deployment key not found", deploymentProperties.containsKey("app.sftp.other.param"));
		assertEquals("Invalid deployment value", "value2", deploymentProperties.get("app.sftp.other.param"));
	}

	@Test
	public void testParseMultipleDeploymentPropertiesMultipleValues() {
		TaskLaunchRequestProperties taskLaunchRequestProperties = new TaskLaunchRequestProperties();
		taskLaunchRequestProperties.setDeploymentProperties(
			"app.sftp.param=value1,value2,app.sftp.other.param=other1,other2");

		Map<String, String> deploymentProperties = DeploymentPropertiesParser.parseDeploymentProperties(
			taskLaunchRequestProperties);

		assertTrue("Invalid number of deployment properties: " + deploymentProperties.size(),
			deploymentProperties.size() == 2);
		assertTrue("Expected deployment key not found", deploymentProperties.containsKey("app.sftp.param"));
		assertEquals("Invalid deployment value", "value1,value2", deploymentProperties.get("app.sftp.param"));
		assertTrue("Expected deployment key not found", deploymentProperties.containsKey("app.sftp.other.param"));
		assertEquals("Invalid deployment value", "other1,other2", deploymentProperties.get("app.sftp.other.param"));
	}
}
