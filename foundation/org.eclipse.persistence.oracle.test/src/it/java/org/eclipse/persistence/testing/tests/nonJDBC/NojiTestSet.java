/*
 * Copyright (c) 2018, 2024 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

package org.eclipse.persistence.testing.tests.nonJDBC;

// javase imports
import java.io.FileInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Properties;
import java.util.Vector;
import org.w3c.dom.Document;

// JUnit imports
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// EclipseLink imports
import org.eclipse.persistence.internal.helper.NonSynchronizedVector;
import org.eclipse.persistence.internal.sessions.factories.ObjectPersistenceWorkbenchXMLProject;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.oxm.XMLMarshaller;
import org.eclipse.persistence.platform.database.jdbc.JDBCTypes;
import org.eclipse.persistence.platform.database.oracle.plsql.OraclePLSQLTypes;
import org.eclipse.persistence.platform.database.oracle.plsql.PLSQLStoredProcedureCall;
import org.eclipse.persistence.queries.DataReadQuery;
import org.eclipse.persistence.sessions.DatabaseRecord;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Project;
import org.eclipse.persistence.sessions.factories.XMLProjectReader;

// Domain imports
import static org.eclipse.persistence.testing.tests.nonJDBC.NonJDBCTestHelper.buildTestProject;
import static org.eclipse.persistence.testing.tests.nonJDBC.NonJDBCTestHelper.buildWorkbenchXMLProject;
import static org.eclipse.persistence.testing.tests.nonJDBC.NonJDBCTestHelper.CONSTANT_PROJECT_BUILD_VERSION;
import static org.eclipse.persistence.testing.tests.nonJDBC.NonJDBCTestHelper.comparer;
import static org.eclipse.persistence.testing.tests.nonJDBC.NonJDBCTestHelper.TEST_DOT_PROPERTIES_KEY;
import static org.eclipse.persistence.testing.tests.nonJDBC.NonJDBCTestHelper.xmlParser;

    /*
     N == Non-JDBC type
     j == JDBC type
     i - IN parameter
     o - OUT parameter
     io - INOUT parameter
     */

public class NojiTestSet {

    // testsuite fixture(s)
    static ObjectPersistenceWorkbenchXMLProject workbenchXMLProject;
    static Project project = null;
    @BeforeClass
    public static void setUpProjects() {
        try {
            Properties p = new Properties();
            String testPropertiesPath = System.getProperty(TEST_DOT_PROPERTIES_KEY);
            p.load(new FileInputStream(testPropertiesPath));
            project = buildTestProject(p);
            workbenchXMLProject = buildWorkbenchXMLProject();
        }
        catch (Exception e) {
            fail("error setting up Project's database properties " + e.getMessage());
        }
    }

    @Test
    public void writeToXml() {
        // (x out binary_integer, y in varchar)
        PLSQLStoredProcedureCall call = new PLSQLStoredProcedureCall();
        call.setProcedureName("noji");
        call.addNamedOutputArgument("X", OraclePLSQLTypes.BinaryInteger);
        call.addNamedArgument("Y", JDBCTypes.VARCHAR_TYPE, 40);
        DataReadQuery query = new DataReadQuery();
        query.setCall(call);
        query.addArgument("Y", String.class);
        project.getDescriptor(Empty.class).getQueryManager().addQuery("Noji", query);
        Project projectToXml = project.clone();
        // trim off login 'cause it changes under test - this way, a comparison
        // can be done to a control document
        projectToXml.setDatasourceLogin(null);
        XMLContext context = new XMLContext(workbenchXMLProject);
        XMLMarshaller marshaller = context.createMarshaller();
        Document doc = marshaller.objectToXML(projectToXml);
        Document controlDoc = xmlParser.parse(new StringReader(NOJI_PROJECT_XML));
        assertTrue("control document not same as instance document",
            comparer.isNodeEqual(controlDoc, doc));
    }
    public static final String NOJI_PROJECT_XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<eclipselink:object-persistence version=\"" + CONSTANT_PROJECT_BUILD_VERSION + "\"" + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"" + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + " xmlns:eclipselink=\"http://xmlns.oracle.com/ias/xsds/eclipselink\">" +
          "<eclipselink:name>nonJDBCTestProject</eclipselink:name>" +
          "<eclipselink:class-mapping-descriptors>" +
            "<eclipselink:class-mapping-descriptor xsi:type=\"eclipselink:relational-class-mapping-descriptor\">" +
              "<eclipselink:class>org.eclipse.persistence.testing.tests.nonJDBC.Empty</eclipselink:class>" +
              "<eclipselink:alias>Empty</eclipselink:alias>" +
              "<eclipselink:primary-key>" +
                "<eclipselink:field table=\"EMPTY\" name=\"ID\" xsi:type=\"eclipselink:column\"/>" +
              "</eclipselink:primary-key>" +
              "<eclipselink:events xsi:type=\"eclipselink:event-policy\"/>" +
              "<eclipselink:querying xsi:type=\"eclipselink:query-policy\">" +
                "<eclipselink:queries>" +
                  "<eclipselink:query name=\"Noji\" xsi:type=\"eclipselink:data-read-query\">" +
                    "<eclipselink:arguments>" +
                      "<eclipselink:argument name=\"Y\">" +
                        "<eclipselink:type>java.lang.String</eclipselink:type>" +
                      "</eclipselink:argument>" +
                    "</eclipselink:arguments>" +
                    "<eclipselink:maintain-cache>false</eclipselink:maintain-cache>" +
                    "<eclipselink:call xsi:type=\"eclipselink:plsql-stored-procedure-call\">" +
                      "<eclipselink:procedure-name>noji</eclipselink:procedure-name>" +
                        "<eclipselink:arguments>" +
                          "<eclipselink:argument xsi:type=\"eclipselink:plsql-type\" type-name=\"BinaryInteger\">" +
                          "<eclipselink:name>X</eclipselink:name>" +
                          "<eclipselink:index>0</eclipselink:index>" +
                          "<eclipselink:direction>OUT</eclipselink:direction>" +
                        "</eclipselink:argument>" +
                        "<eclipselink:argument xsi:type=\"eclipselink:jdbc-type\" type-name=\"VARCHAR_TYPE\">" +
                           "<eclipselink:name>Y</eclipselink:name>" +
                          "<eclipselink:index>1</eclipselink:index>" +
                           "<eclipselink:length>40</eclipselink:length>" +
                          "</eclipselink:argument>" +
                       "</eclipselink:arguments>" +
                    "</eclipselink:call>" +
                    "<eclipselink:container xsi:type=\"eclipselink:list-container-policy\">" +
                         "<eclipselink:collection-type>java.util.Vector</eclipselink:collection-type>" +
                    "</eclipselink:container>" +
                  "</eclipselink:query>" +
                "</eclipselink:queries>" +
              "</eclipselink:querying>" +
              "<eclipselink:attribute-mappings>" +
                "<eclipselink:attribute-mapping xsi:type=\"eclipselink:direct-mapping\">" +
                  "<eclipselink:attribute-name>id</eclipselink:attribute-name>" +
                  "<eclipselink:field table=\"EMPTY\" name=\"ID\" xsi:type=\"eclipselink:column\"/>" +
                "</eclipselink:attribute-mapping>" +
              "</eclipselink:attribute-mappings>" +
              "<eclipselink:descriptor-type>independent</eclipselink:descriptor-type>" +
              "<eclipselink:instantiation/>" +
              "<eclipselink:copying xsi:type=\"eclipselink:instantiation-copy-policy\"/>" +
                "<eclipselink:tables>" +
                  "<eclipselink:table name=\"EMPTY\"/>" +
                "</eclipselink:tables>" +
            "</eclipselink:class-mapping-descriptor>" +
          "</eclipselink:class-mapping-descriptors>" +
        "</eclipselink:object-persistence>";

    @Test
    public void readFromXml() {
        Project projectFromXML = XMLProjectReader.read(new StringReader(NOJI_PROJECT_XML),
            this.getClass().getClassLoader());
        projectFromXML.setDatasourceLogin(project.getDatasourceLogin());
        project = projectFromXML;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void runQuery() {
        DatabaseSession s = project.createDatabaseSession();
        s.dontLogMessages();
        s.login();
        Vector queryArgs = new NonSynchronizedVector();
        queryArgs.add("testsdfsdfasdfsdfsdfsdfsdfsdfdfsdfsdffds");
        boolean worked = false;
        String msg = null;
        Object o = null;
        try {
          o = s.executeQuery("Noji", Empty.class, queryArgs);
          worked = true;
        }
        catch (Exception e) {
          msg = e.getMessage();
        }
        assertTrue("invocation noji failed: " + msg, worked);
        assertNotNull("result is supposed to be not-null", o);
        Vector results = (Vector)o;
        DatabaseRecord record = (DatabaseRecord)results.get(0);
        BigDecimal x = (BigDecimal)record.get("X");
        assertEquals("wrong x value", 33, x.intValue());
        s.logout();
    }
}
