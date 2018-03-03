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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * {@linkplain ClassLoader} used to load classes from a Jar file including classes from any nested Jar file.
 */
public final class ApplicationJarClassLoader extends URLClassLoader {

	static {
		registerAsParallelCapable();
	}

	/**
	 * By setting an {@linkplain ClassFilter} during {@linkplain ApplicationJarClassLoader} creation the load behavior
	 * can be fine tuned. Default behavior is to search for all classes in the application Jars first. If a class filter
	 * is defined only class names matched by the filter are searched in the application Jars.
	 */
	public static class ClassFilter {

		private final Set<String> includePrefixes = new HashSet<>();
		private final Set<String> excludePrefixes = new HashSet<>();

		/**
		 * Add an include prefix to the filter.
		 *
		 * @param includePrefix the include prefix to add.
		 * @return the updated {@linkplain ClassFilter}.
		 */
		public ClassFilter include(String includePrefix) {
			this.includePrefixes.add(includePrefix);
			return this;
		}

		/**
		 * Add an exclude prefix to the filter.
		 *
		 * @param excludePrefix the exclude prefix to add.
		 * @return the updated {@linkplain ClassFilter}.
		 */
		public ClassFilter exclude(String excludePrefix) {
			this.excludePrefixes.add(excludePrefix);
			return this;
		}

		/**
		 * Check whether a specific class name matches the filter.
		 * <p>
		 * A class name matches the filter if it matches at least one of the include filters and none of the exclude
		 * filters.
		 * </p>
		 *
		 * @param name the class name to check.
		 * @return {@code true} if the class name matches the filter. {@code false} otherwise.
		 */
		public boolean matches(String name) {
			boolean matches = true;

			if (!this.includePrefixes.isEmpty()) {
				matches = false;
				for (String includePrefix : this.includePrefixes) {
					if (name.startsWith(includePrefix)) {
						matches = true;
						break;
					}
				}
			}
			if (matches) {
				for (String excludePrefix : this.excludePrefixes) {
					if (name.startsWith(excludePrefix)) {
						matches = false;
						break;
					}
				}
			}
			return matches;
		}

	}

	/**
	 * Convenience function to create an empty {@linkplain ClassFilter}.
	 *
	 * @return an empty {@linkplain ClassFilter}.
	 */
	public static ClassFilter filter() {
		return new ClassFilter();
	}

	private final ClassLoader parent;
	private final ClassFilter filter;

	/**
	 * Construct {@linkplain ApplicationJarClassLoader}.
	 *
	 * @param jarFileUrl the {@linkplain URL} of the Jar file containing the classes to load.
	 * @param parent the parent {@linkplain ClassLoader} used to access the jar as well as to load filtered classes.
	 * @throws IOException if an I/O error occurs while accessing the Jar file.
	 */
	public ApplicationJarClassLoader(URL jarFileUrl, ClassLoader parent) throws IOException {
		this(jarFileUrl, parent, filter());
	}

	/**
	 * Construct {@linkplain ApplicationJarClassLoader}.
	 *
	 * @param jarFileUrl the {@linkplain URL} of the Jar file containing the classes to load.
	 * @param parent the parent {@linkplain ClassLoader} used to access the jar as well as to load filtered classes.
	 * @param filter the {@linkplain ClassFilter} defining which classes should be loaded from the application Jars.
	 * @throws IOException if an I/O error occurs while accessing the Jar file.
	 */
	public ApplicationJarClassLoader(URL jarFileUrl, ClassLoader parent, ClassFilter filter) throws IOException {
		this(jarFileUrl, scanJarFile(jarFileUrl), new ApplicationJarURLStreamHandlerFactory(parent), filter);
	}

	private ApplicationJarClassLoader(URL jarFileUrl, List<String> jarJars, ApplicationJarURLStreamHandlerFactory shf,
			ClassFilter filter) throws IOException {
		super(assembleClasspath(jarFileUrl, jarJars, shf), null);
		this.parent = shf.getResourceLoader();
		this.filter = filter;
	}

	private static List<String> scanJarFile(URL jarFileUrl) throws IOException {
		List<String> jarJars;

		try (JarFile jarFile = new JarFile(jarFileUrl.getFile())) {
			jarJars = jarFile.stream().filter(entry -> entry.getName().endsWith(".jar")).map(JarEntry::getName)
					.collect(Collectors.toList());
		}
		return jarJars;
	}

	private static URL[] assembleClasspath(URL jarFileUrl, List<String> jarJars,
			ApplicationJarURLStreamHandlerFactory shf) throws IOException {
		List<URL> classpathUrls = new ArrayList<>();

		classpathUrls.add(jarFileUrl);
		for (String jarJar : jarJars) {
			classpathUrls.add(shf.getJarJarUrl(jarJar));
		}
		return classpathUrls.toArray(new URL[classpathUrls.size()]);
	}

	@Override
	public Class<?> loadClass(@Nullable String name) throws ClassNotFoundException {
		return (name != null && this.filter.matches(name) ? super.loadClass(name) : this.parent.loadClass(name));
	}

}
