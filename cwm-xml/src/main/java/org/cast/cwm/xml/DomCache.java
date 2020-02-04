/*
 * Copyright 2011-2020 CAST, Inc.
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
package org.cast.cwm.xml;

import lombok.Data;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.xml.transform.TransformParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * A cache of TransformResult objects.
 * 
 * @author borisgoldowsky
 *
 */
public class DomCache {
	
	
	private static final Logger log = LoggerFactory.getLogger(DomCache.class);
	private static CacheManager manager;
	private static final String CACHE_NAME = DomCache.class.getName();

	public DomCache() {
		manager = CacheManager.getInstance();
		Cache cache = manager.getCache(CACHE_NAME);
        if (cache == null) {
            log.warn("Could not find configuration [" + CACHE_NAME + "]; using defaults.");
            manager.addCache(CACHE_NAME);
        }
	}
	
	protected Ehcache getCache() {
		return manager.getEhcache(CACHE_NAME);		
	}
	
	public Element get (ICacheableModel<? extends IXmlPointer> mXml, String transform, TransformParameters params) {
		DOMKey key = new DOMKey(mXml.getKey(), transform, params);
		Element cacheElt = getCache().get(key);
		return cacheElt;
	}
	
	public void put (ICacheableModel<? extends IXmlPointer> mXml, String transform, TransformResult tr, TransformParameters params) {
		DOMKey key = new DOMKey(mXml.getKey(), transform, params);
		getCache().put(new Element(key, tr));
	}
	
	public Time lastUpdated (ICacheableModel<? extends IXmlPointer> mXml, String transform, TransformParameters params) {
		Element cacheElt = getCache().get(new DOMKey(mXml.getKey(), transform, params));
		long updated = 0;
		if (cacheElt != null) {
			updated = cacheElt.getLastUpdateTime();
			if (updated == 0)
				updated = cacheElt.getCreationTime();
		}
		return (updated == 0 ? null : Time.millis(updated));
	}

	
	/**
	 * A key composed of the model (which uniquely points to some XML) and the registered name of the transformer.
	 * TODO: may be better to use the hashCodes so the cache just has to do string comparison, not call our equals() method.
	 * @author borisgoldowsky
	 *
	 */
	@Data
	protected static class DOMKey implements Serializable {
		private Serializable xmlKey;
		private String transform;
		private TransformParameters params;
		
		private static final long serialVersionUID = 1L;

		public DOMKey (Serializable xmlKey, String transform, TransformParameters params) {
			this.xmlKey = xmlKey;
			this.transform = transform;
			this.params = params;
		}
	}
	
}
