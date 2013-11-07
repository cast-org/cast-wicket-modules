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
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
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
import org.cast.cwm.data.builders.ResponseCriteriaBuilder;
import org.cast.cwm.data.models.PromptModel;
import org.cast.cwm.data.models.ResponseListModel;
import org.cast.cwm.data.models.ResponseModel;
import org.hibernate.Criteria;
import org.hibernate.LockOptions;
import org.hibernate.Session.LockRequest;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.google.inject.Inject;
//import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
//import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;

/**
 * A Hibernate implementation of the service class used to save/load/modify
 * {@link Response} objects.  Eventually, this should be split into Abstract
 * and HibernateImpl classes.
 * 
 * @author jbrookover
 *
 */
public class ResponseService implements IResponseService {

	@Inject
	protected ICwmService cwmService;
	
	@Inject
	private IEventService eventService;

	protected static ResponseService instance;
	
	@Getter @Setter
	protected Class<? extends Response> responseClass = Response.class;

	protected ResponseService() {/* Protected Constructor - use injection */}
	
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
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#newResponse(org.apache.wicket.model.IModel, org.cast.cwm.data.IResponseType, org.apache.wicket.model.IModel)
	 */
	public IModel<Response> newResponse (IModel<User> user, IResponseType type, IModel<? extends Prompt> prompt) {
		Response instance = newResponse();
		instance.setUser(user.getObject());
		instance.setType(type);
		instance.setPrompt(prompt.getObject());
		return new ResponseModel(instance);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#getResponsesForPrompt(org.apache.wicket.model.IModel)
	 */
	@Deprecated
	public IModel<List<Response>> getResponsesForPrompt(IModel<? extends Prompt> p) {
		return getResponsesForPrompt(p, null);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#getResponseForPrompt(org.apache.wicket.model.IModel, org.apache.wicket.model.IModel)
	 */
	public IModel<Response> getResponseForPrompt(IModel<? extends Prompt> p, IModel<User> u) {
		ResponseCriteriaBuilder c = new ResponseCriteriaBuilder();
		c.setPromptModel(p);
		c.setUserModel(u);
		return new ResponseModel(c);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#getResponsesForPrompt(org.apache.wicket.model.IModel, org.apache.wicket.model.IModel)
	 */
	@Deprecated
	public IModel<List<Response>> getResponsesForPrompt(IModel<? extends Prompt> p, IModel<User> u) {
		ResponseCriteriaBuilder c = new ResponseCriteriaBuilder();
		c.setPromptModel(p);
		c.setUserModel(u);
		return new ResponseListModel(c);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#getResponsesForPrompt(org.apache.wicket.model.IModel, java.util.Date, java.util.Date)
	 */
	public IModel<List<Response>> getResponsesForPrompt(IModel<? extends Prompt> p, Date from, Date to) {
		ResponseCriteriaBuilder c = new ResponseCriteriaBuilder();
		c.setPromptModel(p);
		c.setFromDate(from);
		c.setToDate(to);
		return new ResponseListModel(c);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#getResponsesForPeriod(org.apache.wicket.model.IModel, org.apache.wicket.model.IModel)
	 */
	public IModel<List<Response>> getResponsesForPeriod(IModel<? extends Prompt> p, IModel<Period> period) {
		ResponseCriteriaBuilder c = new ResponseCriteriaBuilder();
		c.setPromptModel(p);
		c.setPeriodModel(period);
		return new ResponseListModel(c);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#getResponseProviderForPrompt(org.apache.wicket.model.IModel)
	 */
	public ISortableDataProvider<Response> getResponseProviderForPrompt(IModel<? extends Prompt> p) {
		return getResponseProviderForPrompt(p, null);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#getResponseProviderForPrompt(org.apache.wicket.model.IModel, org.apache.wicket.model.IModel)
	 */
	public ISortableDataProvider<Response> getResponseProviderForPrompt(IModel<? extends Prompt> p, IModel<User> u) {
		ResponseCriteriaBuilder c = new ResponseCriteriaBuilder();
		c.setPromptModel(p);
		c.setUserModel(u);
		return new SortableHibernateProvider<Response>(Response.class, c);
	}
	
	public ISortableDataProvider<Response> getResponseProviderForPromptAndPeriod(IModel<? extends Prompt> p, IModel<Period> mPeriod) {
		ResponseCriteriaBuilder c = new ResponseCriteriaBuilder();
		c.setPromptModel(p);
		c.setPeriodModel(mPeriod);
		return new SortableHibernateProvider<Response>(Response.class, c);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#getResponseCountForPrompt(org.apache.wicket.model.IModel, org.cast.cwm.data.IResponseType, org.apache.wicket.model.IModel)
	 */
	public Long getResponseCountForPrompt(IModel<? extends Prompt> mPrompt, IResponseType type, IModel<? extends User> mUser) {
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
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#getLatestResponseByType(org.apache.wicket.model.IModel, org.cast.cwm.data.IResponseType)
	 */
	public IModel<Response> getLatestResponseByType(IModel<? extends Prompt> p, IResponseType type) {
		ResponseCriteriaBuilder c = new ResponseCriteriaBuilder();
		c.setPromptModel(p);
		c.setResponseType(type);
		c.setMaxResults(1);
		return new ResponseModel(c);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#getLatestResponseByTypeForUser(org.apache.wicket.model.IModel, org.apache.wicket.model.IModel, org.cast.cwm.data.IResponseType)
	 */
	public IModel<Response> getLatestResponseByTypeForUser(IModel<? extends Prompt> p, IModel<User> u, IResponseType type) {
		ResponseCriteriaBuilder c = new ResponseCriteriaBuilder();
		c.setPromptModel(p);
		c.setResponseType(type);
		c.setUserModel(u);
		c.setMaxResults(1);
		return new ResponseModel(c);
	}

	
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#saveTextResponse(org.apache.wicket.model.IModel, java.lang.String, java.lang.String)
	 */
	public void saveTextResponse(IModel<Response> response, String message, String pageName) {
		genericSaveResponse(response, message, null, null, null, null, pageName);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#saveStarRating(org.apache.wicket.model.IModel, int)
	 */
	public void saveStarRating(IModel<Response> mResponse, int score) {
		genericSaveResponse(mResponse, null, score, null, score, null, null);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#saveBinaryResponse(org.apache.wicket.model.IModel, byte[], java.lang.String, java.lang.String, java.lang.String)
	 */
	public void saveBinaryResponse(IModel<Response> r, byte[] bytes, String mimeType, String fileName, String pageName) {
		BinaryFileData bd = new BinaryFileData(fileName, mimeType, bytes);
		genericSaveResponse(r, null, null, null, null, bd, pageName);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#attachBinaryResponse(org.apache.wicket.model.IModel, org.apache.wicket.markup.html.form.upload.FileUpload)
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
		cwmService.flushChanges();
		
		return new HibernateObjectModel<BinaryFileData>(dbFile);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#saveSVGResponse(org.apache.wicket.model.IModel, java.lang.String, java.lang.String)
	 */
	public void saveSVGResponse(IModel<Response> mResponse, String svg, String pageName) {
		saveTextResponse(mResponse, svg, pageName);
	}

	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#saveFlashAudioResponse(org.apache.wicket.model.IModel, java.lang.String, java.lang.String)
	 */
	public void saveFlashAudioResponse(IModel<Response> response, String audioId, String pageName) {
		saveTextResponse(response, audioId, pageName); // Flash audio just saves id in 3rd party server
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#genericSaveResponse(org.apache.wicket.model.IModel, java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.Integer, org.cast.cwm.data.BinaryFileData, java.lang.String)
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
		rd.setEvent(eventService.savePostEvent(true, pageName).getObject());

		// Flush changes to datastore
		cwmService.flushChanges();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#saveResponseWithoutData(org.apache.wicket.model.IModel)
	 */
	public void saveResponseWithoutData (IModel<Response> mResponse) {
		cwmService.confirmDatastoreModel(mResponse);
		Response response = mResponse.getObject();
		// If the Response is new, save it.
		if (response.isTransient()) {
			if (response.getCreateDate() == null)
				response.setCreateDate(new Date());
			Databinder.getHibernateSession().save(response);
		}
		cwmService.flushChanges();
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#setResponseSortOrder(org.apache.wicket.model.IModel, org.apache.wicket.model.IModel, java.lang.Integer)
	 */
	public void setResponseSortOrder(IModel<? extends Prompt> mPrompt, IModel<Response> mResponse, Integer index) {
		
		// DataProvider for Responses for this Prompt.
		// It is assumed that the target Response is included in this list.
		ISortableDataProvider<Response> dataProvider = getResponseProviderForPrompt(mPrompt);
		dataProvider.getSortState().setPropertySortOrder("sortOrder", SortOrder.ASCENDING);
		
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
		
		cwmService.flushChanges();
		
	}
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#deleteResponse(org.apache.wicket.model.IModel)
	 */
	public void deleteResponse(IModel<Response> r) {

		cwmService.confirmDatastoreModel(r);

		// Do nothing for a transient Response object
		if (r.getObject().isTransient())
			return;
				
		// "Delete" this response
		r.getObject().setValid(false);
		
		// Remove any ordering
		r.getObject().setSortOrder(null);
		
		// Flush changes to datastore
		cwmService.flushChanges();
		
		eventService.saveEvent("post:delete", String.valueOf(r.getObject().getId()), null);
	}

	
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#getResponseById(java.lang.Long)
	 */
	public IModel<Response> getResponseById(Long id) {
		return new ResponseModel(id);
	}
	
	/* (non-Javadoc)
	 * @see org.cast.cwm.service.IResponseService#getPromptById(java.lang.Long)
	 */
	public IModel<Prompt> getPromptById(Long id) {
		return new PromptModel(id);
	}
	
}
