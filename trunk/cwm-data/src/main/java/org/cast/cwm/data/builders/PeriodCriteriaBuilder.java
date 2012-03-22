package org.cast.cwm.data.builders;

import lombok.Getter;
import lombok.Setter;
import net.databinder.models.hib.CriteriaBuilder;

import org.cast.cwm.data.Site;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

/**
 * A simple CriteriaBuilder that locates a Period.
 * 
 * @author lmccormack
 *
 */
@Getter
@Setter
public class PeriodCriteriaBuilder implements CriteriaBuilder {
	
	private static final long serialVersionUID = 1L;
	private String name = null;
	private Site site = null;
	private Integer maxResults = null;	

	public PeriodCriteriaBuilder() {
		
	}
	
	public void build(Criteria criteria) {
		if (name != null)
			criteria.add(Restrictions.eq("name", name));
		if (site != null)
			criteria.add(Restrictions.eq("site", site));
		if (maxResults != null)
			criteria.setMaxResults(maxResults);
		criteria.setCacheable(true);
	}	

}