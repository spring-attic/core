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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.cloud.stream.app.file.LocalDirectoryResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Reconfigure {@link LocalDirectoryResolver} to work with Cloud Foundry Volume Services if the app is bound to a
 * cloudfoundry nfs instance.
 *
 * @author David Turanski
 **/
@Configuration
@ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
public class NFSLocalDirectoryResolverAutoReconfiguration {

	@Bean
	public static NFSConfigPostProcessor nfsConfigPostProcessor(Environment environment) {
		return new NFSConfigPostProcessor(environment);
	}

	static class NFSConfigPostProcessor implements BeanPostProcessor {

		private final Environment environment;

		NFSConfigPostProcessor(Environment environment) {
			this.environment = environment;
		}

		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (bean instanceof LocalDirectoryResolver) {
				VcapService nfs = NFS.load(environment.getProperty("VCAP_SERVICES"),
					environment.getProperty("cloudfoundry.nfs.service.name", "nfs"));
				if (nfs != null) {
					bean = new LocalDirectoryResolver(nfs.getVolumeMounts().get(0).getContainerDir());
				}
			}
			return bean;
		}
	}
}
