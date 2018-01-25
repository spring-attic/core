/*
 * Copyright 2018 the original author or authors.
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

package org.springframework.cloud.stream.app.postprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

/**
 * An {@link EnvironmentPostProcessor} to set the {@code spring.cloud.stream.bindings.{input,output}.contentType}
 * channel properties to a default of {@code application/octet-stream} if it has not been set already.
 *
 * Subclasses may extend this class to change the default content type and channel name(s).
 *
 * @author Chris Schaefer
 */
public class ContentTypeEnvironmentPostProcessor implements EnvironmentPostProcessor {
	private String[] channelNames = { Source.OUTPUT, Sink.INPUT };
	private String contentType = "application/octet-stream";

	protected static final String PROPERTY_SOURCE_KEY_NAME = ContentTypeEnvironmentPostProcessor.class.getName();
	protected static final String CONTENT_TYPE_PROPERTY_PREFIX = "spring.cloud.stream.bindings.";
	protected static final String CONTENT_TYPE_PROPERTY_SUFFIX = ".contentType";

	public ContentTypeEnvironmentPostProcessor() {
		super();
	}

	protected ContentTypeEnvironmentPostProcessor(String[] channelNames) {
		this.channelNames = channelNames;
	}

	protected ContentTypeEnvironmentPostProcessor(String contentType) {
		this.contentType = contentType;
	}

	protected ContentTypeEnvironmentPostProcessor(String[] channelNames, String contentType) {
		this.channelNames = channelNames;
		this.contentType = contentType;
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment configurableEnvironment, SpringApplication springApplication) {
		Properties properties = new Properties();

		for (String channelName : channelNames) {
			String propertyKey = CONTENT_TYPE_PROPERTY_PREFIX + channelName + CONTENT_TYPE_PROPERTY_SUFFIX;

			if (!configurableEnvironment.containsProperty(propertyKey)) {
				properties.setProperty(propertyKey, contentType);
			}
		}

		if (!properties.isEmpty()) {
			PropertiesPropertySource propertiesPropertySource =
					new PropertiesPropertySource(PROPERTY_SOURCE_KEY_NAME, properties);
			configurableEnvironment.getPropertySources().addLast(propertiesPropertySource);
		}
	}
}
