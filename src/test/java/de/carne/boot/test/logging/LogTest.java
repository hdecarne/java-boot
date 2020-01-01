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
package de.carne.boot.test.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.boot.logging.Log;

/**
 * Test {@linkplain Log} class.
 */
class LogTest {

	@Test
	void testLogNames() {
		Log defaultLog = new Log();

		Assertions.assertEquals(getClass().getName(), defaultLog.logger().getName());
		Assertions.assertEquals(getClass().getName(), defaultLog.toString());

		Log customLog = new Log(Object.class);

		Assertions.assertEquals(Object.class.getName(), customLog.logger().getName());
		Assertions.assertEquals(Object.class.getName(), customLog.toString());

		Log rootLog = Log.root();

		Assertions.assertEquals("", rootLog.logger().getName());
		Assertions.assertEquals("", rootLog.toString());
	}

}
