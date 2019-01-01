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

import de.carne.boot.logging.Log;
import de.carne.boot.logging.LogLevel;
import de.carne.boot.logging.LogRecorder;

/**
 * Common logging test functions.
 */
class LoggingTests {

	private LoggingTests() {
		// Prevent instantiation
	}

	static void logTestMessages(Log log) {
		Exception thrown = new IllegalStateException();

		log.trace("Trace message");
		log.trace(thrown, "Trace message (with exception)");
		log.debug("Debug message");
		log.debug(thrown, "Debug message (with exception)");
		log.info("Info message");
		log.info(thrown, "Info message (with exception)");
		log.warning("Warning message");
		log.warning(thrown, "Warning message (with exception)");
		log.error("Error message");
		log.error(thrown, "Error message (with exception)");
		log.notice("Notice message");
		log.notice(thrown, "Notice message (with exception)");
	}

	static void logTestMessagesAndAssert(Log log, int expectedRecordCount) {
		LogRecorder recorder = new LogRecorder(LogLevel.LEVEL_TRACE);

		recorder.includeRecord(record -> true);
		recorder.addLog(log);

		try (LogRecorder.Session session = recorder.start(true)) {
			session.includeThread(thread -> true);
			logTestMessages(log);
			Assertions.assertEquals(expectedRecordCount, session.getRecords().size());
		}
	}

}
