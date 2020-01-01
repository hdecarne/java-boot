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

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.boot.logging.LocalizedFilter;
import de.carne.boot.logging.Log;
import de.carne.boot.logging.LogBuffer;
import de.carne.boot.logging.Logs;

/**
 * Test {@linkplain LocalizedFilter} class.
 */
class LocalizedFilterTest {

	@Test
	void testLocalizedFilter() throws IOException {
		Logs.readConfig("logging-localized.properties");

		Log standardLog = new Log(LogTest.class);
		Log localizedLog1 = new Log(getClass().getName());
		Log localizedLog2 = new Log(LogsTest.class, getClass().getName());
		LogRecordCounter counter = new LogRecordCounter();

		LogBuffer.addHandler(standardLog, counter, true);
		LoggingTests.logTestMessagesAndAssert(localizedLog1, 6);

		Assertions.assertEquals(6, counter.getPublishCount());

		LoggingTests.logTestMessagesAndAssert(localizedLog2, 6);

		Assertions.assertEquals(12, counter.getPublishCount());

		LoggingTests.logTestMessagesAndAssert(standardLog, 6);

		Assertions.assertEquals(12, counter.getPublishCount());
	}

}
