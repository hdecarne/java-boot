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
package de.carne.boot.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.carne.boot.Application;
import de.carne.boot.ApplicationInitializationException;

/**
 * Test {@linkplain Application} class.
 */
class ApplicationTest3 {

	@BeforeAll
	static void setUpBeforeClass() {
		System.setProperty("de.carne.boot.Application", "test3");
		System.setProperty("de.carne.boot.Application.DEBUG", "true");
	}

	private static final String[] TEST_ARGS = new String[] {};

	@Test
	void test() {
		ApplicationInitializationException exception = Assertions.assertThrows(ApplicationInitializationException.class,
				() -> {
					Application.main(TEST_ARGS);
				});

		Assertions.assertEquals("Failed to read application configuration: /META-INF/de.carne.boot.Application.test3",
				exception.getMessage());
	}

}
