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
package org.cast.cwm.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * <p>
 * The type of content that a {@link Response} object represents and, therefore, a 
 * {@link ResponseData} object holds.  This will indicate what format the content 
 * is in, when it should be used, and whether certain extendible fields should be 
 * accessed (e.g. binary data).
 * 
 * TODO: There's too much overlap between PromptTypes and ResponseTypes.  The typing
 * should go in the prompt (Rating,
 * </p>
 * @author jbrookover
 *
 */
@EqualsAndHashCode
public class ResponseType implements IResponseType {

	@Getter
	private String name;

	@Getter
	private String display;
	
	private static final long serialVersionUID = 1L;

	public ResponseType(String name, String display) {
		this.name = name;
		this.display = display;
	}
	
	@Override
	public String toString() {
		return display;
	}
}
