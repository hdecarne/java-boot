/*
 * Copyright (c) 2018-2019 Holger de Carne and contributors, All Rights Reserved.
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
import java.util.Enumeration;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Generic main class responsible for bootstrapping of the actual application. This also includes the proper setup of
 * the class loader depending on the execution context.
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
	 * Application entry point if run as Main class.
	 *
	 * @param args command line arguments.
	 */
	public static void main(String[] args) {
		int status = -1;

		try {
			if (DEBUG) {
				debug("Booting application...");
			}
			status = APPLICATION_MAIN.set(setupApplication()).run(args);
			if (DEBUG) {
				debug("Application finished with status: %1$d", status);
			}
		} catch (RuntimeException e) {
			error(e, "Application failed with exception: %1$s", e.getClass().getTypeName());
		}
		if (status != 0) {
			System.exit(status);
		}
	}

	/**
	 * Application entry point if run from another class (e.g. during tests).
	 *
	 * @param args command line arguments.
	 * @return the application status.
	 */
	public static int run(String[] args) {
		int status = -1;

		if (DEBUG) {
			debug("Running application...");
		}
		status = APPLICATION_MAIN.set(setupApplication()).run(args);
		if (DEBUG) {
			debug("Application finished with status: %1$d", status);
		}
		return status;
	}

	private static ApplicationMain setupApplication() {
		// Setup ClassLoader
		ClassLoader applicationClassLoader = setupApplicationClassLoader();

		// Determine application configuration resource
		String configName = getApplicationConfigName();

		if (DEBUG) {
			debug("Using application configuration: %1$s", configName);
		}

		Enumeration<URL> configUrls;

		try {
			configUrls = applicationClassLoader.getResources(configName);
		} catch (IOException e) {
			throw new ApplicationInitializationException(
					error(e, "Failed to locate application configuration: %1$s", configName));
		}
		if (!configUrls.hasMoreElements()) {
			throw new ApplicationInitializationException(
					error(null, "Failed to locate application configuration: %1$s", configName));
		}

		URL configUrl = configUrls.nextElement();

		if (configUrls.hasMoreElements()) {
			StringBuilder configUrlsString = new StringBuilder();

			configUrlsString.append(configUrl.toExternalForm());
			do {
				URL extraConfigUrl = configUrls.nextElement();

				configUrlsString.append(System.lineSeparator()).append('\t').append(extraConfigUrl.toExternalForm());
			} while (configUrls.hasMoreElements());
			throw new ApplicationInitializationException(
					error(null, "Found multiple application configurations: %1$s", configUrlsString));
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

		ApplicationMain applicationMain;

		try {
			applicationMain = Class.forName(applicationMainName, true, applicationClassLoader)
					.asSubclass(ApplicationMain.class).getConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new ApplicationInitializationException(
					error(e, "Failed to load & instantiate application main class: %1$s", applicationMainName), e);
		}
		return applicationMain;
	}

	private static String getApplicationConfigName() {
		StringBuilder configName = new StringBuilder();

		configName.append("META-INF/").append(Application.class.getName());

		String configNameSuffix = System.getProperty(Application.class.getName());

		if (configNameSuffix != null && !configNameSuffix.isEmpty()) {
			configName.append('.').append(configNameSuffix);
		}
		return configName.toString();
	}

	private static void evalConfigProperty(String propertyLine) {
		String trimmedPropertyLine = propertyLine.trim();

		if (DEBUG) {
			debug(" %1$s", trimmedPropertyLine);
		}
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
	private static ClassLoader setupApplicationClassLoader() {
		URL codeLocation = Application.class.getResource(Application.class.getSimpleName() + ".class");

		if (DEBUG) {
			debug("Code location: %1$s", codeLocation.toExternalForm());
		}

		String codeLocationProtocol = codeLocation.getProtocol();
		ClassLoader bootstrapClassLoader = Thread.currentThread().getContextClassLoader();

		if (bootstrapClassLoader == null) {
			bootstrapClassLoader = Application.class.getClassLoader();
		}

		Objects.requireNonNull(bootstrapClassLoader);

		ApplicationJarClassLoader applicationClassLoader = null;

		if ("jar".equals(codeLocationProtocol)) {
			try {
				JarURLConnection jarConnection = (JarURLConnection) codeLocation.openConnection();

				applicationClassLoader = new ApplicationJarClassLoader(jarConnection, bootstrapClassLoader);
			} catch (IOException e) {
				throw new ApplicationInitializationException(error(e,
						"Failed to access application jar via configuration: %1$s", codeLocation.toExternalForm()), e);
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

	/**
	 * Gets the currently running {@linkplain ApplicationMain} instance.
	 *
	 * @param <T> the actual main class type to retrieve.
	 * @param clazz the actual type of the {@linkplain ApplicationMain} class.
	 * @return the currently running {@linkplain ApplicationMain} class.
	 */
	public static <T extends ApplicationMain> T getMain(Class<T> clazz) {
		return clazz.cast(APPLICATION_MAIN.get());
	}

}
