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
package org.cast.cwm.data.behavior;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.TextRequestHandler;
import org.apache.wicket.util.upload.*;
import org.cast.cwm.data.BinaryFileData;
import org.cast.cwm.data.UserContent;
import org.cast.cwm.service.ICwmService;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.util.List;

/**
 * Creates an AJAX callback URL for posting mp3 audio data.
 * The given UserContent object will be updated with the posted audio.
 * Audio is expected as a base64-encoded form post, which is the way the CFW audio recorder sends it.
 *
 * @author bgoldowsky
 */
@Slf4j
public class AudioUploadBehavior<T extends UserContent> extends AbstractAjaxBehavior {

    @Inject
    ICwmService cwmService;

    private IModel<T> mContent;

    public AudioUploadBehavior(IModel<T> mContent) {
        super();
        this.mContent = mContent;
    }

    @Override
    public void onRequest() {
        HttpServletRequest req = ((ServletWebRequest)getComponent().getRequest()).getContainerRequest();
        FileUpload upload = new FileUpload(new DiskFileItemFactory(Application.get()
                .getResourceSettings()
                .getFileCleaner()));
        try {
            boolean saved = false;
            List<FileItem> fileItems = upload.parseRequest(new ServletRequestContext(req));
            for (FileItem item : fileItems) {
                if (item.getFieldName().equals("data")) {
                    String base64data = item.getString();
                    String expectedPrefix = "data:audio/mp3;base64,";
                    if (base64data.startsWith(expectedPrefix)) {
                        byte[] data = DatatypeConverter.parseBase64Binary(base64data.substring(expectedPrefix.length()));
                        log.debug("Saving audio data: {}", item);
                        // Note, recorder reports audio/mp3 mime type, but audio/mpeg is more standard
                        BinaryFileData bfd = new BinaryFileData("audio data", "audio/mpeg", data);
                        mContent.getObject().setPrimaryFile(bfd);
                        cwmService.flushChanges();
                        saved = true;
                    } else {
                        log.error("Uploaded audio data does not start with expected prefix");
                    }
                }
            }
            sendResponse(saved);
        } catch (FileUploadException e) {
            e.printStackTrace();
            sendResponse(false);
        }
    }

    private void sendResponse(boolean success) {
        RequestCycle.get().scheduleRequestHandlerAfterCurrent(
                new TextRequestHandler("text/plain", "UTF-8",
                        success ? "OK" : "ERROR"));
    }

    @Override
    public void detach(Component component) {
        super.detach(component);
        mContent.detach();
    }

}
