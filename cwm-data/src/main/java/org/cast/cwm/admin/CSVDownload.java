/*
 * Copyright 2011 CAST, Inc.
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Locale;

import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.IResourceStreamWriter;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.string.StringValueConversionException;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.data.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.generationjava.io.CsvWriter;

/**
 * A CSV-formatted downloadable dump of data.
 *
 */
class CSVDownload extends WebResource {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(CSVDownload.class);

	protected IDataColumn[] columns;
	protected IDataProvider<Event> dataProvider;

	/**
	 * Configure a download with a given data provider and set of columns
	 * @param columns
	 * @param dataProvider
	 */
	CSVDownload (IDataColumn[] columns, IDataProvider<Event> dataProvider) {
		super();
		this.columns = columns;
		this.dataProvider = dataProvider;
	}

	/**
	 * @see org.apache.wicket.markup.html.WebResource#setHeaders(org.apache.wicket.protocol.http.WebResponse)
	 */
	protected void setHeaders(WebResponse response)
	{
		super.setHeaders(response);
		response.setAttachmentHeader("log.csv");
	}

	@Override
	public IResourceStream getResourceStream() {
		return new CSVResourceStreamWriter();
	}

	private final class CSVResourceStreamWriter implements IResourceStreamWriter {
		private static final long serialVersionUID = 1L;
		
		CsvWriter writer = null;		

		public void write(OutputStream output) {
			try {
				writer = new CsvWriter(new OutputStreamWriter(output, "UTF-8"));
				
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
						String columnValue = col.getItemString(new HibernateObjectModel<Event>(e));
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

		public void close() throws IOException {
			if (writer != null) {
				writer.close();
			}
		}

		public String getContentType() {
			return "text/csv";
		}

		public InputStream getInputStream()
		throws ResourceStreamNotFoundException {
			// OK to return null since we're an IResourceStreamWriter
			return null;
		}

		public Time lastModifiedTime() {
			return Time.now();
		}

		public long length() {
			return -1;  // -1 means unknown length
		}

		public Locale getLocale() {
			// TODO Auto-generated method stub
			return null;
		}

		public void setLocale(Locale locale) {
			// TODO Auto-generated method stub

		}
	}

}