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
package org.cast.cwm.data.resource;

import net.sf.lamejb.LameCodecFactory;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTester;
import org.cast.cwm.data.BinaryFileData;
import org.cast.cwm.service.ICwmService;
import org.cast.cwm.test.CwmTestApplication;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ConvertedMP3DataResourceTest {

	private WicketTester tester;

    BinaryFileData sample;
	IModel<BinaryFileData> mSample;

	private ICwmService cwmService;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void setUp() {
        sample = getSampleWavData();
        mSample = Model.of(sample);

        Map<Class<?>, Object> injectionMap = new HashMap<Class<?>, Object>();
		
		cwmService = mock(ICwmService.class);
		when(cwmService.getById(BinaryFileData.class, 1L)).thenReturn(mSample);
		injectionMap.put(ICwmService.class, cwmService);

        CwmTestApplication application = new CwmTestApplication(injectionMap);
		tester = new WicketTester(application);
	}

    @Test
    public void canInstantiateLameEncoder() {
        new LameCodecFactory().createCodec();
    }

	@Test
	public void canRetrieveResource() {
		PageParameters pp = new PageParameters().add("id", 1);
		tester.startResourceReference(new ConvertedMP3DataResourceReference(), pp);
		verify(cwmService).getById(BinaryFileData.class, 1L);
		assertEquals("audio/mpeg", tester.getLastResponse().getContentType());
        byte[] bytes = tester.getLastResponse().getBinaryContent();
        // FF FB is magic number for basic mp3 file
        // FF F3 is mp3 ADTS
        assertEquals("First byte of result", (byte)0xFF, bytes[0]);
        assertEquals("2nd byte of result", (byte)0xF3, bytes[1]);
        assertEquals("expected length of output", 112327, bytes.length);
	}
	
	private BinaryFileData getSampleWavData() {
        // Load sample WAV data
        InputStream wavStream = this.getClass().getClassLoader().getResourceAsStream("sample.wav");
        byte[] data = new byte[1240000];
        int bytes;
        try {
            bytes = wavStream.read(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertEquals("Sample data was not of expected length", 311340, bytes);
        return new BinaryFileData("sample", "audio/wav", data);
	}
}
