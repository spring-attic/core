/*
 * Copyright 2015-2016 the original author or authors.
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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * {@code SecurityCommonAutoConfiguration} properties.
 *
 * @author Christian Tzolov
 * @author Artem Bilan
 */
@ConfigurationProperties("spring.cloud.security")
@Validated
public class SecurityCommonAutoConfigurationProperties {


	/**
	 * The security enabling flag.
	 */
	private boolean enabled = true;

	/**
	 * The security CSRF enabling flag. Makes sense only if 'enableSecurity = true'.
	 */
	private boolean csrfEnabled = true;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isCsrfEnabled() {
		return csrfEnabled;
	}

	public void setCsrfEnabled(boolean csrfEnabled) {
		this.csrfEnabled = csrfEnabled;
	}
}
