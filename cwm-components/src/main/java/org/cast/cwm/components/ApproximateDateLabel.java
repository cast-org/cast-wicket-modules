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
package org.cast.cwm.components;

import org.apache.wicket.datetime.DateConverter;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A Label that displays its model-object Date with varying precision depending on how recent it is.
 * This will be a string like one of the following:
 * <ul>
 *     <li><strong>9:43AM Today</strong> (if today)</li>
 *     <li><strong>9:43AM Yesterday</strong> (if yesterday)</li>
 *     <li><strong>March 3</strong> (if this year)</li>
 *     <li><strong>March 3, 2014</strong> (otherwise)</li>
 * </ul>
 *
 */
public class ApproximateDateLabel extends DateLabel {

	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ApproximateDateLabel.class);

	public ApproximateDateLabel(String id, IModel<Date> mDate) {
		super(id, mDate, new ApproximateDateConverter());
	}

	public ApproximateDateLabel(String id, Date date) {
		this(id, Model.of(date));
	}

	protected static class ApproximateDateConverter extends DateConverter {

		private static final long serialVersionUID = 1L;
		
		// For dates that are today
		private static final String TODAY_PATTERN = "h:mma 'Today'";
		private static final String YESTERDAY_PATTERN =  "h:mma 'Yesterday'";
		private static final String THIS_YEAR_PATTERN =  "MMMM d";
		private static final String DEFAULT_PATTERN =  "MMMM d, yyyy";

		public ApproximateDateConverter() {
			super(true);
		}

		@Override
		public String convertToString(Date value, Locale locale) {
            TimeZone zone = getClientTimeZone();
            if (getApplyTimeZoneDifference() && zone != null) {
				log.trace("Converting {} to time zone {}", value, zone);
                // Determine format to use, based on the what day the given date
                // falls on in the client's time zone.
                DateTimeZone ctz = DateTimeZone.forTimeZone(getClientTimeZone());
                DateTime dt = new DateTime(value.getTime(), ctz);
                DateTimeFormatter format = getFormat(dt);
                format = format.withZone(DateTimeZone.forTimeZone(zone));
                return format.print(dt);
            } else {
                // Client time zone is unknown or turned off.
                // Same as above but using default time zone.
				log.trace("No client time zone info (or applyTimeZoneDifference is off), displaying date as is: {}", value);
                DateTime dt = new DateTime(value);
                DateTimeFormatter format = getFormat(dt);
                return format.print(dt);
            }
		}


		protected DateTimeFormatter getFormat(DateTime dt) {
			String pattern;
            // First check if given time is yesterday - might be in a different year...
			DateTime now = new DateTime(dt.getZone());
			if (now.minusDays(1).toDateMidnight().isEqual(dt.toDateMidnight())) {
				pattern = YESTERDAY_PATTERN;
			} else if (dt.getYear() == now.getYear()) {
				if (dt.getDayOfYear() == now.getDayOfYear())
					pattern = TODAY_PATTERN;
				else
					pattern = THIS_YEAR_PATTERN;
			} else {
				pattern = DEFAULT_PATTERN;
			}
			return DateTimeFormat.forPattern(pattern);
		}

		@Override
		public String getDatePattern(Locale locale) {
			throw new RuntimeException ("Shouldn't be called");
		}

		@Override
		protected DateTimeFormatter getFormat(Locale locale) {
			throw new RuntimeException ("Shouldn't be called");
		}
	}	
}