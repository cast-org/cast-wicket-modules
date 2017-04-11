/*
 * Copyright 2011-2017 CAST, Inc.
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
package org.cast.cwm.data.models;

import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.User;

import java.util.Iterator;
import java.util.List;

/**
 * Model that will display a User's Periods as a comma-separated list.
 * ChainingModel to a model of a user, so that model is detached when this one is.
 * If the user model is null or has a null object, this model will return null.
 *
 * @author bgoldowsky
 */
public class UserPeriodNamesModel extends ChainingModel<String> {

	public UserPeriodNamesModel(IModel<User> mUser) {
		super(mUser);
	}

	@Override
	@SuppressWarnings("unchecked")
	public String getObject() {
		IModel<User> mUser = ((IModel<User>) getTarget());
		if (mUser == null || mUser.getObject() == null)
			return null;

		AppendingStringBuffer output = new AppendingStringBuffer();
		List<Period> list = mUser.getObject().getPeriodsAsList();
		Iterator<Period> listIt = list.listIterator();
		while (listIt.hasNext()) {
			Period p = listIt.next();
			output.append(p.getName());
			if (listIt.hasNext())
				output.append(", ");
		}
		return output.toString();
	}
}
