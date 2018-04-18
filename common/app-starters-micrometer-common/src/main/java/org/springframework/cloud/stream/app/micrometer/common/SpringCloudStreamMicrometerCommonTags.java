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
package org.springframework.cloud.stream.app.micrometer.common;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto configuration extends the micrometer metrics with additional tags such as: stream name, application name,
 * instance index and guids. Later are necessary to allow discrimination and aggregation of app metrics by external
 * metrics collection and visualizaiton tools.
 *
 * @author Christian Tzolov
 */
@Configuration
public class SpringCloudStreamMicrometerCommonTags {

	@Value("${spring.cloud.dataflow.cluster.name:default}")
	private String clusterName;

	@Value("${spring.cloud.dataflow.stream.name:unknown}")
	private String streamName;

	@Value("${spring.cloud.dataflow.stream.app.label:unknown}")
	private String applicationName;

	@Value("${instance.index:unknown}")
	private String instanceIndex;

	@Value("${spring.cloud.application.guid:unknown}")
	private String applicationGuid;

	@Value("${spring.cloud.dataflow.stream.app.type:unknown}")
	private String applicationType;

	@Bean
	public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
		return registry -> registry.config()
				.commonTags("clusterName", clusterName)
				.commonTags("streamName", streamName)
				.commonTags("applicationName", applicationName)
				.commonTags("applicationType", applicationType)
				.commonTags("instanceIndex", instanceIndex)
				.commonTags("applicationGuid", applicationGuid);
	}

	/**
	 * This is a work around for: https://github.com/micrometer-metrics/micrometer/issues/544
	 */
	@Bean
	public MeterRegistryCustomizer<MeterRegistry> renameNameTag() {
		return registry -> {
			if (registry.getClass().getCanonicalName().contains("AtlasMeterRegistry")) {
				registry.config().meterFilter(MeterFilter.renameTag("spring.integration", "name", "aname"));
			}
		};
	}
}
