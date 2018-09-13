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

package org.springframework.cloud.stream.app.file;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author David Turanski
 **/
public class LocalDirectoryResolver {

	private final static Log log = LogFactory.getLog(LocalDirectoryResolver.class);

	private final String rootPath;

	public LocalDirectoryResolver() {
		this(null);
	}

	public LocalDirectoryResolver(String rootPath) {
		this.rootPath = rootPath;
	}

	public File resolve(String targetPath) {

		Path path;
		if (rootPath != null) {
			if (Paths.get(targetPath).isAbsolute()) {
				log.warn(String.format("converting absolute path %s to relative using root path %s",targetPath,
					rootPath));
			}

			path = Paths.get(rootPath, targetPath);
		}
		else {
			path = Paths.get(targetPath);
		}

		return path.toFile();
	}
}