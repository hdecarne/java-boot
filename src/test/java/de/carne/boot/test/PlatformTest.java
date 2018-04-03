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
package de.carne.boot.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.boot.Platform;

/**
 * Test {@linkplain Platform} class.
 */
class PlatformTest {

	@Test
	void testOSDetection() {
		System.out.println("OS_ARCH: " + Platform.SYSTEM_OS_ARCH);
		System.out.println("OS_NAME: " + Platform.SYSTEM_OS_NAME);
		System.out.println("OS_VERSION: " + Platform.SYSTEM_OS_VERSION);

		boolean[] osFlags = new boolean[] { Platform.IS_LINUX, Platform.IS_MACOS, Platform.IS_WINDOWS };
		int matchIndex = -1;

		for (int osFlagIndex = 0; osFlagIndex < osFlags.length; osFlagIndex++) {
			if (osFlags[osFlagIndex]) {
				Assertions.assertTrue(matchIndex < 0,
						"Duplicate OS match (index1: " + matchIndex + " index2: " + osFlagIndex);
				matchIndex = osFlagIndex;
			}
		}
		Assertions.assertTrue(matchIndex >= 0, "No OS match for: '" + Platform.SYSTEM_OS_NAME + "'");
	}

}
