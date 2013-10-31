package org.cast.cwm.admin;

import java.util.Arrays;

import net.databinder.hib.Databinder;
import net.sf.ehcache.CacheManager;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.Cache;
import org.hibernate.stat.Statistics;

/**
 * A page to show some information about Hibernate's cache, and allow the administrator to clear it.
 * 
 * At some point, we could extend this to show more interesting statistics such as hit rate per cache - both 
 * EHcache and Hibernate are capable of gathering and reporting a great deal of statistical information.
 * 
 * @author bgoldowsky
 *
 */
public class CacheManagementPage extends AdminPage {

	private static final long serialVersionUID = 1L;

	public CacheManagementPage(PageParameters parameters) {
		super(parameters);
		
		Statistics statistics = Databinder.getHibernateSessionFactory().getStatistics();
		
		add(new Label("statisticsEnabled", statistics.isStatisticsEnabled() ? "on" : "off"));
		
		long hitCount = statistics.getSecondLevelCacheHitCount();
		long missCount = statistics.getSecondLevelCacheMissCount();
		long totalCount = hitCount+missCount;

		add(new Label("slcPut", String.valueOf(statistics.getSecondLevelCachePutCount())));
		add(new Label("slcHit", String.valueOf(hitCount)));
		add(new Label("slcMiss", String.valueOf(missCount)));
		add(new Label("slcHitP", String.valueOf(totalCount>0 ? Math.round(100d * hitCount / totalCount) : 0)));
		add(new Label("slcMissP", String.valueOf(totalCount>0 ? Math.round(100d * missCount / totalCount) : 0)));

		CacheManager ehcache = CacheManager.getInstance();
		String[] caches = ehcache.getCacheNames();
		Arrays.sort(caches);
		
		RepeatingView cacheView = new RepeatingView("cacheView");
		add(cacheView);
		for (String name : caches) {
			WebMarkupContainer eView = new WebMarkupContainer(cacheView.newChildId());
			cacheView.add(eView);
			net.sf.ehcache.Cache cache = ehcache.getCache(name);
			int size = cache.getSize();

			eView.add(new Label("name", name));
			eView.add(new Label("items", size > 0 ? String.valueOf(size) : ""));
		}

		add(new ClearCacheLink("clear"));
	
	}
	
	
	private static class ClearCacheLink extends Link<Void> {

		private static final long serialVersionUID = 1L;

		public ClearCacheLink(String id) {
			super(id);
		}

		@Override
		public void onClick() {
			Cache cache = Databinder.getHibernateSessionFactory().getCache();
			cache.evictEntityRegions();
			cache.evictCollectionRegions();
			cache.evictNaturalIdRegions();
			cache.evictDefaultQueryRegion();
			
			getRequestCycle().setResponsePage(CacheManagementPage.class);
		}
		
	}

}
