/*
 * Copyright (c) 2018 Holger de Carne and contributors, All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.carne.boot;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * {@linkplain ClassLoader} implementation used to load classes from a single Jar file including any nested Jar file
 * during class search.
 */
public final class ApplicationJarClassLoader extends URLClassLoader {

	static {
		registerAsParallelCapable();
	}

	/**
	 * Constructs a new {@linkplain ApplicationJarClassLoader} instance.
	 *
	 * @param jarFileUrl the {@linkplain URL} of the Jar file containing the classes to load.
	 * @param parent the parent {@linkplain ClassLoader} used to access the Jar file as well as for delegation.
	 * @throws IOException if an I/O error occurs while accessing the Jar file.
	 */
	public ApplicationJarClassLoader(URL jarFileUrl, ClassLoader parent) throws IOException {
		this(scanJarFile(jarFileUrl), new ApplicationJarURLStreamHandlerFactory(parent));
	}

	private ApplicationJarClassLoader(List<String> jarJars, ApplicationJarURLStreamHandlerFactory shf)
			throws IOException {
		super(assembleClasspath(jarJars, shf), shf.getResourceLoader());
	}

	private static List<String> scanJarFile(URL jarFileUrl) throws IOException {
		List<String> jarJars;

		try (JarFile jarFile = new JarFile(jarFileUrl.getFile())) {
			jarJars = jarFile.stream().filter(entry -> entry.getName().endsWith(".jar")).map(JarEntry::getName)
					.collect(Collectors.toList());
		}
		return jarJars;
	}

	private static URL[] assembleClasspath(List<String> jarJars, ApplicationJarURLStreamHandlerFactory shf)
			throws IOException {
		List<URL> classpathUrls = new ArrayList<>();

		for (String jarJar : jarJars) {
			classpathUrls.add(shf.getJarJarUrl(jarJar));
		}
		return classpathUrls.toArray(new URL[classpathUrls.size()]);
	}

}
