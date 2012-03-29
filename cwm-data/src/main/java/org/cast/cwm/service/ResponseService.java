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
package org.cast.cwm.service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import net.databinder.hib.Databinder;
import net.databinder.models.hib.HibernateObjectModel;
import net.databinder.models.hib.SortableHibernateProvider;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.BinaryFileData;
import org.cast.cwm.data.Prompt;
import org.cast.cwm.data.Response;
import org.cast.cwm.data.ResponseData;
import org.cast.cwm.data.ResponseType;
import org.cast.cwm.data.User;
import org.cast.cwm.data.builders.ResponseCriteriaBuilder;
import org.cast.cwm.data.models.PromptModel;
import org.cast.cwm.data.models.ResponseListModel;
import org.cast.cwm.data.models.ResponseModel;
import org.hibernate.Criteria;
import org.hibernate.LockOptions;
import org.hibernate.Session.LockRequest;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * A Hibernate implementation of the service class used to save/load/modify
 * {@link Response} objects.  Eventually, this should be split into Abstract
 * and HibernateImpl classes.
 * 
 * @author jbrookover
 *
 */
public class ResponseService {

	protected static ResponseService instance;
	
	@Getter @Setter
	protected Class<? extends Response> responseClass = Response.class;

	protected ResponseService() { /* Protected Constructor - use ResponseService.get() */};
	
	public static ResponseService get() {
		return instance;
	}

	/**
	 * Use this Service class.  Called in {@link Application#init()}.
	 */
	public static void useAsServiceInstance() {
		ResponseService.instance = new ResponseService();
	}

	/** Create a new Response object of the application's preferred class */
	protected Response newResponse() {
		try {
			return responseClass.newInstance();
		} catch (Exception e) {
			throw new WicketRuntimeException("Could not instantiate response class", e);
		}
	}
	
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
	public IModel<Response> newResponse (IModel<User> user, ResponseType type, IModel<? extends Prompt> prompt) {
		Response instance = newResponse();
		instance.setUser(user.getObject());
		instance.setType(type);
		instance.setPrompt(prompt.getObject());
		return new ResponseModel(instance);
	}
	
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
	public IModel<List<Response>> getResponsesForPrompt(IModel<? extends Prompt> p) {
		return getResponsesForPrompt(p, null);
	}
	
	/**
	 * Get a single response by a given user for a given prompt.  This assumes that only
	 * one such response should exist.  If multiple responses are returned, an exception
	 * will be thrown.  In such cases, use {@link #getResponsesForPrompt(IModel, IModel)}.
	 * 
	 * Note: Deprecated.  You should really use a ISortableProvider.  See
	 * {@link #getResponseProviderForPrompt(IModel, IModel)}.
	 * 
	 * @param p
	 * @param u
	 * @return
	 */
	@Deprecated
	public IModel<Response> getResponseForPrompt(IModel<? extends Prompt> p, IModel<User> u) {
		ResponseCriteriaBuilder c = new ResponseCriteriaBuilder();
		c.setPromptModel(p);
		c.setUserModel(u);
		return new ResponseModel(c);
	}
	
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
	public IModel<List<Response>> getResponsesForPrompt(IModel<? extends Prompt> p, IModel<User> u) {
		ResponseCriteriaBuilder c = new ResponseCriteriaBuilder();
		c.setPromptModel(p);
		c.setUserModel(u);
		return new ResponseListModel(c);
	}
	
	public IModel<List<Response>> getResponsesForPrompt(IModel<? extends Prompt> p, Date from, Date to) {
		ResponseCriteriaBuilder c = new ResponseCriteriaBuilder();
		c.setPromptModel(p);
		c.setFromDate(from);
		c.setToDate(to);
		return new ResponseListModel(c);
	}
	
	public ISortableDataProvider<Response> getResponseProviderForPrompt(IModel<? extends Prompt> p) {
		return getResponseProviderForPrompt(p, null);
	}
	
	public ISortableDataProvider<Response> getResponseProviderForPrompt(IModel<? extends Prompt> p, IModel<User> u) {
		ResponseCriteriaBuilder c = new ResponseCriteriaBuilder();
		c.setPromptModel(p);
		c.setUserModel(u);
		return new SortableHibernateProvider<Response>(Response.class, c);
	}
	
	/**
	 * Get the total number of valid responses for the given prompt.
	 * If a response type is specified, only responses of that type will be counted.
	 * If mUser is specified, then only responses by that user will be counted.
	 * @param mPrompt the prompt for which responses will be counted
	 * @param type the type of responses to count (null to count all)
	 * @param mUser the user whose responses will be counted, or null to count all users.
	 * @return the count
	 */
	public Long getResponseCountForPrompt(IModel<? extends Prompt> mPrompt, ResponseType type, IModel<? extends User> mUser) {
		Criteria c = Databinder.getHibernateSession().createCriteria(Response.class);
		c.add(Restrictions.eq("prompt", mPrompt.getObject()));
		if (type != null)
			c.add(Restrictions.eq("type", type));
		if (mUser != null && mUser.getObject() != null)
			c.add(Restrictions.eq("user", mUser.getObject()));
		c.add(Restrictions.eq("valid", true));
		c.setProjection(Projections.rowCount());
		c.setCacheable(true);
		return (Long) c.uniqueResult();
	}
	
	/**
	 * Get the latest response, from any user, for a given prompt and response type.
	 * 
	 * @param p
	 * @param type
	 * @return
	 */
	public IModel<Response> getLatestResponseByType(IModel<? extends Prompt> p, ResponseType type) {
		ResponseCriteriaBuilder c = new ResponseCriteriaBuilder();
		c.setPromptModel(p);
		c.setResponseType(type);
		c.setMaxResults(1);
		return new ResponseModel(c);
	}

	/**
	 * Get the latest response, from the provided user, for a given prompt and response type.
	 * 
	 * @param p
	 * @param u
	 * @param type
	 * @return
	 */
	public IModel<Response> getLatestResponseByTypeForUser(IModel<? extends Prompt> p, IModel<User> u, ResponseType type) {
		ResponseCriteriaBuilder c = new ResponseCriteriaBuilder();
		c.setPromptModel(p);
		c.setResponseType(type);
		c.setUserModel(u);
		c.setMaxResults(1);
		return new ResponseModel(c);
	}

	
	
	public void saveTextResponse(IModel<Response> response, String message, String pageName) {
		genericSaveResponse(response, message, null, null, null, null, pageName);
	}
	
	public void saveStarRating(IModel<Response> mResponse, int score) {
		genericSaveResponse(mResponse, null, score, null, score, null, null);
	}
	
	public void saveBinaryResponse(IModel<Response> r, byte[] bytes, String mimeType, String fileName, String pageName) {
		BinaryFileData bd = new BinaryFileData(fileName, mimeType, bytes);
		genericSaveResponse(r, null, null, null, null, bd, pageName);
	}
	
	/**
	 * Attach a {@link BinaryFileData
	 * @param mResponse
	 * @param bytes
	 * @param mimeType
	 * @param fileName
	 * @param pageName
	 */
	public IModel<BinaryFileData> attachBinaryResponse(IModel<Response> mResponse, FileUpload file) {

		// If the response is transient, create it
		// Otherwise, grab the 
		if (mResponse.getObject().isTransient()) {
			saveResponseWithoutData(mResponse);
		}
		
		// Lock the Response
		LockRequest lock = Databinder.getHibernateSession().buildLockRequest(LockOptions.UPGRADE);
		lock.lock(mResponse.getObject());

		// Upload file
		BinaryFileData dbFile = new BinaryFileData(file.getClientFileName(), file.getContentType(), file.getBytes());
		Databinder.getHibernateSession().save(dbFile);
		
		// Associate File with Response
		mResponse.getObject().getFiles().add(dbFile);
		
		// Persist Changes
		CwmService.get().flushChanges();
		
		return new HibernateObjectModel<BinaryFileData>(dbFile);
	}

	public void saveSVGResponse(IModel<Response> mResponse, String svg, String pageName) {
		saveTextResponse(mResponse, svg, pageName);
	}

	public void saveFlashAudioResponse(IModel<Response> response, String audioId, String pageName) {
		saveTextResponse(response, audioId, pageName); // Flash audio just saves id in 3rd party server
	}
	
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
	public void genericSaveResponse(IModel<Response> mResponse, String text, Integer score, Integer attempted, Integer total, BinaryFileData bd, String pageName) {		
		
		saveResponseWithoutData (mResponse);

		// Create and associate a new ResponseData object for this response
		// which will be saved via Cascade.ALL.
		ResponseData rd = mResponse.getObject().getNewResponseDataObject();
		if (text != null)
			rd.setText(text);
		if (score != null)
			rd.setScore(score);
		if (attempted != null)
			rd.setAttempted(attempted);
		if (total != null)
			rd.setTotal(total);
		if (bd != null) {
			rd.setBinaryFileData(bd);
		}
		
		// Save (and keep a reference to) an event
		rd.setEvent(EventService.get().savePostEvent(true, pageName).getObject());

		// Flush changes to datastore
		CwmService.get().flushChanges();
	}
	
	public void saveResponseWithoutData (IModel<Response> mResponse) {
		CwmService.get().confirmDatastoreModel(mResponse);
		Response response = mResponse.getObject();
		// If the Response is new, save it.
		if (response.isTransient()) {
			if (response.getCreateDate() == null)
				response.setCreateDate(new Date());
			Databinder.getHibernateSession().save(response);
		}
		CwmService.get().flushChanges();
	}
	
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
	public void setResponseSortOrder(IModel<? extends Prompt> mPrompt, IModel<Response> mResponse, Integer index) {
		
		// DataProvider for Responses for this Prompt.
		// It is assumed that the target Response is included in this list.
		ISortableDataProvider<Response> dataProvider = getResponseProviderForPrompt(mPrompt);
		dataProvider.getSortState().setPropertySortOrder("sortOrder", ISortState.ASCENDING);
		
		if (index == null)
			index = dataProvider.size() - 1;
		
		if (index < 0)
			throw new IllegalArgumentException("Index cannot be negative.");
		
		if (index >= (dataProvider.size()))
			throw new IndexOutOfBoundsException("Index {" + index + "} exceeds the number of Responses {" + dataProvider.size() + "}.");
		
		Iterator<? extends Response> iterator = dataProvider.iterator(0, dataProvider.size());

		int iteratorIndex = 0;
		boolean foundResponse = false;

		while (iterator.hasNext()) {
			Response r = iterator.next();

			// Reached the target response
			if (r.equals(mResponse.getObject()))
				foundResponse = true;
			
			// Overwrite sortOrder; making room for insertion if necessary
			if (iteratorIndex >= index && !foundResponse)
				r.setSortOrder(iteratorIndex + 1);
			else
				r.setSortOrder(iteratorIndex);
			
			iteratorIndex++;
		}
		
		if (!foundResponse)
			throw new IllegalStateException("Unknown behavior - Response was not found in iterator.");
		
		mResponse.getObject().setSortOrder(index);
		
		CwmService.get().flushChanges();
		
	}
	/**
	 * Delete a response from the datastore.  This does not actually remove
	 * any data, but sets {@link Response#setValid(boolean)} to 'false.'
	 *  
	 * @param r the response to be deleted
	 */
	public void deleteResponse(IModel<Response> r) {

		CwmService.get().confirmDatastoreModel(r);

		// Do nothing for a transient Response object
		if (r.getObject().isTransient())
			return;
				
		// "Delete" this response
		r.getObject().setValid(false);
		
		// Remove any ordering
		r.getObject().setSortOrder(null);
		
		// Flush changes to datastore
		CwmService.get().flushChanges();
		
		EventService.get().saveEvent("post:delete", String.valueOf(r.getObject().getId()), null);
	}

	
	
	public IModel<Response> getResponseById(Long id) {
		return new ResponseModel(id);
	}
	
	public IModel<Prompt> getPromptById(Long id) {
		return new PromptModel(id);
	}
	
}
