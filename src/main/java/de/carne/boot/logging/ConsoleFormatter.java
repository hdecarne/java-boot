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
package de.carne.boot.logging;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.boot.platform.Platform;

/**
 * A {@linkplain Formatter} providing a simple out of the box log line format.
 */
public class ConsoleFormatter extends Formatter {

	private static final String ANSI_STYLE_PREFIX = "\033[";
	private static final String ANSI_RESET = "\033[0m";

	private final DateTimeFormatter tsPattern;
	private final boolean enableAnsiOutput;
	private final String levelStyleTrace;
	private final String levelStyleDebug;
	private final String levelStyleInfo;
	private final String levelStyleWarning;
	private final String levelStyleError;
	private final String levelStyleNotice;
	private final String exceptionStyle;

	/**
	 * Constructs new {@linkplain ConsoleFormatter} instance.
	 */
	public ConsoleFormatter() {
		LogManager manager = LogManager.getLogManager();
		String propertyBase = getClass().getName();

		this.tsPattern = DateTimeFormatter
				.ofPattern(Logs.getStringProperty(manager, propertyBase + ".tsPattern", "yyyy-MM-dd HH:mm:ss,SSS"));
		this.enableAnsiOutput = Logs.getBooleanProperty(manager, propertyBase + ".enableAnsiOutput",
				enableAnsiOutputDefault());
		this.levelStyleTrace = ANSI_STYLE_PREFIX
				+ Logs.getStringProperty(manager, propertyBase + ".levelStyleTrace", "37m");
		this.levelStyleDebug = ANSI_STYLE_PREFIX
				+ Logs.getStringProperty(manager, propertyBase + ".levelStyleDebug", "37m");
		this.levelStyleInfo = ANSI_STYLE_PREFIX
				+ Logs.getStringProperty(manager, propertyBase + ".levelStyleInfo", "36m");
		this.levelStyleWarning = ANSI_STYLE_PREFIX
				+ Logs.getStringProperty(manager, propertyBase + ".levelStyleWarning", "33m");
		this.levelStyleError = ANSI_STYLE_PREFIX
				+ Logs.getStringProperty(manager, propertyBase + ".levelStyleError", "91m");
		this.levelStyleNotice = ANSI_STYLE_PREFIX
				+ Logs.getStringProperty(manager, propertyBase + ".levelStyleNotice", "32m");
		this.exceptionStyle = ANSI_STYLE_PREFIX
				+ Logs.getStringProperty(manager, propertyBase + ".exceptionStyle", "37m");
	}

	private static boolean enableAnsiOutputDefault() {
		return (Platform.IS_LINUX || Platform.IS_MACOS) && System.console() != null;
	}

	@Override
	public String format(@Nullable LogRecord record) {
		StringBuilder buffer = new StringBuilder();

		if (record != null) {
			formatMillis(buffer, record.getMillis());
			buffer.append(' ');
			formatLevel(buffer, record.getLevel());
			buffer.append(' ');
			buffer.append(record.getLoggerName());
			buffer.append(": ");
			buffer.append(formatMessage(record));
			buffer.append(System.lineSeparator());
			formatThrown(buffer, record.getThrown());
		}
		return buffer.toString();
	}

	private StringBuilder formatMillis(StringBuilder buffer, long millis) {
		return buffer.append(
				this.tsPattern.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())));
	}

	private StringBuilder formatLevel(StringBuilder buffer, @Nullable Level level) {
		int levelValue = (level != null ? level.intValue() : Integer.MAX_VALUE);
		String levelStyle;
		String levelString;

		if (levelValue <= LogLevel.LEVEL_TRACE.intValue()) {
			levelStyle = this.levelStyleTrace;
			levelString = "TRACE  ";
		} else if (levelValue <= LogLevel.LEVEL_DEBUG.intValue()) {
			levelStyle = this.levelStyleDebug;
			levelString = "DEBUG  ";
		} else if (levelValue <= LogLevel.LEVEL_INFO.intValue()) {
			levelStyle = this.levelStyleInfo;
			levelString = "INFO   ";
		} else if (levelValue <= LogLevel.LEVEL_WARNING.intValue()) {
			levelStyle = this.levelStyleWarning;
			levelString = "WARNING";
		} else if (levelValue <= LogLevel.LEVEL_ERROR.intValue()) {
			levelStyle = this.levelStyleError;
			levelString = "ERROR  ";
		} else if (levelValue <= LogLevel.LEVEL_NOTICE.intValue()) {
			levelStyle = this.levelStyleNotice;
			levelString = "NOTICE ";
		} else {
			levelStyle = this.levelStyleError;
			levelString = "?????? ";
		}
		if (this.enableAnsiOutput) {
			buffer.append(levelStyle);
		}
		buffer.append(levelString);
		if (this.enableAnsiOutput) {
			buffer.append(ANSI_RESET);
		}
		return buffer;
	}

	private StringBuilder formatThrown(StringBuilder buffer, @Nullable Throwable thrown) {
		if (thrown != null) {
			if (this.enableAnsiOutput) {
				buffer.append(this.exceptionStyle);
			}
			try (PrintWriter bufferWriter = new PrintWriter(new Writer() {

				@Override
				public void write(char @Nullable [] cbuf, int off, int len) throws IOException {
					buffer.append(cbuf, off, len);
				}

				@Override
				public void flush() throws IOException {
					// Nothing to do
				}

				@Override
				public void close() throws IOException {
					// Nothing to do
				}

			})) {
				thrown.printStackTrace(bufferWriter);
			}
			if (this.enableAnsiOutput) {
				buffer.append(ANSI_RESET);
			}
		}
		return buffer;
	}

}
