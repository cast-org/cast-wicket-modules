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
package org.cast.cwm.service;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.model.Model;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.Site;
import org.cast.cwm.data.User;
import org.cast.cwm.test.CwmDataInjectionTestHelper;
import org.cast.cwm.test.CwmDataTestCase;
import org.cwm.db.service.IDBService;
import org.cwm.db.service.IModelProvider;
import org.cwm.db.service.SimpleModelProvider;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author bgoldowsky
 */
public class UserSpreadsheetReaderTest extends CwmDataTestCase {

	UserSpreadsheetReader reader;
	Site site = new Site();

	@Override
	public void populateInjection(CwmDataInjectionTestHelper helper) {
		super.populateInjection(helper);
		helper.injectUserService(this);
		helper.injectCwmService(this);
		helper.injectMock(IDBService.class);
		helper.injectObject(IModelProvider.class, new SimpleModelProvider());

		// Our mock SiteService will acknowledge one existing site.
		ISiteService siteService = helper.injectMock(ISiteService.class);
		when(siteService.getSiteByName(eq("existing_site"))).thenReturn(Model.of(site));
		when(siteService.newSite()).thenReturn(new Site());
		when(siteService.newPeriod()).thenReturn(new Period());

		//when(siteService.getSiteByName(anyString())).thenReturn(Model.of((Site)null));
	}

	@Override
	public void setUpData() {
		super.setUpData();

	}

	@Test
	public void rejectsEmptyFile() {
		reader = new UserSpreadsheetReader();
		assertFalse(reader.readInput(IOUtils.toInputStream("")));
		assertEquals("Must include a 'username' column.\n"
						+ "Must include a 'password' column.\n"
						+ "Must include a 'type' column.\n"
						+ "Must include a 'firstname' column.\n"
						+ "Must include a 'lastname' column.\n"
						+ "Must include a 'period' column.\n"
						+ "Must include a 'site' column or a default site.\n",
				reader.getGlobalError());
	}

	@Test
	public void acceptsMinimalFile() {
		reader = new UserSpreadsheetReader();
		String minimalFile = "type,username,password,firstname,lastname,period,site\r\n"
				+ "s,user,pwd,first,last,per1,existing_site\r\n";
		assertTrue("should accept this file", reader.readInput(IOUtils.toInputStream(minimalFile)));
		assertEquals("should have no global error", "", reader.getGlobalError());

		assertEquals("should have resulted in 1 potential user", 1, reader.getPotentialUsers().size());
		UserSpreadsheetReader.PotentialUserSave potential = reader.getPotentialUsers().get(0);
		assertEquals("", potential.getError());
		assertEquals(1L, potential.getCsvRecord().getRecordNumber());

		User user = potential.getUser().getObject();
		assertEquals(Role.STUDENT, user.getRole());
		assertEquals("user", user.getUsername());
		assertEquals("first", user.getFirstName());
		assertEquals("last", user.getLastName());
		assertEquals("first last", user.getFullName());
	}

	@Test
	public void acceptsMacFormatCSV() {
		reader = new UserSpreadsheetReader();
		String minimalFile = "type,username,password,firstname,lastname,period,site\r"
				+ "s,user,pwd,first,last,per1,existing_site\r";
		assertTrue("should accept this file", reader.readInput(IOUtils.toInputStream(minimalFile)));
		assertEquals("should have no global error", "", reader.getGlobalError());

		assertEquals("should have resulted in 1 potential user", 1, reader.getPotentialUsers().size());
		UserSpreadsheetReader.PotentialUserSave potential = reader.getPotentialUsers().get(0);
		assertEquals("", potential.getError());
		assertEquals(1L, potential.getCsvRecord().getRecordNumber());

		User user = potential.getUser().getObject();
		assertEquals(Role.STUDENT, user.getRole());
		assertEquals("user", user.getUsername());
		assertEquals("first", user.getFirstName());
		assertEquals("last", user.getLastName());
		assertEquals("first last", user.getFullName());
	}

	@Test
	public void acceptsTwoUserFile() {
		reader = new UserSpreadsheetReader();
		String minimalFile = "type,username,password,firstname,lastname,period,site\r\n"
				+ "S,user1,pwd,first1,last1,per1,existing_site\r\n"
				+ "T,user2,pwd,first2,last2,per1,existing_site\r\n";
		assertTrue("should accept this file", reader.readInput(IOUtils.toInputStream(minimalFile)));
		assertEquals("should have no global error", "", reader.getGlobalError());

		assertEquals("should have resulted in 2 potential users", 2, reader.getPotentialUsers().size());
		UserSpreadsheetReader.PotentialUserSave potential = reader.getPotentialUsers().get(0);
		assertEquals("", potential.getError());
		assertEquals(1L, potential.getCsvRecord().getRecordNumber());

		User user = potential.getUser().getObject();
		assertEquals(Role.STUDENT, user.getRole());
		assertEquals("user1", user.getUsername());
		assertEquals("first1", user.getFirstName());
		assertEquals("last1", user.getLastName());
		assertEquals("first1 last1", user.getFullName());

		potential = reader.getPotentialUsers().get(1);
		assertEquals("", potential.getError());
		assertEquals(2L, potential.getCsvRecord().getRecordNumber());

		user = potential.getUser().getObject();
		assertEquals(Role.TEACHER, user.getRole());
		assertEquals("user2", user.getUsername());
		assertEquals("first2", user.getFirstName());
		assertEquals("last2", user.getLastName());
		assertEquals("first2 last2", user.getFullName());
	}

	@Test
	public void rejectsDuplicateUsername() {
		reader = new UserSpreadsheetReader();
		String minimalFile = "type,username,password,firstname,lastname,period,site\r\n"
				+ "s,user,pwd,first1,last1,per1,existing_site\r\n"
				+ "s,user,pwd,first2,last2,per1,existing_site\r\n";
		assertFalse("Should reject this file", reader.readInput(IOUtils.toInputStream(minimalFile)));
		assertEquals("", reader.getGlobalError());

		assertEquals("should have resulted in 2 potential users", 2, reader.getPotentialUsers().size());
		UserSpreadsheetReader.PotentialUserSave potential = reader.getPotentialUsers().get(0);
		assertEquals("", potential.getError());

		potential = reader.getPotentialUsers().get(1);
		assertEquals("Username user is a duplicate in this list.\n" +
				"SubjectId user is a duplicate in this list.\n",
				potential.getError());
	}

}