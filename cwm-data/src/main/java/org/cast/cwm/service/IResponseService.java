/*
 * Copyright 2011-2016 CAST, Inc.
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

import java.util.Date;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.BinaryFileData;
import org.cast.cwm.data.IResponseType;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.ResponseData;
import org.cast.cwm.data.User;

public interface IResponseService {

	/** 
	 * get the subclass of Response to be used.
	 * 
	 * @return responseClass
	 */
	Class<? extends Response> getResponseClass();
	
	/** 
	 * set the subclass of Response to be used.
	 * 
	 * @param responseClass
	 */
	IResponseService setResponseClass(Class<? extends Response> responseClass);
	
	/** 
	 * Create a new Response object of the application's preferred class,
	 * starting with the basic information provided, and wrap it in a model that
	 * is supported by the datastore.
	 * 
	 * @param user
	 * @param type
	 * @param prompt
	 * @return 
	 */
	IModel<Response> newResponse(IModel<User> user, IResponseType type,
			IModel<? extends Prompt> prompt);

	/**
	 * Get a list of responses for a given prompt.  This will
	 * include responses from ALL users.  The list is sorted by 
	 * date with the most recent response first.
	 * 
	 * Note: Deprecated.  You should really use a ISortableProvider.  See
	 * {@link #getResponseProviderForPrompt(IModel)}.
	 * 
	 * @param p
	 * @return
	 */
	@Deprecated
	IModel<List<Response>> getResponsesForPrompt(IModel<? extends Prompt> p);

	/**
	 * Get a single response by a given user for a given prompt.  This assumes that only
	 * one such response should exist.  If multiple responses are returned, an exception
	 * will be thrown.  In such cases, use {@link #getResponsesForPrompt(IModel, IModel)},
	 * or preferably, ISortableProvider.  See
	 * {@link #getResponseProviderForPrompt(IModel, IModel)}.
	 * 
	 * @param p
	 * @param u
	 * @return
	 */
	IModel<Response> getResponseForPrompt(IModel<? extends Prompt> p,
			IModel<User> u);

	/**
	 * Get a given user's responses for a given prompt, sorted by date
	 * with the most recent response first.
	 * 
	 * Note: Deprecated.  You should really use a ISortableProvider.  See
	 * {@link #getResponseProviderForPrompt(IModel)}.
	 * 
	 * 
	 * @param p the prompt
	 * @param u the user
	 * @return a list of {@link Response} objects
	 */
	@Deprecated
	IModel<List<Response>> getResponsesForPrompt(IModel<? extends Prompt> p,
			IModel<User> u);

	IModel<List<Response>> getResponsesForPrompt(IModel<? extends Prompt> p,
			Date from, Date to);

	IModel<List<Response>> getResponsesForPeriod(IModel<? extends Prompt> p,
			IModel<Period> period);

	public ISortableDataProvider<Response,String> getResponseProviderForPrompt(IModel<? extends Prompt> p);

	public ISortableDataProvider<Response,String> getResponseProviderForPrompt(IModel<? extends Prompt> p, IModel<User> u);

	public ISortableDataProvider<Response,String> getResponseProviderForPromptAndPeriod(IModel<? extends Prompt> p, IModel<Period> mPeriod);
	
	/**
	 * Get the total number of valid responses for the given prompt.
	 * If a response type is specified, only responses of that type will be counted.
	 * If mUser is specified, then only responses by that user will be counted.
	 * @param mPrompt the prompt for which responses will be counted
	 * @param type the type of responses to count (null to count all)
	 * @param mUser the user whose responses will be counted, or null to count all users.
	 * @return the count
	 */
	Long getResponseCountForPrompt(IModel<? extends Prompt> mPrompt,
			IResponseType type, IModel<? extends User> mUser);

	/**
	 * Get the latest response, from any user, for a given prompt and response type.
	 * 
	 * @param p
	 * @param type
	 * @return
	 */
	IModel<Response> getLatestResponseByType(IModel<? extends Prompt> p,
			IResponseType type);

	/**
	 * Get the latest response, from the provided user, for a given prompt and response type.
	 * 
	 * @param p
	 * @param u
	 * @param type
	 * @return
	 */
	IModel<Response> getLatestResponseByTypeForUser(IModel<? extends Prompt> p,
			IModel<User> u, IResponseType type);

	void saveTextResponse(IModel<Response> response, String message,
			String pageName);

	void saveStarRating(IModel<Response> mResponse, int score);

	void saveBinaryResponse(IModel<Response> r, byte[] bytes, String mimeType,
			String fileName, String pageName);

	/**
	 * Attach a {@link BinaryFileData
	 * @param mResponse
	 * @param bytes
	 * @param mimeType
	 * @param fileName
	 * @param pageName
	 */
	IModel<BinaryFileData> attachBinaryResponse(IModel<Response> mResponse,
			FileUpload file);

	void saveSVGResponse(IModel<Response> mResponse, String svg, String pageName);

	void saveFlashAudioResponse(IModel<Response> response, String audioId,
			String pageName);

	/**
	 * Create a new {@link ResponseData} value for the given {@link Response}.  You can also
	 * use this method to create an entirely new Response by passing in a transient object.
	 * 
	 * @param type
	 * @param prompt
	 * @param user
	 * @param text
	 * @param score
	 * @param total
	 */
	void genericSaveResponse(IModel<Response> mResponse, String text,
			Integer score, Integer attempted, Integer total, BinaryFileData bd,
			String pageName);

	void saveResponseWithoutData(IModel<Response> mResponse);

	/**
	 * Uses {@link Response#setSortOrder(Integer)} to adjust the location of a 
	 * {@link Response} in a list of responses for a given {@link Prompt}.  Adjusts
	 * all affected Response objects whose sortOrder changes.
	 * 
	 * Note 1: This method assumes that the response has already been persisted (and therefore attached
	 * to the prompt).
	 * 
	 * Note 2: This method overrides any existing sortOrder values and re-indexes them
	 * into sequential order.  This erases any drift from 'deleting' Responses, which
	 * does not cascade to the sortOrder and leaves gaps in the sequence.
	 * 
	 * TODO: This has not been extensively tested.
	 * 
	 * @param iterator
	 * @param modelObject
	 * @param index target index; null value appends to the end
	 */
	void setResponseSortOrder(IModel<? extends Prompt> mPrompt,
			IModel<Response> mResponse, Integer index);

	/**
	 * Delete a response from the datastore.  This does not actually remove
	 * any data, but sets {@link Response#setValid(boolean)} to 'false.'
	 *  
	 * @param r the response to be deleted
	 */
	void deleteResponse(IModel<Response> r);

	IModel<Response> getResponseById(Long id);

	IModel<Prompt> getPromptById(Long id);

}