package org.cast.cwm.components;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.wicket.datetime.DateConverter;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.model.IModel;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ApproximateDateLabel extends DateLabel {

	private static final long serialVersionUID = 1L;

	public ApproximateDateLabel(String id, IModel<Date> mDate) {
		super(id, mDate, new ApproximateDateConverter());
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

		/**
		 * @see org.apache.wicket.util.convert.IConverter#convertToString(java.lang.Object,
		 *      java.util.Locale)
		 */
		@Override
		public String convertToString(Object value, Locale locale)
		{
			DateTime dt = new DateTime(((Date)value).getTime(), getTimeZone());
			DateTimeFormatter format = getFormat(dt);

			if (getApplyTimeZoneDifference())
			{
				TimeZone zone = getClientTimeZone();
				if (zone != null)
				{
					// apply time zone to formatter
					format = format.withZone(DateTimeZone.forTimeZone(zone));
				}
			}
			return format.print(dt);
		}


		protected DateTimeFormatter getFormat(DateTime dt) {
			String pattern;
			DateTime now = new DateTime();
			// First check if it's yesterday - might be in a different year...
			if (now.minusDays(1).toDateMidnight().isEqual(dt.toDateMidnight())) {
				pattern = YESTERDAY_PATTERN;
			} else if (dt.getYear() == now.getYear()) {
				// same year
				if (dt.getDayOfYear() == now.getDayOfYear())
					pattern = TODAY_PATTERN;
				else
					pattern = THIS_YEAR_PATTERN;
			} else {
				pattern = DEFAULT_PATTERN;
			}
			return DateTimeFormat.forPattern(pattern).withLocale(getLocale()).withPivotYear(2000);
		}

		@Override
		public String getDatePattern() {
			throw new RuntimeException ("Shouldn't be called");
		}

		@Override
		protected DateTimeFormatter getFormat() {
			throw new RuntimeException ("Shouldn't be called");
		}
	}	
}