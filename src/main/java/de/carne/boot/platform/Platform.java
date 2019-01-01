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
package de.carne.boot.platform;

import java.util.Objects;

/**
 * Utility class providing functions to determine Java platform type and capabilities.
 */
public final class Platform {

	private Platform() {
		// Prevent instantiation
	}

	/**
	 * System property: {@code "os.arch"}
	 */
	public static final String SYSTEM_OS_ARCH = Objects.requireNonNull(System.getProperty("os.arch"));

	/**
	 * System property: {@code "os.name"}
	 */
	public static final String SYSTEM_OS_NAME = Objects.requireNonNull(System.getProperty("os.name"));

	/**
	 * System property: {@code "os.version"}
	 */
	public static final String SYSTEM_OS_VERSION = Objects.requireNonNull(System.getProperty("os.version"));

	/**
	 * Operating System: Linux
	 */
	public static final boolean IS_LINUX = SYSTEM_OS_NAME.toUpperCase().startsWith("LINUX");

	/**
	 * Operating System: macOS
	 */
	public static final boolean IS_MACOS = SYSTEM_OS_NAME.startsWith("Mac OS X");

	/**
	 * Operating System: Windows
	 */
	public static final boolean IS_WINDOWS = SYSTEM_OS_NAME.startsWith("Windows");

}
