/*
 * Copyright (c) 2016-2018 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.boot.logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility class providing {@linkplain Log} related functions.
 */
public final class Logs {

	private Logs() {
		// prevent instantiation
	}

	/**
	 * Default {@linkplain ErrorManager} instance to use for error reporting.
	 */
	public static final ErrorManager DEFAULT_ERROR_MANAGER = new ErrorManager();

	private static final String CONFIG_BOOT = "logging-boot.properties";

	static {
		// Touch our custom level class to make sure the level names are registered
		LogLevel.LEVEL_NOTICE.getName();
		// Makes sure the {@linkplain LogManager} is configured in a minimal way (unless not specific configuration has
		// already been applied).
		if (System.getProperty("java.util.logging.config.class") == null
				&& System.getProperty("java.util.logging.config.file") == null) {
			File userConfig = new File(new File(System.getProperty("java.home"), "lib"), "logging.properites");

			if (!userConfig.exists()) {
				try {
					readConfig(CONFIG_BOOT);
				} catch (IOException e) {
					DEFAULT_ERROR_MANAGER.error("Failed to read logging config: " + CONFIG_BOOT, e,
							ErrorManager.GENERIC_FAILURE);
				}
			}
		}
	}

	static void initialize() {
		// Nothing to do here; loading this class is sufficient
	}

	/**
	 * FLushs all currently configured {@linkplain Handler} instance (e.g. during application exit).
	 */
	public static void flush() {
		LogManager manager = LogManager.getLogManager();
		Enumeration<String> loggerNames = manager.getLoggerNames();
		Set<Handler> handlers = new HashSet<>();

		while (loggerNames.hasMoreElements()) {
			String loggerName = loggerNames.nextElement();
			Logger logger = manager.getLogger(loggerName);

			if (logger != null) {
				for (Handler handler : logger.getHandlers()) {
					if (handlers.add(handler)) {
						handler.flush();
					}
				}
			}
		}
	}

	/**
	 * Standard name for default logging config.
	 */
	public static final String CONFIG_DEFAULT = "logging-default.properties";

	/**
	 * Standard name for verbose logging config.
	 */
	public static final String CONFIG_VERBOSE = "logging-verbose.properties";

	/**
	 * Standard name for debug logging config.
	 */
	public static final String CONFIG_DEBUG = "logging-debug.properties";

	/**
	 * Reads and applies a {@linkplain LogManager} configuration.
	 *
	 * @param config the name of the configuration to read.
	 * @throws IOException if an I/O error occurs while reading the configuration.
	 */
	public static void readConfig(String config) throws IOException {
		LogManager manager = LogManager.getLogManager();

		try (InputStream configInputStream = openConfig(config)) {
			manager.readConfiguration(configInputStream);
		}
		applyApplicationConfig(manager);
	}

	/**
	 * Reads and applies a {@linkplain LogManager} configuration.
	 *
	 * @param config the {@linkplain URL} of the configuration to read.
	 * @throws IOException if an I/O error occurs while reading the configuration.
	 */
	public static void readConfig(URL config) throws IOException {
		LogManager manager = LogManager.getLogManager();

		try (InputStream configInputStream = config.openStream()) {
			manager.readConfiguration(configInputStream);
		}
		applyApplicationConfig(manager);
	}

	@SuppressWarnings("resource")
	private static InputStream openConfig(String config) throws FileNotFoundException {
		InputStream configInputStream;

		try {
			configInputStream = new FileInputStream(config);
		} catch (@SuppressWarnings("unused") FileNotFoundException e) {
			configInputStream = null;
		}
		if (configInputStream == null) {
			configInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(config);
			if (configInputStream == null) {
				throw new FileNotFoundException("Unable to open logging config: " + config);
			}
		}
		return configInputStream;
	}

	private static void applyApplicationConfig(LogManager manager) {
		Logger rootLogger = manager.getLogger("");
		LogLevel rootLoggerLevel = LogLevel.fromLevel(rootLogger.getLevel());
		String[] handlerNames = getStringsProperty(manager, "application.handlers");

		for (String handlerName : handlerNames) {
			try {
				Handler handler = newClassInstance(handlerName, Handler.class);
				Level level = getLevelProperty(manager, "application." + handlerName + ".info", rootLoggerLevel);

				handler.setLevel(level);
				rootLogger.addHandler(handler);
			} catch (ReflectiveOperationException e) {
				Logs.DEFAULT_ERROR_MANAGER.error("Unable to instantiate handler: " + handlerName, e,
						ErrorManager.GENERIC_FAILURE);
			}
		}
	}

	/**
	 * Gets a {@code String} property from a {@linkplain LogManager}'s current configuration.
	 *
	 * @param manager the {@linkplain LogManager} to get the configuration from.
	 * @param name the property name to evaluate.
	 * @param defaultValue the the default value to return in case the property is undefined.
	 * @return the defined value or the default value if the property is undefined.
	 */
	public static String getStringProperty(LogManager manager, String name, String defaultValue) {
		String property = manager.getProperty(name);

		return (property != null ? property : defaultValue);
	}

	/**
	 * Gets a {@code String} array property from a {@linkplain LogManager}'s current configuration.
	 *
	 * @param manager the {@linkplain LogManager} to get the configuration from.
	 * @param name the property name to evaluate.
	 * @return the defined value or the default value if the property is undefined.
	 */
	public static String[] getStringsProperty(LogManager manager, String name) {
		String property = manager.getProperty(name);
		List<String> propertyValue = new ArrayList<>();

		if (property != null) {
			StringTokenizer propertyTokens = new StringTokenizer(property, " ,");

			while (propertyTokens.hasMoreElements()) {
				String propertyToken = propertyTokens.nextToken().trim();

				if (propertyToken.length() > 0) {
					propertyValue.add(propertyToken);
				}
			}
		}
		return propertyValue.toArray(new String[propertyValue.size()]);
	}

	/**
	 * Gets a {@code int} property from a {@linkplain LogManager}'s current configuration.
	 *
	 * @param manager the {@linkplain LogManager} to get the configuration from.
	 * @param name the property name to evaluate.
	 * @param defaultValue the the default value to return in case the property is undefined.
	 * @return the defined value or the default value if the property is undefined.
	 */
	public static int getIntProperty(LogManager manager, String name, int defaultValue) {
		String property = manager.getProperty(name);
		int propertyValue = defaultValue;

		if (property != null) {
			try {
				propertyValue = Integer.parseInt(property);
			} catch (NumberFormatException e) {
				DEFAULT_ERROR_MANAGER.error("Invalid int property " + name, e, ErrorManager.GENERIC_FAILURE);
			}
		}
		return propertyValue;
	}

	/**
	 * Gets a {@code boolean} property from a {@linkplain LogManager}'s current configuration.
	 *
	 * @param manager the {@linkplain LogManager} to get the configuration from.
	 * @param name the property name to evaluate.
	 * @param defaultValue the the default value to return in case the property is undefined.
	 * @return the defined value or the default value if the property is undefined.
	 */
	public static boolean getBooleanProperty(LogManager manager, String name, boolean defaultValue) {
		String property = manager.getProperty(name);
		boolean propertyValue = defaultValue;

		if (property != null) {
			propertyValue = Boolean.parseBoolean(property.trim());
		}
		return propertyValue;
	}

	/**
	 * Gets a {@linkplain Level} property from a {@linkplain LogManager}'s current configuration.
	 *
	 * @param manager the {@linkplain LogManager} to get the configuration from.
	 * @param name the property name to evaluate.
	 * @param defaultValue the the default value to return in case the property is undefined.
	 * @return the defined value or the default value if the property is undefined.
	 */
	public static Level getLevelProperty(LogManager manager, String name, LogLevel defaultValue) {
		String property = manager.getProperty(name);
		Level propertyValue = defaultValue;

		if (property != null) {
			try {
				propertyValue = Level.parse(property.trim());
			} catch (Exception e) {
				Logs.DEFAULT_ERROR_MANAGER.error("Invalid level property: " + name, e, ErrorManager.GENERIC_FAILURE);
			}
		}
		return propertyValue;
	}

	/**
	 * Gets a {@linkplain Filter} property from a {@linkplain LogManager}'s current configuration.
	 *
	 * @param manager the {@linkplain LogManager} to get the configuration from.
	 * @param name the property name to evaluate.
	 * @param defaultValue the the default value to return in case the property is undefined.
	 * @return the defined value or the default value if the property is undefined.
	 */
	@Nullable
	public static Filter getFilterProperty(LogManager manager, String name, @Nullable Filter defaultValue) {
		String property = manager.getProperty(name);
		Filter propertyValue = defaultValue;

		if (property != null) {
			try {
				propertyValue = newClassInstance(property, Filter.class);
			} catch (ReflectiveOperationException e) {
				Logs.DEFAULT_ERROR_MANAGER.error("Invalid filter property: " + name, e, ErrorManager.GENERIC_FAILURE);
			}
		}
		return propertyValue;
	}

	/**
	 * Gets a {@linkplain Formatter} property from a {@linkplain LogManager}'s current configuration.
	 *
	 * @param manager the {@linkplain LogManager} to get the configuration from.
	 * @param name the property name to evaluate.
	 * @param defaultValue the the default value to return in case the property is undefined.
	 * @return the defined value or the default value if the property is undefined.
	 */
	public static Formatter getFormatterProperty(LogManager manager, String name, Formatter defaultValue) {
		String property = manager.getProperty(name);
		Formatter propertyValue = defaultValue;

		if (property != null) {
			try {
				propertyValue = newClassInstance(property, Formatter.class);
			} catch (ReflectiveOperationException e) {
				Logs.DEFAULT_ERROR_MANAGER.error("Invalid formatter property: " + name, e,
						ErrorManager.GENERIC_FAILURE);
			}
		}
		return propertyValue;
	}

	private static <T> T newClassInstance(String name, Class<T> type) throws ReflectiveOperationException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		if (classLoader == null) {
			classLoader = Logs.class.getClassLoader();
		}
		return Class.forName(name, false, classLoader).asSubclass(type).getConstructor().newInstance();
	}

}
