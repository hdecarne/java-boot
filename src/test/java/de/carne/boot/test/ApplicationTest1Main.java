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
package de.carne.boot.test;

import org.junit.jupiter.api.Assertions;

import de.carne.boot.Application;
import de.carne.boot.ApplicationMain;

/**
 * @see ApplicationTest1a
 */
public class ApplicationTest1Main implements ApplicationMain {

	@Override
	public String name() {
		return getClass().getName();
	}

	@Override
	public int run(String[] args) {
		Assertions.assertEquals(2, args.length);
		Assertions.assertEquals("--arg", args[0]);
		Assertions.assertEquals("test1", args[1]);
		Assertions.assertEquals(this, Application.getMain(ApplicationTest1Main.class));
		return 0;
	}

}
