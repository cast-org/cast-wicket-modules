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
package org.cast.cwm.data.component;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.cast.cwm.data.BinaryFileData;
import org.cast.cwm.test.CwmDataBaseTestCase;
import org.cast.cwm.test.TestIdSetter;
import org.junit.Test;

public class Html5PlayerPanelTest extends CwmDataBaseTestCase {

    private BinaryFileData bfd;

	@Override
	public void setUpData() {
		super.setUpData();
        bfd = new BinaryFileData("test", "audio/wav", new byte[] { 1, 2, 3});
        TestIdSetter.setId(BinaryFileData.class, bfd, 1L);
	}
	
	@Override
	public void populateInjection() {
	}

	@Test
	public void canRender() {
		tester.startComponentInPage(new Html5PlayerPanel("panel", Model.of(bfd)));
		tester.assertComponent("panel", Html5PlayerPanel.class);
	}

    @Test
    public void showsAudioWhenThereIsAudio() {
        tester.startComponentInPage(new Html5PlayerPanel("panel", Model.of(bfd)));
        tester.assertComponent("panel:audio", WebMarkupContainer.class);
        tester.assertVisible("panel:audio");
        tester.assertInvisible("panel:noaudio");
    }

    @Test
    public void showsMessageWhenThereIsNoAudio() {
        bfd.setData(null);
        tester.startComponentInPage(new Html5PlayerPanel("panel", Model.of(bfd)));
        tester.assertInvisible("panel:audio");
        tester.assertComponent("panel:noaudio", WebMarkupContainer.class);
        tester.assertVisible("panel:noaudio");
    }

    @Test
    public void showsSourceLink() {
        tester.startComponentInPage(new Html5PlayerPanel("panel", Model.of(bfd)));
        tester.assertComponent("panel", Html5PlayerPanel.class);
        tester.assertComponent("panel:audio:source", ConvertedMp3AudioSource.class);
        Component source = tester.getComponentFromLastRenderedPage("panel:audio:source");
        tester.assertAttribute("Incorrect mime type", "audio/mpeg", source, "type");
        tester.assertAttribute("Unexpected source/@src attribute",
                "./resource/org.cast.cwm.data.resource.ConvertedMP3DataResourceReference/mp3?id=1",
                source, "src");
    }

}
