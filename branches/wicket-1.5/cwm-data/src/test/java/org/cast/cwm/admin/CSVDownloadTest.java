/*
 * Copyright 2011-2015 CAST, Inc.
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
package org.cast.cwm.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.tester.WicketTester;
import org.cast.cwm.data.Event;
import org.junit.Before;
import org.junit.Test;

public class CSVDownloadTest {

	private WicketTester tester;

	@Before
	public void setUp() {
		tester = new WicketTester();
	}
	
	@Test
	public void oneCellTable() {
		tester.startResource(new CSVDownload<Event>(getDataColumns(1), getDataProvider(1)));
		tester.assertContains("^Column0\ndata\n$");
	}
	
	@Test
	public void twoCellTable() {
		tester.startResource(new CSVDownload<Event>(getDataColumns(1), getDataProvider(2)));
		tester.assertContains("^Column0\ndata\ndata\n$");
	}
	
	@Test
	public void fourCellTable() {
		tester.startResource(new CSVDownload<Event>(getDataColumns(2), getDataProvider(2)));
		tester.assertContains("^Column0,Column1\ndata,data\ndata,data\n$");
	}

	
	private List<IDataColumn<Event>> getDataColumns(int nColumns) {
		List<IDataColumn<Event>> cols = new ArrayList<IDataColumn<Event>>(nColumns);
		for (int i=0; i<nColumns; i++) 
			cols.add(new StringDataColumn("Column" + i));
		return cols;
	}
	
	private IDataProvider<Event> getDataProvider(int nRows) {
		ArrayList<Event> list = new ArrayList<Event>();
		for (int i=0; i<nRows; i++)
			list.add(new Event());
		return new ListDataProvider<Event>(list);
	}
	
	private class StringDataColumn extends AbstractDataColumn<Event> {

		private static final long serialVersionUID = 1L;

		public StringDataColumn(String headerString) {
			super(headerString);
		}

		public String getItemString(IModel<Event> rowModel) {
			return "data";
		}
	}
	
	
	
}
