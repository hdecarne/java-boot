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
package de.carne.boot.test.check;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.boot.check.Check;

/**
 * Test {@linkplain Check} class.
 */
class CheckTest {

	@Test
	void testCheckNotNullPassed() {
		Assertions.assertNotNull(Check.notNull(this));
		Assertions.assertNotNull(Check.notNull(this, getClass().getSimpleName()));
	}

	@Test
	void testCheckNotNullFailed() {
		Assertions.assertThrows(NullPointerException.class, () -> {
			Check.notNull(null);
		});
		Assertions.assertThrows(NullPointerException.class, () -> {
			Check.notNull(null, getClass().getSimpleName());
		});
	}

	@Test
	void testCheckIsInstancePassed() {
		Assertions.assertEquals(this, Check.isInstanceOf(this, CheckTest.class));
		Assertions.assertEquals(this, Check.isInstanceOf(this, CheckTest.class, getClass().getSimpleName()));
	}

	@Test
	void testCheckIsInstanceFailed() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Check.isInstanceOf(this, String.class);
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Check.isInstanceOf(this, String.class, getClass().getSimpleName());
		});
	}

	@Test
	void testCheckAssertTruePassed() {
		Check.assertTrue(true);
		Check.assertTrue(true, getClass().getSimpleName());
	}

	@Test
	void testCheckAssertTrueFailed() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			Check.assertTrue(false);
		});
		Assertions.assertThrows(IllegalStateException.class, () -> {
			Check.assertTrue(false, getClass().getSimpleName());
		});
	}

	@Test
	void testCheckFail() {
		Assertions.assertThrows(IllegalStateException.class, () -> Check.fail());
		Assertions.assertThrows(IllegalStateException.class, () -> {
			Check.fail(getClass().getSimpleName());
		});
	}

}
