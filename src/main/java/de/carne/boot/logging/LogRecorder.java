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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import de.carne.boot.Nullable;

/**
 * This class is used to record {@linkplain LogRecord}s in a selective manner (e.g. to display detailed results of long
 * running operations).
 */
public final class LogRecorder {

	private final List<Predicate<LogRecord>> includeRecords = new ArrayList<>();
	private final List<Predicate<LogRecord>> excludeRecords = new ArrayList<>();

	private final List<Logger> loggers = new ArrayList<>();

	/**
	 * Construct {@linkplain LogRecorder}.
	 *
	 * @param level The {@linkplain Level} to use for {@linkplain LogRecord} filtering (all {@linkplain LogRecord}s
	 *        below this level are ignored).
	 */
	public LogRecorder(Level level) {
		excludeRecord(record -> record.getLevel().intValue() < level.intValue());
	}

	/**
	 * Add an include {@linkplain Predicate} to the recorder.
	 * <p>
	 * To be recorded an issued {@linkplain LogRecord} must match at least one of the include {@linkplain Predicate}s.
	 *
	 * @param include The include {@linkplain Predicate} to apply.
	 * @return The updated {@linkplain LogRecorder}.
	 */
	public LogRecorder includeRecord(Predicate<LogRecord> include) {
		this.includeRecords.add(include);
		return this;
	}

	/**
	 * Add an exclude {@linkplain Predicate} to the recorder.
	 * <p>
	 * To be recorded an issued {@linkplain LogRecord} must not match any of the exclude {@linkplain Predicate}s.
	 *
	 * @param exclude The exclude {@linkplain Predicate} to apply.
	 * @return The updated {@linkplain LogRecorder}.
	 */
	public LogRecorder excludeRecord(Predicate<LogRecord> exclude) {
		this.excludeRecords.add(exclude);
		return this;
	}

	/**
	 * Add a {@linkplain Log} instance to the {@linkplain LogRecorder} for recording.
	 *
	 * @param log The {@linkplain Log} instance to add to the recording.
	 * @return The updated {@linkplain LogRecorder}.
	 */
	public LogRecorder addLog(Log log) {
		return addLogger(log.logger());
	}

	/**
	 * Add a {@linkplain Logger} instance to the {@linkplain LogRecorder} for recording.
	 *
	 * @param logger The {@linkplain Logger} instance to add to the recording.
	 * @return The updated {@linkplain LogRecorder}.
	 */
	public LogRecorder addLogger(Logger logger) {
		this.loggers.add(logger);
		return this;
	}

	/**
	 * Start the recording {@linkplain Session}.
	 * <p>
	 * After this method has been invoked the {@linkplain LogRecorder} collects all {@linkplain LogRecord}s that
	 * <ul>
	 * <li>issued via one of the added loggers</li>
	 * <li>match the setup filtering</li>
	 * </ul>
	 * until the {@linkplain Session}'s {@linkplain Session#close()} method is called.
	 *
	 * @param currentThreadOnly Whether to include only {@linkplain LogRecord}s issued by the current
	 *        {@linkplain Thread}.
	 * @return The started {@linkplain Session}.
	 */
	public Session start(boolean currentThreadOnly) {
		return new Session(currentThreadOnly);
	}

	void startSession(Session session) {
		this.loggers.stream().forEach(logger -> logger.addHandler(session));
	}

	void stopSession(Session session) {
		this.loggers.stream().forEach(logger -> logger.removeHandler(session));
	}

	boolean testRecord(LogRecord record) {
		return this.excludeRecords.stream().noneMatch(exclude -> exclude.test(record)) && (this.includeRecords.isEmpty()
				|| this.includeRecords.stream().anyMatch(include -> include.test(record)));
	}

	/**
	 * This class represents a running recording session.
	 *
	 * @see LogRecorder#start(boolean)
	 * @see #close()
	 */
	public class Session extends Handler implements AutoCloseable {

		private final List<Predicate<Thread>> includeThreads = new ArrayList<>();
		private final List<Predicate<Thread>> excludeThreads = new ArrayList<>();

		private final Queue<LogRecord> buffer = new ConcurrentLinkedQueue<>();

		private final AtomicBoolean locked = new AtomicBoolean();

		Session(boolean currentThreadOnly) {
			if (currentThreadOnly) {
				Thread currentThread = Thread.currentThread();

				excludeThread(thread -> !thread.equals(currentThread));
			}
			startSession(this);
		}

		/**
		 * Add an include {@linkplain Predicate} to the session.
		 * <p>
		 * To be recorded an issued {@linkplain LogRecord}'s thread must match at least one of the include
		 * {@linkplain Predicate}s.
		 *
		 * @param include The include {@linkplain Predicate} to apply.
		 * @return The updated {@linkplain Session}.
		 */
		public Session includeThread(Predicate<Thread> include) {
			this.includeThreads.add(include);
			return this;
		}

		/**
		 * Add an exclude {@linkplain Predicate} to the {@linkplain Session}.
		 * <p>
		 * To be recorded an issued {@linkplain LogRecord}'s thread must not match any of the exclude
		 * {@linkplain Predicate}s.
		 *
		 * @param exclude The exclude {@linkplain Predicate} to apply.
		 * @return The updated {@linkplain Session}.
		 */
		public Session excludeThread(Predicate<Thread> exclude) {
			this.excludeThreads.add(exclude);
			return this;
		}

		/**
		 * Get the {@linkplain LogRecord}s that have been recorded by this {@linkplain Session} so far.
		 *
		 * @return The recorded {@linkplain LogRecord}s.
		 */
		public Collection<LogRecord> getRecords() {
			return Collections.unmodifiableCollection(this.buffer);

		}

		private boolean testThread(Thread thread) {
			return this.excludeThreads.stream().noneMatch(exclude -> exclude.test(thread))
					&& (this.includeThreads.isEmpty()
							|| this.includeThreads.stream().anyMatch(include -> include.test(thread)));
		}

		@Override
		public void publish(@Nullable LogRecord record) {
			if (record != null && this.locked.compareAndSet(false, true) && testThread(Thread.currentThread())
					&& testRecord(record)) {
				this.buffer.add(record);
				this.locked.set(false);
			}
		}

		@Override
		public void flush() {
			// Nothing to do
		}

		@Override
		public void close() {
			stopSession(this);
		}

	}

}
