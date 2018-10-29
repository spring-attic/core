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

import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author Christian Tzolov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AbstractSecurityCommonTests.AutoConfigurationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AbstractSecurityCommonTests {

	//@Autowired
	//protected SecurityCommonConfiguration.HttpSourceSecurityConfiguration httpSourceSecurityConfiguration;

	//@Autowired
	//protected Processor processor;

	//@Autowired
	//protected MessageCollector messageCollector;

	@Autowired
	protected MockMvc mvc;

	@SpringBootApplication
	@Controller
	//@EnableBinding(Processor.class)
	public static class AutoConfigurationApplication {
		public static void main(String[] args) {
			SpringApplication.run(AutoConfigurationApplication.class, args);
		}
	}
}
