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
package org.cast.cwm.glossary;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

import org.cast.cwm.xml.ICacheableModel;
import org.cast.cwm.xml.IXmlPointer;

/**
 * A simple, in-memory implementation of IWritableGlossaryEntry, based on the
 * idea that a glossary entry is defined by an XmlSection.
 * 
 * Applications wishing to store glossary words in a database (for purposes of
 * referencing, etc) should create a different implementation of
 * {@link IGlossaryEntry} and specify it when constructing the Glossary.
 */
public class BaseEntry implements IWritableGlossaryEntry {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	protected String identifier;

	@Getter
	@Setter
	protected String headword;

	@Getter
	@Setter
	protected String sortForm;

	@Getter
	protected Set<String> alternateForms = new HashSet<String>();

	@Getter
	@Setter
	protected String shortDef;

	@Getter
	@Setter
	protected ICacheableModel<? extends IXmlPointer> xmlPointer;

	public void addAlternateForm(String form) {
		alternateForms.add(form);
	}
}
