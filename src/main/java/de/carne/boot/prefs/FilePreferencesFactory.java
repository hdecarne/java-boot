/*
 * Copyright (c) 2018-2020 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.boot.prefs;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.boot.Exceptions;
import de.carne.boot.logging.Log;

/**
 * Custom {@linkplain PreferencesFactory} implementation for storing preferences into human readable text (*.conf)
 * files.
 */
public class FilePreferencesFactory implements PreferencesFactory {

	private static final Log LOG = new Log();

	private static final String PROPERTY_STORE_HOME = FilePreferences.class.getName();

	private static final @Nullable String STORE_HOME = System.getProperty(PROPERTY_STORE_HOME);

	@Override
	public Preferences systemRoot() {
		FilePreferencesStore store;

		if (STORE_HOME != null) {
			store = FilePreferencesStore.fromFile(systemRootFile());
		} else {
			LOG.debug("Store home property {0} not set; using transient store", PROPERTY_STORE_HOME);

			store = FilePreferencesStore.fromData(new Properties());
		}
		return store.root();
	}

	@Override
	public Preferences userRoot() {
		FilePreferencesStore store;

		if (STORE_HOME != null) {
			store = FilePreferencesStore.fromFile(userRootFile());
		} else {
			LOG.debug("Store home property {0} not set; using transient store", PROPERTY_STORE_HOME);

			store = FilePreferencesStore.fromData(new Properties());
		}
		return store.root();
	}

	/**
	 * Gets the file path used to store system preferences.
	 *
	 * @return the file path used to store system preferences.
	 */
	public static Path systemRootFile() {
		String systemName = getSystemName();

		return customRootFile("system." + systemName + ".conf");
	}

	/**
	 * Gets the file path used to store user preferences.
	 *
	 * @return the file path used to store user preferences.
	 */
	public static Path userRootFile() {
		return customRootFile("user.conf");
	}

	/**
	 * Gets the file path used to store custom preferences.
	 *
	 * @param name the file name to use.
	 * @return the file path used to store custom preferences.
	 */
	public static Path customRootFile(String name) {
		return resolveStoreHomeFile(name);
	}

	/**
	 * Creates a {@linkplain Preferences} object backed up by a custom configuration file.
	 * <p>
	 * The created {@linkplain Preferences} object can be stored if the submitted custom configuration file is writable
	 * by the VM.
	 *
	 * @param file the custom configuration file to use as a preference store.
	 * @return the created {@linkplain Preferences} object.
	 */
	public static Preferences customRoot(Path file) {
		return FilePreferencesStore.fromFile(file).root();
	}

	/**
	 * Creates a {@linkplain Preferences} object backed up by a custom configuration file.
	 * <p>
	 * The created {@linkplain Preferences} object can be stored if the submitted custom configuration file is writable
	 * by the VM.
	 *
	 * @param file the custom configuration file to use as a preference store.
	 * @return the created {@linkplain Preferences} object.
	 */
	public static Preferences customRoot(File file) {
		return customRoot(file.toPath());
	}

	/**
	 * Creates a {@linkplain Preferences} object backed up by a {@linkplain Properties} object.
	 * <p>
	 * The created {@linkplain Preferences} object cannot be stored.
	 *
	 * @param data the {@linkplain Properties} object defining the preference data.
	 * @return the created {@linkplain Preferences} object.
	 */
	public static Preferences customRoot(Properties data) {
		return FilePreferencesStore.fromData(data).root();
	}

	/**
	 * Flush any file based {@linkplain FilePreferences} instance to the backing store.
	 */
	public static void flush() {
		FilePreferencesStore.flushFileStores();
	}

	private static Path resolveStoreHomeFile(String name) {
		Path userHome = Paths.get(System.getProperty("user.home", "."));
		Path storeHome = userHome
				.resolve(STORE_HOME != null ? STORE_HOME : "." + FilePreferencesFactory.class.getPackage().getName());

		return storeHome.resolve(name);
	}

	private static String getSystemName() {
		String systemName;

		try {
			String hostname = InetAddress.getLocalHost().getHostName();
			int domainIndex = hostname.indexOf('.');

			systemName = (domainIndex > 0 ? hostname.substring(0, domainIndex) : hostname);
		} catch (UnknownHostException e) {
			Exceptions.ignore(e);
			systemName = "localhost";
		}
		return systemName;
	}

}
