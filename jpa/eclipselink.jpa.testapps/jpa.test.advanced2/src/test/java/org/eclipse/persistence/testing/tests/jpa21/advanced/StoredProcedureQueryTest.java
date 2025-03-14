/*
 * Copyright (c) 2013, 2024 Oracle and/or its affiliates. All rights reserved.
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
//     01/23/2013-2.5 Guy Pelletier
//       - 350487: JPA 2.1 Specification defined support for Stored Procedure Calls
package org.eclipse.persistence.testing.tests.jpa21.advanced;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Parameter;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.TransactionRequiredException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.queries.ColumnResult;
import org.eclipse.persistence.queries.ConstructorResult;
import org.eclipse.persistence.queries.EntityResult;
import org.eclipse.persistence.queries.FieldResult;
import org.eclipse.persistence.queries.ResultSetMappingQuery;
import org.eclipse.persistence.queries.SQLResultSetMapping;
import org.eclipse.persistence.queries.StoredProcedureCall;
import org.eclipse.persistence.sessions.server.ServerSession;
import org.eclipse.persistence.testing.framework.QuerySQLTracker;
import org.eclipse.persistence.testing.framework.jpa.junit.JUnitTestCase;
import org.eclipse.persistence.testing.models.jpa21.advanced.Address;
import org.eclipse.persistence.testing.models.jpa21.advanced.AdvancedTableCreator;
import org.eclipse.persistence.testing.models.jpa21.advanced.Employee;
import org.eclipse.persistence.testing.models.jpa21.advanced.EmployeeDetails;
import org.eclipse.persistence.testing.models.jpa21.advanced.LargeProject;
import org.eclipse.persistence.testing.models.jpa21.advanced.Project;
import org.eclipse.persistence.testing.models.jpa21.advanced.SmallProject;
import org.junit.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoredProcedureQueryTest extends JUnitTestCase {
    public StoredProcedureQueryTest() {}

    public StoredProcedureQueryTest(String name) {
        super(name);
        setPuName(getPersistenceUnitName());
    }

    @Override
    public String getPersistenceUnitName() {
       return "advanced2x";
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.setName("StoredProcedureQueryTest");

        suite.addTest(new StoredProcedureQueryTest("testSetup"));

        // Add the EM level stored procedure query tests.
        suite.addTest(new StoredProcedureQueryTest("testQueryExecute1"));
        suite.addTest(new StoredProcedureQueryTest("testQueryExecute2"));
        suite.addTest(new StoredProcedureQueryTest("testQueryExecuteUpdate"));
        suite.addTest(new StoredProcedureQueryTest("testQueryGetResultList"));
        suite.addTest(new StoredProcedureQueryTest("testQueryWithMultipleResultsFromCode"));
        suite.addTest(new StoredProcedureQueryTest("testQueryWithNamedFieldResult"));
        suite.addTest(new StoredProcedureQueryTest("testQueryWithNamedFieldResultTranslationIntoNumbered"));
        suite.addTest(new StoredProcedureQueryTest("testQueryWithNumberedFieldResult"));
        suite.addTest(new StoredProcedureQueryTest("testQueryWithResultClass"));
        suite.addTest(new StoredProcedureQueryTest("testQueryWithOutParam"));
        suite.addTest(new StoredProcedureQueryTest("testStoredProcedureParameterAPI"));
        suite.addTest(new StoredProcedureQueryTest("testStoredProcedureQuerySysCursor_Named"));
        suite.addTest(new StoredProcedureQueryTest("testStoredProcedureQuerySysCursor_Positional"));
        suite.addTest(new StoredProcedureQueryTest("testStoredProcedureQuerySysCursor_ResultList_Named"));
        suite.addTest(new StoredProcedureQueryTest("testStoredProcedureQuerySysCursor_ResultList_Positional"));
        suite.addTest(new StoredProcedureQueryTest("testStoredProcedureQueryExceptionWrapping1"));
        suite.addTest(new StoredProcedureQueryTest("testStoredProcedureQueryExceptionWrapping2"));

        // Add the named Annotation query tests.
        suite.addTest(NamedStoredProcedureQueryTest.suite());

        // These are EM API validation tests. These tests delete and update so
        // be careful where you introduce new tests.
        suite.addTest(new StoredProcedureQueryTest("testClassCastExceptionOnExecuteWithNoOutputParameters"));
        suite.addTest(new StoredProcedureQueryTest("testGetResultListOnDeleteQuery"));
        suite.addTest(new StoredProcedureQueryTest("testGetResultListOnUpdateQuery"));
        suite.addTest(new StoredProcedureQueryTest("testGetSingleResultOnDeleteQuery"));
        suite.addTest(new StoredProcedureQueryTest("testGetSingleResultOnUpdateQuery"));

        return suite;
    }

    /**
     * The setup is done as a test, both to record its failure, and to allow execution in the server.
     */
    public void testSetup() {
        new AdvancedTableCreator().replaceTables(getPersistenceUnitServerSession());
        EmployeePopulator employeePopulator = new EmployeePopulator();
        employeePopulator.buildExamples();
        employeePopulator.persistExample(getPersistenceUnitServerSession());
        clearCache();
    }

    /**
     * Test a class cast exception.
     */
    public void testClassCastExceptionOnExecuteWithNoOutputParameters() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            try {
                StoredProcedureQuery query = em.createStoredProcedureQuery("Read_All_Employees");
                boolean returnValue = query.execute();
                assertTrue("Execute didn't return true", returnValue);
                @SuppressWarnings({"unchecked"})
                List<Employee> employees = query.getResultList();
                assertFalse("No employees were returned", employees.isEmpty());
            } catch (ClassCastException e) {
                if (isTransactionActive(em)){
                    rollbackTransaction(em);
                }

                fail("ClassCastException caught" + e);
            } finally {
                closeEntityManager(em);
            }
        }
    }

    /**
     * Test an expected exception.
     */
    public void testGetResultListOnDeleteQuery() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            boolean exceptionCaught = false;

            try {
                Object result = em.createStoredProcedureQuery("Delete_All_Responsibilities").getResultList();
            } catch (IllegalStateException e) {
                if (isTransactionActive(em)){
                    rollbackTransaction(em);
                }

                exceptionCaught = true;
            } finally {
                closeEntityManager(em);
            }

            assertTrue("Expected Illegal state exception was not caught", exceptionCaught);
        }
    }

    /**
     * Test an expected exception.
     */
    public void testGetResultListOnUpdateQuery() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            boolean exceptionCaught = false;

            try {
                String postalCodeTypo = "R3 1B9";
                String postalCodeCorrection = "R3B 1B9";

                StoredProcedureQuery query = em.createStoredProcedureQuery("Update_Address_Postal_Code");
                query.registerStoredProcedureParameter("new_p_code_v", String.class, ParameterMode.IN);
                query.registerStoredProcedureParameter("old_p_code_v", String.class, ParameterMode.IN);

                Object results = query.setParameter("new_p_code_v", postalCodeCorrection).setParameter("old_p_code_v", postalCodeTypo).getResultList();
            } catch (IllegalStateException e) {
                if (isTransactionActive(em)){
                    rollbackTransaction(em);
                }

                exceptionCaught = true;
            } finally {
                closeEntityManager(em);
            }

            assertTrue("Expected Illegal state exception was not caught", exceptionCaught);
        }
    }

    /**
     * Test an expected exception.
     */
    public void testGetSingleResultOnDeleteQuery() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            boolean exceptionCaught = false;

            try {
                Object result = em.createStoredProcedureQuery("Delete_All_Responsibilities").getSingleResult();
            } catch (IllegalStateException e) {
                if (isTransactionActive(em)){
                    rollbackTransaction(em);
                }

                exceptionCaught = true;
            } finally {
                closeEntityManager(em);
            }

            assertTrue("Expected Illegal state exception was not caught", exceptionCaught);
        }
    }

    /**
     * Test an expected exception.
     */
    public void testGetSingleResultOnUpdateQuery() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            boolean exceptionCaught = false;

            try {
                String postalCodeTypo = "R3 1B9";
                String postalCodeCorrection = "R3B 1B9";

                StoredProcedureQuery query = em.createStoredProcedureQuery("Update_Address_Postal_Code");
                query.registerStoredProcedureParameter("new_p_code_v", String.class, ParameterMode.IN);
                query.registerStoredProcedureParameter("old_p_code_v", String.class, ParameterMode.IN);

                query.setParameter("new_p_code_v", postalCodeCorrection);
                query.setParameter("old_p_code_v", postalCodeTypo);

                // Make these calls to test the getParameter call with a name.
                Parameter<?> paramNew = query.getParameter("new_p_code_v");
                Parameter<?> paramOld = query.getParameter("old_p_code_v");

                Object results = query.getSingleResult();
            } catch (IllegalStateException e) {
                if (isTransactionActive(em)){
                    rollbackTransaction(em);
                }

                exceptionCaught = true;
            } finally {
                closeEntityManager(em);
            }

            assertTrue("Expected Illegal state exception was not caught", exceptionCaught);
        }
    }

    /**
     * Tests a StoredProcedureQuery that does an update though EM API
     */
    public void testQueryExecute1() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            try {
                // Create some data (with errors)
                String postalCodeTypo = "K2J 0L8";
                String postalCodeCorrection = "K2G 6W2";

                beginTransaction(em);

                Address address1 = new Address();
                address1.setCity("Winnipeg");
                address1.setPostalCode(postalCodeTypo);
                address1.setProvince("MB");
                address1.setStreet("510 Main Street");
                address1.setCountry("Canada");
                em.persist(address1);

                Address address2 = new Address();
                address2.setCity("Winnipeg");
                address2.setPostalCode(postalCodeTypo);
                address2.setProvince("MB");
                address2.setStreet("512 Main Street");
                address2.setCountry("Canada");
                em.persist(address2);

                Address address3 = new Address();
                address3.setCity("Winnipeg");
                address3.setPostalCode(postalCodeCorrection);
                address3.setProvince("MB");
                address3.setStreet("514 Main Street");
                address3.setCountry("Canada");
                em.persist(address3);

                commitTransaction(em);

                // Clear the cache
                em.clear();
                clearCache();

                // Build the named stored procedure query, execute and test.
                StoredProcedureQuery query = em.createStoredProcedureQuery("Result_Set_And_Update_Address", Address.class, Employee.class);

                assertEquals("Parameter list was not empty.", 0, query.getParameters().size());

                query.registerStoredProcedureParameter("new_p_code_v", String.class, ParameterMode.IN);
                query.registerStoredProcedureParameter("old_p_code_v", String.class, ParameterMode.IN);
                query.registerStoredProcedureParameter("employee_count_v", Integer.class, ParameterMode.OUT);

                query.setParameter("new_p_code_v", postalCodeCorrection);
                query.setParameter("old_p_code_v", postalCodeTypo);

                boolean result = query.execute();

                assertEquals("Parameter list size was incorrect, actual: " + query.getParameters().size() + ", expecting 3.", 3, query.getParameters().size());

                Object parameterValue = query.getParameterValue("new_p_code_v");
                assertEquals("The IN parameter value was not preserved, expected: " + postalCodeCorrection + ", actual: " + parameterValue, parameterValue, postalCodeCorrection);

                assertTrue("Result did not return true for a result set.", result);

                @SuppressWarnings({"unchecked"})
                List<Address> addressResults = query.getResultList();

                assertTrue("The query didn't have any more results.", query.hasMoreResults());
                @SuppressWarnings({"unchecked"})
                List<Employee> employeeResults = query.getResultList();
                int numberOfEmployes = employeeResults.size();

                // Should return false (no more results)
                assertFalse("The query had more results.", query.hasMoreResults());

                // Get the update count.
                int updateCount = query.getUpdateCount();
                assertEquals("Update count incorrect: " + updateCount, 2, updateCount);

                // Update count should return -1 now.
                assertEquals("Update count should be -1.", -1, query.getUpdateCount());

                // Check output parameters by name.
                Object outputParamValueFromName = query.getOutputParameterValue("employee_count_v");
                assertNotNull("The output parameter was null.", outputParamValueFromName);
                // TODO: to investigate. This little bit is hacky. For some
                // reason MySql returns a Long here. By position is ok, that is,
                // it returns an Integer (as we registered)
                if (outputParamValueFromName instanceof Long) {
                    assertEquals("Incorrect value returned, expected " + numberOfEmployes + ", got: " + outputParamValueFromName, outputParamValueFromName, (long) numberOfEmployes);
                } else if (outputParamValueFromName instanceof Integer) {
                    assertEquals("Incorrect value returned, expected " + numberOfEmployes + ", got: " + outputParamValueFromName, outputParamValueFromName, numberOfEmployes);
                }

                // TODO: else, don't worry about it for now ...

                // Do some negative tests ...
                try {
                    query.getOutputParameterValue(null);
                    fail("No IllegalArgumentException was caught with a null parameter name.");
                } catch (IllegalArgumentException e) {
                    // Expected, swallow.
                }

                try {
                    query.getOutputParameterValue("emp_count");
                    fail("No IllegalArgumentException was caught with invalid parameter name.");
                } catch (IllegalArgumentException e) {
                    // Expected, swallow.
                }

                try {
                    query.getOutputParameterValue("new_p_code_v");
                    fail("No IllegalArgumentException was caught with IN parameter name.");
                } catch (IllegalArgumentException e) {
                    // Expected, swallow.
                }

                // Check output parameters by position.
                Object outputParamValueFromPosition = query.getOutputParameterValue(3);

                // TODO: See previous investigate todo. A Long is returned here instead of an Integer on MySQL.
                // This test also mixes index and named parameters, so this test my be invalid to begin with.
                assertNotNull("The output parameter was null.", outputParamValueFromPosition);
                if (outputParamValueFromName instanceof Long) {
                    assertEquals("Incorrect value returned, expected " + numberOfEmployes + ", got: " + outputParamValueFromPosition, outputParamValueFromPosition, (long) numberOfEmployes);
                } else if (outputParamValueFromName instanceof Integer) {
                    assertEquals("Incorrect value returned, expected " + numberOfEmployes + ", got: " + outputParamValueFromPosition, outputParamValueFromPosition, numberOfEmployes);
                }

                // Do some negative tests ...
                try {
                    query.getOutputParameterValue(8);
                    fail("No IllegalArgumentException was caught with position out of bounds.");
                } catch (IllegalArgumentException e) {
                    // Expected, swallow.
                }

                try {
                    query.getOutputParameterValue(1);
                    fail("No IllegalArgumentException was caught with an IN parameter position.");
                } catch (IllegalArgumentException e) {
                    // Expected, swallow.
                }

                // Just because we don't trust anyone ... :-)
                Address a1 = em.find(Address.class, address1.getId());
                assertEquals("The postal code was not updated for address 1.", a1.getPostalCode(), postalCodeCorrection);
                Address a2 = em.find(Address.class, address2.getId());
                assertEquals("The postal code was not updated for address 2.", a2.getPostalCode(), postalCodeCorrection);
                Address a3 = em.find(Address.class, address3.getId());
                assertEquals("The postal code was not updated for address 3.", a3.getPostalCode(), postalCodeCorrection);
            } finally {
                // The open statement/connection will be closed here.
                closeEntityManagerAndTransaction(em);
            }
        }
    }

    /**
     * Tests a StoredProcedureQuery that does an update though EM API
     * This is the same test as above except different retrieval path.
     */
    public void testQueryExecute2() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            try {
                // Create some data (with errors)
                String postalCodeTypo = "K2J 0L8";
                String postalCodeCorrection = "K2G 6W2";

                beginTransaction(em);

                Address address1 = new Address();
                address1.setCity("Winnipeg");
                address1.setPostalCode(postalCodeTypo);
                address1.setProvince("MB");
                address1.setStreet("510 Main Street");
                address1.setCountry("Canada");
                em.persist(address1);

                Address address2 = new Address();
                address2.setCity("Winnipeg");
                address2.setPostalCode(postalCodeTypo);
                address2.setProvince("MB");
                address2.setStreet("512 Main Street");
                address2.setCountry("Canada");
                em.persist(address2);

                Address address3 = new Address();
                address3.setCity("Winnipeg");
                address3.setPostalCode(postalCodeCorrection);
                address3.setProvince("MB");
                address3.setStreet("514 Main Street");
                address3.setCountry("Canada");
                em.persist(address3);

                commitTransaction(em);

                // Clear the cache
                em.clear();
                clearCache();

                // Build the named stored procedure query, execute and test.
                StoredProcedureQuery query = em.createStoredProcedureQuery("Result_Set_And_Update_Address", Address.class, Employee.class);
                query.registerStoredProcedureParameter("new_p_code_v", String.class, ParameterMode.IN);
                query.registerStoredProcedureParameter("old_p_code_v", String.class, ParameterMode.IN);
                query.registerStoredProcedureParameter("employee_count_v", Integer.class, ParameterMode.OUT);

                boolean result = query.setParameter("new_p_code_v", postalCodeCorrection).setParameter("old_p_code_v", postalCodeTypo).execute();
                assertTrue("Result did not return true for a result set.", result);

                // This shouldn't affect where we are in the retrieval of the query.
                assertTrue("We have didn't have any more results", query.hasMoreResults());
                assertTrue("We have didn't have any more results", query.hasMoreResults());
                assertTrue("We have didn't have any more results", query.hasMoreResults());
                assertTrue("We have didn't have any more results", query.hasMoreResults());
                assertTrue("We have didn't have any more results", query.hasMoreResults());
                assertTrue("We have didn't have any more results", query.hasMoreResults());
                assertTrue("We have didn't have any more results", query.hasMoreResults());

                @SuppressWarnings({"unchecked"})
                List<Address> addressResults = query.getResultList();

                // We know there should be more results so ask for them without checking for has more results.
                @SuppressWarnings({"unchecked"})
                List<Employee> employeeResults = query.getResultList();
                int numberOfEmployes = employeeResults.size();

                // Should return false (no more results)
                assertFalse("The query had more results.", query.hasMoreResults());
                assertFalse("The query had more results.", query.hasMoreResults());
                assertFalse("The query had more results.", query.hasMoreResults());
                assertFalse("The query had more results.", query.hasMoreResults());

                assertNull("getResultList after no results did not return null", query.getResultList());

                // Get the update count.
                int updateCount = query.getUpdateCount();
                assertEquals("Update count incorrect: " + updateCount, 2, updateCount);

                // Update count should return -1 now.
                assertEquals("Update count should be -1.", -1, query.getUpdateCount());
                assertEquals("Update count should be -1.", -1, query.getUpdateCount());
                assertEquals("Update count should be -1.", -1, query.getUpdateCount());
                assertEquals("Update count should be -1.", -1, query.getUpdateCount());
                assertEquals("Update count should be -1.", -1, query.getUpdateCount());

                // Check output parameters by name.
                Object outputParamValueFromName = query.getOutputParameterValue("employee_count_v");
                assertNotNull("The output parameter was null.", outputParamValueFromName);
                // TODO: to investigate. This little bit is hacky. For some
                // reason MySql returns a Long here. By position is ok, that is,
                // it returns an Integer (as we registered)
                if (outputParamValueFromName instanceof Long) {
                    assertEquals("Incorrect value returned, expected " + numberOfEmployes + ", got: " + outputParamValueFromName, outputParamValueFromName, (long) numberOfEmployes);
                } else if (outputParamValueFromName instanceof Integer) {
                    assertEquals("Incorrect value returned, expected " + numberOfEmployes + ", got: " + outputParamValueFromName, outputParamValueFromName, numberOfEmployes);
                }
                // TODO: else, don't worry about it for now ...

                // Do some negative tests ...
                try {
                    query.getOutputParameterValue(null);
                    fail("No IllegalArgumentException was caught with a null parameter name.");
                } catch (IllegalArgumentException e) {
                    // Expected, swallow.
                }

                try {
                    query.getOutputParameterValue("emp_count");
                    fail("No IllegalArgumentException was caught with invalid parameter name.");
                } catch (IllegalArgumentException e) {
                    // Expected, swallow.
                }

                try {
                    query.getOutputParameterValue("new_p_code_v");
                    fail("No IllegalArgumentException was caught with IN parameter name.");
                } catch (IllegalArgumentException e) {
                    // Expected, swallow.
                }

                // Check output parameters by position.
                Object outputParamValueFromPosition = query.getOutputParameterValue(3);

                // TODO: See previous investigate todo. A Long is returned here instead of an Integer on MySQL.
                // This test also mixes index and named parameters, so this test my be invalid to begin with.
                assertNotNull("The output parameter was null.", outputParamValueFromPosition);
                if (outputParamValueFromName instanceof Long) {
                    assertEquals("Incorrect value returned, expected " + numberOfEmployes + ", got: " + outputParamValueFromPosition, outputParamValueFromPosition, (long) numberOfEmployes);
                } else if (outputParamValueFromName instanceof Integer) {
                    assertEquals("Incorrect value returned, expected " + numberOfEmployes + ", got: " + outputParamValueFromPosition, outputParamValueFromPosition, numberOfEmployes);
                }

                // Do some negative tests ...
                try {
                    query.getOutputParameterValue(8);
                    fail("No IllegalArgumentException was caught with position out of bounds.");
                } catch (IllegalArgumentException e) {
                    // Expected, swallow.
                }

                try {
                    query.getOutputParameterValue(1);
                    fail("No IllegalArgumentException was caught with an IN parameter position.");
                } catch (IllegalArgumentException e) {
                    // Expected, swallow.
                }

                // Just because we don't trust anyone ... :-)
                Address a1 = em.find(Address.class, address1.getId());
                assertEquals("The postal code was not updated for address 1.", a1.getPostalCode(), postalCodeCorrection);
                Address a2 = em.find(Address.class, address2.getId());
                assertEquals("The postal code was not updated for address 2.", a2.getPostalCode(), postalCodeCorrection);
                Address a3 = em.find(Address.class, address3.getId());
                assertEquals("The postal code was not updated for address 3.", a3.getPostalCode(), postalCodeCorrection);
            } finally {
                // The open statement/connection will be closed here.
                closeEntityManagerAndTransaction(em);
            }
        }
    }

    /**
     * Tests a StoredProcedureQuery that does an update though EM API
     */
    public void testQueryExecuteUpdate() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            try {
                String postalCodeTypo = "R3 1B9";
                String postalCodeCorrection = "R3B 1B9";

                StoredProcedureQuery query = em.createStoredProcedureQuery("Update_Address_Postal_Code");
                query.registerStoredProcedureParameter("new_p_code_v", String.class, ParameterMode.IN);
                query.registerStoredProcedureParameter("old_p_code_v", String.class, ParameterMode.IN);

                try {
                    query.setParameter("new_p_code_v", postalCodeCorrection).setParameter("old_p_code_v", postalCodeTypo).executeUpdate();
                    fail("TransactionRequiredException not caught");
                } catch (TransactionRequiredException e) {
                   // ignore since expected exception.
                }

                beginTransaction(em);

                Address address1 = new Address();
                address1.setCity("Winnipeg");
                address1.setPostalCode(postalCodeTypo);
                address1.setProvince("MB");
                address1.setStreet("510 Main Street");
                address1.setCountry("Canada");
                em.persist(address1);

                Address address2 = new Address();
                address2.setCity("Winnipeg");
                address2.setPostalCode(postalCodeTypo);
                address2.setProvince("MB");
                address2.setStreet("512 Main Street");
                address2.setCountry("Canada");
                em.persist(address2);

                em.flush();

                // Clear the cache
                em.clear();
                clearCache();

                query = em.createStoredProcedureQuery("Update_Address_Postal_Code");
                query.registerStoredProcedureParameter("new_p_code_v", String.class, ParameterMode.IN);
                query.registerStoredProcedureParameter("old_p_code_v", String.class, ParameterMode.IN);

                int results = query.setParameter("new_p_code_v", postalCodeCorrection).setParameter("old_p_code_v", postalCodeTypo).executeUpdate();

                assertEquals("Update count incorrect.", 2, results);

                // Just because we don't trust anyone ... :-)
                Address a1 = em.find(Address.class, address1.getId());
                assertEquals("The postal code was not updated for address 1.", a1.getPostalCode(), postalCodeCorrection);
                Address a2 = em.find(Address.class, address2.getId());
                assertEquals("The postal code was not updated for address 2.", a2.getPostalCode(), postalCodeCorrection);
            } finally {
                closeEntityManagerAndTransaction(em);
            }
        }
    }

    /**
     * Tests a StoredProcedureQuery that does an select though EM API
     */
    public void testQueryGetResultList() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            try {
                beginTransaction(em);

                Address address1 = new Address();
                address1.setCity("Edmonton");
                address1.setPostalCode("T5B 4M9");
                address1.setProvince("AB");
                address1.setStreet("7424 118 Avenue");
                address1.setCountry("Canada");
                em.persist(address1);
                em.flush();

                // Clear the cache
                em.clear();
                clearCache();

                StoredProcedureQuery query = em.createStoredProcedureQuery("Read_Address");
                query.registerStoredProcedureParameter("address_id_v", Integer.class, ParameterMode.IN);

                @SuppressWarnings({"unchecked"})
                List<Object[]> addresses = query.setParameter("address_id_v", address1.getId()).getResultList();
                assertEquals("Incorrect number of addresses returned", 1, addresses.size());
                Object[] addressContent = addresses.get(0);
                assertEquals("Incorrect data content size", 6, addressContent.length);
                assertEquals("Id content incorrect", addressContent[0], (long) address1.getId());
                assertEquals("Steet content incorrect", addressContent[1], address1.getStreet());
                assertEquals("City content incorrect", addressContent[2], address1.getCity());
                assertEquals("Country content incorrect", addressContent[3], address1.getCountry());
                assertEquals("Province content incorrect", addressContent[4], address1.getProvince());
                assertEquals("Postal Code content incorrect", addressContent[5], address1.getPostalCode());
            } finally {
                closeEntityManagerAndTransaction(em);
            }
        }
    }

    /**
     * Test multiple result sets by setting the SQL results set mapping from code.
     */
    public void testQueryWithMultipleResultsFromCode() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            // SQL result set mapping for employee.
            SQLResultSetMapping employeeResultSetMapping = new SQLResultSetMapping("EmployeeResultSetMapping");
            employeeResultSetMapping.addResult(new EntityResult(Employee.class));

            // SQL result set mapping for address.
            SQLResultSetMapping addressResultSetMapping = new SQLResultSetMapping("AddressResultSetMapping");
            addressResultSetMapping.addResult(new EntityResult(Address.class));

            // SQL result set mapping for project (using inheritance and more complex result)
            SQLResultSetMapping projectResultSetMapping = new SQLResultSetMapping("ProjectResultSetMapping");
            EntityResult projectEntityResult = new EntityResult(Project.class);
            projectResultSetMapping.addResult(projectEntityResult);
            projectEntityResult = new EntityResult(SmallProject.class);
            projectEntityResult.addFieldResult(new FieldResult("id", "SMALL_ID"));
            projectEntityResult.addFieldResult(new FieldResult("name", "SMALL_NAME"));
            projectEntityResult.addFieldResult(new FieldResult("description", "SMALL_DESCRIPTION"));
            projectEntityResult.addFieldResult(new FieldResult("teamLeader", "SMALL_TEAMLEAD"));
            projectEntityResult.addFieldResult(new FieldResult("version", "SMALL_VERSION"));
            projectEntityResult.setDiscriminatorColumn("SMALL_DESCRIM");
            projectResultSetMapping.addResult(projectEntityResult);
            projectResultSetMapping.addResult(new ColumnResult("BUDGET_SUM"));

            // SQL result set mapping for employee using constructor results.
            SQLResultSetMapping employeeConstrustorResultSetMapping = new SQLResultSetMapping("EmployeeConstructorResultSetMapping");
            ConstructorResult constructorResult = new ConstructorResult(EmployeeDetails.class);
            ColumnResult columnResult = new ColumnResult("EMP_ID");
            columnResult.getColumn().setType(Integer.class);
            constructorResult.addColumnResult(columnResult);
            columnResult = new ColumnResult("F_NAME");
            columnResult.getColumn().setType(String.class);
            constructorResult.addColumnResult(columnResult);
            columnResult = new ColumnResult("L_NAME");
            columnResult.getColumn().setType(String.class);
            constructorResult.addColumnResult(columnResult);
            columnResult = new ColumnResult("R_COUNT");
            columnResult.getColumn().setType(Integer.class);
            constructorResult.addColumnResult(columnResult);
            employeeConstrustorResultSetMapping.addResult(constructorResult);

            StoredProcedureCall call = new StoredProcedureCall();
            call.setProcedureName("Read_Multiple_Result_Sets");
            call.setHasMultipleResultSets(true);
            call.setReturnMultipleResultSetCollections(true);

            ResultSetMappingQuery query = new ResultSetMappingQuery(call);
            query.addSQLResultSetMapping(employeeResultSetMapping);
            query.addSQLResultSetMapping(addressResultSetMapping);
            query.addSQLResultSetMapping(projectResultSetMapping);
            query.addSQLResultSetMapping(employeeConstrustorResultSetMapping);

            @SuppressWarnings({"unchecked"})
            List<List<?>> allResults = (List<List<?>>) getPersistenceUnitServerSession().executeQuery(query);
            assertNotNull("No results returned", allResults);
            assertEquals("Incorrect number of results returned", 4, allResults.size());

            // Verify first result set mapping --> Employee
            List<?> results0 = allResults.get(0);
            assertNotNull("No Employee results returned", results0);
            assertTrue("Empty Employee results returned", !results0.isEmpty());

            // Verify second result set mapping --> Address
            List<?> results1 = allResults.get(1);
            assertNotNull("No Address results returned", results1);
            assertTrue("Empty Address results returned", !results1.isEmpty());

            // Verify third result set mapping --> Project
            List<?> results2 = allResults.get(2);
            assertNotNull("No Project results returned", results2);
            assertTrue("Empty Project results returned", !results2.isEmpty());

            for (Object result2 : results2) {
                Object[] result2Element = (Object[]) result2;
                assertTrue("Failed to Return 3 items", (result2Element.length == 3));
                // Using Number as Different db/drivers  can return different types
                // e.g. Oracle with ijdbc14.jar returns BigDecimal where as Derby
                // with derbyclient.jar returns Double. NOTE: the order of checking
                // here is valid and as defined by the spec.
                assertTrue("Failed to return LargeProject", (result2Element[0] instanceof LargeProject));
                assertTrue("Failed To Return SmallProject", (result2Element[1] instanceof SmallProject));
                assertTrue("Failed to return column",(result2Element[2] instanceof Number));
                Assert.assertNotEquals("Returned same data in both result elements", ((SmallProject) result2Element[1]).getName(), ((LargeProject) result2Element[0]).getName());
            }

            // Verify fourth result set mapping --> Employee Constructor Result
            List<?> results3 = allResults.get(3);
            assertNotNull("No Employee constructor results returned", results3);
            assertTrue("Empty Employee constructor results returned", !results3.isEmpty());
        }
    }

    /**
     * Tests a StoredProcedureQuery using a result-set mapping though EM API
     */
    public void testQueryWithNamedFieldResult() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            try {
                beginTransaction(em);

                Address address = new Address();
                address.setCity("Winnipeg");
                address.setPostalCode("R3B 1B9");
                address.setProvince("MB");
                address.setStreet("510 Main Street");
                address.setCountry("Canada");
                em.persist(address);
                em.flush();

                // Clear the cache
                em.clear();
                clearCache();

                StoredProcedureQuery query = em.createStoredProcedureQuery("Read_Address_Mapped_Named", "address-column-result-map");
                query.registerStoredProcedureParameter("address_id_v", Integer.class, ParameterMode.IN);

                Object[] values = (Object[]) query.setParameter("address_id_v", address.getId()).getSingleResult();
                assertTrue("Address data not found or returned using stored procedure", ((values != null) && (values.length == 6)));
                assertNotNull("No results returned from store procedure call", values[1]);
                assertEquals("Address not found using stored procedure", address.getStreet(), values[1]);
            } finally {
                closeEntityManagerAndTransaction(em);
            }
        }
    }

    /**
     * Tests a StoredProcedureQuery using a result-set mapping though EM API
     */
    public void testQueryWithNamedFieldResultTranslationIntoNumbered() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            Map<String, String> properties = new HashMap<>();
            properties.put(PersistenceUnitProperties.NAMING_INTO_INDEXED, "true");
            EntityManager em = createEntityManager(properties);
            QuerySQLTracker querySQLTracker = new QuerySQLTracker(em.unwrap(ServerSession.class));

            try {
                beginTransaction(em);

                Address address = new Address();
                address.setCity("Winnipeg");
                address.setPostalCode("R3B 1B9");
                address.setProvince("MB");
                address.setStreet("510 Main Street");
                address.setCountry("Canada");
                em.persist(address);
                em.flush();

                // Clear the cache
                em.clear();
                clearCache();

                StoredProcedureQuery query = em.createStoredProcedureQuery("Read_Address_Mapped_Named", "address-column-result-map");
                query.registerStoredProcedureParameter("address_id_v", Integer.class, ParameterMode.IN);

                Object[] values = (Object[]) query.setParameter("address_id_v", address.getId()).getSingleResult();
                assertTrue("Address data not found or returned using stored procedure", ((values != null) && (values.length == 6)));
                assertNotNull("No results returned from store procedure call", values[1]);
                assertEquals("Address not found using stored procedure", address.getStreet(), values[1]);

                String sqlStatement = querySQLTracker.getSqlStatements().get(0);
                int count = (sqlStatement.split("=>", -1).length) - 1;
                assertEquals("Transformation into numbered parameters was not called", 1, count);
            } finally {
                closeEntityManagerAndTransaction(em);
            }
        }
    }

    /**
     * Tests a NamedStoredProcedureQuery using a result-set-mapping using
     * positional parameters (and more than the procedure expects).
     */
    public void testQueryWithNumberedFieldResult() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            try {
                beginTransaction(em);

                Address address1 = new Address();
                address1.setCity("Ottawa");
                address1.setPostalCode("K2J 0L7");
                address1.setProvince("ON");
                address1.setStreet("321 Main");
                address1.setCountry("Canada");
                em.persist(address1);
                em.flush();

                // Clear the cache
                em.clear();
                clearCache();

                StoredProcedureQuery query = em.createNamedStoredProcedureQuery("ReadAddressMappedNumberedFieldResult");
                @SuppressWarnings({"unchecked"})
                List<Address> addresses = query.setParameter(1, address1.getId()).getResultList();

                assertEquals("Too many addresses returned", 1, addresses.size());

                Address address2 = addresses.get(0);

                assertNotNull("Address returned from stored procedure is null", address2);
                assertTrue("Address didn't build correctly using stored procedure", (address1.getId() == address2.getId()));
                assertEquals("Address didn't build correctly using stored procedure", address1.getStreet(), address2.getStreet());
                assertEquals("Address didn't build correctly using stored procedure", address1.getCountry(), address2.getCountry());
                assertEquals("Address didn't build correctly using stored procedure", address1.getProvince(), address2.getProvince());
                assertEquals("Address didn't build correctly using stored procedure", address1.getPostalCode(), address2.getPostalCode());
            } finally {
                closeEntityManagerAndTransaction(em);
            }
        }
    }

    /**
     * Tests a StoredProcedureQuery using a class though EM API
     */
    public void testQueryWithResultClass() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            try {
                beginTransaction(em);

                Address address1 = new Address();
                address1.setCity("Victoria");
                address1.setPostalCode("V9A 6A9");
                address1.setProvince("BC");
                address1.setStreet("785 Lampson Street");
                address1.setCountry("Canada");
                em.persist(address1);
                em.flush();

                // Clear the cache
                em.clear();
                clearCache();

                StoredProcedureQuery query = em.createStoredProcedureQuery("Read_Address", org.eclipse.persistence.testing.models.jpa21.advanced.Address.class);
                query.registerStoredProcedureParameter("address_id_v", Integer.class, ParameterMode.IN);

                Address address2 = (Address) query.setParameter("address_id_v", address1.getId()).getSingleResult();
                assertNotNull("Address returned from stored procedure is null", address2);
                assertTrue("Address didn't build correctly using stored procedure", (address1.getId() == address2.getId()));
                assertEquals("Address didn't build correctly using stored procedure", address1.getStreet(), address2.getStreet());
                assertEquals("Address didn't build correctly using stored procedure", address1.getCountry(), address2.getCountry());
                assertEquals("Address didn't build correctly using stored procedure", address1.getProvince(), address2.getProvince());
                assertEquals("Address didn't build correctly using stored procedure", address1.getPostalCode(), address2.getPostalCode());
            } finally {
                closeEntityManagerAndTransaction(em);
            }
        }
    }

    /**
     * Tests a StoredProcedureQuery using a class though EM API
     */
    public void testQueryWithOutParam() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            try {
                beginTransaction(em);

                Address address = new Address();
                address.setCity("TestCity");
                address.setPostalCode("V4U 1P2");
                address.setProvince("Nunavut");
                address.setStreet("269 Lust Lane");
                address.setCountry("Canada");
                em.persist(address);
                em.flush();

                // Clear the cache
                em.clear();
                clearCache();

                StoredProcedureQuery query = em.createStoredProcedureQuery("Read_Address_City");
                query.registerStoredProcedureParameter("address_id_v", Integer.class, ParameterMode.IN);
                query.registerStoredProcedureParameter("city_v", String.class, ParameterMode.OUT);

                boolean result = query.setParameter("address_id_v", address.getId()).execute();
                String city = (String) query.getOutputParameterValue("city_v");
                assertEquals("Incorrect city was returned.", address.getCity(), city);
            } finally {
                closeEntityManagerAndTransaction(em);
            }
        }
    }

    /**
     * Test stored procedure parameter API.
     */
    public void testStoredProcedureParameterAPI() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            try {
                StoredProcedureQuery query = em.createStoredProcedureQuery("Parameter_Testing");
                query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
                query.registerStoredProcedureParameter(2, Integer.class, ParameterMode.INOUT);
                query.registerStoredProcedureParameter(3, Integer.class, ParameterMode.OUT);

                query.setParameter(1, "1");
                query.setParameter(2, 2);

                // Make this call to test the getParameter call with a position.
                Parameter<?> param1 = query.getParameter(1);
                Parameter<?> param2 = query.getParameter(2);
                Parameter<?> param3 = query.getParameter(3);

            } catch (IllegalArgumentException e) {
                if (isTransactionActive(em)){
                    rollbackTransaction(em);
                }

                fail("IllegalArgumentException was caught");
            } finally {
                closeEntityManager(em);
            }
        }
    }

    /**
     * Tests a StoredProcedureQuery using a system cursor. Also tests
     * getParameters call BEFORE query execution.
     */
    public void testStoredProcedureQuerySysCursor_Named() {
        if (supportsStoredProcedures() && getPlatform().isOracle() ) {
            EntityManager em = createEntityManager();

            try {
                StoredProcedureQuery query = em.createStoredProcedureQuery("Read_Using_Sys_Cursor", Employee.class);
                query.registerStoredProcedureParameter("f_name_v", String.class, ParameterMode.IN);
                query.registerStoredProcedureParameter("p_recordset", void.class, ParameterMode.REF_CURSOR);

                // Test the getParameters call BEFORE query execution.
                assertEquals("The number of parameters returned was incorrect, actual: " + query.getParameters().size() + ", expected 2", 2, query.getParameters().size());

                query.setParameter("f_name_v", "Fred");

                boolean execute = query.execute();

                assertTrue("Execute returned false.", execute);

                @SuppressWarnings({"unchecked"})
                List<Employee> employees = (List<Employee>) query.getOutputParameterValue("p_recordset");
                assertFalse("No employees were returned", employees.isEmpty());
            } finally {
                closeEntityManagerAndTransaction(em);
            }
        }
    }

    /**
     * Tests a StoredProcedureQuery using a system cursor. Also tests
     * getParameters call BEFORE query execution.
     */
    public void testStoredProcedureQuerySysCursor_Positional() {
        if (supportsStoredProcedures() && getPlatform().isOracle() ) {
            EntityManager em = createEntityManager();

            try {
                StoredProcedureQuery query = em.createStoredProcedureQuery("Read_Using_Sys_Cursor", Employee.class);
                query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
                query.registerStoredProcedureParameter(2, void.class, ParameterMode.REF_CURSOR);

                // Test the getParameters call BEFORE query execution.
                assertEquals("The number of parameters returned was incorrect, actual: " + query.getParameters().size() + ", expected 2", 2, query.getParameters().size());

                query.setParameter(1, "Fred");

                boolean execute = query.execute();

                assertTrue("Execute returned false.", execute);

                @SuppressWarnings({"unchecked"})
                List<Employee> employees = (List<Employee>) query.getOutputParameterValue(2);
                assertFalse("No employees were returned", employees.isEmpty());
            } finally {
                closeEntityManager(em);
            }
        }
    }

    /**
     * Tests a StoredProcedureQuery using a system cursor. Also tests
     * getParameters call AFTER query execution. Parameters are passed via name.
     */
    public void testStoredProcedureQuerySysCursor_ResultList_Named() {
        if (supportsStoredProcedures() && getPlatform().isOracle() ) {
            EntityManager em = createEntityManager();

            try {
                // Test stored procedure query created through API. //
                beginTransaction(em);

                StoredProcedureQuery query = em.createStoredProcedureQuery("Read_Using_Sys_Cursor");
                query.registerStoredProcedureParameter("f_name_v", String.class, ParameterMode.IN);
                query.registerStoredProcedureParameter("p_recordset", void.class, ParameterMode.REF_CURSOR);

                query.setParameter("f_name_v", "Fred");

                boolean execute = query.execute();

                assertTrue("Execute returned false.", execute);

                // Test the getParameters call AFTER query execution.
                assertEquals("The number of paramters returned was incorrect, actual: " + query.getParameters().size() + ", expected 2", 2, query.getParameters().size());

                @SuppressWarnings({"unchecked"})
                List<Employee> employees = (List<Employee>)query.getResultList();
                assertFalse("No employees were returned", employees.isEmpty());

                commitTransaction(em);

                // Test now with the named stored procedure. //
                beginTransaction(em);

                StoredProcedureQuery query2 = em.createNamedStoredProcedureQuery("ReadUsingNamedSysCursor");
                query2.setParameter("f_name_v", "Fred");

                boolean execute2 = query2.execute();

                @SuppressWarnings({"unchecked"})
                List<Employee> employees2 = (List<Employee>)query2.getResultList();
                assertFalse("No employees were returned from name stored procedure query.", employees2.isEmpty());

                commitTransaction(em);
            } finally {
                closeEntityManagerAndTransaction(em);
            }
        }
    }

    /**
     * Tests a StoredProcedureQuery using a system cursor. Also tests
     * getParameters call AFTER query execution. Parameters are passed via position.
     */
    public void testStoredProcedureQuerySysCursor_ResultList_Positional() {
        if (supportsStoredProcedures() && getPlatform().isOracle() ) {
            EntityManager em = createEntityManager();

            try {
                // Test stored procedure query created through API. //
                beginTransaction(em);

                StoredProcedureQuery query = em.createStoredProcedureQuery("Read_Using_Sys_Cursor");
                query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
                query.registerStoredProcedureParameter(2, void.class, ParameterMode.REF_CURSOR);

                query.setParameter(1, "Fred");

                boolean execute = query.execute();

                assertTrue("Execute returned false.", execute);

                // Test the getParameters call AFTER query execution.
                assertEquals("The number of paramters returned was incorrect, actual: " + query.getParameters().size() + ", expected 2", 2, query.getParameters().size());

                @SuppressWarnings({"unchecked"})
                List<Employee> employees = (List<Employee>)query.getResultList();
                assertFalse("No employees were returned", employees.isEmpty());

                commitTransaction(em);

                // Test now with the named stored procedure. //
                beginTransaction(em);

                StoredProcedureQuery query2 = em.createNamedStoredProcedureQuery("ReadUsingUnNamedSysCursor");
                query2.setParameter(1, "Fred");

                boolean execute2 = query2.execute();

                @SuppressWarnings({"unchecked"})
                List<Employee> employees2 = (List<Employee>)query2.getResultList();
                assertFalse("No employees were returned from name stored procedure query.", employees2.isEmpty());

                commitTransaction(em);
            } finally {
                closeEntityManagerAndTransaction(em);
            }
        }
    }

    /**
     * Tests StoredProcedureQuery exception wrapping.
     */
    public void testStoredProcedureQueryExceptionWrapping1() {
        EntityManager em = createEntityManager();
        try {
            jakarta.persistence.Query query = em.createNativeQuery("DoesNotExist", Employee.class);

            Object execute = query.getResultList();
            fail("Executing a bad native SQL query did not throw a PersistenceException and instead returned: "+execute);
        } catch (jakarta.persistence.PersistenceException pe) {
            //expected.
        } catch (RuntimeException re) {
            fail("Executing a bad native SQL query did not throw a PersistenceException and instead threw: "+re);
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * Tests StoredProcedureQuery exception wrapping.
     */
    public void testStoredProcedureQueryExceptionWrapping2() {
        EntityManager em = createEntityManager();
        try {
            StoredProcedureQuery query = em.createStoredProcedureQuery("DoesNotExist", Employee.class);

            boolean execute = query.execute();
            fail("Executing a non-existent stored procedure did not throw a PersistenceException and instead returned: "+execute);
        } catch (jakarta.persistence.PersistenceException pe) {
            //expected.
        } catch (RuntimeException re) {
            fail("Executing a non-existent stored procedure did not throw a PersistenceException and instead threw: "+re);
        } finally {
            closeEntityManager(em);
        }
    }
}
