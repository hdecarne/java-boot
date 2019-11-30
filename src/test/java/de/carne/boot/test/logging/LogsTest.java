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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.carne.boot.logging.Config;
import de.carne.boot.logging.LocalizedFilter;
import de.carne.boot.logging.Log;
import de.carne.boot.logging.LogBuffer;
import de.carne.boot.logging.LogLevel;
import de.carne.boot.logging.Logs;

/**
 * Test {@linkplain Logs} class.
 */
class LogsTest {

	@BeforeAll
	static void setUpSystemProperties() {
		System.setProperty("java.util.logging.config.class", Config.class.getName());
	}

	@Test
	void testLogConfigSuccess() throws IOException {
		Log log = new Log();

		Assertions.assertTrue(log.isWarningLoggable());
		Assertions.assertFalse(log.isInfoLoggable());
		LoggingTests.logTestMessagesAndAssert(log, 6);
		Logs.readConfig("logging-notice.properties");
		Assertions.assertTrue(log.isNoticeLoggable());
		Assertions.assertFalse(log.isErrorLoggable());
		LoggingTests.logTestMessagesAndAssert(log, 2);
		Logs.readConfig("logging-error.properties");
		Assertions.assertTrue(log.isErrorLoggable());
		Assertions.assertFalse(log.isWarningLoggable());
		LoggingTests.logTestMessagesAndAssert(log, 4);
		Logs.readConfig("logging-warning.properties");
		Assertions.assertTrue(log.isWarningLoggable());
		Assertions.assertFalse(log.isInfoLoggable());
		LoggingTests.logTestMessagesAndAssert(log, 6);
		Logs.readConfig("logging-info.properties");
		Assertions.assertTrue(log.isInfoLoggable());
		Assertions.assertFalse(log.isDebugLoggable());
		LoggingTests.logTestMessagesAndAssert(log, 8);
		Logs.readConfig("logging-debug.properties");
		Assertions.assertTrue(log.isDebugLoggable());
		Assertions.assertFalse(log.isTraceLoggable());
		LoggingTests.logTestMessagesAndAssert(log, 10);
		Logs.readConfig("logging-trace.properties");
		Assertions.assertTrue(log.isTraceLoggable());
		LoggingTests.logTestMessagesAndAssert(log, 12);
		Logs.readConfig(Objects.requireNonNull(getClass().getResource("/logging-warning.properties")));
		Assertions.assertTrue(log.isWarningLoggable());
		Assertions.assertFalse(log.isInfoLoggable());
		LoggingTests.logTestMessagesAndAssert(log, 6);
	}

	@Test
	void testLogConfigFailure() {
		Assertions.assertThrows(FileNotFoundException.class, () -> {
			Logs.readConfig("logging-unknown.properties");
		});
	}

	@Test
	void testApplicationConfig() throws IOException {
		Logs.readConfig("logging-application.properties");

		Assertions.assertNotNull(LogBuffer.get(Logger.getLogger("")));
	}

	@Test
	void testLogManagerProperties() throws IOException {
		Logs.readConfig(Logs.CONFIG_DEFAULT);

		LogManager manager = LogManager.getLogManager();
		String propertyBase = getClass().getName();

		Assertions.assertEquals("Invalid", Logs.getStringProperty(manager, propertyBase + ".invalid", ""));
		Assertions.assertEquals("Unknown", Logs.getStringProperty(manager, propertyBase + ".unnown", "Unknown"));

		Assertions.assertEquals(1, Logs.getIntProperty(manager, propertyBase + ".intOne", -1));
		Assertions.assertEquals(2, Logs.getIntProperty(manager, propertyBase + ".intTwo", -1));
		Assertions.assertEquals(-1, Logs.getIntProperty(manager, propertyBase + ".invalid", -1));

		Assertions.assertTrue(Logs.getBooleanProperty(manager, propertyBase + ".booleanTrue", false));
		Assertions.assertFalse(Logs.getBooleanProperty(manager, propertyBase + ".booleanFalse", true));
		Assertions.assertTrue(Logs.getBooleanProperty(manager, propertyBase + ".booleanUnknown", true));
		Assertions.assertFalse(Logs.getBooleanProperty(manager, propertyBase + ".invalid", true));

		Assertions.assertEquals(LogLevel.LEVEL_DEBUG,
				Logs.getLevelProperty(manager, propertyBase + ".levelDebug", LogLevel.LEVEL_ERROR));
		Assertions.assertEquals(LogLevel.LEVEL_WARNING,
				Logs.getLevelProperty(manager, propertyBase + ".levelWarning", LogLevel.LEVEL_ERROR));
		Assertions.assertEquals(LogLevel.LEVEL_ERROR,
				Logs.getLevelProperty(manager, propertyBase + ".levelUnknown", LogLevel.LEVEL_ERROR));
		Assertions.assertEquals(LogLevel.LEVEL_DEBUG,
				Logs.getLevelProperty(manager, propertyBase + ".invalid", LogLevel.LEVEL_DEBUG));

		Assertions
				.assertTrue(Logs.getFilterProperty(manager, propertyBase + ".filter", null) instanceof LocalizedFilter);
		Assertions.assertNull(Logs.getFilterProperty(manager, propertyBase + ".invalid", null));

		Assertions.assertTrue(Logs.getFormatterProperty(manager, propertyBase + ".formatter",
				new SimpleFormatter()) instanceof XMLFormatter);
		Assertions.assertTrue(Logs.getFormatterProperty(manager, propertyBase + ".invalid",
				new SimpleFormatter()) instanceof SimpleFormatter);
	}

	@Test
	void testFlush() {
		Assertions.assertDoesNotThrow(() -> Logs.flush());
	}

}
