/*
 * Copyright 2011-2019 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cast.cwm;

import org.apache.wicket.util.file.File;
import org.cast.cwm.AppConfiguration.ConfigurationException;

public interface IAppConfiguration {

	/**
	 * Basic method to look up and return the configuration value specified in the configuration file.
	 * @param key name of the configuration property
	 * @return value as a string
	 * @throws ConfigurationException if key is not found
	 */
	public abstract String getProperty(String key);

	/**
	 * Find and return an optional string-valued configuration property.
	 * If the property is not specified, the given defaultValue is returned instead.
	 * @param key name of the configuration property
	 * @param defaultValue value to return if not found
	 * @return the specified or default value
	 */
	public abstract String getString(String key, String defaultValue);

	/**
	 * Find and return a required string-valued configuration property.
	 * If the property is not set, and exception will be thrown.
	 * @param key name of the configuration property
	 * @return the specified property value
	 */
	public abstract String getString(String key);

	/**
	 * Find and return an optional integer-type configuration property.
	 * If the property is not found, the given defaultValue will be returned instead. 
	 * @param key name of the configuration property
	 * @param defaultValue value to return if not found
	 * @return specified or default value.
	 * 
	 * @throws ConfigurationException if the property value can't be converted to an integer
	 */
	public abstract Integer getInteger(String key, Integer defaultValue);

	/**
	 * Find and return a required integer-type configuration property.
	 * If the property is not set, and exception will be thrown.
	 * @param key name of the configuration property
	 * @return the specified property value
	 * 
	 * @throws ConfigurationException if the property value can't be converted to an integer
	 */
	public abstract int getInteger(String key);

	/**
	 * Return optional property value, which should be a filename, as a File.
	 * Filenames can either be absolute, or relative to this AppConfiguration's base directory
	 * (which is generally the directory in which the properties file is located).
	 * @param key name of the configuration property
	 * @return value as a File, or null
	 * @throws ConfigurationException if property value is not a path to a readable file
	 */
	public abstract File getOptionalFile(String key);

	/**
	 * Return required property value, which should be a filename, as a File.
	 * Filenames can either be absolute, or relative to this AppConfiguration's base directory
	 * (which is generally the directory in which the properties file is located).
	 * @param key name of the configuration property
	 * @return value as a File
	 * @throws ConfigurationException if key is not found or is not the path to a readable file.
	 */
	public abstract File getFile(String key);

}