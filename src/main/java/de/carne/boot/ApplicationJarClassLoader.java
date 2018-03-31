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
 * {@linkplain ClassLoader} implementation used to load classes from a single Jar file including any nested Jar file
 * during class search.
 */
public final class ApplicationJarClassLoader extends URLClassLoader {

	static {
		registerAsParallelCapable();
	}

	/**
	 * By setting an {@linkplain ClassFilter} during {@linkplain ApplicationJarClassLoader} creation the load behavior
	 * can be fine tuned.
	 */
	public static class ClassFilter {

		private final boolean parentFirst;
		private final Set<String> includePrefixes = new HashSet<>();
		private final Set<String> excludePrefixes = new HashSet<>();

		ClassFilter(boolean parentFirst) {
			this.parentFirst = parentFirst;
		}

		/**
		 * Adds an include prefix to the filter.
		 *
		 * @param includePrefix the include prefix to add.
		 * @return the updated {@linkplain ClassFilter}.
		 */
		public ClassFilter include(String includePrefix) {
			this.includePrefixes.add(includePrefix);
			return this;
		}

		/**
		 * Adds an exclude prefix to the filter.
		 *
		 * @param excludePrefix the exclude prefix to add.
		 * @return the updated {@linkplain ClassFilter}.
		 */
		public ClassFilter exclude(String excludePrefix) {
			this.excludePrefixes.add(excludePrefix);
			return this;
		}

		/**
		 * Gets the {@code parentFirst} attribute set during filter creation.
		 * <p>
		 * The {@code parentFirst} attribute controls whether the normal delegation pattern for class loading is used
		 * ({@code true}) or if the parent class loader is only used for classes not matching the filter
		 * ({@code false}).
		 *
		 * @return whether to to use normal delegation pattern ({@code true}) or not ({@code false}).
		 */
		public boolean parentFirst() {
			return this.parentFirst;
		}

		/**
		 * Checks whether a specific class name matches the filter.
		 * <p>
		 * A class name matches the filter if it matches at least one of the include filters and none of the exclude
		 * filters.
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

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();

			buffer.append("parentFirst: ");
			buffer.append(this.parentFirst);
			buffer.append(" includes:");
			for (String includePrefix : this.includePrefixes) {
				buffer.append(' ');
				buffer.append(includePrefix);
			}
			buffer.append(" excludes:");
			for (String excludePrefix : this.excludePrefixes) {
				buffer.append(' ');
				buffer.append(excludePrefix);
			}
			return buffer.toString();
		}

	}

	/**
	 * Creates an empty {@linkplain ClassFilter} instance.
	 *
	 * @param parentFirst whether to use standard delegation ({@code true}) or not ({@code false}).
	 * @return an empty {@linkplain ClassFilter}.
	 */
	public static ClassFilter filter(boolean parentFirst) {
		return new ClassFilter(parentFirst);
	}

	private final ClassLoader parent;
	private final ClassFilter filter;

	/**
	 * Constructs a new {@linkplain ApplicationJarClassLoader} instance.
	 *
	 * @param jarFileUrl the {@linkplain URL} of the Jar file containing the classes to load.
	 * @param parent the parent {@linkplain ClassLoader} used to access the Far file as well as for delegation.
	 * @throws IOException if an I/O error occurs while accessing the Jar file.
	 */
	public ApplicationJarClassLoader(URL jarFileUrl, ClassLoader parent) throws IOException {
		this(jarFileUrl, parent, filter(true));
	}

	/**
	 * Constructs a new {@linkplain ApplicationJarClassLoader} instance.
	 *
	 * @param jarFileUrl the {@linkplain URL} of the Jar file containing the classes to load.
	 * @param parent the parent {@linkplain ClassLoader} used to access the Far file as well as for delegation.
	 * @param filter the {@linkplain ClassFilter} defining the actual load behavior.
	 * @throws IOException if an I/O error occurs while accessing the Jar file.
	 */
	public ApplicationJarClassLoader(URL jarFileUrl, ClassLoader parent, ClassFilter filter) throws IOException {
		this(jarFileUrl, scanJarFile(jarFileUrl), new ApplicationJarURLStreamHandlerFactory(parent), filter);
	}

	private ApplicationJarClassLoader(URL jarFileUrl, List<String> jarJars, ApplicationJarURLStreamHandlerFactory shf,
			ClassFilter filter) throws IOException {
		super(assembleClasspath(jarFileUrl, jarJars, shf), (filter.parentFirst() ? shf.getResourceLoader() : null));
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
