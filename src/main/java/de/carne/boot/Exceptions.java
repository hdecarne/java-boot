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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.boot.logging.Log;
import de.carne.util.Strings;

/**
 * Utility class providing {@linkplain Exception}/{@linkplain Throwable} handling related functions.
 */
public final class Exceptions {

	private static final Log LOG = new Log();

	private Exceptions() {
		// Prevent instantiation
	}

	/**
	 * Makes a {@linkplain Throwable} unchecked by wrapping it into a {@linkplain RuntimeException} if needed.
	 *
	 * @param exception the {@linkplain Throwable} to wrap.
	 * @return the created {@linkplain RuntimeException}.
	 */
	public static RuntimeException toRuntime(Throwable exception) {
		return (exception instanceof RuntimeException ? (RuntimeException) exception
				: new RuntimeException(exception.getLocalizedMessage(), exception));
	}

	/**
	 * Ignores a {@linkplain Throwable}.
	 * <p>
	 * This function logs the {@linkplain Throwable} using the trace log level and discards it.
	 *
	 * @param exception the {@linkplain Throwable} to ignore (may be {@code null}).
	 */
	public static void ignore(@Nullable Throwable exception) {
		if (exception != null) {
			LOG.trace(exception, "Ignored exception: {0}", exception.getClass().getTypeName());
		}
	}

	/**
	 * Warns about a {@linkplain Throwable}.
	 * <p>
	 * This function logs the {@linkplain Throwable} using the warning log level and discards it.
	 *
	 * @param exception the {@linkplain Throwable} to warn about (may be {@code null}).
	 */
	public static void warn(@Nullable Throwable exception) {
		if (exception != null) {
			LOG.warning(exception, "Ignored exception: {0}", exception.getClass().getTypeName());
		}
	}

	/**
	 * Gets an {@linkplain Throwable}'s best message.
	 * <p>
	 * This function returns a default message in case {@code null} is submitted. Otherwise it tries
	 * {@linkplain Throwable#getLocalizedMessage()} and {@linkplain Throwable#getMessage()} to get the best possible
	 * message. The former function do not return anything the {@linkplain Throwable}'s type is returned.
	 * </p>
	 *
	 * @param exception the {@linkplain Throwable} to get the message from.
	 * @return the {@linkplain Throwable}'s message.
	 */
	@SuppressWarnings("null")
	public static String getMessage(@Nullable Throwable exception) {
		String message = "<none>";

		if (exception != null) {
			message = exception.getLocalizedMessage();
			if (Strings.isEmpty(message)) {
				message = exception.getMessage();
				if (Strings.isEmpty(message)) {
					message = exception.getClass().getName();
				}
			}
		}
		return message;
	}

	/**
	 * Gets a {@linkplain Throwable}'s stack trace.
	 *
	 * @param exception the {@linkplain Throwable} to get the stack trace for.
	 * @return the {@linkplain Throwable}'s stack trace.
	 */
	public static String getStackTrace(Throwable exception) {
		String stackTrace = null;

		try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
			exception.printStackTrace(pw);
			pw.flush();
			stackTrace = sw.toString();
		} catch (IOException e) {
			warn(e);
		}
		return (stackTrace != null ? stackTrace : "<no stack trace>");
	}

	/**
	 * Determines the best textual representation of a {@linkplain Throwable}.
	 *
	 * @param e The {@linkplain Throwable} to get the textual representation for.
	 * @return The {@linkplain Throwable}'s message or it's type (if no message is available).
	 */
	public static String toString(Throwable e) {
		StringBuilder buffer = new StringBuilder();

		buffer.append(e.getClass().getName());

		String message = e.getLocalizedMessage();

		if (message == null || message.isEmpty()) {
			message = e.getMessage();
		}
		if (message != null && !message.isEmpty()) {
			buffer.append(": ").append(message);
		}
		return buffer.toString();
	}

}
