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

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.boot.logging.Log;
import de.carne.boot.logging.Logs;

/**
 * Test {@linkplain de.carne.boot.logging.ProxyHandler} class.
 */
class ProxyHandlerTest {

	@Test
	void testLog4j2Proxy() throws IOException {
		Logs.readConfig("logging-log4j2proxy.properties");

		Log log = new Log();

		LogEventCounter.resetCounter();
		LoggingTests.logTestMessages(log);
		Assertions.assertEquals(10, LogEventCounter.getCounter());
	}

	@Test
	void testSlf4jProxy() throws IOException {
		Logs.readConfig("logging-slf4jproxy.properties");

		Log log = new Log();

		LogEventCounter.resetCounter();
		LoggingTests.logTestMessages(log);
		Assertions.assertEquals(10, LogEventCounter.getCounter());
	}

	@Test
	void testAutoProxy() throws IOException {
		Logs.readConfig("logging-autoproxy.properties");

		Log log = new Log();

		LogEventCounter.resetCounter();
		LoggingTests.logTestMessages(log);
		Assertions.assertEquals(10, LogEventCounter.getCounter());
	}

}
