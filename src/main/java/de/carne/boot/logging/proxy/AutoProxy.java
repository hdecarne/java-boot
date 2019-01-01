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
package de.carne.boot.logging.proxy;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * auto detecting proxy.
 */
public class AutoProxy implements Proxy {

	private final Proxy proxy;

	/**
	 * Constructs {@linkplain AutoProxy}.
	 */
	public AutoProxy() {
		this.proxy = autoDetectProxy();
	}

	@Override
	public void publish(LogRecord logRecord, Formatter formatter) {
		this.proxy.publish(logRecord, formatter);
	}

	private static Proxy autoDetectProxy() {
		ClassLoader cl = AutoProxy.class.getClassLoader();
		Proxy proxy;

		if (cl.getResource("org/apache/logging/log4j/LogManager.class") != null) {
			proxy = new Log4j2Proxy();
		} else if (cl.getResource("org/slf4j/LoggerFactory.class") != null) {
			proxy = new Slf4jProxy();
		} else {
			proxy = (logRecord, formatter) -> {
				// No-op proxy
			};
		}
		return proxy;
	}

}
