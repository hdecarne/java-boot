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

import java.util.logging.Filter;
import java.util.logging.LogRecord;

import de.carne.boot.check.Nullable;

/**
 * {@linkplain Filter} for logging of localized messages only.
 * <p>
 * A message is considered localized if it is accompanied by a resource bundle.
 */
public class LocalizedFilter implements Filter {

	@Override
	public boolean isLoggable(@Nullable LogRecord record) {
		return (record != null && record.getResourceBundleName() != null);
	}

}
