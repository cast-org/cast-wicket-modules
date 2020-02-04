/*
 * Copyright 2011-2020 CAST, Inc.
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
package org.cast.cwm.dav;

import com.xmlmind.davclient.DAVClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Authenticator;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton object to keep track of DAV Client sessions, without having to store them in 
 * sessions or serialized objects.

 * @author bgoldowsky
 *
 */
public class DavClientManager {
	
	private static DavClientManager instance = new DavClientManager();
	
	// Map of clients by the name they are given when added to this Manager.
	protected Map<String, DAVClient> clientMap = new HashMap<String, DAVClient>();
	
	private static final Logger log = LoggerFactory.getLogger(DavClientManager.class);

	public static DavClientManager get() {
		return instance;
	}

	/**
	 * Simple-minded method that sets up a basic Authenticator sufficient for
	 * a single authenticated DAV connection.
	 * @param username the username with which to authenticate for all challenges
	 * @param password the password to use
	 */
	public void setDefaultAuthentication (String username, String password) {
		Authenticator davAuth = new SimpleAuthenticator(username, password);
		Authenticator.setDefault(davAuth);
	}
	
	/**
	 * Retrieve a known client by name.
	 * 
	 * @param name unique name of the client as given to {@link #createClient(String, String, String)}) or {@link #addClient(String, DAVClient)}
	 * @return the DAVClient, or null if none is known with that name.
	 */
	public DAVClient getClient (String name) {
		return clientMap.get(name);
	}
	
	public void createClient (String name, String server, String path) {
		try {
			DAVClient davClient = new DAVClient("http", server, 80, path, "UTF-8");
			addClient(name, davClient);
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
	}
	
	public void addClient (String name, DAVClient client) {
		if (clientMap.containsKey(name))
			throw new IllegalArgumentException("Attempt to add a second client with the same name");
		clientMap.put(name, client);
		log.debug("Added DAV client: {}", client);

	}
	
	public void removeClient (String name) {
		clientMap.remove(name);
	}
	
}
