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
package org.cast.cwm;

import java.util.Properties;

import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.type.Type;

/**
 * Used by Hibernate Annotations to generate pooled IDs for 
 * stored objects.  This is simply a method to keep the
 * annotation in data classes to one single line.
 * 
 * @author jbrookover
 *
 */
public class CwmIdGenerator extends SequenceStyleGenerator {
	
	@Override
	public void configure(Type type, Properties params, Dialect dialect) throws MappingException {
		params.put("optimizer", "pooled");
		params.put("increment_size", "10");
		params.put("sequence_name", "cwm_id_sequence");
		super.configure(type, params, dialect);
	}
}
