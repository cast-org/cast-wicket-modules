/*
 * Copyright 2011-2018 CAST, Inc.
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.jcip.annotations.NotThreadSafe;

import java.util.Date;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@NotThreadSafe
public class Mp3CacheTest {

    private Mp3Cache cache;

    private byte[] data = new byte[] { 1, 2, 3 };

    @Before
    public void setUp() {
        cache = new Mp3Cache();
    }

    @After
    public void tearDown() {
        cache.clear();
    }

    @Test
    public void canStoreAndRetrieve() {
        cache.storeInCache(1L, data);
        assertNotNull("Date didn't get set", cache.getCachedDate(1L));
        assertEquals("Cache value doesn't match", data, cache.getCachedMp3(1L));
    }

    @Test
    public void getNullForNewId() {
        cache.storeInCache(1L, data);
        assertNull("Should be null if not previously stored", cache.getCachedDate(2L));
    }

    @Test // FIXME this fails occasionally
    public void blocksForSecondRead() throws Exception {
        // These will accept the values from the secondary thread.
        final Exchanger<Exception> exceptionExchanger = new Exchanger<Exception>();
        final Exchanger<Date> secondDateExchanger = new Exchanger<Date>();
        final Exchanger<byte[]> secondMp3Exchanger = new Exchanger<byte[]>();

        // Primary thread looks up item, it won't be there, but lock will be set.
        Date firstDate = cache.getCachedDate(1L);
        assertNull(firstDate);

        // Secondary thread should block until data can be retrieved.
        new Thread() {
            @Override
            public void run() {
                Exception thrown = null;
                try {
                    Date secondDate = cache.getCachedDate(1L);
                    secondDateExchanger.exchange(secondDate);
                    byte[] secondMp3 = cache.getCachedMp3(1L);
                    secondMp3Exchanger.exchange(secondMp3);
                } catch (Exception e) {
                    thrown = e;
                }
                try {
                    exceptionExchanger.exchange(thrown);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        // Wait a second for second thread to attempt to read value
        // This simulates time taken for mp3 conversion
        Thread.sleep(1000);

        // Now store a value into cache
        firstDate = cache.storeInCache(1L, data);
        assertNotNull(firstDate);

        // Other thread should have waited and picked up this stored value.
        Date secondDate = secondDateExchanger.exchange(null, 1L, TimeUnit.SECONDS);
        assertEquals("Same date should be returned to both threads", firstDate, secondDate);

        byte[] secondMp3 = secondMp3Exchanger.exchange(null, 1L, TimeUnit.SECONDS);
        assertEquals("Second thread should get mp3 data", data, secondMp3);

        // Make sure other thread didn't hit an exception
        Exception e = exceptionExchanger.exchange(null, 1L, TimeUnit.SECONDS);
        if (e != null)
            throw e;
    }


}
