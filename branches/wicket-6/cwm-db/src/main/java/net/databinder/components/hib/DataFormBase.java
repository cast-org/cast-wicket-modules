/*
 * Databinder: a simple bridge from Wicket to Hibernate
 * Copyright (C) 2008  Nathan Hamblen nathan@technically.us
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.databinder.components.hib;

import net.databinder.hib.Databinder;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;

/**
 * Base class for forms that commit in onSubmit(). This is extended by DataForm, and may be
 * extended directly by client forms when DataForm is not appropriate. Transactions
 * are committed only when no errors are displayed.
 * @author Nathan Hamblen
 */
public class DataFormBase<T> extends Form<T> {
	private Object factoryKey;
	public DataFormBase(String id) {
		super(id);
	}
	public DataFormBase(final String id, IModel<T> model)
	{
		super(id, model);
	}
	
	public Object getFactoryKey() {
		return factoryKey;
	}

	public DataFormBase setFactoryKey(Object key) {
		this.factoryKey = key;
		return this;
	}
	
	protected Session getHibernateSession() {
		return Databinder.getHibernateSession(factoryKey);
	}
	
	/** Default implementation calls {@link #commitTransactionIfValid()}. */
	protected void onSubmit() {
		commitTransactionIfValid();
	}
	
	/**
	 * Commit transaction if no errors are registered for any form component.
	 * @return true if transaction was committed 
	 */
	protected boolean commitTransactionIfValid() {
		try {
			if (!hasError()) {
				Session session = Databinder.getHibernateSession(factoryKey);
				session.flush(); // needed for conv. sessions, harmless otherwise
				onBeforeCommit();
				session.getTransaction().commit();
				session.beginTransaction();
				return true;
			}
		} catch (StaleObjectStateException e) {
			error(getString("version.mismatch", null)); // report error
		}
		return false;
	}
	
	/** Called before committing a transaction by {@link #commitTransactionIfValid()}. */
	protected void onBeforeCommit() { };

}
