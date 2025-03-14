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
package org.eclipse.persistence.testing.tests.jpa21.advanced.xml;

import jakarta.persistence.EntityManager;
import jakarta.persistence.StoredProcedureQuery;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.persistence.testing.framework.jpa.junit.JUnitTestCase;
import org.eclipse.persistence.testing.models.jpa21.advanced.xml.Address;
import org.eclipse.persistence.testing.models.jpa21.advanced.xml.AdvancedTableCreator;
import org.eclipse.persistence.testing.models.jpa21.advanced.xml.Employee;
import org.eclipse.persistence.testing.models.jpa21.advanced.xml.LargeProject;
import org.eclipse.persistence.testing.models.jpa21.advanced.xml.SmallProject;
import org.junit.Assert;

import java.util.List;

public class XMLNamedStoredProcedureQueryTest extends JUnitTestCase {
    public XMLNamedStoredProcedureQueryTest() {}

    public XMLNamedStoredProcedureQueryTest(String name) {
        super(name);
        setPuName(getPersistenceUnitName());
    }

    /**
     * Return the the persistence unit name for this test suite.
     */
    @Override
    public String getPersistenceUnitName() {
        return "advanced2x-xml";
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.setName("XMLNamedStoredProcedureQueryTest");

        suite.addTest(new XMLNamedStoredProcedureQueryTest("testSetup"));
        suite.addTest(new XMLNamedStoredProcedureQueryTest("testQueryExecuteUpdateOnSelectQueryWithNoResultClass"));
        suite.addTest(new XMLNamedStoredProcedureQueryTest("testQueryExecuteUpdateOnSelectQueryWithResultClass"));
        suite.addTest(new XMLNamedStoredProcedureQueryTest("testQueryExecuteWithNamedCursors"));
        suite.addTest(new XMLNamedStoredProcedureQueryTest("testQueryExecuteWithUnNamedCursor"));
        suite.addTest(new XMLNamedStoredProcedureQueryTest("testQueryGetResultListWithNamedCursors"));
        suite.addTest(new XMLNamedStoredProcedureQueryTest("testQueryWithMultipleResults"));
        suite.addTest(new XMLNamedStoredProcedureQueryTest("testQueryWithNamedColumnResult"));
        suite.addTest(new XMLNamedStoredProcedureQueryTest("testQueryWithNamedFieldResult"));
        suite.addTest(new XMLNamedStoredProcedureQueryTest("testQueryWithNumberedFieldResult"));
        suite.addTest(new XMLNamedStoredProcedureQueryTest("testQueryWithResultClass"));

        return suite;
    }

    /**
     * The setup is done as a test, both to record its failure, and to allow execution in the server.
     */
    public void testSetup() {
        new AdvancedTableCreator().replaceTables(getPersistenceUnitServerSession());
        org.eclipse.persistence.testing.tests.jpa21.advanced.xml.EmployeePopulator employeePopulator = new EmployeePopulator();
        employeePopulator.buildExamples();
        employeePopulator.persistExample(getPersistenceUnitServerSession());
        clearCache();
    }

    /**
     * Tests an execute update on a named stored procedure that does a select.
     * NamedStoredProcedure defines a result class.
     */
    public void testQueryExecuteUpdateOnSelectQueryWithNoResultClass() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            try {
                beginTransaction(em);
                em.createNamedStoredProcedureQuery("XMLReadAllAddressesWithNoResultClass").executeUpdate();
                commitTransaction(em);
            } catch (IllegalStateException e) {
                if (isTransactionActive(em)){
                    rollbackTransaction(em);
                }

                return;
            } finally {
                closeEntityManager(em);
            }

            fail("Expected Illegal state exception was not thrown.");
        }
    }

    /**
     * Tests an execute update on a named stored procedure that does a select.
     * NamedStoredProcedure defines a result class.
     */
    public void testQueryExecuteUpdateOnSelectQueryWithResultClass() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            try {
                beginTransaction(em);
                em.createNamedStoredProcedureQuery("XMLReadAddressWithResultClass").setParameter("address_id_v", 1).executeUpdate();
                commitTransaction(em);
            } catch (IllegalStateException e) {
                if (isTransactionActive(em)){
                    rollbackTransaction(em);
                }

                return;
            } finally {
                closeEntityManager(em);
            }

            fail("Expected Illegal state exception was not thrown.");
        }
    }

    /**
     * Tests a StoredProcedureQuery using multiple names cursors.
     */
    public void testQueryExecuteWithNamedCursors() {
        if (supportsStoredProcedures() && getPlatform().isOracle() ) {
            EntityManager em = createEntityManager();

            try {
                StoredProcedureQuery query = em.createNamedStoredProcedureQuery("XMLReadUsingNamedRefCursors");

                boolean returnVal = query.execute();

                @SuppressWarnings({"unchecked"})
                List<Employee> employees = (List<Employee>) query.getOutputParameterValue("CUR1");
                assertFalse("No employees were returned", employees.isEmpty());
                @SuppressWarnings({"unchecked"})
                List<Address> addresses = (List<Address>) query.getOutputParameterValue("CUR2");
                assertFalse("No addresses were returned", addresses.isEmpty());
            } catch (RuntimeException e) {
                if (isTransactionActive(em)){
                    rollbackTransaction(em);
                }

                throw e;
            } finally {
                closeEntityManager(em);
            }
        }
    }

    /**
     * Tests a StoredProcedureQuery with an unnamed cursor.
     */
    public void testQueryExecuteWithUnNamedCursor() {
        if (supportsStoredProcedures() && getPlatform().isOracle() ) {
            EntityManager em = createEntityManager();

            try {
                StoredProcedureQuery query = em.createNamedStoredProcedureQuery("XMLReadUsingUnNamedRefCursor");

                boolean returnVal = query.execute();

                @SuppressWarnings({"unchecked"})
                List<Employee> employees = (List<Employee>) query.getOutputParameterValue(1);
                assertFalse("No employees were returned", employees.isEmpty());
            } catch (RuntimeException e) {
                if (isTransactionActive(em)){
                    rollbackTransaction(em);
                }

                throw e;
            } finally {
                closeEntityManager(em);
            }
        }
    }

    /**
     * Tests a StoredProcedureQuery on Oracle using named ref cursors.
     */
    public void testQueryGetResultListWithNamedCursors() {
        if (supportsStoredProcedures() && getPlatform().isOracle() ) {
            EntityManager em = createEntityManager();

            try {
                StoredProcedureQuery query = em.createNamedStoredProcedureQuery("XMLReadUsingNamedRefCursors");

                @SuppressWarnings({"unchecked"})
                List<Employee> employees = (List<Employee>) query.getResultList();
                assertFalse("No employees were returned", employees.isEmpty());

                @SuppressWarnings({"unchecked"})
                List<Address> addresses = (List<Address>) query.getResultList();
                assertFalse("No addresses were returned", addresses.isEmpty());
            } catch (RuntimeException e) {
                if (isTransactionActive(em)){
                    rollbackTransaction(em);
                }

                throw e;
            } finally {
                closeEntityManager(em);
            }
        }
    }

    /**
     * Test multiple result sets by setting the SQL results set mapping from annotation.
     */
    public void testQueryWithMultipleResults() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            StoredProcedureQuery multipleResultSetQuery = createEntityManager().createNamedStoredProcedureQuery("XMLReadUsingMultipleResultSetMappings");

            // Verify first result set mapping --> Employee
            List<?> results = multipleResultSetQuery.getResultList();
            assertNotNull("No Employee results returned", results);
            assertTrue("Empty Employee results returned", !results.isEmpty());

            // Verify second result set mapping --> Address
            assertTrue("Address results not available", multipleResultSetQuery.hasMoreResults());
            results = multipleResultSetQuery.getResultList();
            assertNotNull("No Address results returned", results);
            assertTrue("Empty Address results returned", !results.isEmpty());

            // Verify third result set mapping --> Project
            assertTrue("Projects results not available", multipleResultSetQuery.hasMoreResults());
            results = multipleResultSetQuery.getResultList();
            assertNotNull("No Project results returned", results);
            assertTrue("Empty Project results returned", !results.isEmpty());

            for (Object result : results) {
                Object[] resultElement = (Object[]) result;
                assertTrue("Failed to Return 3 items", (resultElement.length == 3));
                // Using Number as Different db/drivers  can return different types
                // e.g. Oracle with ijdbc14.jar returns BigDecimal where as Derby
                // with derbyclient.jar returns Double. NOTE: the order of checking
                // here is valid and as defined by the spec.
                assertTrue("Failed to return LargeProject", (resultElement[0] instanceof LargeProject) );
                assertTrue("Failed To Return SmallProject", (resultElement[1] instanceof SmallProject) );
                assertTrue("Failed to return column",(resultElement[2] instanceof Number) );
                Assert.assertNotEquals("Returned same data in both result elements", ((SmallProject) resultElement[1]).getName(), ((LargeProject) resultElement[0]).getName());
            }

            // Verify fourth result set mapping --> Employee Constructor Result
            assertTrue("Employee constructor results not available", multipleResultSetQuery.hasMoreResults());
            results = multipleResultSetQuery.getResultList();
            assertNotNull("No Employee constructor results returned", results);
            assertTrue("Empty Employee constructor results returned", !results.isEmpty());

            // Verify there as no more results available
            assertFalse("More results available", multipleResultSetQuery.hasMoreResults());
        }
    }

    /**
     * Tests a NamedStoredProcedureQuery annotation using a result-set mapping.
     */
    public void testQueryWithNamedColumnResult() {
        // TODO: investigate if this test should work on Oracle as written.
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            try {
                beginTransaction(em);

                Address address = new Address();
                address.setCity("Ottawa");
                address.setPostalCode("K1G 6P3");
                address.setProvince("ON");
                address.setStreet("123 Street");
                address.setCountry("Canada");
                em.persist(address);
                commitTransaction(em);

                // Clear the cache
                em.clear();
                clearCache();

                Object[] values = (Object[]) em.createNamedStoredProcedureQuery("XMLReadAddressMappedNamedColumnResult").setParameter("address_id_v", address.getId()).getSingleResult();
                assertTrue("Address data not found or returned using stored procedure", ((values != null) && (values.length == 6)));
                assertNotNull("No results returned from store procedure call", values[1]);
                assertEquals("Address not found using stored procedure", address.getStreet(), values[1]);
            } catch (RuntimeException e) {
                if (isTransactionActive(em)){
                    rollbackTransaction(em);
                }

                throw e;
            } finally {
                closeEntityManager(em);
            }
        }
    }

    /**
     * Tests a NamedStoredProcedureQuery using a result-set mapping.
     */
    public void testQueryWithNamedFieldResult() {
        // TODO: investigate if this test should work on Oracle as written.
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            try {
                beginTransaction(em);

                Address address1 = new Address();
                address1.setCity("Ottawa");
                address1.setPostalCode("K1G 6P3");
                address1.setProvince("ON");
                address1.setStreet("123 Street");
                address1.setCountry("Canada");

                em.persist(address1);
                commitTransaction(em);

                // Clear the cache
                em.clear();
                clearCache();

                Address address2 = (Address) em.createNamedStoredProcedureQuery("XMLReadAddressMappedNamedFieldResult").setParameter("address_id_v", address1.getId()).getSingleResult();
                assertNotNull("Address returned from stored procedure is null", address2);
                assertTrue("Address not found using stored procedure", (address1.getId() == address2.getId()));
            } catch (RuntimeException e) {
                if (isTransactionActive(em)){
                    rollbackTransaction(em);
                }

                throw e;
            } finally {
                closeEntityManager(em);
            }
        }
    }

    /**
     * Tests a NamedStoredProcedureQuery using positional paramters.
     */
    public void testQueryWithNumberedFieldResult() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            try {
                beginTransaction(em);

                Address address1 = new Address();
                address1.setCity("Ottawa");
                address1.setPostalCode("K1G 6P3");
                address1.setProvince("ON");
                address1.setStreet("123 Street");
                address1.setCountry("Canada");
                em.persist(address1);
                commitTransaction(em);

                // Clear the cache
                em.clear();
                clearCache();

                Address address2 = (Address) em.createNamedStoredProcedureQuery("XMLReadAddressMappedNumberedFieldResult").setParameter(1, address1.getId()).getSingleResult();
                assertNotNull("Address returned from stored procedure is null", address2);
                assertTrue("Address didn't build correctly using stored procedure", (address1.getId() == address2.getId()));
                assertEquals("Address didn't build correctly using stored procedure", address1.getStreet(), address2.getStreet());
                assertEquals("Address didn't build correctly using stored procedure", address1.getCountry(), address2.getCountry());
                assertEquals("Address didn't build correctly using stored procedure", address1.getProvince(), address2.getProvince());
                assertEquals("Address didn't build correctly using stored procedure", address1.getPostalCode(), address2.getPostalCode());
            } catch (RuntimeException e) {
                if (isTransactionActive(em)){
                    rollbackTransaction(em);
                }

                throw e;
            } finally {
                closeEntityManager(em);
            }
        }
    }

    /**
     * Tests a NamedStoredProcedureQuery using a result-class.
     */
    public void testQueryWithResultClass() {
        if (supportsStoredProcedures() && getPlatform().isMySQL()) {
            EntityManager em = createEntityManager();

            try {
                beginTransaction(em);

                Address address1 = new Address();
                address1.setCity("Ottawa");
                address1.setPostalCode("K1G 6P3");
                address1.setProvince("ON");
                address1.setStreet("123 Street");
                address1.setCountry("Canada");
                em.persist(address1);
                commitTransaction(em);

                // Clear the cache
                em.clear();
                clearCache();

                Address address2 = (Address) em.createNamedStoredProcedureQuery("XMLReadAddressWithResultClass").setParameter("address_id_v", address1.getId()).getSingleResult();
                assertNotNull("Address returned from stored procedure is null", address2);
                assertTrue("Address didn't build correctly using stored procedure", (address1.getId() == address2.getId()));
                assertEquals("Address didn't build correctly using stored procedure", address1.getStreet(), address2.getStreet());
                assertEquals("Address didn't build correctly using stored procedure", address1.getCountry(), address2.getCountry());
                assertEquals("Address didn't build correctly using stored procedure", address1.getProvince(), address2.getProvince());
                assertEquals("Address didn't build correctly using stored procedure", address1.getPostalCode(), address2.getPostalCode());
            } catch (RuntimeException e) {
                if (isTransactionActive(em)){
                    rollbackTransaction(em);
                }

                throw e;
            } finally {
                closeEntityManager(em);
            }
        }
    }
}
