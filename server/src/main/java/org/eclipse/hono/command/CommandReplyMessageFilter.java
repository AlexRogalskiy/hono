/**
 * Copyright (c) 2016 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bosch Software Innovations GmbH - initial creation
 *
 */

package org.eclipse.hono.command;

import org.apache.qpid.proton.message.Message;
import org.eclipse.hono.util.BaseMessageFilter;
import org.eclipse.hono.util.MessageHelper;
import org.eclipse.hono.util.RegistrationConstants;
import org.eclipse.hono.util.ResourceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A filter for verifying the format of command reply messages.
 */
public final class CommandReplyMessageFilter extends BaseMessageFilter {

    private static final Logger LOG = LoggerFactory.getLogger(CommandReplyMessageFilter.class);

    private CommandReplyMessageFilter() {
        // prevent instantiation
    }

    /**
     * Checks whether a given command reply message contains all required properties.
     * <p>
     * For successful verification, the message must contain a <em>device_id</em> in its set
     * of <em>application</em> properties. If the link target contains a <em>deviceId</em> segment
     * then its value must match that of the property as well.
     * </p>
     * <p>
     * After successful verification the following properties are added to the message's <em>annotations</em>:
     * <ul>
     * <li><em>device_id</em> - the ID of the device that reported the data.</li>
     * <li><em>tenant_id</em> - the ID of the tenant as indicated by the link target's second segment.</li>
     * <li><em>resource_id</em> - the full resource path including the endpoint, the tenant and the device ID.</li>
     * </ul>
     *
     * @param linkTarget the link target address to match the command message's properties against.
     * @param msg the message to verify.
     * @return {@code true} if the given message complies with the <em>Command&amp;Control</em> API specification, {@code false}
     *         otherwise.
     */
    public static boolean verify(final ResourceIdentifier linkTarget, final Message msg) {
        if (!verifyStandardProperties(linkTarget, msg)) {
            return false;
        } else if (msg.getCorrelationId() == null) {
            LOG.trace("message has no correlation-id");
            return false;
        } else if (!hasValidStatus(msg)) {
            LOG.trace("message [{}] does not contain valid status property", msg.getMessageId());
            return false;
        } else {
            return true;
        }
    }

    private static boolean hasValidStatus(final Message msg)
    {
        final Integer status = MessageHelper.getApplicationProperty(msg.getApplicationProperties(),
                RegistrationConstants.APP_PROPERTY_STATUS, Integer.class);
        if (status != null) {
            return true;
        } else {
            LOG.trace("message [{}] does not contain valid status property (not a number): {}", msg.getMessageId(), status);
            return false;
        }
    }
}