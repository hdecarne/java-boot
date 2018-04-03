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

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import de.carne.boot.Nullable;

/**
 * Test helper class used for log message counting.
 */
class LogRecordCounter extends Handler {

	private int publishCount = 0;

	private int flushCount = 0;

	private int closeCount = 0;

	LogRecordCounter() {
		// Just to make this class accessible to the outer class
	}

	public int getPublishCount() {
		return this.publishCount;
	}

	public int getFlushCount() {
		return this.flushCount;
	}

	public int getCloseCount() {
		return this.closeCount;
	}

	@Override
	public void publish(@Nullable LogRecord record) {
		this.publishCount++;
	}

	@Override
	public void flush() {
		this.flushCount++;
	}

	@Override
	public void close() {
		this.closeCount++;
	}

}
