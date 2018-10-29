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

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Christian Tzolov
 */
@RunWith(Enclosed.class)
public class SecurityCommonTest {

	@TestPropertySource(properties = {
			"spring.cloud.security.enabled=false",
			"management.endpoints.web.exposure.include=health,info"})
	public static class SimpleTests extends AbstractSecurityCommonTests {

		@Test
		public void testOne() throws Exception {
			this.mvc.perform(get("/info")).andExpect(status().isOk());
			//this.mvc.perform(get("/info")).andExpect(status().isOk()).andExpect(content().string("Hello World"));
		}

	}
}
