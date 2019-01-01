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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import de.carne.boot.logging.LogLevel;
import de.carne.boot.logging.ProxyHandler;

/**
 * SLF4J (<a href= "https://www.slf4j.org">https://www.slf4j.org</a>) proxy.
 */
public class Slf4jProxy implements Proxy {

	private final Logger logger = LoggerFactory.getLogger(ProxyHandler.class);

	@Override
	public void publish(LogRecord logRecord, Formatter formatter) {
		int levelValue = logRecord.getLevel().intValue();

		if (levelValue <= LogLevel.LEVEL_TRACE.intValue()) {
			if (this.logger.isTraceEnabled()) {
				this.logger.trace(MarkerFactory.getMarker(logRecord.getLoggerName()), formatter.format(logRecord),
						logRecord.getThrown());
			}
		} else if (levelValue <= LogLevel.LEVEL_DEBUG.intValue()) {
			if (this.logger.isDebugEnabled()) {
				this.logger.debug(MarkerFactory.getMarker(logRecord.getLoggerName()), formatter.format(logRecord),
						logRecord.getThrown());
			}
		} else if (levelValue <= LogLevel.LEVEL_INFO.intValue()) {
			if (this.logger.isInfoEnabled()) {
				this.logger.info(MarkerFactory.getMarker(logRecord.getLoggerName()), formatter.format(logRecord),
						logRecord.getThrown());
			}
		} else if (levelValue <= LogLevel.LEVEL_WARNING.intValue()) {
			if (this.logger.isWarnEnabled()) {
				this.logger.warn(MarkerFactory.getMarker(logRecord.getLoggerName()), formatter.format(logRecord),
						logRecord.getThrown());
			}
		} else if (this.logger.isErrorEnabled()) {
			this.logger.error(MarkerFactory.getMarker(logRecord.getLoggerName()), formatter.format(logRecord),
					logRecord.getThrown());
		}
	}

}
