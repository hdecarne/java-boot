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
package de.carne.boot.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.Nullable;

/**
 * {@linkplain Handler} implementation used to add/remove {@linkplain Handler} instances programmatically during
 * application runtime. This class keeps a buffer of published {@linkplain LogRecord}s to make them available to added
 * {@linkplain Handler} instances (e.g. to display log messages issued during application startup in UI).
 */
public class LogBuffer extends Handler {

	private final int limit;
	private final Queue<LogRecord> buffer;
	private final Set<Handler> handlers = new HashSet<>();
	private final AtomicBoolean locked = new AtomicBoolean();

	/**
	 * Constructs a new {@linkplain LogBuffer} instance.
	 */
	public LogBuffer() {
		LogManager manager = LogManager.getLogManager();
		String propertyBase = getClass().getName();

		this.limit = Logs.getIntProperty(manager, propertyBase + ".limit", 1000);
		this.buffer = new ArrayDeque<>(this.limit);
		setLevel(Logs.getLevelProperty(manager, propertyBase + ".level", LogLevel.LEVEL_WARNING));
		setFilter(Logs.getFilterProperty(manager, propertyBase + ".filter", null));
	}

	/**
	 * Gets the {@linkplain LogBuffer} attached to the submitted {@linkplain Log} (if any).
	 *
	 * @param log the {@linkplain Log} to get the {@linkplain LogBuffer} for.
	 * @return the found {@linkplain LogBuffer} or {@code null} if none has been configured.
	 */
	@Nullable
	public static LogBuffer get(Log log) {
		return get(log.logger());
	}

	/**
	 * Gets the {@linkplain LogBuffer} attached to the submitted {@linkplain Logger} (if any).
	 *
	 * @param logger the {@linkplain Logger} to get the {@linkplain LogBuffer} for.
	 * @return the found {@linkplain LogBuffer} or {@code null} if none has been configured.
	 */
	@Nullable
	public static LogBuffer get(Logger logger) {
		LogBuffer logBuffer = null;
		Logger currentLogger = logger;

		while (logBuffer == null && currentLogger != null) {
			for (Handler handler : currentLogger.getHandlers()) {
				if (handler instanceof LogBuffer) {
					logBuffer = (LogBuffer) handler;
					break;
				}
			}
			currentLogger = currentLogger.getParent();
		}
		return logBuffer;
	}

	/**
	 * Adds a {@linkplain Handler} to this {@linkplain LogBuffer} instance for {@linkplain LogRecord} consuming.
	 * <p>
	 * If the submitted {@linkplain Log} has no {@linkplain LogBuffer} attached the call is ignored.
	 *
	 * @param log the {@linkplain Log} identifying the {@linkplain LogBuffer} to add to.
	 * @param handler the {@linkplain Handler} to add.
	 * @param republishBuffer whether to republish buffered {@linkplain LogRecord}s to the {@linkplain Handler}.
	 * @see #get(Log)
	 * @see #addHandler(Handler, boolean)
	 */
	public static void addHandler(Log log, Handler handler, boolean republishBuffer) {
		addHandler(log.logger(), handler, republishBuffer);
	}

	/**
	 * Adds a {@linkplain Handler} to this {@linkplain LogBuffer} instance for {@linkplain LogRecord} consuming.
	 * <p>
	 * If the submitted {@linkplain Logger} has no {@linkplain LogBuffer} attached the call is ignored.
	 *
	 * @param logger the {@linkplain Logger} identifying the {@linkplain LogBuffer} to add to.
	 * @param handler the {@linkplain Handler} to add.
	 * @param republishBuffer whether to republish buffered {@linkplain LogRecord}s to the {@linkplain Handler}.
	 * @see #get(Logger)
	 * @see #addHandler(Handler, boolean)
	 */
	public static void addHandler(Logger logger, Handler handler, boolean republishBuffer) {
		LogBuffer logBuffer = get(logger);

		if (logBuffer != null) {
			logBuffer.addHandler(handler, republishBuffer);
		}
	}

	/**
	 * Adds a {@linkplain Handler} to this {@linkplain LogBuffer} instance for {@linkplain LogRecord} consuming.
	 * <p>
	 * Any already buffered {@linkplain LogRecord} is sent to the {@linkplain Handler} during this operation.
	 *
	 * @param handler the {@linkplain Handler} to add.
	 * @param republishBuffer whether to republish buffered {@linkplain LogRecord}s to the {@linkplain Handler}.
	 */
	public synchronized void addHandler(Handler handler, boolean republishBuffer) {
		for (LogRecord record : this.buffer) {
			handler.publish(record);
		}
		this.handlers.add(handler);
	}

	/**
	 * Gets a previously registered {@linkplain Handler} of a specific type. If the submitted {@linkplain Log} has no
	 * {@linkplain LogBuffer} attached the call is ignored.
	 *
	 * @param <T> the actual type of the requested {@linkplain Handler}.
	 * @param log the {@linkplain Log} identifying the {@linkplain LogBuffer} to search.
	 * @param handlerType the type of {@linkplain Handler} to get.
	 * @return the found {@linkplain Handler} or {@code null}.
	 */
	@Nullable
	public static <T extends Handler> T getHandler(Log log, Class<T> handlerType) {
		return getHandler(log.logger(), handlerType);
	}

	/**
	 * Gets a previously registered {@linkplain Handler} of a specific type. If the submitted {@linkplain Logger} has no
	 * {@linkplain LogBuffer} attached the call is ignored.
	 *
	 * @param <T> the actual type of the requested {@linkplain Handler}.
	 * @param logger the {@linkplain Logger} identifying the {@linkplain LogBuffer} to search.
	 * @param handlerType the type of {@linkplain Handler} to get.
	 * @return the found {@linkplain Handler} or {@code null}.
	 */
	@Nullable
	public static <T extends Handler> T getHandler(Logger logger, Class<T> handlerType) {
		LogBuffer logBuffer = get(logger);

		return (logBuffer != null ? logBuffer.getHandler(handlerType) : null);
	}

	/**
	 * Gets a previously registered {@linkplain Handler} of a specific type.
	 *
	 * @param <T> the actual type of the requested {@linkplain Handler}.
	 * @param handlerType the type of {@linkplain Handler} to get.
	 * @return the found {@linkplain Handler} or {@code null}.
	 */
	@Nullable
	public synchronized <T extends Handler> T getHandler(Class<T> handlerType) {
		@Nullable T found = null;

		for (Handler handler : this.handlers) {
			if (handler.getClass().equals(handlerType)) {
				found = handlerType.cast(handler);
				break;
			}
		}
		return found;
	}

	/**
	 * Removes a previously added {@linkplain Handler} from this {@linkplain LogBuffer} instance.
	 * <p>
	 * If the submitted {@linkplain Log} has no {@linkplain LogBuffer} attached the call is ignored.
	 *
	 * @param log the {@linkplain Log} identifying the {@linkplain LogBuffer} to remove from.
	 * @param handler the {@linkplain Handler} to remove.
	 * @see #addHandler(Log, Handler, boolean)
	 * @see #get(Log)
	 * @see #removeHandler(Handler)
	 */
	public static void removeHandler(Log log, Handler handler) {
		removeHandler(log.logger(), handler);
	}

	/**
	 * Removes a previously added {@linkplain Handler} from this {@linkplain LogBuffer} instance.
	 * <p>
	 * If the submitted {@linkplain Logger} has no {@linkplain LogBuffer} attached the call is ignored.
	 *
	 * @param logger the {@linkplain Logger} identifying the {@linkplain LogBuffer} to remove from.
	 * @param handler the {@linkplain Handler} to remove.
	 * @see #addHandler(Logger, Handler, boolean)
	 * @see #get(Logger)
	 * @see #removeHandler(Handler)
	 */
	public static void removeHandler(Logger logger, Handler handler) {
		LogBuffer logBuffer = get(logger);

		if (logBuffer != null) {
			logBuffer.removeHandler(handler);
		}
	}

	/**
	 * Removes a previously added {@linkplain Handler} from this {@linkplain LogBuffer} instance.
	 * <p>
	 * After removal the {@linkplain Handler} will no longer receive any {@linkplain LogRecord}s.
	 *
	 * @param handler the {@linkplain Handler} to remove.
	 * @see #addHandler(Handler, boolean)
	 */
	public synchronized void removeHandler(Handler handler) {
		this.handlers.remove(handler);
	}

	/**
	 * Exports the buffered {@linkplain LogRecord}s to a {@linkplain File}.
	 * <p>
	 * If the submitted {@linkplain Log} has no {@linkplain LogBuffer} attached the call is ignored.
	 *
	 * @param log the {@linkplain Log} identifying the {@linkplain LogBuffer} to export from.
	 * @param file the {@linkplain File} to export to.
	 * @param append whether to append ({@code true}) in case of an existing file or not ({@code false}).
	 * @throws IOException if an I/O error occurs during export.
	 */
	public static void exportTo(Log log, File file, boolean append) throws IOException {
		exportTo(log.logger(), file, append);
	}

	/**
	 * Exports the buffered {@linkplain LogRecord}s to a {@linkplain File}.
	 * <p>
	 * If the submitted {@linkplain Logger} has no {@linkplain LogBuffer} attached the call is ignored.
	 *
	 * @param logger the {@linkplain Logger} identifying the {@linkplain LogBuffer} to export from.
	 * @param file the {@linkplain File} to export to.
	 * @param append whether to append ({@code true}) in case of an existing file or not ({@code false}).
	 * @throws IOException if an I/O error occurs during export.
	 */
	public static void exportTo(Logger logger, File file, boolean append) throws IOException {
		LogBuffer logBuffer = get(logger);

		if (logBuffer != null) {
			logBuffer.exportTo(file, append);
		}
	}

	/**
	 * Exports the buffered {@linkplain LogRecord}s to a {@linkplain File}.
	 *
	 * @param file the {@linkplain File} to export to.
	 * @param append whether to append ({@code true}) in case of an existing file or not ({@code false}).
	 * @throws IOException if an I/O error occurs during export.
	 */
	public synchronized void exportTo(File file, boolean append) throws IOException {
		try (Writer writer = new FileWriter(file, append)) {
			LogLineFormatter formatter = new LogLineFormatter();

			for (LogRecord record : this.buffer) {
				writer.write(formatter.format(record));
			}
		}
	}

	/**
	 * Performs a flush operation on this {@linkplain LogBuffer} instance.
	 * <p>
	 * The flush request is forwarded to any currently registered {@linkplain Handler} and any buffered
	 * {@linkplain LogRecord} will be discarded. If the submitted {@linkplain Logger} has no {@linkplain LogBuffer}
	 * attached the call is ignored.
	 *
	 * @param log The {@linkplain Log} identifying the {@linkplain LogBuffer} to flush.
	 * @see #get(Log)
	 */
	public static void flush(Log log) {
		flush(log.logger());
	}

	/**
	 * Performs a flush operation on this {@linkplain LogBuffer} instance.
	 * <p>
	 * The flush request is forwarded to any currently registered {@linkplain Handler} and any buffered
	 * {@linkplain LogRecord} will be discarded. If the submitted {@linkplain Logger} has no {@linkplain LogBuffer}
	 * attached the call is ignored.
	 *
	 * @param logger the {@linkplain Logger} identifying the {@linkplain LogBuffer} to flush.
	 * @see #get(Logger)
	 */
	public static void flush(Logger logger) {
		LogBuffer logBuffer = get(logger);

		if (logBuffer != null) {
			logBuffer.flush();
		}
	}

	@Override
	public void publish(@Nullable LogRecord record) {
		if (record != null && isLoggable(record) && this.locked.compareAndSet(false, true)) {
			try {
				synchronized (this) {
					while (this.buffer.size() >= this.limit) {
						this.buffer.remove();
					}
					this.buffer.add(record);
					this.handlers.forEach(handler -> handler.publish(record));
				}
			} finally {
				this.locked.set(false);
			}
		}
	}

	@Override
	public synchronized void flush() {
		this.handlers.forEach(Handler::flush);
		this.buffer.clear();
	}

	@Override
	public synchronized void close() {
		this.handlers.forEach(Handler::close);
		this.handlers.clear();
	}

}
