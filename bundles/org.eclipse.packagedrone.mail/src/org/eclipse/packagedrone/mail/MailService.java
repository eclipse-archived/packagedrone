/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.mail;

/**
 * A service to send e-mails
 */
public interface MailService
{
    /**
     * @param to
     *            the recipient address
     * @param subject
     *            the subject (without prefix)
     * @param text
     *            the plain text content
     * @throws Exception
     *             if anything goes wrong
     */
    public default void sendMessage ( final String to, final String subject, final String text ) throws Exception
    {
        sendMessage ( to, subject, text, null );
    }

    /**
     * @param to
     *            the recipient address
     * @param subject
     *            the subject (without prefix)
     * @param text
     *            the plain text content
     * @param html
     *            optionally the HTML formatted content
     * @throws Exception
     *             if anything goes wrong
     */
    public void sendMessage ( String to, String subject, String text, String html ) throws Exception;

    /**
     * Send a message
     * <p>
     * The content of the message is read from the readable parameter. The
     * method will not close the readable.
     * </p>
     *
     * @param to
     *            the recipient address
     * @param subject
     *            the subject (without prefix)
     * @param readable
     *            the readable providing the content
     * @throws Exception
     *             if anything goes wrong
     */
    public void sendMessage ( String to, String subject, Readable readable ) throws Exception;
}
