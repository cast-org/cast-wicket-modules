/*
 * Copyright 2011-2018 CAST, Inc.
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
package org.cast.cwm.components;

import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

public class DelimitedRefreshingViewTest {

	private WicketTester tester;

	@Before
	public void setUp() {
		tester = new WicketTester();
	}
	
	@Test
	public void canRender() {
		tester.startComponentInPage(makeView(), makeMarkup());
		tester.assertComponent("view", DelimitedRefreshingView.class);
	}

	@Test
	public void showsAllItems() {
		tester.startComponentInPage(makeView(), makeMarkup());
		tester.assertContains("first item");
		tester.assertContains("second item");
		tester.assertContains("third item");
	}
	
	@Test
	public void includesDefaultDelimiter() {
		tester.startComponentInPage(makeView(), makeMarkup());
		tester.assertContains("first item</span></div> <div");
	}
	
	@Test
	public void includesSettableDelimiter() {
		DelimitedRefreshingView view = makeView();
		view.setDelimiter("DeLimit");
		tester.startComponentInPage(view, makeMarkup());
		tester.assertContains("first item</span></div>DeLimit<div");
	}

	private DelimitedRefreshingView<String> makeView() {
		DelimitedRefreshingView<String> view = new DelimitedRefreshingView<String>("view") {
			@Override
			protected Iterator<IModel<String>> getItemModels() {
				return new ModelIteratorAdapter<String>(Arrays.asList("first item", "second item", "third item")) {
					@Override
					protected IModel<String> model(String object) {
						return Model.of(object);
					}
				};
			}

			@Override
			protected void populateItem(Item item) {
				item.add(new Label("label", item.getModel()));
			}
		};
		return view;
	}

	private IMarkupFragment makeMarkup() {
		return Markup.of("<html><head></head><body><div wicket:id=\"view\"><span wicket:id=\"label\"></span></div></body></html>");
	}

}
