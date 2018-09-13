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

package org.springframework.cloud.stream.app.file.cloudfoundry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author David Turanski
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
class VcapService {
	@JsonIgnore
	private final static ObjectMapper objectMapper = new ObjectMapper();

	private Map<String, Object> credentials = new HashMap<>();

	private String label;

	private String name;

	private String plan;

	private String provider;

	@JsonProperty("instance_name")
	private String instanceName;

	@JsonProperty("syslog_drain_url")
	private String syslogDrainURL;

	private List<String> tags;

	@JsonProperty("volume_mounts")
	private List<VolumeMount> volumeMounts;

	public Map<String, Object> getCredentials() {
		return credentials;
	}

	public void setCredentials(Map<String, Object> credentials) {
		this.credentials = credentials;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPlan() {
		return plan;
	}

	public void setPlan(String plan) {
		this.plan = plan;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getSyslogDrainURL() {
		return syslogDrainURL;
	}

	public void setSyslogDrainURL(String syslogDrainURL) {
		this.syslogDrainURL = syslogDrainURL;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public List<VolumeMount> getVolumeMounts() {
		return volumeMounts;
	}

	public void setVolumeMounts(List<VolumeMount> volumeMounts) {
		this.volumeMounts = volumeMounts;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public String toString() {
		try {
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
