/*
 * Copyright (c) 1998, 2022 Oracle and/or its affiliates. All rights reserved.
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
//     06/16/2009-2.0 Guy Pelletier
//       - 277039: JPA 2.0 Cache Usage Settings
//     07/16/2009-2.0 Guy Pelletier
//       - 277039: JPA 2.0 Cache Usage Settings
//     06/09/2010-2.0.3 Guy Pelletier
//       - 313401: shared-cache-mode defaults to NONE when the element value is unrecognized
//     06/19/2014-2.6: - Tomas Kraus (Oracle)
//       - Fixed PU name and registered this suite in FullRegressionTestSuite
//       - 437578: Tests to verify @Cacheable inheritance in JPA 2.1
package org.eclipse.persistence.testing.tests.jpa.xml.cacheable;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * The test is testing against "ENABLE_SELECTIVE" persistence unit which has <shared-cache-mode> to be ENABLE_SELECTIVE
 */
public class XmlCacheableEnableSelectiveTest extends XmlCacheableTestBase {

    public XmlCacheableEnableSelectiveTest() {
        super();
    }

    public XmlCacheableEnableSelectiveTest(String name) {
        super(name);
    }

    @Override
    public String getPersistenceUnitName() {
        return "ENABLE_SELECTIVE";
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.setName("XmlCacheableEnableSelectiveTest");

        suite.addTest(new XmlCacheableEnableSelectiveTest("testSetup"));
        suite.addTest(new XmlCacheableEnableSelectiveTest("testCachingOnENABLE_SELECTIVE"));

        return suite;
    }

    /**
     * Verifies the cacheable settings when caching (from persistence.xml) is set to ENABLE_SELECTIVE using {@code <shared-cache-mode>}.
     */
    public void testCachingOnENABLE_SELECTIVE() {
        assertCachingOnENABLE_SELECTIVE(getPersistenceUnitServerSession());
    }

}
