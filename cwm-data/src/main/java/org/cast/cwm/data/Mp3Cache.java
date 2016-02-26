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
package org.cast.cwm.data;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.BlockingCache;

import java.util.Date;

/**
 * Cache for holding converted mp3 audio.
 *
 * A BlockingCache is used to prevent multiple conversions of the same audio from happening.
 * This means that a "get" will lock that key in the cache, such that later "gets" will block
 * until a value has been "put" into the cache.  The lock will timeout and throw an exception
 * after 30 seconds to prevent the possibility of requests blocking forever.
 *
 * @see org.cast.cwm.data.resource.ConvertedMP3DataResource
 * @see net.sf.ehcache.constructs.blocking.BlockingCache
*/
@Slf4j
public class Mp3Cache {

    private static final String CACHE_NAME = Mp3Cache.class.getName();

    private static final int CACHE_TIMEOUT_MILLIS = 30 * 1000;

    private BlockingCache cache;

    public Mp3Cache() {
        Cache underlyingCache = CacheManager.getInstance().getCache(CACHE_NAME);
        if (underlyingCache == null) {
            log.warn("Could not find configuration [" + CACHE_NAME + "]; using defaults.");
            CacheManager.getInstance().addCache(CACHE_NAME);
            underlyingCache = CacheManager.getInstance().getCache(CACHE_NAME);
        }
        cache = new BlockingCache(underlyingCache);
        cache.setTimeoutMillis(CACHE_TIMEOUT_MILLIS);
    }

    /**
     * Check for a cached mp3 for the given id; return modification date.
     * If there is no such item, null is returned, and other threads' calls to this method with the
     * same id will block until the first thread has converted and stored the mp3 into the cache.
     * @param id id of the BinaryFileData object with the WAV audio
     * @return date mp3 conversion was cached, or null if not found.
     */
    public Date getCachedDate (Long id) {
        Element element = cache.get(id);
        if (element != null)
            return new Date(element.getLatestOfCreationAndUpdateTime());
        else
            return null;
    }

    /**
     * Check for an mp3 for the given id and return it.
     * If there is no such item, null is returned, and other threads' calls to this method with the
     * same id will block until the first thread has converted and stored the mp3 into the cache.
     * @param id id of the BinaryFileData object with the WAV audio
     * @return byte array for the mp3, or empty byte array if not found.
     */
    public byte[] getCachedMp3 (Long id) {
        Element element = cache.get(id);
        if (element != null)
            return (byte[]) element.getObjectValue();
        else
            return new byte[] {};
    }

    /**
     * Store mp3 data into cache.
     * @param id id of the BinaryFileData object with the WAV audio
     * @param mp3 byte array of mp3 audio data
     * @return the creation/update date of the cache element
     */
    public Date storeInCache (Long id, byte[] mp3) {
        Element element = new Element(id, mp3);
        cache.put(element);
        return new Date(element.getLatestOfCreationAndUpdateTime());
    }

    /**
     * Remove all data from the cache.  Mainly used for testing.
     */
    public void clear () {
        cache.removeAll();
    }

}
