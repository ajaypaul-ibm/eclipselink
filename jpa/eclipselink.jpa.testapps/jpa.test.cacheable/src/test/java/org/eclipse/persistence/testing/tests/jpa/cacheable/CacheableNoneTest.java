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
package org.eclipse.persistence.testing.tests.jpa.cacheable;

import junit.framework.*;

/*
 * The test is testing against "NONE" persistence unit which has <shared-cache-mode> to be NONE
 */
public class CacheableNoneTest extends CacheableTestBase {

    public CacheableNoneTest() {
        super();
    }

    public CacheableNoneTest(String name) {
        super(name);
    }

    @Override
    public String getPersistenceUnitName() {
        return "NONE";
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.setName("CacheableNoneTest");

        suite.addTest(new CacheableNoneTest("testSetup"));
        suite.addTest(new CacheableNoneTest("testCachingOnNONE"));
        suite.addTest(new CacheableNoneTest("testDetailsOrder_Isolated"));
        suite.addTest(new CacheableNoneTest("testDetailsOrder_Isolated_BeginEarlyTransaction"));

        return suite;
    }

    /**
     * Verifies the cacheable settings when caching (from persistence.xml) is set to NONE using {@code <shared-cache-mode>}.
     */
    public void testCachingOnNONE() {
        assertCachingOnNONE(getPersistenceUnitServerSession());
    }

}
