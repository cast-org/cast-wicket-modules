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
import lombok.extern.slf4j.Slf4j;
import net.databinder.hib.Databinder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.wicket.Component;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.cwm.db.service.IDBService;
import org.cwm.db.service.IModelProvider;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

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
@Slf4j
public class UserSpreadsheetReader implements Serializable, ISpreadsheetReader {
	
	@Inject
	private ICwmService cwmService;
	
	@Inject
	private ISiteService siteService;
	
	@Inject
	private IUserService userService;

	@Inject
	private IModelProvider modelProvider;

	@Inject
	private IDBService dbService;

	@Getter
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
	 * @param stream the input stream of CSV data
	 * @return true if no errors encountered.
	 */
	@Override
	public boolean readInput(InputStream stream) {
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

		globalError = checkRequiredHeaders(headerMap);
		if (!Strings.isEmpty(globalError))
			return false;

		// Read the CSV file, create PotentialUserSave objects, record error messages, add to potentialUsers List
		try {
			boolean errors = false; // have errors been encountered?
			for (CSVRecord record : parser) {

				try {
					User user = createUserObject(record);
					String messages = populateUserObject(user, record);
					if (Strings.isEmpty(messages))
						messages = validateUser(user);

					// Add a PotentialUserSave to the list.
					potentialUsers.add(new PotentialUserSave(modelProvider.modelOf(user), messages, record));
					if (!Strings.isEmpty(messages))
						errors = true;

				} catch (ArrayIndexOutOfBoundsException e) {
					// This can happen if the last row is missing values; Excel doesn't fill them out to the last column
					log.error("Caught exception importing line {}: {}", parser.getCurrentLineNumber(), e.getClass());
					potentialUsers.add(new PotentialUserSave(null, "Data missing from CSV.\n", record));
					errors = true;
				} catch (Exception e) {
					e.printStackTrace();
					log.error("Caught exception importing line {}: {}", parser.getCurrentLineNumber(), e.getClass());
					potentialUsers.add(new PotentialUserSave(null, "Error: " + e, record));
					errors = true;
				}
			}

			// If CSV file has only one line, it is either empty or has unrecognized LF/CR values.
			if (parser.getCurrentLineNumber() == 1) {
	  			potentialUsers.add(new PotentialUserSave(null, "Empty or Corrupted File.  Note: Save as Windows CSV.", null));
				globalError = "Empty or Corrupted File - LF/CR values may be invalid!";
				throw new CharacterCodingException();
			}
			return (!errors);

  		} catch (CharacterCodingException e) {
  			log.error("Empty or Corrupted File - only 1 line found - CR/LF issue?. {}", e.getClass());
  			return false;
  		}

	}

	/**
	 * Checks that all mandatory columns exist in this spreadsheet
	 * @return error message string; empty string if ok
	 */
	protected String checkRequiredHeaders(Map<String, Integer> map) {
		String message = "";
		for (String field : getRequiredFields()) {
			if (!map.containsKey(field))
				message += String.format("Must include a '%s' column.\n", field);
		}
		if (defaultSite == null && !map.containsKey("site"))
			message += "Must include a 'site' column or a default site.\n";
		return message;
	}

	/**
	 * Return a list of field headers that are always required.
	 * @return list of strings.
	 */
	protected List<String> getRequiredFields() {
		return Arrays.asList("username", "password", "type", "firstname", "lastname", "period");
	}

	/**
	 * Checks that the given User has reasonable values for its fields.
	 * Checks uniqueness within appropriate bounds, including checking against
	 * both the database and the current list of PotentialUsers.
	 * @param user the User object to check
	 * @return error message string; empty if ok
	 */
	protected String validateUser(User user) {
		String messages = "";

		// Check database for duplicate username
		if (userService.getByUsername(user.getUsername()).getObject() != null) {
			messages += "Username " + user.getUsername() + " already exists in database. \n";
		}

		// Check database for duplicate subjectId
		if (userService.getBySubjectId(user.getSubjectId()).getObject() != null) {
			messages += "SubjectId " + user.getSubjectId() + " already exists in database. \n";
		}

		// Check database for duplicate email addresses when an email address exists
		if (user.getEmail() != null && userService.getByEmail(user.getEmail()).getObject() != null) {
			messages += "Email " + user.getEmail() + " already exists in database. \n";
		}
		messages += checkForListDuplicates(user);

		return messages;
	}

	// Check uploaded user list for duplicate username, subjectId, "Full Name"
	protected String checkForListDuplicates(User user) {
		String messages = "";
		for (PotentialUserSave pe : potentialUsers) {
			// Don't attempt to compare to null records (which may be included to mark lines with syntax errors).
			if (pe.getUser() != null && pe.getUser().getObject() != null) {
				User existing = pe.getUser().getObject();
				if (user.getUsername().matches(existing.getUsername())) {
					messages += "Username " + user.getUsername() + " is a duplicate in this list.\n";
				}
				if (user.getSubjectId().matches(existing.getSubjectId())) {
					messages += "SubjectId " + user.getSubjectId() + " is a duplicate in this list.\n";
				}
				if (user.getFullName().matches(existing.getFullName())
						&& user.hasPeriodInCommonWith(existing)) {
					messages += "Full name \'" + user.getFullName() + "\' is duplicated in this list.\n";
				}
			}
		}
		return messages;
	}

	/** 
	 * Saves the potential users, periods, and sites.
	 * @param triggerComponent the component that triggered the save (for logging).
	 */
	@Override
	public void save(Component triggerComponent) {
		for (Site site : potentialSites.values()) {
			if (site.isTransient())
				dbService.save(site);
		}
		
		for (Map<String, Period> map : potentialPeriods.values())
			for (Period period : map.values()) {
				if (period.isTransient()) {
					dbService.save(period);
					siteService.onPeriodCreated(modelProvider.modelOf(period));
				}
			}

		for(PotentialUserSave potentialUser : potentialUsers) {
			IModel<User> mUser = potentialUser.getUser();
			User u = mUser.getObject();
			u.setValid(true);
			u.setCreateDate(new Date());
			dbService.save(u);
			userService.onUserCreated(mUser, null);
		}

		cwmService.flushChanges();
	}

	protected User createUserObject(CSVRecord record) {
		return userService.newUser();
	}

	/**
	 * Adds a record of values to a User object.
	 * 
	 * @param user User object to be filled in
	 * @param record a record of fields that will populate the User object
	 * @return error message as a string if there are errors, empty string if successful
	 */
  	protected String populateUserObject(User user, CSVRecord record) {
  		
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
		if (notEmpty(record, "period")) {
  			for (String periodName : get(record, "period").split(","))
  				user.getPeriods().add(getPeriod(site, periodName.trim()));
  		} else if (defaultPeriod != null && defaultPeriod.getObject() != null) {
  			user.getPeriods().add(defaultPeriod.getObject());
  		} else {
  			errors += "Must specify period. \n";
  		}
  		
  		// Set Names
		if (notEmpty(record, "firstname"))
  			user.setFirstName(get(record, "firstname"));
  		else
  			errors += "Must specify \"firstname.\" \n";
		if (notEmpty(record, "lastname"))
  			user.setLastName(get(record, "lastname"));
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
		user.setPermission(permission);

  		// Set Type
  		if(notEmpty(record, "type")) {
  			String type = get(record, "type").toLowerCase();
  			if (Role.forRoleString(type) != null)
  				user.setRole(Role.forRoleString(type));
  			else if (type.substring(0, 1).toLowerCase().equals("t"))
  				user.setRole(Role.TEACHER);
  			else
  				user.setRole(Role.STUDENT);
  		} else {
  			errors += "Must specify mUser type. \n";
  			user.setRole(null);
  		}
  		
  		// Set Password
		if(notEmpty(record, "password")) {
  			user.setPassword(get(record, "password"));
  		} else {
  			errors += "Must specify password. \n";
  			user.setPassword("");
  		}
 
  		// Set Username
		if(notEmpty(record, "username")) {
  			user.setUsername(get(record, "username"));
  		} else {
  			errors += "Must specify username. \n";
  			user.setUsername("");
  		}
  		
  		// Set SubjectId (Default to Username)
		if(notEmpty(record, "subjectid")) {
  			user.setSubjectId(get(record, "subjectid"));
  		} else {
  			user.setSubjectId(get(record, "username"));
  		}

  		// Set email
		if(notEmpty(record, "email")) {
  			user.setEmail(get(record, "email"));
  		}

  	  	return errors;
  	}

	// Determine whether the named field has a real, non-empty value.
	protected boolean notEmpty(CSVRecord record, String fieldname) {
		Integer position = headerMap.get(fieldname);
		if (position == null)
			return false;
		String value = record.get(position);
		return (value != null && ! value.isEmpty());
	}

	// Get the value for the named field.
	protected String get(CSVRecord record, String fieldname) {
		Integer fieldIndex = headerMap.get(fieldname);
		if (fieldIndex != null)
			return record.get(headerMap.get(fieldname));
		else
			return null;
	}

	// Make sure Site exists in our map of Sites, and return it.
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
			site.setSiteId(siteName);
  			potentialSites.put(siteName, site);
  		}
  		return site;
  	}

	// make sure Period exists in the map, and return it
  	protected Period getPeriod (Site site, String periodName) {
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
			period.setClassId(periodName);
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

	public ISpreadsheetReader setDefaultSite(IModel<Site> mDefaultSite) {
		this.defaultSite = mDefaultSite;
		return this;
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
		private CSVRecord csvRecord;
		
		public PotentialUserSave(IModel<User> user, String error, CSVRecord csvRecord) {
			this.user = user;
			this.error = error;
			this.csvRecord = csvRecord;
		}
	}
}
