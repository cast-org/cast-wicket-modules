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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTester;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.ResponseData;
import org.cast.cwm.service.ICwmService;
import org.cast.cwm.test.GuiceInjectedTestApplication;
import org.junit.Before;
import org.junit.Test;

public class SvgImageResourceTest {

	private WicketTester tester;
	
	ResponseData sampleSvg = getResponseDataObject();
	IModel<ResponseData> mSampleSvg = Model.of(sampleSvg);

	private ICwmService cwmService;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void setUp() {
		
		Map<Class<? extends Object>, Object> injectionMap = new HashMap<Class<? extends Object>, Object>();
		
		cwmService = mock(ICwmService.class);
		when(cwmService.getById(ResponseData.class, 1L)).thenReturn(mSampleSvg);
		injectionMap.put(ICwmService.class, cwmService);
		
		GuiceInjectedTestApplication application = new GuiceInjectedTestApplication(injectionMap);
		tester = new WicketTester(application);
	}
	
	@Test
	public void canRetrieveResource() {
		PageParameters pp = new PageParameters().add("id", 1);
		tester.startResourceReference(new SvgImageResourceReference(), pp);
		//tester.dumpPage();
		verify(cwmService).getById(ResponseData.class, 1L);
		assertEquals("image/svg+xml", tester.getLastResponse().getContentType());
		tester.assertContains(Pattern.quote("<?xml version='1.0' encoding='UTF-8' ?>"));
		tester.assertContains("<svg>");
	}
	
	private ResponseData getResponseDataObject() {
		ResponseData rd = new Response().getNewResponseDataObject();
		rd.setText("<svg></svg>");
		return rd;
	}
}
