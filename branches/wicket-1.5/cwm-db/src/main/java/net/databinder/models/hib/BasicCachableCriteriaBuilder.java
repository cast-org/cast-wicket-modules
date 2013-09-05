package net.databinder.models.hib;

import org.hibernate.Criteria;

/**
 * Simple CriteriaBuilder implementation that sets the cacheable flag.
 * Other than caching, identical to {@link BasicCriteriaBuilder}.
 * 
 * @author bgoldowsky
 *
 */
public class BasicCachableCriteriaBuilder extends BasicCriteriaBuilder {

	private static final long serialVersionUID = 1L;

	@Override
	public void buildUnordered(Criteria criteria) {
		super.buildUnordered(criteria);
		criteria.setCacheable(true);
	}

}
