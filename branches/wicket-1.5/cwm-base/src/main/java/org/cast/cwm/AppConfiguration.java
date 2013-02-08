/*
 * Copyright 2011-2013 CAST, Inc.
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

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.file.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds instance-specific configuration information for an application.
 * This is generally loaded from a file outside the webapp, so that it can differ between
 * development, QA, and live instances.
 * 
 * TODO right now this is just a wrapper for {@link java.util.Properties} with a little more 
 * error checking, but in the future I'd like to have it manage a list of known, data-typed, 
 * documented, and namespaced configuration items.
 * 
 * @author bgoldowsky
 *
 */
public class AppConfiguration {
	
	String baseDirectory;
	Properties properties;
	
	private final static Logger log = LoggerFactory.getLogger(AppConfiguration.class);

	/**
	 * Create a new Configuration from the properties file given.
	 * @param filePath file to load
	 * @return the resulting AppConfiguration
	 */
	public static AppConfiguration loadFor (WebApplication app) {
		String propPath = app.getServletContext().getInitParameter("propertiesFile");
		if(propPath == null)
			throw new RuntimeException("No configuration properties file path set");
		return loadFrom(propPath);
	}

	/**
	 * Create a new Configuration in the default manner for the given WebApplication.
	 * In our standard setup, this looks up the "propertiesFile" init parameter and 
	 * loads properties from the location.
	 * 
	 * @param application the WebApplication for which to load configuration information
	 * @return the resulting AppConfiguration
	 */
	public static AppConfiguration loadFrom (String filePath) {
		File file = new File(filePath);
		Properties appProperties = new Properties();
		try {
			appProperties.load(new FileInputStream(file));
		} catch(Exception e) {
			throw new AppConfiguration.ConfigurationException("Error configuring application", e);
		}

		log.info("Loading App Properties from {}", filePath);
		return new AppConfiguration(file.getParent(), appProperties);
	}

	
	public AppConfiguration (String baseFilename, Properties properties) {
		this.baseDirectory = baseFilename;
		this.properties = properties;
	}

	/**
	 * Basic method to look up and return the configuration value specified in the configuration file.
	 * @param key name of the configuration property
	 * @return value as a string
	 * @throws ConfigurationException if key is not found
	 */
	public String getProperty (String key) {
		String value = properties.getProperty(key);
		return value==null ? null : value.trim();
	}
	
	/**
	 * Find and return an optional string-valued configuration property.
	 * If the property is not specified, the given defaultValue is returned instead.
	 * @param key name of the configuration property
	 * @param defaultValue value to return if not found
	 * @return the specified or default value
	 */
	public String getString (String key, String defaultValue) {
		String value = getProperty(key);
		if (value == null)
			return defaultValue;
		return value;
	}

	/**
	 * Find and return a required string-valued configuration property.
	 * If the property is not set, and exception will be thrown.
	 * @param key name of the configuration property
	 * @return the specified property value
	 */
	public String getString (String key) {
		String value = getProperty(key);
		if (value == null)
			throw new ConfigurationException("Required configuration property " + key + " was not set.");
		return value;
	}
	
	/**
	 * Find and return an optional integer-type configuration property.
	 * If the property is not found, the given defaultValue will be returned instead. 
	 * @param key name of the configuration property
	 * @param defaultValue value to return if not found
	 * @return specified or default value.
	 * 
	 * @throws ConfigurationException if the property value can't be converted to an integer
	 */
	public Integer getInteger (String key, Integer defaultValue) {
		String value = properties.getProperty(key);
		if (value==null)
			return defaultValue;
		try {
			return (Integer.valueOf(value));
		} catch (NumberFormatException e) {
			throw new ConfigurationException(
					String.format("Configuration property %s should have integer value, but was \"%s\"",
					key, value));
		}		
	}
	
	/**
	 * Find and return a required integer-type configuration property.
	 * If the property is not set, and exception will be thrown.
	 * @param key name of the configuration property
	 * @return the specified property value
	 * 
 	 * @throws ConfigurationException if the property value can't be converted to an integer
	 */
	public int getInteger (String key) {
		Integer value = getInteger(key, null);
		if (value==null)
			throw new ConfigurationException(
					String.format("Required configuration property \"%s\" was not set", key));
		return value;			
	}
	
	/**
	 * Return optional property value, which should be a filename, as a File.
	 * Filenames can either be absolute, or relative to this AppConfiguration's base directory
	 * (which is generally the directory in which the properties file is located).
	 * @param key name of the configuration property
	 * @return value as a File, or null
	 * @throws ConfigurationException if property value is not a path to a readable file
	 */	
	public File getOptionalFile (String key) {
		String fileName = getProperty(key);
		if (fileName == null)
			return null;
		File file = new File(fileName); // Try interpreting as an absolute filename
		if (!file.isAbsolute())
			file = new File(baseDirectory, fileName); // Try interpreting relative to base
		if (!file.exists())
			throw new ConfigurationException(
					String.format("File %s specified for configuration property %s does not exist", file, key));
		if (!file.canRead())
			throw new ConfigurationException(
					String.format("File %s specified for configuration property %s is not readable", file, key));
		return file;		
	}
	
	/**
	 * Return required property value, which should be a filename, as a File.
	 * Filenames can either be absolute, or relative to this AppConfiguration's base directory
	 * (which is generally the directory in which the properties file is located).
	 * @param key name of the configuration property
	 * @return value as a File
	 * @throws ConfigurationException if key is not found or is not the path to a readable file.
	 */
	public File getFile (String key) {
		File file = getOptionalFile(key);
		if (file == null)
			throw new ConfigurationException(
				String.format("Required configuration property \"%s\" was not set", key));
		return file;
	}

	
	public static class ConfigurationException extends RuntimeException {

		private static final long serialVersionUID = 1L;
		
		public ConfigurationException(String string) {
			super(string);
		}

		public ConfigurationException(String string, Exception e) {
			super(string, e);
		}	
	}

}
