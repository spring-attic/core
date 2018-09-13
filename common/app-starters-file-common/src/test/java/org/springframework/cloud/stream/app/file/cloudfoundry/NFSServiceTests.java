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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;

import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author David Turanski
 **/
public class NFSServiceTests {

	@Test
	public void test() throws IOException {

		InputStream is = new ClassPathResource("vcap-service-nfs.json").getInputStream();

		VcapService nfs = NFS.load(is, "nfs");

		List<VolumeMount> volumeMounts = nfs.getVolumeMounts();
		assertThat(volumeMounts.get(0).getContainerDir()).isEqualTo("/vcap/foo/bar");

	}
}
