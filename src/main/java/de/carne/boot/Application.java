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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.StringTokenizer;

/**
 * Generic main class responsible for bootstrapping of the actual application and taking care of proper class loader
 * setup depending on the execution context.
 */
public final class Application {

	private Application() {
		// prevent instantiation
	}

	// Early log support
	private static final boolean DEBUG = Boolean.getBoolean(Application.class.getName() + ".debug");

	@SuppressWarnings("squid:S106")
	private static String debug(String format, Object... args) {
		String msg = String.format(format, args);

		System.out.println(msg);
		return msg;
	}

	@SuppressWarnings("squid:S106")
	private static String error(@Nullable Throwable thrown, String format, Object... args) {
		String msg = String.format(format, args);

		System.err.println(msg);
		if (thrown != null) {
			thrown.printStackTrace(System.err);
		}
		return msg;
	}

	private static final InstanceHolder<ApplicationMain> APPLICATION_MAIN = new InstanceHolder<>();

	/**
	 * Main entry point.
	 *
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		boolean testMode = false;

		if (DEBUG) {
			debug("Booting application...");
			testMode = new Exception().getStackTrace().length > 1;
			debug("Test mode detected: " + testMode);
		}

		int status = -1;

		try {
			status = APPLICATION_MAIN.set(evalConfig()).run(args);
			if (DEBUG) {
				debug("Application finished with status: %1$d", status);
			}
		} catch (RuntimeException e) {
			error(e, "Application failed with exception: %1$s", e.getClass().getTypeName());
			if (testMode) {
				throw e;
			}
		}
		if (status != 0) {
			System.exit(status);
		}
	}

	private static ApplicationMain evalConfig() {
		// Determine application configuration resource
		String configName = getConfigName();

		if (DEBUG) {
			debug("Using application configuration: %1$s", configName);
		}

		URL configUrl = Application.class.getResource(configName);

		if (configUrl == null) {
			throw new ApplicationInitializationException(
					error(null, "Failed to locate application configuration: %1$s", configName));
		}

		if (DEBUG) {
			debug("Found application configuration: %1$s", configUrl.toExternalForm());
		}

		// Read and evaluate application configuration resource
		String applicationMainName;

		try (BufferedReader configReader = new BufferedReader(new InputStreamReader(configUrl.openStream()))) {
			applicationMainName = configReader.readLine();
			if (applicationMainName == null) {
				throw new EOFException(
						error(null, "Empty application configuration: %1$s", configUrl.toExternalForm()));
			}

			if (DEBUG) {
				debug("Using application main class: %1$s", applicationMainName);
				debug("Applying system properties:");
			}

			String propertyLine;

			while ((propertyLine = configReader.readLine()) != null) {
				evalConfigProperty(propertyLine);
			}
		} catch (IOException e) {
			throw new ApplicationInitializationException(
					error(e, "Failed to read application configuration: %1$s", configName), e);
		}

		// Load & instantiate application main class
		if (DEBUG) {
			debug("Loading & instantiating application main class: %1$s", applicationMainName);
		}

		ClassLoader classLoader = setupClassLoader(configUrl);
		ApplicationMain applicationMain;

		try {
			applicationMain = Class.forName(applicationMainName, true, classLoader).asSubclass(ApplicationMain.class)
					.getConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new ApplicationInitializationException(
					error(e, "Failed to load & instantiate application main class: %1$s", applicationMainName), e);
		}
		return applicationMain;
	}

	private static String getConfigName() {
		StringBuilder configName = new StringBuilder();

		configName.append("/META-INF/").append(Application.class.getName());

		String configNameSuffix = System.getProperty(Application.class.getName());

		if (configNameSuffix != null && !configNameSuffix.isEmpty()) {
			configName.append('.').append(configNameSuffix);
		}
		return configName.toString();
	}

	private static void evalConfigProperty(String propertyLine) {
		String trimmedPropertyLine = propertyLine.trim();

		if (trimmedPropertyLine.length() > 0 && !trimmedPropertyLine.startsWith("#")) {
			int splitIndex = trimmedPropertyLine.indexOf('=');

			if (splitIndex < 0) {
				System.setProperty(trimmedPropertyLine, Boolean.TRUE.toString());
			} else if (splitIndex > 0) {
				String key = trimmedPropertyLine.substring(0, splitIndex).trim();
				String value = trimmedPropertyLine.substring(splitIndex + 1).trim();

				System.setProperty(key, value);
			} else {
				error(null, "Ignoring invalid system property configuration: %1$s", propertyLine);
			}
		}
	}

	@SuppressWarnings("squid:S2095")
	private static ClassLoader setupClassLoader(URL configUrl) {
		String configUrlProtocol = configUrl.getProtocol();
		ClassLoader bootstrapClassLoader = Application.class.getClassLoader();
		ApplicationJarClassLoader applicationClassLoader = null;

		if ("jar".equals(configUrlProtocol)) {
			try {
				JarURLConnection jarConnection = (JarURLConnection) configUrl.openConnection();
				ApplicationJarClassLoader.ClassFilter filter = getBootstrapClassesFilter();

				if (DEBUG) {
					debug("Bootstrap-Classes: %1$s", filter);
				}
				applicationClassLoader = new ApplicationJarClassLoader(jarConnection.getJarFileURL(),
						bootstrapClassLoader, filter);
			} catch (IOException e) {
				throw new ApplicationInitializationException(error(e,
						"Failed to access application jar via configuration: %1$s", configUrl.toExternalForm()), e);
			}
			if (DEBUG) {
				debug("Class-Path:");
				for (URL url : applicationClassLoader.getURLs()) {
					debug(" %1$s", url.toExternalForm());
				}
			}
			Thread.currentThread().setContextClassLoader(applicationClassLoader);
		}
		return (applicationClassLoader != null ? applicationClassLoader : bootstrapClassLoader);
	}

	private static ApplicationJarClassLoader.ClassFilter getBootstrapClassesFilter() {
		ApplicationJarClassLoader.ClassFilter filter = ApplicationJarClassLoader.filter()
				.exclude(Application.class.getPackage().getName());
		String bootstrapClassesProperty = System.getProperty(Application.class.getName() + ".bootstrapClasses", "");
		StringTokenizer bootstrapClasses = new StringTokenizer(bootstrapClassesProperty, "|");

		while (bootstrapClasses.hasMoreTokens()) {
			filter.exclude(bootstrapClasses.nextToken());
		}
		return filter;
	}

	/**
	 * Get the currently executing {@linkplain ApplicationMain} instance.
	 *
	 * @param <T> the actual main class type to retrieve.
	 * @param clazz the actual type of the {@linkplain ApplicationMain} class.
	 * @return the currently executing {@linkplain ApplicationMain} class.
	 */
	public static <T extends ApplicationMain> T getMain(Class<T> clazz) {
		return clazz.cast(APPLICATION_MAIN.get());
	}

}
