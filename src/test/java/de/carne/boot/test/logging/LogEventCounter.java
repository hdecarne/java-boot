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
package de.carne.boot.test.logging;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Test appender for log event counting.
 */
@Plugin(name = "Counter", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class LogEventCounter extends AbstractAppender {

	private static final AtomicInteger appendCounter = new AtomicInteger();

	static void resetCounter() {
		LogEventCounter.appendCounter.set(0);
	}

	static int getCounter() {
		return LogEventCounter.appendCounter.get();
	}

	/**
	 * See {@linkplain AbstractAppender}.
	 *
	 * @param name see {@linkplain AbstractAppender}.
	 * @param filter see {@linkplain AbstractAppender}.
	 * @param layout see {@linkplain AbstractAppender}.
	 * @param ignoreExceptions see {@linkplain AbstractAppender}.
	 */
	public LogEventCounter(String name, Filter filter, Layout<? extends Serializable> layout,
			boolean ignoreExceptions) {
		super(name, filter, layout, ignoreExceptions);
	}

	/**
	 * See {@linkplain #createAppender(String, boolean, Layout, Filter)}.
	 *
	 * @param name see {@linkplain #createAppender(String, boolean, Layout, Filter)}.
	 * @param ignoreExceptions see {@linkplain #createAppender(String, boolean, Layout, Filter)}.
	 * @param layout see {@linkplain #createAppender(String, boolean, Layout, Filter)}.
	 * @param filter see {@linkplain #createAppender(String, boolean, Layout, Filter)}.
	 * @return see {@linkplain #createAppender(String, boolean, Layout, Filter)}.
	 */
	@PluginFactory
	public static LogEventCounter createAppender(@PluginAttribute("name") String name,
			@PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
			@PluginElement("Layout") Layout<? extends Serializable> layout, @PluginElement("Filters") Filter filter) {
		return new LogEventCounter(name, filter, layout, ignoreExceptions);
	}

	@Override
	public void append(@Nullable LogEvent event) {
		LogEventCounter.appendCounter.incrementAndGet();
	}

}
