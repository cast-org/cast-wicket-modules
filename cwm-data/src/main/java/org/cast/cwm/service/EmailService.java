/*
 * Copyright 2011 CAST, Inc.
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

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.model.IModel;
import org.cast.cwm.CwmApplication;
import org.cast.cwm.data.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods for sending email messages required by the program.
 *
 */
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);
	
	/**
	 * Substitution Delimiter.  Wrap 'variables' in your Subject and Body with this
	 * string and they will be replaced before the message is sent.
	 * 
	 * @see {@link #substituteVars(String, Map)}
	 * 
	 */
	protected static final String subDelimiter = "@#@";
	
	protected static EmailService instance = new EmailService(); 
	
	private EmailSender emailSender;

	public static EmailService get() {
		return instance;
	}

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
	public void sendMail (String recipient, String subject, String body, IModel<User> replyToUser, Map<String,String> substitutionVariables) {
		
		EmailMessage message = new EmailMessage();
		message.setFrom(CwmApplication.get().getMailFromAddress());
		message.setTo(recipient);		
		message.setSubject(substituteVars(subject, substitutionVariables));
		message.setBody(substituteVars(body, substitutionVariables));

		if (replyToUser.getObject() != null)
			message.setReplyTo(replyToUser.getObject().getEmail());
		sendMail(message);
		
	}
	
	/**
	 * Send a pre-assembled message.  Simple string substitution will be done on the
	 * body and subject fields of the message.
	 * 
	 * @param message
	 * @param substitutionVariables
	 */
	public void sendMail(EmailMessage message, Map<String, String> substitutionVariables) {
		
		message.setSubject(substituteVars(message.getSubject(), substitutionVariables));
		message.setBody(substituteVars(message.getBody(), substitutionVariables));
	
		sendMail(message);
	}
	
	/**
	 * Send a pre-assembled message.
	 * 
	 * @param message
	 */
	public void sendMail(EmailMessage message) {
		
		if (emailSender == null) {
			log.debug("Starting email sending thread");
			emailSender = new EmailSender(CwmApplication.get().getMailHost());
			emailSender.start();
		}
		
		emailSender.sendQueue.add(message);
	}

	/**
	 * Replace wrapped keys in a string with substitution variables from 
	 * the map.
	 * 
	 * @see #subDelimiter
	 * 
	 * @param text
	 * @param variables
	 * @return
	 */
	protected String substituteVars (String text, Map<String,String> variables) {
		if (variables != null)
			for (Entry<String,String> e : variables.entrySet())
				text = text.replaceAll(subDelimiter + e.getKey() + subDelimiter, e.getValue() !=null ? e.getValue() : "");
		return text;
	}

	/**
	 * A simple container for holding the parts of an Email Message.
	 * 
	 * @author jbrookover
	 *
	 */
	@Getter @Setter
	public static class EmailMessage implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		private String to;
		private String from;
		private String replyTo;
		private String subject;
		private String body;
		
		public String toString() {
			return "[Email " + (replyTo!=null ? replyTo : from) + "->" + to + " (" + subject + ")]";
		}
	}

	/**
	 * A separate Thread for sending email messages.
	 * Allows for web requests to return immediately, rather than waiting a potentially long time for email
	 * delivery to the SMTP server to complete.  Also uses a queue, so that only one email will be sent
	 * at a time to avoid overwhelming the mail server.
	 * 
	 * @author borisgoldowsky
	 *
	 */
	protected class EmailSender extends Thread {

		Properties props = new Properties();

		private BlockingQueue<EmailMessage> sendQueue = new LinkedBlockingQueue<EmailMessage>();

		private final Logger log = LoggerFactory.getLogger(EmailSender.class);
		
		protected EmailSender (String smtpServer) {
			props.put("mail.smtp.host", smtpServer);
		}

		@Override
		public void run() {
			EmailMessage message;
			
			// Loop this forever, waiting for new messages.
			while (true) {
				try {
					message = sendQueue.take(); // block until a message is ready to go.
					sendEmail(message);
				} catch (InterruptedException e) {
					// This is probably fine - shut down while doing nothing?
					log.warn("Interrupted while waiting for email");
				}
			}
		}

		// Attempt to deliver an email.
		private boolean sendEmail (EmailMessage message) {
			try {
				// create some properties and get the default javax.mail.Session
				Session session = Session.getDefaultInstance(props, null);

				Message msg = new MimeMessage(session);
				msg.setFrom(new InternetAddress(message.from));
				msg.setRecipient(Message.RecipientType.TO, new InternetAddress(message.to));
				msg.setSubject(message.subject);
				msg.setContent(message.body, "text/plain");

				if (message.replyTo != null) {
					InternetAddress[] addresses = new InternetAddress[1];
					addresses[0] = new InternetAddress(message.replyTo);
					msg.setReplyTo(addresses);
				}

				Transport.send(msg);
				
			} catch (MessagingException e) {
				log.error("Failed to send email {}", message);
				e.printStackTrace();
				return false;
			}
			log.info ("Sent email {}", message);
			return true;
		}
	}
}
