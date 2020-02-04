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
package org.cast.cwm.service;

import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;

import java.io.Serializable;
import java.util.Map;

/**
 * @author bgoldowsky
 */
public interface IEmailService {
    /**
     * Send a message with the provided fields.  Simple string substitution will be done on
     * the body and subject.
     *
     * @param recipient
     * @param subject
     * @param body
     * @param replyToUser
     * @param substitutionVariables
     */
    void sendMail (String recipient, String subject, String body, IModel<User> replyToUser, Map<String, String> substitutionVariables);

    /**
     * Send a pre-assembled message.  Simple string substitution will be done on the
     * body and subject fields of the message.
     *
     * @param message
     * @param substitutionVariables
     */
    void sendMail(EmailMessage message, Map<String, String> substitutionVariables);

    /**
     * Send a pre-assembled message.
     *
     * @param message
     */
    void sendMail(EmailMessage message);

    /**
     * A simple container for holding the parts of an Email Message.
     *
     * @author jbrookover
     *
     */
    @Getter
    @Setter
    public static class EmailMessage implements Serializable {

        private static final long serialVersionUID = 1L;

        private String to;
        private String from;
        private String replyTo;
        private String subject;
        private String body;

        @Override
        public String toString() {
            return "[Email " + (replyTo!=null ? replyTo : from) + "->" + to + " (" + subject + ")]";
        }
    }
}
