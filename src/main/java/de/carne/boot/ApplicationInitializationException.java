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

/**
 * This exception indicates an initialization error during application startup.
 */
public class ApplicationInitializationException extends RuntimeException {

	// Serialization support
	private static final long serialVersionUID = 320622053571968997L;

	/**
	 * Construct {@linkplain ApplicationInitializationException}.
	 *
	 * @param message The exception message.
	 */
	public ApplicationInitializationException(String message) {
		super(message);
	}

	/**
	 * Construct {@linkplain ApplicationInitializationException}.
	 *
	 * @param message The exception message.
	 * @param cause The causing exception.
	 */
	public ApplicationInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

}
