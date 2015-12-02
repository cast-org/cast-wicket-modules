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
package org.cast.cwm.admin;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.string.StringValueConversionException;
import org.cast.cwm.data.provider.IteratorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

/**
 * A CSV-formatted downloadable dump of data.
 *
 * Contains a row for the column headers followed by the data rows.
 * If "includeDocumentationRow" is set to true, then an additional row
 * will be generated with documentation strings for any columns that implement IDocumentedColumn.
 *
 */
public class CSVDownload<E extends Serializable> extends AbstractResource {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(CSVDownload.class);

	@Getter
	@Setter
	protected boolean includeDocumentationRow = false;

	protected List<IDataColumn<E>> columns;
	private IteratorProvider<? extends E> iteratorProvider;

	/**
	 * Configure a download with a given iterator provider and set of columns
	 *
	 * @param columns          list of data columns
	 * @param iteratorProvider class that will supply the iterator
	 */
	public CSVDownload(final List<IDataColumn<E>> columns, final IteratorProvider<E> iteratorProvider) {
		super();
		this.columns = columns;
		this.iteratorProvider = iteratorProvider;
	}

	/**
	 * Configure a download with a given data provider and set of columns
	 *
	 * @param columns      list of data columns
	 * @param dataProvider data provider holding the query
	 */
	public CSVDownload(final List<IDataColumn<E>> columns, final IDataProvider<E> dataProvider) {
		this(columns, new IteratorProvider<E>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Iterator<? extends E> getIterator() {
				return dataProvider.iterator(0, Long.MAX_VALUE);
			}
		});
	}

	/**
	 * creates a new resource response based on the request attributes
	 *
	 * @param attributes current request attributes from client
	 * @return resource response for answering request
	 */
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		ResourceResponse rr = new ResourceResponse();
		rr.disableCaching();
		rr.setFileName("log.csv");
		rr.setContentDisposition(ContentDisposition.ATTACHMENT);
		rr.setContentType("text/csv");

		if (rr.dataNeedsToBeWritten(attributes)) {
			rr.setWriteCallback(new WriteCallback() {
				@Override
				public void writeData(Attributes attributes) {
					Response response = attributes.getResponse();

					try {
						CSVPrinter writer = new CSVPrinter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"),
								CSVFormat.EXCEL);

						// Write header row
						for (IDataColumn<E> col : columns) {
							writer.print(col.getHeaderString());
						}
						writer.println();

						// Write documentation row, if requested
						if (includeDocumentationRow) {
							for (IDataColumn<E> col : columns) {
								if (col instanceof IDocumentedColumn
										&& ((IDocumentedColumn) col).getDocumentationModel() != null) {
									writer.print(((IDocumentedColumn) col).getDocumentationModel().getObject());
								} else {
									writer.print("");
								}
							}
							writer.println();
						}

						// Write Data
						Iterator<? extends E> it = iteratorProvider.getIterator();
						while (it.hasNext()) {
							E e = it.next();
							for (IDataColumn<E> col : columns) {
								String columnValue = col.getItemString(new Model<E>(e));
								if (columnValue == null) {
									log.warn("Got a null value for {} of item {}", col.getHeaderString(), e);
									columnValue = "null";
								}
								// Clean up text -- CSV file cannot have newlines in it
								writer.print(columnValue.replaceAll("[\r\n]", " "));
							}
							writer.println();
						}
						writer.close();

					} catch (UnsupportedEncodingException e) {
						throw new StringValueConversionException("UTF-8 translation not supported?!", e);
					} catch (IOException e) {
						throw new WicketRuntimeException("Couldn't write to resource", e);
					}
				}
			});
		}

		return rr;
	}

}