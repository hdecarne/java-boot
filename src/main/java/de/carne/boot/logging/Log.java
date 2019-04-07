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
package de.carne.boot.logging;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Wrapper class for the JDK's {@linkplain Logger} class to make logging easy and more efficient.
 */
public final class Log {

	static {
		Logs.initialize();
	}

	private final Logger logger;

	/**
	 * Constructs a new {@linkplain Log} instance.
	 * <p>
	 * The created {@linkplain Logger} is named after the calling class' name.
	 * </p>
	 */
	public Log() {
		this(Logger.getLogger(getCallerClassName()));
	}

	/**
	 * Constructs a new {@linkplain Log} instance.
	 * <p>
	 * The created {@linkplain Logger} is named after the calling class' name.
	 * </p>
	 *
	 * @param resourceBundleName the name of the {@linkplain ResourceBundle} to use for log message localization.
	 */
	public Log(String resourceBundleName) {
		this(Logger.getLogger(getCallerClassName(), resourceBundleName));
	}

	/**
	 * Constructs a new {@linkplain Log} instance.
	 *
	 * @param clazz the {@linkplain Class} to use for the {@linkplain Logger} name.
	 */
	public Log(Class<?> clazz) {
		this(Logger.getLogger(clazz.getName()));
	}

	/**
	 * Constructs a new {@linkplain Log} isntance.
	 *
	 * @param clazz the {@linkplain Class} to use for the {@linkplain Logger} name.
	 * @param resourceBundleName the name of the {@linkplain ResourceBundle} to use for log message localization.
	 */
	public Log(Class<?> clazz, String resourceBundleName) {
		this(Logger.getLogger(clazz.getName(), resourceBundleName));
	}

	private Log(Logger logger) {
		this.logger = logger;
	}

	private static final AtomicReference<@Nullable Log> rootHolder = new AtomicReference<>();

	/**
	 * Gets the {@linkplain Log} instance that represents the root {@linkplain Logger}.
	 *
	 * @return the {@linkplain Log} instance that represents the root {@linkplain Logger}.
	 */
	@SuppressWarnings("null")
	public static Log root() {
		return rootHolder.getAndUpdate(rootLog -> (rootLog != null ? rootLog : new Log(Logger.getLogger(""))));
	}

	/**
	 * Gets the {@linkplain Logger} represented by this instance.
	 *
	 * @return the {@linkplain Logger} represented by this instance.
	 */
	public Logger logger() {
		return this.logger;
	}

	/**
	 * Checks whether a message of the submitted {@linkplain Level} would be logged by this {@linkplain Log} instance.
	 *
	 * @param level the {@linkplain Level} to check.
	 * @return {@code true} if the submitted {@linkplain Level} is enabled.
	 */
	public boolean isLoggable(Level level) {
		return this.logger.isLoggable(level);
	}

	/**
	 * Logs a message with the given severity.
	 *
	 * @param level the {@linkplain Level} of the message.
	 * @param thrown the {@linkplain Throwable} related to the message (may be {@code null}).
	 * @param msg the message to log.
	 * @param parameters the message parameters to log.
	 */
	public void log(Level level, @Nullable Throwable thrown, String msg, Object... parameters) {
		if (this.logger.isLoggable(level)) {
			this.logger.log(level, thrown, () -> MessageFormat.format(msg, parameters));
		}
	}

	/**
	 * Checks whether a {@linkplain LogLevel#LEVEL_NOTICE} message of level would be logged by this {@linkplain Log}
	 * instance.
	 *
	 * @return {@code true} if {@linkplain LogLevel#LEVEL_NOTICE} is enabled.
	 */
	public boolean isNoticeLoggable() {
		return isLoggable(LogLevel.LEVEL_NOTICE);
	}

	/**
	 * Logs a {@linkplain LogLevel#LEVEL_NOTICE} message.
	 *
	 * @param msg the message to log.
	 * @param parameters the message parameters to log.
	 */
	public void notice(String msg, Object... parameters) {
		log(LogLevel.LEVEL_NOTICE, null, msg, parameters);
	}

	/**
	 * Logs a {@linkplain LogLevel#LEVEL_NOTICE} message.
	 *
	 * @param thrown the {@linkplain Throwable} related to the message (may be {@code null}).
	 * @param msg the message to log.
	 * @param parameters the message parameters to log.
	 */
	public void notice(Throwable thrown, String msg, Object... parameters) {
		log(LogLevel.LEVEL_NOTICE, thrown, msg, parameters);
	}

	/**
	 * Checks whether a {@linkplain LogLevel#LEVEL_ERROR} message of level would be logged by this {@linkplain Log}
	 * instance.
	 *
	 * @return {@code true} if {@linkplain LogLevel#LEVEL_ERROR} is enabled.
	 */
	public boolean isErrorLoggable() {
		return isLoggable(LogLevel.LEVEL_ERROR);
	}

	/**
	 * Logs a {@linkplain LogLevel#LEVEL_ERROR} message.
	 *
	 * @param msg the message to log.
	 * @param parameters the message parameters to log.
	 */
	public void error(String msg, Object... parameters) {
		log(LogLevel.LEVEL_ERROR, null, msg, parameters);
	}

	/**
	 * Logs a {@linkplain LogLevel#LEVEL_ERROR} message.
	 *
	 * @param thrown the {@linkplain Throwable} related to the message (may be {@code null}).
	 * @param msg the message to log.
	 * @param parameters the message parameters to log.
	 */
	public void error(Throwable thrown, String msg, Object... parameters) {
		log(LogLevel.LEVEL_ERROR, thrown, msg, parameters);
	}

	/**
	 * Checks whether a {@linkplain LogLevel#LEVEL_WARNING} message of level would be logged by this {@linkplain Log}
	 * instance.
	 *
	 * @return {@code true} if {@linkplain LogLevel#LEVEL_WARNING} is enabled.
	 */
	public boolean isWarningLoggable() {
		return isLoggable(LogLevel.LEVEL_WARNING);
	}

	/**
	 * Logs a {@linkplain LogLevel#LEVEL_WARNING} message.
	 *
	 * @param msg the message to log.
	 * @param parameters the message parameters to log.
	 */
	public void warning(String msg, Object... parameters) {
		log(LogLevel.LEVEL_WARNING, null, msg, parameters);
	}

	/**
	 * Logs a {@linkplain LogLevel#LEVEL_WARNING} message.
	 *
	 * @param thrown the {@linkplain Throwable} related to the message (may be {@code null}).
	 * @param msg the message to log.
	 * @param parameters the message parameters to log.
	 */
	public void warning(Throwable thrown, String msg, Object... parameters) {
		log(LogLevel.LEVEL_WARNING, thrown, msg, parameters);
	}

	/**
	 * Checks whether a {@linkplain LogLevel#LEVEL_INFO} message of level would be logged by this {@linkplain Log}
	 * instance.
	 *
	 * @return {@code true} if {@linkplain LogLevel#LEVEL_INFO} is enabled.
	 */
	public boolean isInfoLoggable() {
		return isLoggable(LogLevel.LEVEL_INFO);
	}

	/**
	 * Logs a {@linkplain LogLevel#LEVEL_INFO} message.
	 *
	 * @param msg the message to log.
	 * @param parameters the message parameters to log.
	 */
	public void info(String msg, Object... parameters) {
		log(LogLevel.LEVEL_INFO, null, msg, parameters);
	}

	/**
	 * Logs a {@linkplain LogLevel#LEVEL_INFO} message.
	 *
	 * @param thrown the {@linkplain Throwable} related to the message (may be {@code null}).
	 * @param msg the message to log.
	 * @param parameters the message parameters to log.
	 */
	public void info(Throwable thrown, String msg, Object... parameters) {
		log(LogLevel.LEVEL_INFO, thrown, msg, parameters);
	}

	/**
	 * Checks whether a {@linkplain LogLevel#LEVEL_DEBUG} message of level would be logged by this {@linkplain Log}
	 * instance.
	 *
	 * @return {@code true} if {@linkplain LogLevel#LEVEL_DEBUG} is enabled.
	 */
	public boolean isDebugLoggable() {
		return isLoggable(LogLevel.LEVEL_DEBUG);
	}

	/**
	 * Logs a {@linkplain LogLevel#LEVEL_DEBUG} message.
	 *
	 * @param msg the message to log.
	 * @param parameters the message parameters to log.
	 */
	public void debug(String msg, Object... parameters) {
		log(LogLevel.LEVEL_DEBUG, null, msg, parameters);
	}

	/**
	 * Logs a {@linkplain LogLevel#LEVEL_DEBUG} message.
	 *
	 * @param thrown the {@linkplain Throwable} related to the message (may be {@code null}).
	 * @param msg the message to log.
	 * @param parameters the message parameters to log.
	 */
	public void debug(Throwable thrown, String msg, Object... parameters) {
		log(LogLevel.LEVEL_DEBUG, thrown, msg, parameters);
	}

	/**
	 * Checks whether a {@linkplain LogLevel#LEVEL_TRACE} message of level would be logged by this {@linkplain Log}
	 * instance.
	 *
	 * @return {@code true} if {@linkplain LogLevel#LEVEL_TRACE} is enabled.
	 */
	public boolean isTraceLoggable() {
		return isLoggable(LogLevel.LEVEL_TRACE);
	}

	/**
	 * Logs a {@linkplain LogLevel#LEVEL_TRACE} message.
	 *
	 * @param msg the message to log.
	 * @param parameters the message parameters to log.
	 */
	public void trace(String msg, Object... parameters) {
		log(LogLevel.LEVEL_TRACE, null, msg, parameters);
	}

	/**
	 * Logs a {@linkplain LogLevel#LEVEL_TRACE} message.
	 *
	 * @param thrown the {@linkplain Throwable} related to the message (may be {@code null}).
	 * @param msg the message to log.
	 * @param parameters the message parameters to log.
	 */
	public void trace(Throwable thrown, String msg, Object... parameters) {
		log(LogLevel.LEVEL_TRACE, thrown, msg, parameters);
	}

	private static String getCallerClassName() {
		int steIndex = 0;
		StackTraceElement[] stes = Thread.currentThread().getStackTrace();
		String myClassName = Log.class.getName();

		while (steIndex < stes.length && !myClassName.equals(stes[steIndex].getClassName())) {
			steIndex++;
		}
		while (steIndex < stes.length && myClassName.equals(stes[steIndex].getClassName())) {
			steIndex++;
		}
		return (steIndex < stes.length ? stes[steIndex].getClassName() : myClassName);
	}

	@Override
	public String toString() {
		return Objects.toString(this.logger.getName());
	}

}
