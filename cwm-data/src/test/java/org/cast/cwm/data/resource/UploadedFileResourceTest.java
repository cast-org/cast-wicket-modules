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
package org.cast.cwm.data.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTester;
import org.cast.cwm.data.BinaryFileData;
import org.cast.cwm.service.ICwmService;
import org.cast.cwm.test.GuiceInjectedTestApplication;
import org.junit.Before;
import org.junit.Test;

public class UploadedFileResourceTest {

	private WicketTester tester;
	
	private ICwmService cwmService;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void setUp() {
		
		Map<Class<? extends Object>, Object> injectionMap = new HashMap<Class<? extends Object>, Object>();
		
		BinaryFileData sampleBFD = getBinaryFileDataObject();
		IModel<BinaryFileData> mSampleBFD = Model.of(sampleBFD);

		cwmService = mock(ICwmService.class);
		when(cwmService.getById(BinaryFileData.class, 1L)).thenReturn(mSampleBFD);
		injectionMap.put(ICwmService.class, cwmService);
		
		GuiceInjectedTestApplication application = new GuiceInjectedTestApplication(injectionMap);
		tester = new WicketTester(application);
	}
	
	@Test
	public void canRetrieveResource() throws ParseException {
		PageParameters pp = new PageParameters().add("id", 1);
		tester.startResourceReference(new UploadedFileResourceReference(), pp);
		//tester.dumpPage();
		
		verify(cwmService).getById(BinaryFileData.class, 1L);
		tester.assertContains("^abc$");
		assertEquals("Content type is not correct", "text/plain", tester.getLastResponse().getContentType());
		assertEquals("Wrong content disposition", "inline", tester.getContentDispositionFromResponseHeader());
		
		// Should be sent with expiry header at least a day in the future
		Date expires = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'").parse(tester.getLastResponse().getHeader("Expires"));
		Date now = new Date();
		long oneDay = 24*60*60*1000;
		assertTrue("Too short, or no expiry header", expires.getTime() > now.getTime()+oneDay);
	}
	
	@Test
	public void sends404ifNotFound() {
		PageParameters pp = new PageParameters().add("id", 2);
		tester.startResourceReference(new UploadedFileResourceReference(), pp);
		assertEquals("Should send 404 error", 404, tester.getLastResponse().getStatus());
	}
	
	private BinaryFileData getBinaryFileDataObject() {
		BinaryFileData bfd = new BinaryFileData("BFD", "text/plain", new byte[] { 'a', 'b', 'c' });
		return bfd;
	}
}
