/*
 * Copyright 2011-2018 CAST, Inc.
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
import java.net.URL;
import java.util.Properties;

import com.google.inject.Singleton;
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
@Singleton
public class AppConfiguration implements IAppConfiguration {
	
	String baseDirectory;
	Properties properties;
	
	private static final String errorMessage = 
			"Could not configure the application\n"
			+ "Please do one of the following:\n"
			+ "   Set an \"appConfig\" parameter in your servlet configuration, or\n"
			+ "   Create a file named \"app.config\" in the classpath.\n";
	
	private final static Logger log = LoggerFactory.getLogger(AppConfiguration.class);

	/**
	 * Create a new IAppConfiguration in the default manner for the given WebApplication.
	 * Looks for servlet init parameters "appConfig" or "propertiesFile";
	 * failing that, looks in the classpath for a file named "app.config".
	 *  
	 * @param application the WebApplication for which to load configuration information
	 * @return the resulting IAppConfiguration
	 */
	public static IAppConfiguration loadFor (WebApplication application) {
		// Loading from specified properties file, if one is given.
		String propPath = application.getServletContext().getInitParameter("appConfig");
		if (propPath==null)
			propPath = application.getServletContext().getInitParameter("propertiesFile");
		if (propPath != null)
			return loadFrom(propPath);
		// Try loading from a file named "app.config" in the classpath
		URL url = AppConfiguration.class.getClassLoader().getResource("app.config");
		if (url != null && url.getProtocol().equals("file"))
			return loadFrom(url.getFile());
		throw new AppConfiguration.ConfigurationException(errorMessage);
	}

	/**
	 * Create an IAppConfiguration from information in the given file.
	 * 
	 * @param filePath the path for the app config file.
	 * @return the resulting IAppConfiguration
	 */
	public static IAppConfiguration loadFrom (String filePath) {
		log.info("Loading App Properties from {}", filePath);
		File file = new File(filePath);
		Properties appProperties = new Properties();
		try {
			appProperties.load(new FileInputStream(file));
		} catch(Exception e) {
			throw new AppConfiguration.ConfigurationException("Error configuring application", e);
		}
		return new AppConfiguration(file.getParent(), appProperties);
	}

	
	public AppConfiguration (String baseFilename, Properties properties) {
		this.baseDirectory = baseFilename;
		this.properties = properties;
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.IAppConfiguratio#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty (String key) {
		String value = properties.getProperty(key);
		return value==null ? null : value.trim();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.IAppConfiguratio#getString(java.lang.String, java.lang.String)
	 */
	@Override
	public String getString (String key, String defaultValue) {
		String value = getProperty(key);
		if (value == null)
			return defaultValue;
		return value;
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.IAppConfiguratio#getString(java.lang.String)
	 */
	@Override
	public String getString (String key) {
		String value = getProperty(key);
		if (value == null)
			throw new ConfigurationException("Required configuration property " + key + " was not set.");
		return value;
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.IAppConfiguratio#getInteger(java.lang.String, java.lang.Integer)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.IAppConfiguratio#getInteger(java.lang.String)
	 */
	@Override
	public int getInteger (String key) {
		Integer value = getInteger(key, null);
		if (value==null)
			throw new ConfigurationException(
					String.format("Required configuration property \"%s\" was not set", key));
		return value;			
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.IAppConfiguratio#getBoolean(String, String)
	 */
	@Override
	public Boolean getBoolean(String key, Boolean defaultValue) {
		String value = properties.getProperty(key);
		if (value==null)
			return defaultValue;
		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") 
				|| value.equalsIgnoreCase("on") || value.equals("1"))
			return Boolean.TRUE;
		if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no") 
				|| value.equalsIgnoreCase("off") || value.equals("0"))
			return Boolean.FALSE;
		throw new ConfigurationException(
				String.format("Configuration property %s should have boolean value, but was \"%s\"",
				key, value));
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.IAppConfiguratio#getBoolean(String)
	 */
	@Override
	public boolean getBoolean(String key) {
		Boolean value = getBoolean(key, null);
		if (value==null)
			throw new ConfigurationException(
					String.format("Required configuration property \"%s\" was not set", key));
		return value;			
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.IAppConfiguratio#getOptionalFile(java.lang.String)
	 */	
	@Override
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
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.IAppConfiguratio#getFile(java.lang.String)
	 */
	@Override
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
