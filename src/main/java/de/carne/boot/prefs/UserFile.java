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
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility class used to create user preferences files with user only access rights.
 */
public final class UserFile {

	private UserFile() {
		// Prevent instantiation
	}

	private static final String FILE_ATTRIBUTE_VIEW_POSIX = "posix";

	/**
	 * Create or open a user preferences file.
	 *
	 * @param file the file to open
	 * @param extraOptions the extra {@linkplain OpenOption} to use.
	 * @return the opened {@linkplain FileChannel}.
	 * @throws IOException if an I/O error occurs.
	 */
	public static FileChannel open(Path file, @NonNull OpenOption... extraOptions) throws IOException {
		Files.createDirectories(file.getParent(), userDirectoryAttributes(file));

		Set<OpenOption> openOptions = new HashSet<>();

		for (OpenOption extraOption : extraOptions) {
			openOptions.add(extraOption);
		}
		openOptions.add(StandardOpenOption.CREATE);
		return FileChannel.open(file, openOptions, userFileAttributes(file));
	}

	private static FileAttribute<?>[] userDirectoryAttributes(Path path) {
		Set<String> fileAttributeViews = path.getFileSystem().supportedFileAttributeViews();
		List<FileAttribute<?>> attributes = new ArrayList<>();

		if (fileAttributeViews.contains(FILE_ATTRIBUTE_VIEW_POSIX)) {
			attributes.add(PosixFilePermissions.asFileAttribute(EnumSet.of(PosixFilePermission.OWNER_READ,
					PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE)));
		}
		return attributes.toArray(new @Nullable FileAttribute<?>[attributes.size()]);
	}

	private static FileAttribute<?>[] userFileAttributes(Path path) {
		Set<String> fileAttributeViews = path.getFileSystem().supportedFileAttributeViews();
		List<FileAttribute<?>> attributes = new ArrayList<>();

		if (fileAttributeViews.contains(FILE_ATTRIBUTE_VIEW_POSIX)) {
			attributes.add(PosixFilePermissions
					.asFileAttribute(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)));
		}
		return attributes.toArray(new @Nullable FileAttribute<?>[attributes.size()]);
	}

}
