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
	 * Return property value as a String
	 * @param key name of the configuration property
	 * @return value as a string
	 * @throws ConfigurationException if key is not found
	 */
	public String getProperty (String key) {
		String value = properties.getProperty(key);
		if (value == null)
			throw new ConfigurationException("Configuration property " + key + " was not set.");
		return value.trim();
	}

	/**
	 * Return property value, which should be a filename, as a File.
	 * Filenames can either be absolute, or relative to this AppConfiguration's base directory
	 * (which is generally the directory in which the properties file is located).
	 * @param key name of the configuration property
	 * @return value as a File, or null
	 * @throws ConfigurationException if key is not found
	 */
	public File getFile (String key) {
		String fileName = getProperty(key);
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
