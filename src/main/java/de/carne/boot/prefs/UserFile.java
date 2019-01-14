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
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.carne.boot.logging.Log;

/**
 * Utility class used to create user preferences files with user only access rights.
 */
public final class UserFile {

	private static final Log LOG = new Log();

	private UserFile() {
		// Prevent instantiation
	}

	private static final String FILE_ATTRIBUTE_VIEW_POSIX = "posix";
	private static final String FILE_ATTRIBUTE_VIEW_ACL = "acl";

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

	@SuppressWarnings("resource")
	private static FileAttribute<?>[] userDirectoryAttributes(Path path) throws IOException {
		FileSystem fileSystem = path.getFileSystem();
		Set<String> fileAttributeViews = fileSystem.supportedFileAttributeViews();
		List<FileAttribute<?>> attributes = new ArrayList<>();

		if (fileAttributeViews.contains(FILE_ATTRIBUTE_VIEW_POSIX)) {
			EnumSet<PosixFilePermission> posixPermissions = EnumSet.of(PosixFilePermission.OWNER_READ,
					PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE);

			attributes.add(PosixFilePermissions.asFileAttribute(posixPermissions));
		} else if (fileAttributeViews.contains(FILE_ATTRIBUTE_VIEW_ACL)) {
			AclEntry acl = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(getCurrentUser(fileSystem))
					.setPermissions(AclEntryPermission.values()).build();

			attributes.add(asFileAttribute(acl));
		} else {
			LOG.warning("No supported access control model found {0} for user directory ''{1}''", fileAttributeViews,
					path);
		}
		return attributes.toArray(new @Nullable FileAttribute<?>[attributes.size()]);
	}

	@SuppressWarnings("resource")
	private static FileAttribute<?>[] userFileAttributes(Path path) throws IOException {
		FileSystem fileSystem = path.getFileSystem();
		Set<String> fileAttributeViews = fileSystem.supportedFileAttributeViews();
		List<FileAttribute<?>> attributes = new ArrayList<>();

		if (fileAttributeViews.contains(FILE_ATTRIBUTE_VIEW_POSIX)) {
			EnumSet<PosixFilePermission> posixPermissions = EnumSet.of(PosixFilePermission.OWNER_READ,
					PosixFilePermission.OWNER_WRITE);

			attributes.add(PosixFilePermissions.asFileAttribute(posixPermissions));
		} else if (fileAttributeViews.contains(FILE_ATTRIBUTE_VIEW_ACL)) {
			AclEntry acl = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(getCurrentUser(fileSystem))
					.setPermissions(AclEntryPermission.values()).build();

			attributes.add(asFileAttribute(acl));
		} else {
			LOG.warning("No supported access control model found {0} for user file ''{1}''", fileAttributeViews, path);
		}
		return attributes.toArray(new @Nullable FileAttribute<?>[attributes.size()]);
	}

	private static UserPrincipal getCurrentUser(FileSystem fileSystem) throws IOException {
		return Objects.requireNonNull(fileSystem.getUserPrincipalLookupService()
				.lookupPrincipalByName(Objects.requireNonNull(System.getProperty("user.name"))));
	}

	private static FileAttribute<List<AclEntry>> asFileAttribute(@NonNull AclEntry... value) {
		return new FileAttribute<List<AclEntry>>() {

			@Override
			public String name() {
				return "acl:acl";
			}

			@Override
			public List<AclEntry> value() {
				return Arrays.asList(value);
			}

		};
	}

}
