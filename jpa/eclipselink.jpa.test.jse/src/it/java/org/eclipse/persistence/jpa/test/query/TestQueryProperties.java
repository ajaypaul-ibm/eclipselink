/*
 * Copyright (c) 2015, 2024 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2015, 2024 IBM Corporation. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     06/30/2015-2.6.0 Will Dazey
//       - 471487: Added test for QueryHints.JDBC_TIMEOUT that checks the executed sql statement
//     09/03/2015 - Will Dazey
//       - 456067 : Added tests to check query timeout units
package org.eclipse.persistence.jpa.test.query;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.jpa.test.framework.DDLGen;
import org.eclipse.persistence.jpa.test.framework.Emf;
import org.eclipse.persistence.jpa.test.framework.EmfRunner;
import org.eclipse.persistence.jpa.test.framework.PUPropertiesProvider;
import org.eclipse.persistence.jpa.test.framework.Property;
import org.eclipse.persistence.jpa.test.query.model.QueryEmployee;
import org.eclipse.persistence.sessions.Connector;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionEvent;
import org.eclipse.persistence.sessions.SessionEventAdapter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EmfRunner.class)
public class TestQueryProperties implements PUPropertiesProvider {

    private static int statementTimeout;

    private final static int propertyTimeout = 3099;

    @Emf(name = "timeoutEMF", classes = { QueryEmployee.class }, createTables = DDLGen.DROP_CREATE, properties = {
            @Property(name = PersistenceUnitProperties.QUERY_TIMEOUT, value = "" + TestQueryProperties.propertyTimeout) })
    private EntityManagerFactory emfTimeout;

    @Emf(name = "timeoutWithUnitSecondsEMF", classes = { QueryEmployee.class }, createTables = DDLGen.DROP_CREATE, properties = {
            @Property(name = PersistenceUnitProperties.QUERY_TIMEOUT, value = "" + TestQueryProperties.propertyTimeout),
            @Property(name = PersistenceUnitProperties.QUERY_TIMEOUT_UNIT, value = "SECONDS") })
    private EntityManagerFactory emfTimeoutSeconds;

    @Emf(name = "timeoutWithUnitMintuesEMF", classes = { QueryEmployee.class }, createTables = DDLGen.DROP_CREATE, properties = {
            @Property(name = PersistenceUnitProperties.QUERY_TIMEOUT, value = "" + TestQueryProperties.propertyTimeout),
            @Property(name = PersistenceUnitProperties.QUERY_TIMEOUT_UNIT, value = "MINUTES") })
    private EntityManagerFactory emfTimeoutMinutes;

    /**
     * Test that setting the property: PersistenceUnitProperties.QUERY_TIMEOUT_UNIT to the default value
     *  will see the expected value of seconds being set on the statement
     */
    @Test
    public void testTimeoutUnitDefault() {
        EntityManager em = emfTimeout.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("SELECT x FROM QueryEmployee x").getResultList();

            //Convert the timeout set (MILLISECONDS) to what is expected by the JDBC layer (SECONDS)
            double queryTimeoutSeconds = TestQueryProperties.propertyTimeout / 1000d;
            //if there was a remainder, it should round up
            if(queryTimeoutSeconds % 1 > 0) {
                queryTimeoutSeconds += 1;
            }

            Assert.assertEquals((int)queryTimeoutSeconds, TestQueryProperties.statementTimeout);
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if(em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Test that setting the property: PersistenceUnitProperties.QUERY_TIMEOUT_UNIT to "SECONDS" value
     *  will see the expected value of seconds being set on the statement
     */
    @Test
    public void testTimeoutUnitSeconds() {
        EntityManager em = emfTimeoutSeconds.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("SELECT x FROM QueryEmployee x").getResultList();

            //Convert the timeout set (SECONDS) to what is expected by the JDBC layer (SECONDS)
            int queryTimeoutSeconds = TestQueryProperties.propertyTimeout;

            Assert.assertEquals(queryTimeoutSeconds, TestQueryProperties.statementTimeout);
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if(em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Test that setting the property: PersistenceUnitProperties.QUERY_TIMEOUT_UNIT to "MINUTES" value
     *  will see the expected value of seconds being set on the statement
     */
    @Test
    public void testTimeoutUnitMinutes() {
        EntityManager em = emfTimeoutMinutes.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("SELECT x FROM QueryEmployee x").getResultList();

            //Convert the timeout set (MINUTES) to what is expected by the JDBC layer (SECONDS)
            int queryTimeoutSeconds = TestQueryProperties.propertyTimeout * 60;

            Assert.assertEquals(queryTimeoutSeconds, TestQueryProperties.statementTimeout);
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if(em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public Map<String, Object> getAdditionalPersistenceProperties(String puName) {
        Map<String, Object> map = new HashMap<>();
        map.put(PersistenceUnitProperties.SESSION_CUSTOMIZER, Customizer.class.getName());
        return map;
    }

    public static class Customizer implements SessionCustomizer {
        @Override
        public void customize(Session session) throws Exception {
            session.getEventManager().addListener(new SessionEventAdapter() {
                @Override
                public void preLogin(SessionEvent event) {
                    DatabaseLogin login = event.getSession().getLogin();
                    login.setConnector(ConnectorInvocationHandler.createStatementProxy(login.getConnector()));
                }
            });
        }
    }

    public static class ConnectorInvocationHandler implements InvocationHandler {
        private Connector wrappedConnector;

        public ConnectorInvocationHandler(Connector stmt) {
            wrappedConnector = stmt;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("connect") && proxy instanceof Connector) {
                return ConnectionInvocationHandler.createStatementProxy((Connection) method.invoke(wrappedConnector, args));
            }
            return method.invoke(wrappedConnector, args);
        }

        public static Connector createStatementProxy(Connector toWrap) {
            return (Connector) Proxy.newProxyInstance(Connector.class.getClassLoader(), new Class<?>[] { Connector.class }, new ConnectorInvocationHandler(toWrap));
        }
    }

    public static class ConnectionInvocationHandler implements InvocationHandler {
        private Connection wrappedConnection;

        public ConnectionInvocationHandler(Connection stmt) {
            wrappedConnection = stmt;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                if (method.getName().equals("prepareStatement") && proxy instanceof Connection) {
                    return PreparedStatementInvocationHandler.createStatementProxy((PreparedStatement) method.invoke(wrappedConnection, args));
                }
                return method.invoke(wrappedConnection, args);
            } catch (Exception e) {
                throw e.getCause();
            }
        }

        public static Connection createStatementProxy(Connection toWrap) {
            return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] { Connection.class }, new ConnectionInvocationHandler(toWrap));
        }
    }

    public static class PreparedStatementInvocationHandler implements InvocationHandler {
        private PreparedStatement wrappedStatement;

        public PreparedStatementInvocationHandler(PreparedStatement stmt) {
            wrappedStatement = stmt;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                //Get the query timeout being set on the statement
                //This value should be in seconds, since that is what the Statement expects
                if (method.getName().equals("setQueryTimeout") && proxy instanceof PreparedStatement) {
                    if(args.length > 0) {
                        TestQueryProperties.statementTimeout = (Integer) args[0];
                    }
                }
                return method.invoke(wrappedStatement, args);
            } catch (Exception e) {
                throw e.getCause();
            }
        }

        public static PreparedStatement createStatementProxy(PreparedStatement toWrap) {
            return (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(), new Class<?>[] { PreparedStatement.class }, new PreparedStatementInvocationHandler(toWrap));
        }
    }
}
