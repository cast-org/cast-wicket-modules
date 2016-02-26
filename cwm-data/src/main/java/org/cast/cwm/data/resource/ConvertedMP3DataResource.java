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
package org.cast.cwm.data.resource;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.sf.lamejb.impl.std.StreamEncoderWAVImpl;
import net.sf.lamejb.jna.std.MpegMode;
import net.sf.lamejb.std.LameConfig;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.util.time.Time;
import org.cast.cwm.data.BinaryFileData;
import org.cast.cwm.data.Mp3Cache;
import org.cast.cwm.service.ICwmService;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;

/**
 * A Resource that converts an audio BinaryFileData to mp3 then serves it to the browser.
 * Caching is used to avoid converting the same data more than once.
 *
 */
@Slf4j
public class ConvertedMP3DataResource extends AbstractResource {

    @Inject
    private ICwmService cwmService;

    private static Mp3Cache mp3Cache;

    private static final long serialVersionUID = 1L;

    public ConvertedMP3DataResource() {
        super();
        Injector.get().inject(this);
    }

    /**
     * @see org.apache.wicket.request.resource.AbstractResource#newResourceResponse(org.apache.wicket.request.resource.IResource.Attributes)
     */
    @Override
    protected ResourceResponse newResourceResponse(final Attributes attributes) {
        final ResourceResponse response = new ResourceResponse();

        // is this 'final' ok?
        final long id = attributes.getParameters().get("id").toLong();

        BinaryFileData bfd = cwmService.getById(BinaryFileData.class, id).getObject();

        if (bfd == null)
            throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_NOT_FOUND, "Data not found [id=" + id + "]");

        response.setContentType("audio/mpeg");

        response.setLastModified(Time.valueOf(bfd.getLastModified()));

        // Test the BFD for sanity
        if (!"audio/wav".equals(bfd.getMimeType())) {
            log.error("Cannot convert BinaryFileData {} with mime type {} to mp3", id, bfd.getMimeType());
        } else if (bfd.getData() == null) {
            log.warn("Request for mp3 version of empty BinaryFileData {}", id);
        } else {
            // Check if we have cached the converted mp3 data; if not, start conversion.
            Mp3Cache cache = getCache();
            Date cachedDate = cache.getCachedDate(id);

            if (cachedDate != null && cachedDate.after(bfd.getLastModified())) {
                // Cached version exists, and is more recent than BFD's last modified
                log.debug("Returning cached mp3 for BFD {}", id);
                response.setContentLength(cache.getCachedMp3(id).length);
            } else {
                // Cached version doesn't exist, it must be created.
                InputStream inputStream;
                long startTime = System.currentTimeMillis();
                try {
                    inputStream = new BufferedInputStream(new ByteArrayInputStream(bfd.getData()));
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    convert(inputStream, outputStream);
                    byte[] bytes = outputStream.toByteArray();
                    float elapsedTime = (System.currentTimeMillis() - startTime) / 1000f;
                    log.debug(String.format("MP3 conversion of BFD %d: %.2fsec, %d bytes converted to %d bytes",
                            id, elapsedTime, bfd.getData().length, bytes.length));
                    cache.storeInCache(id, bytes);
                    // Set Content-Length header
                    response.setContentLength(bytes.length);
                } catch (Exception e) {
                    // exception will generally result in no audio data being written.
                    e.printStackTrace();
                }
            }
        }

        if (response.dataNeedsToBeWritten(attributes)) {
            response.setWriteCallback(new WriteCallback() {
                @Override
                public void writeData(final Attributes attributes) {
                    // Pull mp3 data from cache and write to response.
                    // Cache will have been filled by code above.
                    attributes.getResponse().write(getCache().getCachedMp3(id));
                }
            });
        }
        return response;
    }

    private void convert(InputStream inputStream, OutputStream outputStream) throws IOException {
        try {
            StreamEncoderWAVImpl encoder = new StreamEncoderWAVImpl(inputStream);
            LameConfig conf = encoder.getLameConfig();

            conf.setInSamplerate(22050);
            conf.setBrate(32);
            conf.setBWriteVbrTag(false);
            conf.setMode(MpegMode.MONO);

            encoder.encode(outputStream);
        } finally {
            inputStream.close();
            outputStream.close();
        }
    }

    Mp3Cache getCache() {
        if (mp3Cache == null)
            mp3Cache = new Mp3Cache();
        return mp3Cache;
    }
}
