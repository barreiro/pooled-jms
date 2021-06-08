/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.messaginghub.pooled.jms.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import javax.jms.IllegalStateException;
import javax.jms.IllegalStateRuntimeException;
import javax.jms.InvalidClientIDException;
import javax.jms.InvalidClientIDRuntimeException;
import javax.jms.InvalidDestinationException;
import javax.jms.InvalidDestinationRuntimeException;
import javax.jms.InvalidSelectorException;
import javax.jms.InvalidSelectorRuntimeException;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.JMSSecurityException;
import javax.jms.JMSSecurityRuntimeException;
import javax.jms.MessageFormatException;
import javax.jms.MessageFormatRuntimeException;
import javax.jms.MessageNotWriteableException;
import javax.jms.MessageNotWriteableRuntimeException;
import javax.jms.ResourceAllocationException;
import javax.jms.ResourceAllocationRuntimeException;
import javax.jms.TransactionInProgressException;
import javax.jms.TransactionInProgressRuntimeException;
import javax.jms.TransactionRolledBackException;
import javax.jms.TransactionRolledBackRuntimeException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for various utility methods in the Exception support class.
 */
@Timeout(20)
public class JMSExceptionSupportTest {

    private final String ERROR_MESSAGE = "ExpectedErrorMessage";
    private final String CAUSE_MESSAGE = "ExpectedCauseMessage";

    private final IOException NO_MESSAGE_CAUSE = new IOException();
    private final IOException EMPTY_MESSAGE_CAUSE = new IOException("");

    @Test
    public void testCreateAssignsLinkedException() {
        JMSException result = JMSExceptionSupport.create(ERROR_MESSAGE, new IOException(CAUSE_MESSAGE));
        assertNotNull(result.getLinkedException());
    }

    @Test
    public void testCreateUsesCauseIfJMSExceptionPresent() {
        IOException ioe = new IOException("Ignore me", new JMSSecurityException("error"));
        JMSException result = JMSExceptionSupport.create(ERROR_MESSAGE, ioe);
        assertNotNull(result);
        assertTrue(result instanceof JMSSecurityException);
    }

    @Test
    public void testCreateDoesNotFillLinkedExceptionWhenGivenNonExceptionThrowable() {
        JMSException result = JMSExceptionSupport.create(ERROR_MESSAGE, new AssertionError(CAUSE_MESSAGE));
        assertNull(result.getLinkedException());
    }

    @Test
    public void testCreateFillsMessageFromMessageParam() {
        JMSException result = JMSExceptionSupport.create(ERROR_MESSAGE, new IOException(CAUSE_MESSAGE));
        assertEquals(ERROR_MESSAGE, result.getMessage());
    }

    @Test
    public void testCreateFillsMessageFromMCauseessageParamMessage() {
        JMSException result = JMSExceptionSupport.create(new IOException(CAUSE_MESSAGE));
        assertEquals(CAUSE_MESSAGE, result.getMessage());
    }

    @Test
    public void testCreateFillsMessageFromMCauseessageParamToString() {
        JMSException result = JMSExceptionSupport.create(NO_MESSAGE_CAUSE);
        assertEquals(NO_MESSAGE_CAUSE.toString(), result.getMessage());
    }

    @Test
    public void testCreateFillsMessageFromMCauseessageParamToStringWhenMessageIsEmpty() {
        JMSException result = JMSExceptionSupport.create(EMPTY_MESSAGE_CAUSE);
        assertEquals(EMPTY_MESSAGE_CAUSE.toString(), result.getMessage());
    }

    @Test
    public void testCreateFillsMessageFromCauseMessageParamWhenErrorMessageIsNull() {
        JMSException result = JMSExceptionSupport.create(null, new IOException(CAUSE_MESSAGE));
        assertEquals(CAUSE_MESSAGE, result.getMessage());
    }

    @Test
    public void testCreateFillsMessageFromCauseMessageParamWhenErrorMessageIsEmpty() {
        JMSException result = JMSExceptionSupport.create("", new IOException(CAUSE_MESSAGE));
        assertEquals(CAUSE_MESSAGE, result.getMessage());
    }

    @Test
    public void testCreateMessageFormatExceptionFillsMessageFromCauseMessageParamToString() {
        JMSException result = JMSExceptionSupport.createMessageFormatException(NO_MESSAGE_CAUSE);
        assertEquals(NO_MESSAGE_CAUSE.toString(), result.getMessage());
    }

    @Test
    public void testCreateMessageFormatExceptionFillsMessageFromCauseMessageParamToStringWhenMessageIsEmpty() {
        JMSException result = JMSExceptionSupport.createMessageFormatException(EMPTY_MESSAGE_CAUSE);
        assertEquals(EMPTY_MESSAGE_CAUSE.toString(), result.getMessage());
    }

    @Test
    public void testCreateMessageEOFExceptionFillsMessageFromCauseMessageParamToString() {
        JMSException result = JMSExceptionSupport.createMessageEOFException(NO_MESSAGE_CAUSE);
        assertEquals(NO_MESSAGE_CAUSE.toString(), result.getMessage());
    }

    @Test
    public void testCreateMessageEOFExceptionFillsMessageFromCauseMessageParamToStringWhenMessageIsEmpty() {
        JMSException result = JMSExceptionSupport.createMessageEOFException(EMPTY_MESSAGE_CAUSE);
        assertEquals(EMPTY_MESSAGE_CAUSE.toString(), result.getMessage());
    }

    @Test
    public void testCreateMessageFormatExceptionAssignsLinkedException() {
        JMSException result = JMSExceptionSupport.createMessageFormatException(new IOException(CAUSE_MESSAGE));
        assertNotNull(result.getLinkedException());
    }

    @Test
    public void testCreateMessageFormatExceptionDoesNotFillLinkedExceptionWhenGivenNonExceptionThrowable() {
        JMSException result = JMSExceptionSupport.createMessageFormatException(new AssertionError(CAUSE_MESSAGE));
        assertNull(result.getLinkedException());
    }

    @Test
    public void testCreateMessageEOFExceptionAssignsLinkedException() {
        JMSException result = JMSExceptionSupport.createMessageEOFException(new IOException(CAUSE_MESSAGE));
        assertNotNull(result.getLinkedException());
    }

    @Test
    public void testCreateMessageEOFExceptionDoesNotFillLinkedExceptionWhenGivenNonExceptionThrowable() {
        JMSException result = JMSExceptionSupport.createMessageEOFException(new AssertionError(CAUSE_MESSAGE));
        assertNull(result.getLinkedException());
    }

    @Test
    public void testConvertsJMSExceptionToJMSRuntimeException() {
        assertThrows(JMSRuntimeException.class, () -> {
            throw JMSExceptionSupport.createRuntimeException(new JMSException("error"));
        });
    }

    @Test
    public void testConvertsIllegalStateExceptionToIllegalStateRuntimeException() {
        assertThrows(IllegalStateRuntimeException.class, () -> {
            throw JMSExceptionSupport.createRuntimeException(new IllegalStateException("error"));
        });
    }

    @Test
    public void testConvertsInvalidClientIDExceptionToInvalidClientIDRuntimeException() {
        assertThrows(InvalidClientIDRuntimeException.class, () -> {
            throw JMSExceptionSupport.createRuntimeException(new InvalidClientIDException("error"));
        });
    }

    @Test
    public void testConvertsInvalidDestinationExceptionToInvalidDestinationRuntimeException() {
        assertThrows(InvalidDestinationRuntimeException.class, () -> {
            throw JMSExceptionSupport.createRuntimeException(new InvalidDestinationException("error"));
        });
    }

    @Test
    public void testConvertsInvalidSelectorExceptionToInvalidSelectorRuntimeException() {
        assertThrows(InvalidSelectorRuntimeException.class, () -> {
            throw JMSExceptionSupport.createRuntimeException(new InvalidSelectorException("error"));
        });
    }

    @Test
    public void testConvertsJMSSecurityExceptionToJMSSecurityRuntimeException() {
        assertThrows(JMSSecurityRuntimeException.class, () -> {
            throw JMSExceptionSupport.createRuntimeException(new JMSSecurityException("error"));
        });
    }

    @Test
    public void testConvertsMessageFormatExceptionToMessageFormatRuntimeException() {
        assertThrows(MessageFormatRuntimeException.class, () -> {
            throw JMSExceptionSupport.createRuntimeException(new MessageFormatException("error"));
        });
    }

    @Test
    public void testConvertsMessageNotWriteableExceptionToMessageNotWriteableRuntimeException() {
        assertThrows(MessageNotWriteableRuntimeException.class, () -> {
            throw JMSExceptionSupport.createRuntimeException(new MessageNotWriteableException("error"));
        });
    }

    @Test
    public void testConvertsResourceAllocationExceptionToResourceAllocationRuntimeException() {
        assertThrows(ResourceAllocationRuntimeException.class, () -> {
            throw JMSExceptionSupport.createRuntimeException(new ResourceAllocationException("error"));
        });
    }

    @Test
    public void testConvertsTransactionInProgressExceptionToTransactionInProgressRuntimeException() {
        assertThrows(TransactionInProgressRuntimeException.class, () -> {
            throw JMSExceptionSupport.createRuntimeException(new TransactionInProgressException("error"));
        });
    }

    @Test
    public void testConvertsTransactionRolledBackExceptionToTransactionRolledBackRuntimeException() {
        assertThrows(TransactionRolledBackRuntimeException.class, () -> {
            throw JMSExceptionSupport.createRuntimeException(new TransactionRolledBackException("error"));
        });
    }

    @Test
    public void testConvertsNonJMSExceptionToJMSRuntimeException() {
        assertThrows(JMSRuntimeException.class, () -> {
            throw JMSExceptionSupport.createRuntimeException(new IOException());
        });
    }
}
