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
package de.carne.boot.prefs;

import java.util.Objects;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.boot.check.Check;

/**
 * {@linkplain java.util.prefs.Preferences} implementation backed up by a single text configuration files.
 */
class FilePreferences extends AbstractPreferences {

	private final FilePreferencesStore store;

	public FilePreferences(FilePreferencesStore store) {
		super(null, "");
		this.store = store;
	}

	public FilePreferences(FilePreferences parent, String name) {
		super(parent, name);
		this.store = parent.store;
	}

	@Override
	protected void putSpi(String key, String value) {
		this.store.put(this, key, value);
	}

	@Override
	protected @Nullable String getSpi(String key) {
		return this.store.get(this, key);
	}

	@Override
	protected void removeSpi(String key) {
		this.store.remove(this, key);
	}

	@Override
	protected void removeNodeSpi() throws BackingStoreException {
		this.store.removeNode(this);
	}

	@Override
	protected String[] keysSpi() throws BackingStoreException {
		return this.store.keys(this);
	}

	@Override
	protected String[] childrenNamesSpi() throws BackingStoreException {
		return this.store.childrenNames(this);
	}

	@Override
	protected AbstractPreferences childSpi(@Nullable String name) {
		return this.store.child(this, Objects.requireNonNull(name));
	}

	@Override
	protected void syncSpi() throws BackingStoreException {
		// Should never be called as we overrode sync
		Check.fail();
	}

	@Override
	protected void flushSpi() throws BackingStoreException {
		// Should never be called as we overrode flush
		Check.fail();
	}

	@Override
	public void sync() throws BackingStoreException {
		this.store.sync();
	}

	@Override
	public void flush() throws BackingStoreException {
		this.store.flush();
	}

}
