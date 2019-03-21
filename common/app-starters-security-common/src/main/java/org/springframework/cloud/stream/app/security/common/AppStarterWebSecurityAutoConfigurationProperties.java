/*
 * Copyright 2018 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.security.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code AppStarterWebSecurityAutoConfiguration} properties.
 *
 * @author Christian Tzolov
 * @author Artem Bilan
 *
 * @since 2.1
 */
@ConfigurationProperties("spring.cloud.streamapp.security")
public class AppStarterWebSecurityAutoConfigurationProperties {


	/**
	 * The security enabling flag.
	 */
	private boolean enabled = true;

	/**
	 * The security CSRF enabling flag. Makes sense only if security 'enabled` is `true'.
	 */
	private boolean csrfEnabled = true;

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isCsrfEnabled() {
		return this.csrfEnabled;
	}

	public void setCsrfEnabled(boolean csrfEnabled) {
		this.csrfEnabled = csrfEnabled;
	}
}
