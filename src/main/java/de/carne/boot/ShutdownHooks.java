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
package de.carne.boot;

import java.util.LinkedList;
import java.util.List;

/**
 * Utility class providing the same function as {@linkplain Runtime#addShutdownHook(Thread)} while sharing a single
 * {@linkplain Thread} for all hooks.
 */
public final class ShutdownHooks {

	private ShutdownHooks() {
		// Prevent instantiation
	}

	private static final List<Runnable> HOOKS = new LinkedList<>();

	static {
		Thread shutdownHook = new Thread(ShutdownHooks::trigger, ShutdownHooks.class.getSimpleName());

		shutdownHook.setDaemon(true);
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	/**
	 * Adds a shutdown hook to be invoked during VM shutdown.
	 *
	 * @param hook the shutdown hook to invoke.
	 * @see Runtime#addShutdownHook(Thread)
	 */
	public static void add(Runnable hook) {
		synchronized (HOOKS) {
			HOOKS.add(hook);
		}
	}

	/**
	 * Triggers execution of the registered shutdown hooks.
	 */
	@SuppressWarnings("squid:S1148")
	public static void trigger() {
		synchronized (HOOKS) {
			for (Runnable hook : HOOKS) {
				try {
					hook.run();
				} catch (Exception e) {
					// Do not use any logging functionality or the like at this VM state
					e.printStackTrace();
				}
			}
		}
	}

}
