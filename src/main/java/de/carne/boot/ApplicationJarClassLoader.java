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

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	 * @param jarFile the the Jar file containing the classes to load.
	 * @param parent the parent {@linkplain ClassLoader} to use for delegation.
	 * @throws IOException if an I/O error occurs while accessing the Jar file.
	 */
	public ApplicationJarClassLoader(File jarFile, ClassLoader parent) throws IOException {
		this(jarFile, new URLClassLoader(new URL[] { jarFile.toURI().toURL() }, parent), parent);
	}

	private ApplicationJarClassLoader(File jarFile, URLClassLoader jarLoader, ClassLoader parent) throws IOException {
		super(assembleExternalJarClasspath(jarFile, jarLoader), parent);
	}

	ApplicationJarClassLoader(JarURLConnection inlineJarConnection, ClassLoader parent) throws IOException {
		super(assembleInlineJarClasspath(inlineJarConnection, parent), parent);
	}

	private static URL[] assembleInlineJarClasspath(JarURLConnection jarConnection, ClassLoader jarLoader)
			throws IOException {
		List<String> jarJars;

		try (Stream<JarEntry> jarEntries = jarConnection.getJarFile().stream()) {
			jarJars = jarEntries.filter(entry -> entry.getName().endsWith(".jar")).map(JarEntry::getName)
					.collect(Collectors.toList());
		}
		return assembleClasspath(jarJars, jarLoader);
	}

	private static URL[] assembleExternalJarClasspath(File file, ClassLoader jarLoader) throws IOException {
		List<String> jarJars;

		try (JarFile jarFile = new JarFile(file); Stream<JarEntry> jarEntries = jarFile.stream()) {
			jarJars = jarEntries.filter(entry -> entry.getName().endsWith(".jar")).map(JarEntry::getName)
					.collect(Collectors.toList());
		}
		return assembleClasspath(jarJars, jarLoader);
	}

	private static URL[] assembleClasspath(List<String> jarJars, ClassLoader jarLoader) throws IOException {
		URL[] classpath = new URL[jarJars.size()];

		if (classpath.length > 0) {
			ApplicationJarURLStreamHandlerFactory jarShf = new ApplicationJarURLStreamHandlerFactory(jarLoader);
			int classpathIndex = 0;

			for (String jarJar : jarJars) {
				classpath[classpathIndex] = jarShf.getJarJarUrl(jarJar);
				classpathIndex++;
			}
		}
		return classpath;
	}

}
