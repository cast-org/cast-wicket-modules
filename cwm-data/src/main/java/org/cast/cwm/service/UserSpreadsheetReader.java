/*
 * Copyright 2011-2014 CAST, Inc.
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

import com.google.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import net.databinder.hib.Databinder;
import net.databinder.models.hib.HibernateObjectModel;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.cwm.db.service.IModelProvider;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.CharacterCodingException;
import java.util.*;

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
	
	@Inject
	private IUserService userService;

	@Inject
	private IModelProvider modelProvider;

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
	private Map<String, Integer> headerMap;


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
		potentialSites = new HashMap<String, Site>();
		potentialPeriods = new HashMap<Site, Map<String, Period>>();

		CSVParser parser;
		try {
			parser = CSVFormat.EXCEL
					.withHeader()
					.withIgnoreEmptyLines()
					.withIgnoreSurroundingSpaces()
					.parse(new InputStreamReader(new BOMInputStream(stream), "UTF-8"));
		} catch (IOException e) {
			log.error(e.getMessage());
			globalError = e.getMessage();
			return false;
		}

		// Make our own secondary mapping of header names to fields, by
		// lowercasing and removing spaces from all header names
		headerMap = parser.getHeaderMap();
		for (String hdr : new HashSet<String>(headerMap.keySet())) {
			String normalized = hdr.toLowerCase().replaceAll("\\s", "");
			if (!normalized.equals(hdr)) {
				headerMap.put(normalized, headerMap.get(hdr));
			}
		}

		if (!headerMap.containsKey("username")) {
			globalError = "Must specify a 'username' column";
			return false;
		} else if (!headerMap.containsKey("password")) {
			globalError = "Must specify a 'password' column.";
			return false;
		} else if (!headerMap.containsKey("type")) {
			globalError = "Must specify a 'type' column.";
			return false;
		} else if (!headerMap.containsKey("firstname")) {
			globalError = "Must specify a 'firstname' column.";
			return false;
		} else if (!headerMap.containsKey("lastname")) {
			globalError = "Must specify a 'lastname' column.";
			return false;
		} else if (!headerMap.containsKey("period")) {
			globalError = "Must specify a 'period' column.";
			return false;
		} else if (defaultSite == null && !headerMap.containsKey("site")) {
			globalError = "Must specify a 'site' column or a default site.";
			return false;
		}

		// Read the CSV file, create Person objects, record error messages, add to PotentialUserSave List
		boolean errors = false; // have errors been encountered?
		try {
			for (CSVRecord record : parser) {

				String messages = ""; // Error Messages for this user

				// Create a transient User Object from imported data
				IModel<User> user = new HibernateObjectModel<User>(userService.getUserClass());
				messages += populateUserObject(user, record);

				// Check database for duplicate username
				if (userService.getByUsername(user.getObject().getUsername()).getObject() != null) {
					messages += "Username " + user.getObject().getUsername() + " already exists in database. \n";
				}

				// Check database for duplicate subjectId
				if (userService.getBySubjectId(user.getObject().getSubjectId()).getObject() != null) {
					messages += "SubjectId " + user.getObject().getSubjectId() + " already exists in database. \n";
				}

				// Check database for duplicate email addresses when an email address exists
				if (user.getObject().getEmail() != null && userService.getByEmail(user.getObject().getEmail()).getObject() != null) {
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
				PotentialUserSave pe = new PotentialUserSave(user, messages, parser.getCurrentLineNumber());
				potentialUsers.add(pe);
				if (!"".equals(messages))
					errors = true;
			}

			// If CSV file has only one line, it is either empty or has unrecognized LF/CR values.
			if (parser.getCurrentLineNumber() == 1) {
	  			potentialUsers.add(new PotentialUserSave(null, "Empty or Corrupted File.  Note: Save as Windows CSV.", 0));
				globalError = "Empty or Corrupted File - LF/CR values may be invalid!";
				throw new CharacterCodingException();
			}

//  		} catch (ArrayIndexOutOfBoundsException e) {
//  			// This can happen if the last row is missing values; Excel doesn't fill them out to the last column
//  			log.error("Caught exception importing line {}: {}", line, e.getClass());
//  			potentialUsers = new ArrayList<PotentialUserSave>();
//  			potentialUsers.add(new PotentialUserSave(null, "Data missing/misaligned. Fatal error. \n", line));
//  			return false;
  		} catch (CharacterCodingException e) {
  			log.error("Empty or Corrupted File - only 1 line found - CR/LF issue?. {}", e.getClass());
  			return false;
  		} catch (Exception e) {
  			e.printStackTrace();
  			log.error("Caught exception importing line {}: {}", parser.getCurrentLineNumber(), e.getClass());
  			potentialUsers = new ArrayList<PotentialUserSave>();
  			potentialUsers.add(new PotentialUserSave(null, "Fatal Error" + e, parser.getCurrentLineNumber()));
  			return false;
		}

  		return (!errors);
	}
	
	/** 
	 * Saves the potential users, periods, and sites.
	 */
	public void save() {
		Session session = Databinder.getHibernateSession();
		
		for (Site site : potentialSites.values()) {
			if (site.isTransient())
				session.save(site);
		}
		
		for (Map<String, Period> map : potentialPeriods.values())
			for (Period period : map.values()) {
				if (period.isTransient()) {
					session.save(period);
					siteService.onPeriodCreated(modelProvider.modelOf(period));
				}
			}

		for(PotentialUserSave potentialUser : potentialUsers) {
			IModel<User> mUser = potentialUser.getUser();
			User u = mUser.getObject();
			u.setValid(true);
			u.setCreateDate(new Date());
			session.save(u);
			userService.onUserCreated(mUser, null);
		}

		cwmService.flushChanges();
	}

	/**
	 * Adds a record of values to a User object.
	 * 
	 * @param mUser a model of an empty, transient User object
	 * @param record a record of fields that will populate the User object
	 * @return a string of errors (empty string if none)
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
  	private String populateUserObject(IModel<User> mUser, CSVRecord record)
  		throws IOException, InstantiationException, IllegalAccessException {
  		
  		String errors = "";
  		Site site =  null;
  		
  		// Determine site
		if (notEmpty(record, "site")) {
			site = getSite(get(record, "site"));
  		} else if (defaultSite != null && defaultSite.getObject() != null) {
  			site = defaultSite.getObject();
  		} else {
  			errors += "Must specify site. \n";
  		}
  		
  		// Determine period(s)
		User u = mUser.getObject();
		if (notEmpty(record, "period")) {
  			for (String periodName : get(record, "period").split(","))
  				u.getPeriods().add(getPeriod(site, periodName.trim()));
  		} else if (defaultPeriod != null && defaultPeriod.getObject() != null) {
  			u.getPeriods().add(defaultPeriod.getObject());
  		} else {
  			errors += "Must specify period. \n";
  		}
  		
  		// Set Names
		if (notEmpty(record, "firstname"))
  			u.setFirstName(get(record, "firstname"));
  		else
  			errors += "Must specify \"firstname.\" \n";
		if (notEmpty(record, "lastname"))
  			u.setLastName(get(record, "lastname"));
  		else
  			errors += "Must specify \"lastname.\" \n";

  		// Set Permission
  		boolean permission = false;
		if (notEmpty(record, "permission")) {
			String value = get(record, "permission").trim().toLowerCase();
			if (value.equals("true") || value.equals("1") ) {
				permission = true;
			}
  		}
		u.setPermission(permission);

  		
  		// Set Type
  		if(notEmpty(record, "type")) {
  			String type = get(record, "type").toLowerCase();
  			if (Role.forRoleString(type) != null)
  				u.setRole(Role.forRoleString(type));
  			else if (type.startsWith("t"))
  				u.setRole(Role.TEACHER);
  			else
  				u.setRole(Role.STUDENT);
  		} else {
  			errors += "Must specify mUser type. \n";
  			u.setRole(null);
  		}
  		
  		// Set Password
		if(notEmpty(record, "password")) {
  			u.setPassword(get(record, "password"));
  		} else {
  			errors += "Must specify password. \n";
  			u.setPassword("");
  		}
 
  		// Set Username
		if(notEmpty(record, "username")) {
  			u.setUsername(get(record, "username"));
  		} else {
  			errors += "Must specify username. \n";
  			u.setUsername("");
  		}
  		
  		// Set SubjectId (Default to Username)
		if(notEmpty(record, "subjectid")) {
  			u.setSubjectId(get(record, "subjectid"));
  		} else {
  			u.setSubjectId(get(record, "username"));
  		}

  		// Set email
		if(notEmpty(record, "email")) {
  			u.setEmail(get(record, "email"));
  		}

  	  	return errors;
  	}

	// Determine whether the named field has a real, non-empty value.
	private boolean notEmpty(CSVRecord record, String fieldname) {
		Integer position = headerMap.get(fieldname);
		if (position == null)
			return false;
		String value = record.get(position);
		return (value != null && ! value.isEmpty());
	}

	// Get the value for the named field.
	private String get(CSVRecord record, String fieldname) {
		return record.get(headerMap.get(fieldname));
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
	 * A simple object used for creating a set of Person objects from an uploaded CSV file.
	 * Each instance will either have a valid user object, or a non-empty error saying why.
	 * 
	 * @author jbrookover
	 *
	 */
	@Getter
	@Setter
	public static class PotentialUserSave implements Serializable {

		private static final long serialVersionUID = 1L;

		private IModel<User> user;
		private String error;
		private long line;
		
		public PotentialUserSave(IModel<User> user, String error, long line) {
			this.user = user;
			this.error = error;
			this.line = line;
		}
	}
}
