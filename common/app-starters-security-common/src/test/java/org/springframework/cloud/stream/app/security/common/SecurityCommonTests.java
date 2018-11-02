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
package org.springframework.cloud.stream.app.security.common;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Christian Tzolov
 * @author Artem Bilan
 */
@RunWith(Enclosed.class)
public class SecurityCommonTests {


	@TestPropertySource(properties = {
			"spring.cloud.stream.security.enabled=true",
			"management.endpoints.web.exposure.include=health,info,env",
			"info.name=MY TEST APP"})
	public static class SecurityEnabledManagementSecurityEnabledTests extends AbstractSecurityCommonTests {

		@Test
		@SuppressWarnings("rawtypes")
		public void testHealthEndpoint() {
			ResponseEntity<Map> response = this.restTemplate.getForEntity("/actuator/health", Map.class);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertTrue(response.hasBody());
			Map health = response.getBody();
			assertEquals("UP", health.get("status"));
		}

		@Test
		@SuppressWarnings("rawtypes")
		public void testInfoEndpoint() {
			ResponseEntity<Map> response = this.restTemplate.getForEntity("/actuator/info", Map.class);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertTrue(response.hasBody());
			Map info = response.getBody();
			assertEquals("MY TEST APP", info.get("name"));
		}

		// The ManagementWebSecurityAutoConfiguration exposes only Info and Health endpoint not Env!
		@Test
		@SuppressWarnings("rawtypes")
		public void testEnvEndpoint() {
			ResponseEntity<Map> response = this.restTemplate.getForEntity("/actuator/env", Map.class);
			assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
			assertTrue(response.hasBody());
		}

	}

	@TestPropertySource(properties = {
			"spring.cloud.stream.security.enabled=false",
			"management.endpoints.web.exposure.include=health,info,env",
			"info.name=MY TEST APP" })
	public static class SecurityDisabledManagementSecurityEnabledTests extends AbstractSecurityCommonTests {

		@Test
		@SuppressWarnings("rawtypes")
		public void testHealthEndpoint() {
			ResponseEntity<Map> response = this.restTemplate.getForEntity("/actuator/health", Map.class);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertTrue(response.hasBody());
			Map health = response.getBody();
			assertEquals("UP", health.get("status"));
		}

		@Test
		@SuppressWarnings("rawtypes")
		public void testInfoEndpoint() {
			ResponseEntity<Map> response = this.restTemplate.getForEntity("/actuator/info", Map.class);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertTrue(response.hasBody());
			Map info = response.getBody();
			assertEquals("MY TEST APP", info.get("name"));
		}

		@Test
		@SuppressWarnings("rawtypes")
		public void testEnvEndpoint() {
			ResponseEntity<Map> response = this.restTemplate.getForEntity("/actuator/env", Map.class);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertTrue(response.hasBody());
		}

	}

	@TestPropertySource(properties = {
			"spring.autoconfigure.exclude=org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration",
			"spring.cloud.stream.security.enabled=true",
			"management.endpoints.web.exposure.include=health,info"})
	public static class SecurityEnabledManagementSecurityDisabledUnauthorizedAccessTests extends AbstractSecurityCommonTests {

		@Test
		@SuppressWarnings("rawtypes")
		public void testHealthEndpoint() {
			ResponseEntity<Map> response = this.restTemplate.getForEntity("/actuator/health", Map.class);
			assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
			assertTrue(response.hasBody());
			Map health = response.getBody();
			assertEquals("Unauthorized", health.get("error"));
		}

		@Test
		@SuppressWarnings("rawtypes")
		public void testInfoEndpoint() {
			ResponseEntity<Map> response = this.restTemplate.getForEntity("/actuator/info", Map.class);
			assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
			assertTrue(response.hasBody());
			Map info = response.getBody();
			assertEquals("Unauthorized", info.get("error"));
		}

		@Test
		@SuppressWarnings("rawtypes")
		public void testEnvEndpoint() {
			ResponseEntity<Map> response = this.restTemplate.getForEntity("/actuator/env", Map.class);
			assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
			assertTrue(response.hasBody());
		}
	}

	@TestPropertySource(properties = {
			"spring.autoconfigure.exclude=org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration",
			"spring.cloud.stream.security.enabled=true",
			"management.endpoints.web.exposure.include=health,info,env",
			"info.name=MY TEST APP" })
	public static class SecurityEnabledManagementSecurityDisabledAuthorizedAccessTests extends AbstractSecurityCommonTests {

		@Autowired
		private SecurityProperties securityProperties;

		@Before
		public void before() {
			restTemplate.getRestTemplate().getInterceptors().add(new BasicAuthenticationInterceptor(
					securityProperties.getUser().getName(), securityProperties.getUser().getPassword()));
		}

		@Test
		@SuppressWarnings("rawtypes")
		public void testHealthEndpoint() {
			ResponseEntity<Map> response = this.restTemplate.getForEntity("/actuator/health", Map.class);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertTrue(response.hasBody());
			Map health = response.getBody();
			assertEquals("UP", health.get("status"));
		}

		@Test
		@SuppressWarnings("rawtypes")
		public void testInfoEndpoint() {
			ResponseEntity<Map> response = this.restTemplate.getForEntity("/actuator/info", Map.class);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertTrue(response.hasBody());
			Map info = response.getBody();
			assertEquals("MY TEST APP", info.get("name"));
		}

		@Test
		@SuppressWarnings("rawtypes")
		public void testEnvEndpoint() {
			ResponseEntity<Map> response = this.restTemplate.getForEntity("/actuator/env", Map.class);
			assertEquals(HttpStatus.OK, response.getStatusCode());
			assertTrue(response.hasBody());
		}

	}

}
