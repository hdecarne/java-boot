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
package de.carne.boot.prefs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.boot.Exceptions;

/**
 * File based store.
 */
class FileStore extends FilePreferencesStore {

	private final Path file;

	FileStore(Path file) {
		this.file = file;
	}

	@Override
	protected Properties loadData() throws IOException {
		Properties data = new Properties();

		try (FileChannel dataChannel = FileChannel.open(this.file, StandardOpenOption.READ);
				InputStream dataInputStream = Channels.newInputStream(dataChannel);
				FileLock dataLock = dataChannel.tryLock(0, Long.MAX_VALUE, true)) {
			if (dataLock == null) {
				throw new IOException("Failed to obtain read lock for file: '" + this.file + "'");
			}
			data.load(dataInputStream);
		} catch (NoSuchFileException e) {
			Exceptions.ignore(e);
		}
		return data;
	}

	@Override
	protected Properties syncData(List<Consumer<Properties>> changes) throws IOException {
		Properties data = new Properties();

		try (FileChannel dataChannel = UserFile.open(this.file, StandardOpenOption.READ, StandardOpenOption.WRITE);
				InputStream dataInputStream = Channels.newInputStream(dataChannel);
				OutputStream dataOutputStream = Channels.newOutputStream(dataChannel);
				FileLock dataLock = dataChannel.tryLock(0, Long.MAX_VALUE, false)) {
			if (dataLock == null) {
				throw new IOException("Failed to obtain write lock for file: '" + this.file + "'");
			}
			data.load(dataInputStream);
			for (Consumer<Properties> change : changes) {
				change.accept(data);
			}
			dataChannel.truncate(0);
			data.store(dataOutputStream, null);
		}
		return data;
	}

	@Override
	public int hashCode() {
		return this.file.hashCode();
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		return obj instanceof FileStore && this.file.equals(((FileStore) obj).file);
	}

	@Override
	public String toString() {
		return this.file.toString();
	}

}
