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
import java.util.Properties;
import java.util.Vector;
import org.w3c.dom.Document;

// JUnit imports
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// EclipseLink imports
import org.eclipse.persistence.internal.helper.NonSynchronizedVector;
import org.eclipse.persistence.internal.sessions.factories.ObjectPersistenceWorkbenchXMLProject;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.oxm.XMLMarshaller;
import org.eclipse.persistence.platform.database.oracle.plsql.OraclePLSQLTypes;
import org.eclipse.persistence.platform.database.oracle.plsql.PLSQLStoredProcedureCall;
import org.eclipse.persistence.queries.DataModifyQuery;
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

public class Ni5TestSet {

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
        // (y in int)
        PLSQLStoredProcedureCall call = new PLSQLStoredProcedureCall();
        call.setProcedureName("int_in_test");
        call.addNamedArgument("Y", OraclePLSQLTypes.Int);
        DataModifyQuery query = new DataModifyQuery();
        query.addArgument("Y", Integer.class);
        query.setCall(call);
        project.getDescriptor(Empty.class).getQueryManager().addQuery("Ni5", query);
        Project projectToXml = project.clone();
        // trim off login 'cause it changes under test - this way, a comparison
        // can be done to a control document
        projectToXml.setDatasourceLogin(null);
        XMLContext context = new XMLContext(workbenchXMLProject);
        XMLMarshaller marshaller = context.createMarshaller();
        Document doc = marshaller.objectToXML(projectToXml);
        Document controlDoc = xmlParser.parse(new StringReader(NI5_PROJECT_XML));
        assertTrue("control document not same as instance document",
            comparer.isNodeEqual(controlDoc, doc));
    }
    public static final String NI5_PROJECT_XML =
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
                       "<eclipselink:query name=\"Ni5\" xsi:type=\"eclipselink:data-modify-query\">" +
                          "<eclipselink:arguments>" +
                             "<eclipselink:argument name=\"Y\">" +
                                "<eclipselink:type>java.lang.Integer</eclipselink:type>" +
                             "</eclipselink:argument>" +
                          "</eclipselink:arguments>" +
                          "<eclipselink:call xsi:type=\"eclipselink:plsql-stored-procedure-call\">" +
                             "<eclipselink:procedure-name>int_in_test</eclipselink:procedure-name>" +
                             "<eclipselink:arguments>" +
                                "<eclipselink:argument xsi:type=\"eclipselink:plsql-type\" type-name=\"Int\">" +
                                   "<eclipselink:name>Y</eclipselink:name>" +
                                   "<eclipselink:index>0</eclipselink:index>" +
                                "</eclipselink:argument>" +
                             "</eclipselink:arguments>" +
                          "</eclipselink:call>" +
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
        Project projectFromXML = XMLProjectReader.read(new StringReader(NI5_PROJECT_XML),
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
        queryArgs.add(5);
        boolean worked = false;
        String msg = null;
        try {
          s.executeQuery("Ni5", Empty.class, queryArgs);
          worked = true;
        }
        catch (Exception e) {
          msg = e.getMessage();
        }
        assertTrue("invocation int_in_test failed: " + msg, worked);
        s.logout();
    }
}
