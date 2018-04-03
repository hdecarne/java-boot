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
package de.carne.boot.test.logging;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.boot.logging.Log;
import de.carne.boot.logging.LogBuffer;
import de.carne.boot.logging.Logs;

/**
 * Test {@linkplain LogBuffer} class.
 */
class LogBufferTest {

	@Test
	void testLogBuffer() throws IOException {
		Logs.readConfig(Logs.CONFIG_DEFAULT);

		Log log = new Log();

		Assertions.assertNotNull(LogBuffer.get(log));

		// Log and flush (following checks should see no existing records)
		LoggingTests.logTestMessages(log, 6);

		LogBuffer.flush(log);

		// Check if initial record count is 0
		LogRecordCounter counter1 = new LogRecordCounter();

		Assertions.assertNull(LogBuffer.getHandler(log, LogRecordCounter.class));

		LogBuffer.addHandler(log, counter1, true);

		Assertions.assertEquals(counter1, LogBuffer.getHandler(log, LogRecordCounter.class));

		Assertions.assertEquals(0, counter1.getPublishCount());
		Assertions.assertEquals(0, counter1.getFlushCount());
		Assertions.assertEquals(0, counter1.getCloseCount());

		// Check if test records are coming through
		LoggingTests.logTestMessages(log, 6);

		Assertions.assertEquals(6, counter1.getPublishCount());

		LogRecordCounter counter2 = new LogRecordCounter();

		LogBuffer.addHandler(log, counter2, true);

		Assertions.assertEquals(5, counter2.getPublishCount());

		// Check buffer export
		File tempFile = File.createTempFile(getClass().getName(), ".log");

		tempFile.deleteOnExit();
		LogBuffer.exportTo(log, tempFile, false);

		// Check if removing the handler stops record receiving
		LogBuffer.removeHandler(log, counter2);
		LogBuffer.flush(log);

		Assertions.assertEquals(1, counter1.getFlushCount());
		Assertions.assertEquals(0, counter2.getFlushCount());

		LogBuffer.flush(log);

		Assertions.assertEquals(2, counter1.getFlushCount());

		// Check if close is working
		LogBuffer logBuffer = LogBuffer.get(log);

		Assertions.assertNotNull(logBuffer);

		if (logBuffer != null) {
			logBuffer.close();

			Assertions.assertEquals(1, counter1.getCloseCount());

			logBuffer.close();

			Assertions.assertEquals(1, counter1.getCloseCount());
		}
	}

}
