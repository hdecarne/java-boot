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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;

import de.carne.boot.logging.LogLevel;
import de.carne.boot.logging.ProxyHandler;

/**
 * Log4j 2 (<a href= "https://logging.apache.org/log4j/2.x/">https://logging.apache.org/log4j/2.x/</a>) proxy.
 */
public class Log4j2Proxy implements Proxy {

	@SuppressWarnings("squid:S3416")
	private final Logger logger = LogManager.getLogger(ProxyHandler.class);

	@Override
	public void publish(LogRecord logRecord, Formatter formatter) {
		int levelValue = logRecord.getLevel().intValue();

		if (levelValue <= LogLevel.LEVEL_TRACE.intValue()) {
			if (this.logger.isTraceEnabled()) {
				this.logger.trace(MarkerManager.getMarker(logRecord.getLoggerName()), formatter.format(logRecord),
						logRecord.getThrown());
			}
		} else if (levelValue <= LogLevel.LEVEL_DEBUG.intValue()) {
			if (this.logger.isDebugEnabled()) {
				this.logger.debug(MarkerManager.getMarker(logRecord.getLoggerName()), formatter.format(logRecord),
						logRecord.getThrown());
			}
		} else if (levelValue <= LogLevel.LEVEL_INFO.intValue()) {
			if (this.logger.isInfoEnabled()) {
				this.logger.info(MarkerManager.getMarker(logRecord.getLoggerName()), formatter.format(logRecord),
						logRecord.getThrown());
			}
		} else if (levelValue <= LogLevel.LEVEL_WARNING.intValue()) {
			if (this.logger.isWarnEnabled()) {
				this.logger.warn(MarkerManager.getMarker(logRecord.getLoggerName()), formatter.format(logRecord),
						logRecord.getThrown());
			}
		} else if (this.logger.isErrorEnabled()) {
			this.logger.error(MarkerManager.getMarker(logRecord.getLoggerName()), formatter.format(logRecord),
					logRecord.getThrown());
		}
	}

}
