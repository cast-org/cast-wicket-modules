package net.databinder.models.hib;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

/**
 * Simple CriteriaBuilder implementation that sets the cacheable flag.
 * Other than caching, identical to {@link BasicCriteriaBuilder}.
 * 
 * @author bgoldowsky
 *
 */
public class BasicCacheableCriteriaBuilder extends BasicCriteriaBuilder {

	private static final long serialVersionUID = 1L;

	public BasicCacheableCriteriaBuilder(Criterion... criteria) {
		super(criteria);
	}

	public BasicCacheableCriteriaBuilder(Order order, Criterion... criteria) {
		super(order, criteria);
	}

	@Override
	public void buildUnordered(Criteria criteria) {
		super.buildUnordered(criteria);
		criteria.setCacheable(true);
	}

}
