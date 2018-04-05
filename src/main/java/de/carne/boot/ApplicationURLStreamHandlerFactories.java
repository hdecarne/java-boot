/*
 * Copyright (c) 2018 Holger de Carne and contributors, All Rights Reserved.
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

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

import de.carne.boot.check.Nullable;

/**
 * {@linkplain URLStreamHandlerFactory} implementation that supports multiple protocol handlers per VM by multiplexing
 * the protocol specific requests.
 *
 * @see #register(String, URLStreamHandlerFactory)
 */
public final class ApplicationURLStreamHandlerFactories implements URLStreamHandlerFactory {

	private static final ApplicationURLStreamHandlerFactories THIS = new ApplicationURLStreamHandlerFactories();

	static {
		URL.setURLStreamHandlerFactory(THIS);
	}

	private final Map<String, URLStreamHandlerFactory> factoryMap = new HashMap<>();

	private ApplicationURLStreamHandlerFactories() {
		// Prevent instantiation from outside
	}

	/**
	 * Registers an additional protocol.
	 *
	 * @param protocol the protocol to register.
	 * @param factory the {@linkplain URLStreamHandlerFactory} handling the submitted protocol.
	 * @return the previously registered {@linkplain URLStreamHandlerFactory} or {@code null} if the submitted protocol
	 *         is registered for the first time.
	 */
	public static URLStreamHandlerFactory register(String protocol, URLStreamHandlerFactory factory) {
		return THIS.register0(protocol, factory);
	}

	private synchronized URLStreamHandlerFactory register0(String protocol, URLStreamHandlerFactory factory) {
		return this.factoryMap.put(protocol, factory);
	}

	@Override
	@Nullable
	public synchronized URLStreamHandler createURLStreamHandler(@Nullable String protocol) {
		URLStreamHandlerFactory factory = this.factoryMap.get(protocol);

		return (factory != null ? factory.createURLStreamHandler(protocol) : null);
	}

}
