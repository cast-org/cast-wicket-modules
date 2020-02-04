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
package org.cast.cwm;

import org.apache.wicket.util.watch.IModifiable;

import java.io.InputStream;
import java.io.Serializable;

/**
 * An object that can provide an InputStream on request,
 * and an indication of when the data backing that stream was last modified.
 * 
 * Objects of this sort are serializable and can be passed and stored as data sources.
 */
public interface IInputStreamProvider extends IModifiable, Serializable {
	
	public abstract InputStream getInputStream() throws InputStreamNotFoundException;

}
