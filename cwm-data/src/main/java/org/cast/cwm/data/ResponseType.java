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
package org.cast.cwm.data;

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
public enum ResponseType {
	
	/**
	 * Plain text is stored using {@link ResponseData#ResponseData.setText(String)}.
	 */
	TEXT, 

	/**
	 * Styled HTML text is stored using {@link ResponseData#setText(String)}.
	 */
	HTML, 
	
	/**
	 * Binary audio data is stored using {@link ResponseData#setBinaryFileData(BinaryFileData)}.
	 */
	AUDIO, 
	
	/**
	 * SVG markup is stored using {@link ResponseData#setText(String)}
	 */
	SVG,
	
	/**
	 * Binary data is stored using {@link ResponseData#setBinaryFileData(BinaryFileData)}.
	 */
	UPLOAD,
	
	/**
	 * Highlight colors and word indexes are stored as CSV using {@link ResponseData#ResponseData.setText(String)}.  
	 * For example: "R:1,2,3,5,6,7#Y:22,23,25,26"
	 */
	HIGHLIGHT,
	
	/**
	 * A response to a cloze-type passage (fill in the missing words).  The actual answers
	 * are stored as CSV using {@link ResponseData#ResponseData.setText(String)}.
	 */
	CLOZE, 
	
	/**
	 * A response to a single-select, multiple choice prompt.  Actual answer stored using {@link ResponseData#setText(String)}.
	 */
	SINGLE_SELECT,
	
	/**
	 * Audio recorded via Flash and stored on an external server.  The URL to the Audio
	 * location is stored using {@link ResponseData#setText(String)}.
	 */
	@Deprecated
	FLASH_AUDIO,
	
	/**
	 * A rating (e.g. 1-5).  The value is stored using {@link ResponseData#setScore(int)}
	 */
	STAR_RATING,
	
	/**
	 * A generic score.  
	 * 
	 * TODO: Perhaps this can be used to replace Star Rating and combine Cloze/SingleSelect?
	 */
	SCORE
}
