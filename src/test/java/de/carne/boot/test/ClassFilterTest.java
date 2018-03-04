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
import org.junit.jupiter.api.Test;

import de.carne.boot.ApplicationJarClassLoader;
import de.carne.boot.ApplicationJarClassLoader.ClassFilter;

/**
 * Test {@linkplain ClassFilter}.
 */
class ClassFilterTest {

	@Test
	void testFilter() {
		ClassFilter filter = ApplicationJarClassLoader.filter();

		// Default is to accept all
		Assertions.assertTrue(filter.matches(getClass().getName()));
		Assertions.assertTrue(filter.matches(Object.class.getName()));

		// Include everything explicitly
		filter.include(getClass().getPackage().getName());
		filter.include(Object.class.getPackage().getName());

		Assertions.assertTrue(filter.matches(getClass().getName()));
		Assertions.assertTrue(filter.matches(Object.class.getName()));

		// Exclude something (should overrule include)
		filter.exclude(Object.class.getPackage().getName());

		Assertions.assertTrue(filter.matches(getClass().getName()));
		Assertions.assertFalse(filter.matches(Object.class.getName()));
	}

}
