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
package de.carne.boot.logging.proxy;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Proxy interface for log record forwarding.
 */
@FunctionalInterface
public interface Proxy {

	/**
	 * Publishes the submitted {@linkplain LogRecord}.
	 *
	 * @param logRecord the {@linkplain LogRecord} to publish.
	 * @param formatter the {@linkplain Formatter} to use for message formatting.
	 */
	void publish(LogRecord logRecord, Formatter formatter);

}
