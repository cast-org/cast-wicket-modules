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
package pagination;

import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTestCase;

import java.util.Iterator;

/**
 * @author bgoldowsky
 */
public class BasePaginationTestCase extends WicketTestCase {

	/**
	 * Provides a view of 1000 items in groups of 10 that can be paged.
	 */
	protected static class LongDataView extends DataView<Long> {

		public LongDataView(String id) {
			super(id, new SequenceDataProvider());
			setItemsPerPage(10);
		}

		@Override
		protected void populateItem(Item<Long> item) {

		}

	}

	// Provides a sequence of 1000 Longs
	protected static class SequenceDataProvider implements IDataProvider<Long> {

		private long size = 1000;

		@Override
		public Iterator<? extends Long> iterator(long first, long count) {
			if (first > 1000)
				return new Iterator<Long>() {
					@Override
					public void remove() {
						throw new RuntimeException("Not implemented");
					}

					@Override
					public boolean hasNext() {
						return false;
					}

					@Override
					public Long next() {
						return null;
					}
				};
			if (first + count < size)
				return new SequenceIterator(first, count);
			else
				return new SequenceIterator(first, size - first);
		}

		@Override
		public long size() {
			return size;
		}

		@Override
		public IModel<Long> model(Long object) {
			return Model.of(object);
		}

		@Override
		public void detach() {
		}
	}

	protected static class SequenceIterator extends UnmodifiableIterator<Long> {

		private long index;
		private final long max;

		public SequenceIterator(long first, long count) {
			this.index = first;
			this.max = first + count;
		}

		@Override
		public boolean hasNext() {
			return index<max;
		}

		@Override
		public Long next() {
			return index++;
		}
	}


}
