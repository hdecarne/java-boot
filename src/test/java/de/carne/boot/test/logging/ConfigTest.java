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
package de.carne.boot.test.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.boot.logging.Config;
import de.carne.boot.logging.Log;

/**
 * Test {@linkplain Config} class.
 */
class ConfigTest {

	@Test
	void testConfig() {
		Log log = new Log();

		// Check for default settings
		new Config();

		Assertions.assertFalse(log.isTraceLoggable());

		LoggingTests.logTestMessagesAndAssert(log, 6);

		// Check for trace settings
		System.setProperty(Config.class.getName(), "logging-trace.properties");

		new Config();

		Assertions.assertTrue(log.isTraceLoggable());

		LoggingTests.logTestMessagesAndAssert(log, 12);

		// Check for (unchanged) trace settings
		System.setProperty(Config.class.getName(), "logging-unknown.properties");

		new Config();

		Assertions.assertTrue(log.isTraceLoggable());

		LoggingTests.logTestMessagesAndAssert(log, 12);
	}

}
