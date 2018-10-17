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
package org.cast.cwm.service;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.model.IModel;
import org.cast.cwm.data.User;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Methods for sending email messages required by the program.
 *
 */
@Slf4j
public class EmailService implements IEmailService {

    private final IEmailSender sender;

	/**
	 * Substitution Delimiter.  Wrap 'variables' in your Subject and Body with this
	 * string and they will be replaced before the message is sent.
	 * 
	 * See {@link #substituteVars(String, Map)}
	 * 
	 */
	protected static final String subDelimiter = "@#@";

    @Inject
	public EmailService(IEmailSender sender) {
        this.sender = sender;
    }
	
	@Override
	public void sendMail(String recipient, String subject, String body, IModel<User> replyToUser,
						 Map<String, String> substitutionVariables) {
		EmailMessage message = new EmailMessage();
		message.setTo(recipient);
		message.setSubject(substituteVars(subject, substitutionVariables));
		message.setBody(substituteVars(body, substitutionVariables));

		if (replyToUser != null && replyToUser.getObject() != null)
			message.setReplyTo(replyToUser.getObject().getEmail());
		sendMail(message);
	}
	
	@Override
	public void sendMail(EmailMessage message, Map<String, String> substitutionVariables) {
		message.setSubject(substituteVars(message.getSubject(), substitutionVariables));
		message.setBody(substituteVars(message.getBody(), substitutionVariables));
		sendMail(message);
	}
	
	@Override
	public void sendMail(EmailMessage message) {
		sender.sendEmail(message);
	}

	/**
	 * Replace wrapped keys in a string with substitution variables from 
	 * the map.
	 * 
	 * @see #subDelimiter
	 * 
	 * @param text text template
	 * @param variables map of substitution variables
	 * @return final text string
	 */
	protected String substituteVars (String text, Map<String,String> variables) {
		if (variables != null)
			for (Entry<String,String> e : variables.entrySet())
				text = text.replaceAll(subDelimiter + e.getKey() + subDelimiter, e.getValue() !=null ? e.getValue() : "");
		return text;
	}

}
