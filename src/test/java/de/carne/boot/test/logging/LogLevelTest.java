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

import java.util.logging.Level;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.boot.logging.LogLevel;

/**
 * Test {@linkplain LogLevel} class.
 */
class LogLevelTest {

	@Test
	void testFromLevel() {
		Assertions.assertEquals(LogLevel.LEVEL_NOTICE, LogLevel.fromLevel(Level.OFF));
		Assertions.assertEquals(LogLevel.LEVEL_ERROR, LogLevel.fromLevel(Level.SEVERE));
		Assertions.assertEquals(LogLevel.LEVEL_WARNING, LogLevel.fromLevel(Level.WARNING));
		Assertions.assertEquals(LogLevel.LEVEL_INFO, LogLevel.fromLevel(Level.INFO));
		Assertions.assertEquals(LogLevel.LEVEL_INFO, LogLevel.fromLevel(Level.CONFIG));
		Assertions.assertEquals(LogLevel.LEVEL_DEBUG, LogLevel.fromLevel(Level.FINE));
		Assertions.assertEquals(LogLevel.LEVEL_DEBUG, LogLevel.fromLevel(Level.FINER));
		Assertions.assertEquals(LogLevel.LEVEL_TRACE, LogLevel.fromLevel(Level.ALL));
	}

}
