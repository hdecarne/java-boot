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

import java.util.function.Supplier;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Helper class used for management of the singleton instances.
 *
 * @param <I> the actual instance type.
 */
final class InstanceHolder<I> implements Supplier<I> {

	@Nullable
	private I instance = null;

	public I set(I instance) {
		if (this.instance != null) {
			throw new ApplicationInitializationException("Instance holder already initialized");
		}
		this.instance = instance;
		return instance;
	}

	@Override
	public I get() {
		@Nullable
		I checkedInstance = this.instance;

		if (checkedInstance == null) {
			throw new ApplicationInitializationException("Instance holder not yet initialized");
		}
		return checkedInstance;
	}

}
