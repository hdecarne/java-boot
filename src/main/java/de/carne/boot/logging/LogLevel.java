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
package de.carne.boot.logging;

import java.util.logging.Level;

/**
 * Custom {@linkplain Level} class for the log levels used by class {@linkplain Log}.
 */
public class LogLevel extends Level {

	private static final long serialVersionUID = 9195245351818452478L;

	private static final String RESOURCE_BUNDLE = LogLevel.class.getName() + "Names";

	/**
	 * Level Notice.
	 */
	public static final LogLevel LEVEL_NOTICE = new LogLevel("LEVEL_NOTICE", Level.OFF.intValue() - 1);

	/**
	 * Level Error.
	 */
	public static final LogLevel LEVEL_ERROR = new LogLevel("LEVEL_ERROR", Level.SEVERE.intValue());

	/**
	 * Level Warning.
	 */
	public static final LogLevel LEVEL_WARNING = new LogLevel("LEVEL_WARNING", Level.WARNING.intValue());

	/**
	 * Level Info.
	 */
	public static final LogLevel LEVEL_INFO = new LogLevel("LEVEL_INFO", Level.INFO.intValue());

	/**
	 * Level Debug.
	 */
	public static final LogLevel LEVEL_DEBUG = new LogLevel("LEVEL_DEBUG", Level.FINE.intValue());

	/**
	 * Level Trace.
	 */
	public static final LogLevel LEVEL_TRACE = new LogLevel("LEVEL_TRACE", Level.FINEST.intValue());

	private LogLevel(String name, int levelValue) {
		super(name, levelValue, RESOURCE_BUNDLE);
	}

	/**
	 * Gets the {@linkplain LogLevel} corresponding to a {@linkplain Level} instance.
	 *
	 * @param level the {@linkplain Level} instance to map.
	 * @return the {@linkplain LogLevel} corresponding to a {@linkplain Level} instance.
	 */
	public static LogLevel fromLevel(Level level) {
		int levelValue = level.intValue();
		LogLevel logLevel;

		if (levelValue <= LogLevel.LEVEL_TRACE.intValue()) {
			logLevel = LogLevel.LEVEL_TRACE;
		} else if (levelValue <= LogLevel.LEVEL_DEBUG.intValue()) {
			logLevel = LogLevel.LEVEL_DEBUG;
		} else if (levelValue <= LogLevel.LEVEL_INFO.intValue()) {
			logLevel = LogLevel.LEVEL_INFO;
		} else if (levelValue <= LogLevel.LEVEL_WARNING.intValue()) {
			logLevel = LogLevel.LEVEL_WARNING;
		} else if (levelValue <= LogLevel.LEVEL_ERROR.intValue()) {
			logLevel = LogLevel.LEVEL_ERROR;
		} else {
			logLevel = LogLevel.LEVEL_NOTICE;
		}
		return logLevel;
	}

}
