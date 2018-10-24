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

/**
 * @author Christian Tzolov
 */
@RunWith(Enclosed.class)
public class SecurityCommonTest {

	@TestPropertySource(properties = { "spring.cloud.security.enabled=false" })
	public static class SimpleTests extends AbstractSecurityCommonTest {

		@Test
		public void testOne() throws Exception {
			System.out.println(httpSourceSecurityConfiguration);
		}

	}
}
