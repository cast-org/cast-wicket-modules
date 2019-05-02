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
package org.cast.cwm.figuration.form;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * @author bgoldowsky
 */
public class FigurationTextFieldTest extends WicketTestCase {

    @Test
    public void canRender() {
        tester.startComponentInPage(makeLabeledField());
        tester.assertComponent("id", FigurationTextField.class);
    }

    @Test
    public void showsLabel() {
        tester.startComponentInPage(makeLabeledField());
        tester.assertLabel("id:label:text", "test label");
    }

    @Test
    public void showsTextField() {
        tester.startComponentInPage(makeLabeledField());
        tester.assertComponent("id:input", TextField.class);
    }

    @Test
    public void feedbackStartsEmpty() {
        tester.startComponentInPage(makeLabeledField());
        tester.assertComponent("id:feedback", ComponentFeedbackPanel.class);
        tester.assertInvisible("id:feedback:feedbackul");
    }

    protected FigurationTextField<String> makeLabeledField() {
        FigurationTextField<String> component = new FigurationTextField<>("id", new Model<>(""));
        component.setLabel(Model.of("test label"));
        return component;
    }

}
