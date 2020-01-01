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

import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.boot.logging.proxy.AutoProxy;
import de.carne.boot.logging.proxy.Log4j2Proxy;
import de.carne.boot.logging.proxy.Proxy;
import de.carne.boot.logging.proxy.Slf4jProxy;

/**
 * A {@linkplain Handler} implementation used to forward log records to a 3rd party logging framework.
 */
public class ProxyHandler extends Handler {

	/**
	 * The type of logging framework to forward leg records to.
	 */
	private enum Type {

		/**
		 * Auto detect which of the supported logging frameworks is available.
		 */
		AUTO(AutoProxy.class),

		/**
		 * Log4j 2 (<a href= "https://logging.apache.org/log4j/2.x/">https://logging.apache.org/log4j/2.x/</a>).
		 */
		LOG4J2(Log4j2Proxy.class),

		/**
		 * SLF4J (<a href= "https://www.slf4j.org">https://www.slf4j.org</a>).
		 */
		SLF4J(Slf4jProxy.class);

		private final Class<? extends Proxy> proxyClass;

		private Type(Class<? extends Proxy> proxyClass) {
			this.proxyClass = proxyClass;
		}

		/**
		 * Gets the {@linkplain Proxy} implementation to use for this type.
		 *
		 * @return the {@linkplain Proxy} implementation to use for this type.
		 */
		public Class<? extends Proxy> proxyClass() {
			return this.proxyClass;
		}

	}

	private static final Formatter DEFAULT_FORMATTER = new Formatter() {

		@Override
		public String format(@Nullable LogRecord record) {
			return formatMessage(record);
		}

	};

	private final Proxy proxy;

	/**
	 * Constructs {@linkplain ProxyHandler}.
	 */
	public ProxyHandler() {
		LogManager manager = LogManager.getLogManager();
		String propertyBase = getClass().getName();
		Type type = getTypeProperty(manager, propertyBase + ".type", Type.AUTO);

		this.proxy = getProxyInstance(type.proxyClass());
	}

	@Override
	public void publish(@Nullable LogRecord record) {
		if (record != null) {
			Formatter formatter = getFormatter();

			this.proxy.publish(record, (formatter != null ? formatter : DEFAULT_FORMATTER));
		}
	}

	@Override
	public void flush() {
		// Nothing to do here
	}

	@Override
	public void close() {
		// Nothing to do here
	}

	private static Type getTypeProperty(LogManager manager, String name, Type defaultValue) {
		String typeName = Logs.getStringProperty(manager, name, defaultValue.name());
		Type type = defaultValue;

		try {
			type = Type.valueOf(typeName);
		} catch (IllegalArgumentException e) {
			Logs.DEFAULT_ERROR_MANAGER.error("Invalid type property " + name, e, ErrorManager.GENERIC_FAILURE);
		}
		return type;
	}

	private static Proxy getProxyInstance(Class<? extends Proxy> proxyClass) {
		Proxy proxy;

		try {
			proxy = proxyClass.getConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			Logs.DEFAULT_ERROR_MANAGER.error("Failed to instanticate proxy class " + proxyClass.getName(), e,
					ErrorManager.GENERIC_FAILURE);
			proxy = new AutoProxy();
		}
		return proxy;
	}

}
