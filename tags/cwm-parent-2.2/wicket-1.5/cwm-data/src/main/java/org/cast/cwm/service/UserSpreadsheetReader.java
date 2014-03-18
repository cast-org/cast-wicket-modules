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
package org.cast.cwm.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import net.databinder.hib.Databinder;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.cast.cwm.data.models.UserModel;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.generationjava.io.CsvReader;
import com.google.inject.Inject;

/**
 * Object to read a spreadsheet defining users and create those users in the database.
 * 
 * @author bgoldowsky
 *
 */
public class UserSpreadsheetReader implements Serializable {
	
	@Inject
	private ICwmService cwmService;
	
	@Inject
	private ISiteService siteService;

	@Getter @Setter
	protected IModel<Site> defaultSite;
	
	@Getter @Setter
	protected IModel<Period> defaultPeriod;
	
	@Getter
	protected boolean validData = false;
	
	@Getter
	protected String globalError = null;

	/** 
	 *  Users to be created and associated errors, if any.
	 */
	@Getter
	protected List<PotentialUserSave> potentialUsers;
	
	protected Map<String,Site> potentialSites;
	
	protected Map<Site,Map<String,Period>> potentialPeriods;
	
	private static final Logger log = LoggerFactory.getLogger(UserSpreadsheetReader.class);
	
	private static final long serialVersionUID = 1L;


	public UserSpreadsheetReader() {
		Injector.get().inject(this);
	}
	
	/**
	 * Read spreadsheet of user information and generate potential users.
	 * Returns true if all was sucessful and users could be created as specified.
	 * 
	 * This method does NOT modify the datastore.
	 * 
	 *  @param stream the input stream of CSV data
	 * @return true if no errors encountered.
	 */
	public boolean readInput (InputStream stream) {
		potentialUsers = new ArrayList<PotentialUserSave>();
		potentialSites = new HashMap<String,Site>();
		potentialPeriods = new HashMap<Site,Map<String,Period>>();
  		String[] values = null; // The values of each User row in the CSV file
  		String[] header = null; // The header column row in the CSV file
  		int line = 1;           // Line counter for error reporting.
  		
  		CsvReader reader;
  		try {
			reader = new CsvReader(new InputStreamReader(stream, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage());
			globalError = e.getMessage();
			return false;
		}
  		
  		// Get Headers from CSV file
  		try {
			header = reader.readLine();
		} catch (IOException e) {
			globalError = e.getMessage();
			return false;
		}
		for (int i = 0; i < header.length; i++) {
  			if (header[i].trim().equalsIgnoreCase("first name"))
  				header[i] = "firstname";
  			if (header[i].trim().equalsIgnoreCase("last name"))
  				header[i] = "lastname";
  			header[i] = header[i].trim().toLowerCase();
  		}
		
  		// Check headers for required fields; return single element List on error
  		List<String> headerTest = Arrays.asList(header);
  		if(!headerTest.contains("username")) {
  			globalError = "Must specify a 'username' column";
  			return false;
  		} else if (!headerTest.contains("password")) {
  			globalError = "Must specify a 'password' column.";
 			return false;
  		} else if (!headerTest.contains("type")) {
  			globalError = "Must specify a 'type' column.";
 			return false;
  		} else if (!headerTest.contains("firstname")) {
  			globalError = "Must specify a 'firstname' column.";
  			return false;	
  		} else if (!headerTest.contains("lastname")) {
  			globalError = "Must specify a 'lastname' column.";
  			return false;
  		} else if (!headerTest.contains("period")) {
  			globalError = "Must specify a 'period' column.";
  			return false;
  		} else if (defaultSite==null && !headerTest.contains("site")) {
  			globalError = "Must specify a 'site' column or a default site.";
  			return false;
  		}

  		// Read the CSV file, create Person objects, record error messages, add to PotentialUserSave List
  		boolean errors = false; // have errors been encountered?
  		try {
  			while((values = reader.readLine()) != null) {
  				
  				line++; // Line that the error occurs on
  				String messages = ""; // Error Messages for this user
  				  				
  				// Read the User Data fields from this line in the CSV file
  				Map<String, String> map = new HashMap<String, String>();
  				for(int i = 0; i < header.length; i++) {
  					if (!values[i].equals("")) {
  						map.put(header[i], values[i].trim());
  					}
  				}
  				
  				// Create a transient User Object from imported data
  				IModel<User> user = new UserModel();
  				messages += populateUserObject(user, map);
  				
  				// Check database for duplicate username
  				if (UserService.get().getByUsername(user.getObject().getUsername()).getObject() != null) {
  					messages += "Username " + user.getObject().getUsername() + " already exists in database. \n";
  				}
  				
  				// Check database for duplicate subjectId
  				if (UserService.get().getBySubjectId(user.getObject().getSubjectId()).getObject() != null) {
  					messages += "SubjectId " + user.getObject().getSubjectId() + " already exists in database. \n";
  				}

  				// Check database for duplicate email addresses when an email address exists 				
  				if (user.getObject().getEmail() != null && UserService.get().getByEmail(user.getObject().getEmail()).getObject() != null) {
  					messages += "Email " + user.getObject().getEmail() + " already exists in database. \n";
  				}
  				
  				// Check uploaded user list for duplicate username, subjectId, "Full Name"
  				for (PotentialUserSave pe : potentialUsers) {
  					if (user.getObject().getUsername().matches(pe.getUser().getObject().getUsername())) {
  						messages += "Username " + user.getObject().getUsername() + " is a duplicate in this list. \n";
  					}
  					if (user.getObject().getSubjectId().matches(pe.getUser().getObject().getSubjectId())) {
  						messages += "SubjectId " + user.getObject().getSubjectId() + " is a duplicate in this list. \n";
  					}
  					if (user.getObject().getFullName().matches(pe.getUser().getObject().getFullName()) 
  							&& user.getObject().getPeriods().iterator().next().getName().matches(pe.getUser().getObject().getPeriods().iterator().next().getName())) {
  						messages += user.getObject().getFullName() + " already exists in " + user.getObject().getPeriods().iterator().next().getName() + " in this list. \n";
  					}
  				}
  				
  				// Add a PotentialUserSave to the list.
  				PotentialUserSave pe = new PotentialUserSave(user, messages, line);
  				potentialUsers.add(pe);
  				if (!"".equals(messages))
  					errors = true;
  			}
  			
  			// If CSV file has only one line, it is either empty or has unrecognized LF/CR values.
  			if (line == 1) {
//  			potentialUsers.add(new PotentialUserSave(null, "Empty or Corrupted File.  Note: Save as Windows CSV.", 0));
  	  			globalError = "Empty or Corrupted File - LF/CR values may be invalid!";	  			
  				throw new CharacterCodingException();
  			}
  		
  		} catch (ArrayIndexOutOfBoundsException e) {
  			// This can happen if the last row is missing values; Excel doesn't fill them out to the last column
  			log.error("Caught exception importing line {}: {}", line, e.getClass());
  			potentialUsers = new ArrayList<PotentialUserSave>();
  			potentialUsers.add(new PotentialUserSave(null, "Data missing/misaligned. Fatal error. \n", line));
  			return false;
  		} catch (CharacterCodingException e) {
  			log.error("Empty or Corrupted File - only 1 line found - CR/LF issue?. {}", e.getClass());
  			return false;
  		} catch (Exception e) {
  			e.printStackTrace();
  			log.error("Caught exception importing line {}: {}", line, e.getClass());
  			potentialUsers = new ArrayList<PotentialUserSave>();
  			potentialUsers.add(new PotentialUserSave(null, "Fatal Error" + e, line));
  			return false;
  		}
  	
  		return (!errors);
	}
	
	/** 
	 * Saves the potential users, periods, and sites.
	 * 
	 * @param users
	 */
	public void save() {
		Session session = Databinder.getHibernateSession();
		
		for (Site site : potentialSites.values()) {
			if (site.isTransient())
				session.save(site);
		}
		
		for (Map<String, Period> map : potentialPeriods.values())
			for (Period period : map.values()) {
				if (period.isTransient())
					session.save(period);
			}
	
		for(PotentialUserSave potentialUser : potentialUsers) {
			IModel<User> mUser = potentialUser.getUser();
			User u = mUser.getObject();
			u.setValid(true);
			u.setCreateDate(new Date());
			session.save(u);
		}

		cwmService.flushChanges();
	}
	

	/**
	 * Adds a map of values to a User object.
	 * 
	 * @param user a model of an empty, transient User object
	 * @param map a map of fields that will populate the User object
	 * @param tempPeriods a list of transient {@link Period} objects that this user can pick from or add to
	 * @param site the site where these Period objects should reside
	 * @return a string of errors (empty string if none)
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
  	private String populateUserObject(IModel<User> user, Map<String, String> map) 
  		throws IOException, InstantiationException, IllegalAccessException {
  		
  		String errors = "";
  		Site site =  null;
  		
  		// Determine site
  		if (map.containsKey("site")) {
  			site = getSite(map.get("site"));
  		} else if (defaultSite != null && defaultSite.getObject() != null) {
  			site = defaultSite.getObject();
  		} else {
  			errors += "Must specify site. \n";
  		}
  		
  		// Determine period(s)
  		if (map.containsKey("period")) {
  			for (String periodName : map.get("period").split(","))
  				user.getObject().getPeriods().add(getPeriod(site, periodName.trim()));
  		} else if (defaultPeriod != null && defaultPeriod.getObject() != null) {
  			user.getObject().getPeriods().add(defaultPeriod.getObject());
  		} else {
  			errors += "Must specify period. \n";
  		}
  		
  		// Set Names
  		if (map.containsKey("firstname") && !map.get("firstname").isEmpty())
  			user.getObject().setFirstName(map.get("firstname"));
  		else
  			errors += "Must specify \"firstname.\" \n";  		
  		if (map.containsKey("lastname") && !map.get("lastname").isEmpty())
  			user.getObject().setLastName(map.get("lastname"));
  		else
  			errors += "Must specify \"lastname.\" \n";

  		// Set Permission
  		boolean permission = false;
		if (map.containsKey("permission") && !map.get("permission").isEmpty()) {
			if (map.get("permission").trim().toLowerCase().equals("true") || map.get("permission").trim().equals("1") ) {
				permission = true;
			}
  		}
		user.getObject().setPermission(permission);

  		
  		// Set Type
  		if(map.containsKey("type")) {
  			String type = map.get("type");
  			if (Role.forRoleString(type) != null)
  				user.getObject().setRole(Role.forRoleString(type));
  			else if (type.startsWith("t"))
  				user.getObject().setRole(Role.TEACHER);
  			else
  				user.getObject().setRole(Role.STUDENT);
  		} else {
  			errors += "Must specify user type. \n";
  			user.getObject().setRole(null);
  		}
  		
  		// Set Password
  		if(map.containsKey("password") && !map.get("password").isEmpty()) {
  			user.getObject().setPassword(map.get("password"));
  		} else {
  			errors += "Must specify password. \n";
  			user.getObject().setPassword("");
  		}
 
  		// Set Username
  		if(map.containsKey("username")  && !map.get("username").isEmpty()) {
  			user.getObject().setUsername(map.get("username"));
  		} else {
  			errors += "Must specify username. \n";
  			user.getObject().setUsername("");
  		}
  		
  		// Set SubjectId (Default to Username)
  		if(map.containsKey("subjectid")) {
  			user.getObject().setSubjectId(map.get("subjectid"));
  		} else {
  			user.getObject().setSubjectId(map.get("username"));
  		}

  		// Set email
  		if(map.containsKey("email")) {
  			user.getObject().setEmail(map.get("email"));
  		}

  	  	return errors;
  	}

  	protected Site getSite (String siteName) {
  		Site site = potentialSites.get(siteName);
  		if (site == null) {
  			// try database
  			site = siteService.getSiteByName(siteName).getObject();
  			potentialSites.put(siteName, site);
  		}
  		if (site == null) {
  			// its a new one
  			site = siteService.newSite();
  			site.setName(siteName);
  			potentialSites.put(siteName, site);
  		}
  		return site;
  	}
  	
  	protected Period getPeriod (Site site, String periodName) {
  		// make sure Site exists in the map
  		if (!potentialPeriods.containsKey(site)) {
  			potentialPeriods.put(site, new HashMap<String,Period>());
  		}
  		Period period = potentialPeriods.get(site).get(periodName);
  		if (period == null && !site.isTransient()) {
  			// try the database
  			period = getPeriodBySiteAndName (site, periodName);
  	  		potentialPeriods.get(site).put(periodName, period);
  		}
  		if (period == null) {
  			// create a new one
  			period = siteService.newPeriod();
  			period.setSite(site);
  			period.setName(periodName);
  	  		potentialPeriods.get(site).put(periodName, period);
  		}
  		return period;
  	}
  	
	protected Period getPeriodBySiteAndName (Site site, String name) {
		Criteria criteria = Databinder.getHibernateSession().createCriteria(Period.class);
		criteria.add(Restrictions.eq("name", name));
		criteria.add(Restrictions.eq("site", site));
		return (Period) criteria.uniqueResult();
	}
	

	/**
	 * A simple object used by {@link UserService} for creating a set of Person objects from an uploaded CSV file.
	 * Each instance will either have a valid user object, or a non-empty error saying why.
	 * 
	 * @see UserService#generateUsers(InputStream, Site)
	 * @author jbrookover
	 *
	 */
	@Getter
	@Setter
	public static class PotentialUserSave implements Serializable {

		private static final long serialVersionUID = 1L;

		private IModel<User> user;
		private String error;
		private int line;
		
		public PotentialUserSave(IModel<User> user, String error, int line) {
			this.user = user;
			this.error = error;
			this.line = line;
		}
	}
}
