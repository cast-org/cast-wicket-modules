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
package org.cast.cwm.admin;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.string.StringValueConversionException;
import org.cast.cwm.data.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.generationjava.io.CsvWriter;

/**
 * A CSV-formatted downloadable dump of data.
 *
 */
class CSVDownload extends AbstractResource {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(CSVDownload.class);

	protected List<IDataColumn> columns;
	protected IDataProvider<Event> dataProvider;

	/**
	 * Configure a download with a given data provider and set of columns
	 * @param columns
	 * @param dataProvider
	 */
	CSVDownload (List<IDataColumn> columns, IDataProvider<Event> dataProvider) {
		super();
		this.columns = columns;
		this.dataProvider = dataProvider;
	}

	/**
	 * creates a new resource response based on the request attributes
	 * 
	 * @param attributes
	 *            current request attributes from client
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
						CsvWriter writer = new CsvWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
						
						// Write header row
						for (IDataColumn col : columns) {
							writer.writeField(col.getHeaderString());
						}
						writer.endBlock();
						
						// Write Data
						// DataProvider doesn't have a concept of "unlimited".  Hopefully 
						// a million event records is enough
						Iterator<? extends Event> it = dataProvider.iterator(0, 1000000); 
						while (it.hasNext()) {
							Event e = it.next();
							for (IDataColumn col : columns) {
								String columnValue = col.getItemString(Model.of(e)); // does this need to be a HibernateObjectModel?  That makes it hard to test.
								if (columnValue==null) {
									log.warn("Got a null value for {} of event {}", col.getHeaderString(), e.getId());
									columnValue="null";
								}
								// Clean up text -- CSV file cannot have newlines in it
								writer.writeField(columnValue.replaceAll("[\r\n]", " "));
							}
							writer.endBlock();
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