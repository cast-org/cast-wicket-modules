/*
 * Copyright 2011-2020 CAST, Inc.
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
package org.cast.cwm.service;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.UserContent;

/**
 * An implementation of IUserContentViewerFactory that can display some basic data types.
 *
 */
public class DefaultUserContentViewerFactory implements IUserContentViewerFactory {

	@Override
	public <T extends UserContent> boolean canHandle(IModel<T> mUserContent) {
		return false;
	}

	@Override
	public <T extends UserContent> Component makeContentViewer(String wicketId,
			IModel<T> mUserContent, Integer maxWidth, Integer maxHeight) {
		return null;
	}

}
