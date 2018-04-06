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
package de.carne.boot.prefs;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Transient store.
 */
class DataStore extends FilePreferencesStore {

	private final Properties data;

	DataStore(Properties data) {
		this.data = data;
	}

	@Override
	protected Properties loadData() {
		return this.data;
	}

	@Override
	protected Properties syncData(List<Consumer<Properties>> changes) throws IOException {
		for (Consumer<Properties> change : changes) {
			change.accept(this.data);
		}
		return this.data;
	}

	@Override
	public String toString() {
		return "<transient>";
	}

}
