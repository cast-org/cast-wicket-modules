package net.databinder.models.hib;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingHibernateObjectModel<T> extends HibernateObjectModel<T> {

	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(LoggingHibernateObjectModel.class);

	public LoggingHibernateObjectModel() {
		super();
	}

	public LoggingHibernateObjectModel(Class objectClass,
			CriteriaBuilder criteriaBuilder) {
		super(objectClass, criteriaBuilder);
	}

	public LoggingHibernateObjectModel(Class objectClass, Serializable entityId) {
		super(objectClass, entityId);
	}

	public LoggingHibernateObjectModel(Class objectClass) {
		super(objectClass);
	}

	public LoggingHibernateObjectModel(QueryBuilder queryBuilder) {
		super(queryBuilder);
	}

	public LoggingHibernateObjectModel(String queryString,
			QueryBinder queryBinder) {
		super(queryString, queryBinder);
	}

	public LoggingHibernateObjectModel(T persistentObject) {
		super(persistentObject);
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		log.debug("Attached object for id={}: {}", getObjectId(), this);
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		log.debug("Detached model for id={}", getObjectId());
	}

}
