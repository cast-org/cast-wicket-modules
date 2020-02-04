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

import com.google.common.base.Strings;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.cast.cwm.IAppConfiguration;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A Thread for sending email messages.
 *
 * Should be set up as a singleton.
 *
 * Looks for several configuation strings:
 *  cwm.mailFrom      (required, the "From" address that email will be sent from)
 *  cwm.mailHost      (required - email server to send through)
 *  cwm.mailPort      (optional, default 25)
 *  cwm.mailLogin     (optional, the username to use for authentication with the mail server)
 *  cwm.mailPassword  (optional, but required if cwm.mailLogin is provided)
 *  cwm.mailProtocol  (optional. For secure SMTP you can set this to "smtps")
 *  cwm.mailStartTLS  (optional, default false.  Set to "true" if the startTLS protocol should be used, eg for gmail)
 *  cwm.mailDebug     (optional, default false. Outputs lots of debug information when communicating with mail server)
 *
 * Allows for web requests to return immediately, rather than waiting a potentially long time for email
 * delivery to the SMTP server to complete.  Also uses a queue, so that only one email will be sent
 * at a time to avoid overwhelming the mail server.
 *
 * @author borisgoldowsky
 *
 */
@Slf4j
public class EmailSender extends Thread implements IEmailSender {

    // Props list that gets sent to transport layer
    private final Properties props = new Properties();

    // Username and password for authentication, if required.
    private final String login;
    private final String password;

    private final InternetAddress defaultFromAddress;

    // Queue of messages waiting to be sent
    private final BlockingQueue<IEmailService.EmailMessage> sendQueue = new LinkedBlockingQueue<>();

    @Inject
    public EmailSender(IAppConfiguration appConfiguration) throws AddressException {
        setDaemon(true);

        Boolean debug = appConfiguration.getBoolean("cwm.mailDebug", false);
        if (debug)
            props.put("mail.debug", "true");

        // Read email configuration settings
        String from = appConfiguration.getString("cwm.mailFrom", null);
        if (from != null)
            defaultFromAddress = new InternetAddress(from);
        else
            defaultFromAddress = null;

        String protocol = appConfiguration.getString("cwm.mailProtocol", "smtp");
        props.put("mail.transport.protocol", protocol);

        String host = appConfiguration.getString("cwm.mailHost", null);
        if (host != null)
            props.put("mail."+protocol+".host", host);
        String port = appConfiguration.getString("cwm.mailPort", null);
        if (port != null)
            props.put("mail."+protocol+".port", port);

        Boolean startTLS = appConfiguration.getBoolean("cwm.mailStartTLS", false);
        if (startTLS)
            props.put("mail."+protocol+".starttls.enable", "true");

        login = appConfiguration.getString("cwm.mailLogin", null);
        password = appConfiguration.getString("cwm.mailPassword", null);
        if (login != null)
            if (password != null) {
                props.put("mail.smtp.auth", "true");
            } else {
                throw new IllegalArgumentException("cwm.mailLogin specified without cwm.mailPassword");
            }
    }

    @Override
    public void run() {
        IEmailService.EmailMessage message;

        // Loops until interrupted, waiting for new messages.
        try {
            do {
                message = sendQueue.take(); // block until a message is ready to go.
                deliver(message);
            } while (!this.isInterrupted());
        } catch (InterruptedException e) {
            // when interrupted, shuts down.
        }
        log.warn("EmailSender thread Interrupted, exiting");
    }

    // Attempt to deliver an email.
    @Override
    public void sendEmail(IEmailService.EmailMessage message) {
        sendQueue.add(message);
        if (!this.isAlive())
            this.start();
    }


    private void deliver(IEmailService.EmailMessage message) {
        log.debug("Trying to send email: {}", message);

        // Email sending session.
        Session session = Session.getInstance(props);

        try {
            Message msg = new MimeMessage(session);
            if (!Strings.isNullOrEmpty(message.getFrom()))
                msg.setFrom(new InternetAddress(message.getFrom()));
            else
                msg.setFrom(defaultFromAddress);
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(message.getTo()));
            msg.setSubject(message.getSubject());
            msg.setContent(message.getBody(), "text/plain");

            if (message.getReplyTo() != null) {
                InternetAddress[] addresses = new InternetAddress[1];
                addresses[0] = new InternetAddress(message.getReplyTo());
                msg.setReplyTo(addresses);
            }
            msg.saveChanges();

            Transport transport = session.getTransport();
            transport.connect(login, password);
            transport.sendMessage(msg, msg.getAllRecipients());
            transport.close();

        } catch (Exception e) {
            log.error("Failed to send email {}", message);
            e.printStackTrace();
            return;
        }
        log.info ("Sent email {}", message);
    }

}
