/*
 * Copyright (c) 1998, 2024 Oracle and/or its affiliates. All rights reserved.
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
//     Oracle - initial API and implementation from Oracle TopLink
package org.eclipse.persistence.internal.expressions;

import org.eclipse.persistence.internal.databaseaccess.DatabaseCall;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.helper.DatabaseTable;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.queries.SQLCall;

import java.util.List;

/**
 * <p><b>Purpose</b>: Mirror SQL behavior.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Mirror SQL behavior.
 * <li> Print as SQL string.
 * </ul>
 *    @author Dorin Sandu
 *    @since TOPLink/Java 1.0
 */
public abstract class SQLModifyStatement extends SQLStatement {
    protected DatabaseTable table;
    protected AbstractRecord modifyRow;
    protected List<DatabaseField> returnFields;

    public AbstractRecord getModifyRow() {
        return modifyRow;
    }

    public List<DatabaseField> getReturnFields() {
        return returnFields;
    }

    public DatabaseTable getTable() {
        return table;
    }

    public void setModifyRow(AbstractRecord row) {
        modifyRow = row;
    }

    public void setReturnFields(List<DatabaseField> fields) {
        returnFields = fields;
    }

    public void setTable(DatabaseTable table) {
        this.table = table;
    }

    @Override
    public DatabaseCall buildCall(AbstractSession session) {
        SQLCall sqlCall = buildCallWithoutReturning(session);
        if ((getReturnFields() == null) || getReturnFields().isEmpty()) {
            return sqlCall;
        } else {
            return session.getPlatform().buildCallWithReturning(sqlCall, getReturnFields());
        }
    }

    protected SQLCall buildCallWithoutReturning(AbstractSession session) {
        return null;
    }
}
