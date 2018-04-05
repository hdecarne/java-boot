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
package de.carne.boot.check;

/**
 * Utility class providing code check related functions.
 */
public final class Check {

	private Check() {
		// Prevent instantiation
	}

	/**
	 * Check and ensure that an {@linkplain Object} is not {@code null}.
	 *
	 * @param <T> The actual object type.
	 * @param object The {@linkplain Object} to check.
	 * @return The checked {@linkplain Object} (never {@code null}).
	 * @throws NullPointerException if the submitted argument is {@code null}.
	 */
	public static <T> T notNull(@Nullable T object) {
		if (object == null) {
			throw new NullPointerException();
		}
		return object;
	}

	/**
	 * Check and ensure that an {@linkplain Object} is not {@code null}.
	 *
	 * @param <T> The actual object type.
	 * @param object The {@linkplain Object} to check.
	 * @param message The message to issue if the check fails.
	 * @return The checked {@linkplain Object} (never {@code null}).
	 * @throws NullPointerException if the submitted argument is {@code null}.
	 */
	public static <T> T notNull(@Nullable T object, String message) {
		if (object == null) {
			throw new NullPointerException(message);
		}
		return object;
	}

	/**
	 * Check and ensure that an {@linkplain Object} is an instance of a specific type.
	 *
	 * @param <T> The type to ensure.
	 * @param object The {@linkplain Object} to check.
	 * @param type The type to ensure.
	 * @return The checked {@linkplain Object} (casted to the checked type}).
	 * @throws IllegalArgumentException if the submitted argument is not an instance of the given type.
	 */
	public static <T> T isInstanceOf(Object object, Class<T> type) {
		if (!type.isAssignableFrom(object.getClass())) {
			throw new IllegalArgumentException();
		}
		return type.cast(object);
	}

	/**
	 * Check and ensure that an {@linkplain Object} is an instance of a specific type.
	 * <p>
	 * In case the check fails the {@linkplain String#format(String, Object...)} function is used to format the
	 * exception message.
	 *
	 * @param <T> The type to ensure.
	 * @param object The {@linkplain Object} to check.
	 * @param type The type to ensure.
	 * @param format The format of the message to issue if the check fails.
	 * @param args The arguments to use for message formatting.
	 * @return The checked {@linkplain Object} (casted to the checked type}).
	 * @throws IllegalArgumentException if the submitted argument is not an instance of the given type.
	 */
	public static <T> T isInstanceOf(Object object, Class<T> type, String format, Object... args) {
		if (!type.isAssignableFrom(object.getClass())) {
			throw new IllegalArgumentException(String.format(format, args));
		}
		return type.cast(object);
	}

	/**
	 * Check and ensure that a specific condition is met.
	 *
	 * @param condition The condition to check.
	 * @throws IllegalStateException if the condition is not met.
	 */
	public static void assertTrue(boolean condition) {
		if (!condition) {
			throw new IllegalStateException();
		}
	}

	/**
	 * Check and ensure that a specific condition is met.
	 *
	 * @param condition The condition to check.
	 * @param message The message to issue if the check fails.
	 * @throws IllegalStateException if the condition is not met.
	 */
	public static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}

	/**
	 * Throw an {@linkplain IllegalStateException} to indicate that an unexpected execution state occurred.
	 *
	 * @return Nothing (function never returns).
	 * @throws IllegalStateException any time this function is called.
	 */
	public static IllegalStateException fail() {
		throw new IllegalStateException();
	}

	/**
	 * Throw an {@linkplain IllegalStateException} to indicate that an unexpected execution state occurred.
	 *
	 * @param message The message to issue if the check fails.
	 * @return Nothing (function never returns).
	 * @throws IllegalStateException any time this function is called.
	 */
	public static IllegalStateException fail(String message) {
		throw new IllegalStateException(message);
	}

}
