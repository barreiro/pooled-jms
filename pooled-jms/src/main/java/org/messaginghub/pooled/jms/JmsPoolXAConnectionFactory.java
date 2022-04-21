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
package org.messaginghub.pooled.jms;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.XAConnection;
import jakarta.jms.XAConnectionFactory;
import jakarta.jms.XAJMSContext;
import jakarta.transaction.TransactionManager;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.spi.ObjectFactory;

import org.messaginghub.pooled.jms.pool.PooledConnectionKey;
import org.messaginghub.pooled.jms.pool.PooledXAConnection;
import org.messaginghub.pooled.jms.util.IntrospectionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A pooled connection factory that automatically enlists sessions in the
 * current active XA transaction if any.
 */
public class JmsPoolXAConnectionFactory extends JmsPoolConnectionFactory implements ObjectFactory, Serializable, XAConnectionFactory {

    private static final transient Logger LOG = LoggerFactory.getLogger(JmsPoolXAConnectionFactory.class);
    private static final long serialVersionUID = 7753681333583183646L;

    private TransactionManager transactionManager;
    private boolean tmFromJndi = false;
    private String tmJndiName = "java:/TransactionManager";

    public TransactionManager getTransactionManager() {
        if (transactionManager == null && tmFromJndi) {
            try {
                transactionManager = (TransactionManager) new InitialContext().lookup(getTmJndiName());
            } catch (Throwable ignored) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("exception on tmFromJndi: " + getTmJndiName(), ignored);
                }
            }
        }
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void setConnectionFactory(Object toUse) {
        if (toUse instanceof XAConnectionFactory) {
            connectionFactory = toUse;
        } else {
            throw new IllegalArgumentException("connectionFactory should implement jakarta.jms.XAConnectionFactory");
        }
    }

    @Override
    protected XAConnection createProviderConnection(PooledConnectionKey key) throws JMSException {
        if (connectionFactory instanceof XAConnectionFactory) {
            if (key.getUserName() == null && key.getPassword() == null) {
                return ((XAConnectionFactory) connectionFactory).createXAConnection();
            } else {
                return ((XAConnectionFactory) connectionFactory).createXAConnection(key.getUserName(), key.getPassword());
            }
        } else {
            throw new IllegalStateException("connectionFactory should implement jakarta.jms.XAConnectionFactory");
        }
    }

    @Override
    protected XAJMSContext createProviderContext(String username, String password, int sessionMode) {
        if (connectionFactory instanceof XAConnectionFactory) {
            if (username == null && password == null) {
                return ((XAConnectionFactory) connectionFactory).createXAContext();
            } else {
                return ((XAConnectionFactory) connectionFactory).createXAContext(username, password);
            }
        } else {
            throw new jakarta.jms.IllegalStateRuntimeException("connectionFactory should implement jakarta.jms.XAConnectionFactory");
        }
    }

    @Override
    protected PooledXAConnection createPooledConnection(Connection connection) {
        return new PooledXAConnection(connection, getTransactionManager());
    }

    @Override
    protected JmsPoolXAJMSContext newPooledConnectionContext(JmsPoolConnection connection, int sessionMode) {
        return new JmsPoolXAJMSContext(connection, sessionMode);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        setTmFromJndi(true);
        configFromJndiConf(obj);
        if (environment != null) {
            IntrospectionSupport.setProperties(this, (Map<String, Object>) environment);
        }
        return this;
    }

    private void configFromJndiConf(Object rootContextName) {
        if (rootContextName instanceof String) {
            String name = (String) rootContextName;
            name = name.substring(0, name.lastIndexOf('/')) + "/conf" + name.substring(name.lastIndexOf('/'));
            try {
                InitialContext ctx = new InitialContext();
                NamingEnumeration<Binding> bindings = ctx.listBindings(name);

                while (bindings.hasMore()) {
                    Binding bd = bindings.next();
                    IntrospectionSupport.setProperty(this, bd.getName(), bd.getObject());
                }

            } catch (Exception ignored) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("exception on config from jndi: " + name, ignored);
                }
            }
        }
    }

    public String getTmJndiName() {
        return tmJndiName;
    }

    public void setTmJndiName(String tmJndiName) {
        this.tmJndiName = tmJndiName;
    }

    public boolean isTmFromJndi() {
        return tmFromJndi;
    }

    /**
     * Allow transaction manager resolution from JNDI (ee deployment)
     *
     * @param tmFromJndi
     * 		controls if TXN manager resolution is from JNDI
     */
    public void setTmFromJndi(boolean tmFromJndi) {
        this.tmFromJndi = tmFromJndi;
    }

    @Override
    public XAConnection createXAConnection() throws JMSException {
        return createProviderConnection(new PooledConnectionKey(null, null));
    }

    @Override
    public XAConnection createXAConnection(String userName, String password) throws JMSException {
        return createProviderConnection(new PooledConnectionKey(userName, password));
    }

    @Override
    public XAJMSContext createXAContext() {
        return createProviderContext(null, null, 0);
    }

    @Override
    public XAJMSContext createXAContext(String userName, String password) {
        return createProviderContext(userName, password, 0);
    }
}
