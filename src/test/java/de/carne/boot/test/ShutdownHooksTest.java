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
package de.carne.boot.test;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.boot.ShutdownHooks;

/**
 * Test {@linkplain ShutdownHooks} class.
 */
class ShutdownHooksTest {

	private AtomicBoolean hookInvoked = new AtomicBoolean(false);

	@Test
	void testToRuntimeFromChecked() {
		ShutdownHooks.add(() -> {
			this.hookInvoked.set(true);
		});

		Assertions.assertFalse(this.hookInvoked.get());

		ShutdownHooks.trigger();

		Assertions.assertTrue(this.hookInvoked.get());
	}

}
