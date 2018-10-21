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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.Nullable;

final class ApplicationJarURLStreamHandlerFactory implements URLStreamHandlerFactory {

	private static final String PROTOCOL_PREFIX = "jarjar";
	private static final AtomicInteger PROTOCOL_INDEX = new AtomicInteger();

	private final String protocol;
	private final ClassLoader resourceLoader;

	public ApplicationJarURLStreamHandlerFactory(ClassLoader resourceLoader) {
		this.protocol = PROTOCOL_PREFIX + PROTOCOL_INDEX.getAndIncrement();
		this.resourceLoader = resourceLoader;
		ApplicationURLStreamHandlerFactories.register(this.protocol, this);
	}

	public URL getJarJarUrl(String jarJar) throws MalformedURLException {
		return new URL("jar:" + this.protocol + ":" + jarJar + "!/");
	}

	public ClassLoader getResourceLoader() {
		return this.resourceLoader;
	}

	@Override
	public URLStreamHandler createURLStreamHandler(@Nullable String p) {
		return new URLStreamHandler() {

			@Override
			protected @Nullable URLConnection openConnection(@Nullable URL u) throws IOException {
				return (u != null ? new URLConnection(u) {

					@Override
					public void connect() throws IOException {
						// Nothing to do here
					}

					@Override
					public InputStream getInputStream() throws IOException {
						String fileResource = getURL().getFile();
						InputStream fileResourceStream = getResourceLoader().getResourceAsStream(fileResource);

						if (fileResourceStream == null) {
							throw new FileNotFoundException("Unknown file resource: " + fileResource);
						}
						return fileResourceStream;
					}

				} : null);
			}

		};
	}

}
