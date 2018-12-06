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
package org.cast.cwm.components.models;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SingularPluralModelTest {
	
	@Test
	public void passesThroughNullModelObject () {
		Model<Integer> intModel = new Model<>(null);
		SingularPluralModel model = new SingularPluralModel(intModel, "thing", "things");
		assertNull(model.getObject());
	}

	@Test
	public void intOneIsSingular () {
		Model<Integer> intModel = new Model<>(1);
		SingularPluralModel model = new SingularPluralModel(intModel, "thing", "things");
		assertEquals("thing", model.getObject());
	}

	@Test
	public void intTwoIsPlural () {
		Model<Integer> intModel = new Model<>(2);
		SingularPluralModel model = new SingularPluralModel(intModel, "thing", "things");
		assertEquals("things", model.getObject());
	}

	@Test
	public void longOneIsSingular () {
		IModel<Long> delegate = new Model<>(1L);
		SingularPluralModel model = new SingularPluralModel(delegate, "thing", "things");
		assertEquals("thing", model.getObject());
	}

	@Test
	public void longTwoIsPlural () {
		IModel<Long> delegate = new Model<>(2L);
		SingularPluralModel model = new SingularPluralModel(delegate, "thing", "things");
		assertEquals("things", model.getObject());
	}

	@Test
	public void maxLongIsPlural () {
		IModel<Long> delegate = new Model<>(Long.MAX_VALUE);
		SingularPluralModel model = new SingularPluralModel(delegate, "thing", "things");
		assertEquals("things", model.getObject());
	}

	@Test
	public void floatOneIsSingular () {
		IModel<Float> delegate = new Model<>(1F);
		SingularPluralModel model = new SingularPluralModel(delegate, "thing", "things");
		assertEquals("thing", model.getObject());
	}

	@Test
	public void floatTwoIsPlural () {
		IModel<Float> delegate = new Model<>(2F);
		SingularPluralModel model = new SingularPluralModel(delegate, "thing", "things");
		assertEquals("things", model.getObject());
	}

	@Test
	public void doubleOneIsSingular () {
		IModel<Double> delegate = new Model<>(1D);
		SingularPluralModel model = new SingularPluralModel(delegate, "thing", "things");
		assertEquals("thing", model.getObject());
	}

	@Test
	public void doubleTwoIsPlural () {
		IModel<Double> delegate = new Model<>(2D);
		SingularPluralModel model = new SingularPluralModel(delegate, "thing", "things");
		assertEquals("things", model.getObject());
	}

	@Test
	public void maxDoubleIsPlural () {
		IModel<Double> delegate = new Model<>(Double.MAX_VALUE);
		SingularPluralModel model = new SingularPluralModel(delegate, "thing", "things");
		assertEquals("things", model.getObject());
	}

}
