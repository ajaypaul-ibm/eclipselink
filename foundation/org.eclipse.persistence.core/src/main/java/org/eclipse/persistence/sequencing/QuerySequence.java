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
//     06/30/2011-2.3.1 Guy Pelletier
//       - 341940: Add disable/enable allowing native queries
package org.eclipse.persistence.sequencing;

import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.queries.DataModifyQuery;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.queries.ValueReadQuery;
import org.eclipse.persistence.sessions.DataRecord;

import java.math.BigDecimal;
import java.util.Vector;

/**
 * <p>
 * <b>Purpose</b>: An generic query sequence mechanism.
 * <p>
 * <b>Description</b>
 * This sequence allows the sequence operations to be customized through user defined queries.
 * A select and update query can be set which can use custom SQL or stored procedures to define the sequencing mechanism.
 * If a single stored procedure is used that does the update and select only the select query needs to be set.
 */
public class QuerySequence extends StandardSequence {
    protected ValueReadQuery selectQuery;
    protected DataModifyQuery updateQuery;
    protected boolean shouldAcquireValueAfterInsert;
    protected boolean shouldUseTransaction;
    protected boolean shouldSkipUpdate;
    protected boolean shouldSelectBeforeUpdate;
    protected boolean wasSelectQueryCreated;
    protected boolean wasUpdateQueryCreated;

    public QuerySequence() {
        super();
    }

    /**
     * Create a new sequence with the name.
     */
    public QuerySequence(String name) {
        super(name);
    }

    /**
     * Create a new sequence with the name and sequence pre-allocation size.
     */
    public QuerySequence(String name, int size) {
        super(name, size);
    }

    public QuerySequence(String name, int size, int initialValue) {
        super(name, size, initialValue);
    }

    public QuerySequence(boolean shouldAcquireValueAfterInsert, boolean shouldUseTransaction) {
        super();
        setShouldAcquireValueAfterInsert(shouldAcquireValueAfterInsert);
        setShouldUseTransaction(shouldUseTransaction);
    }

    public QuerySequence(String name, boolean shouldAcquireValueAfterInsert, boolean shouldUseTransaction) {
        super(name);
        setShouldAcquireValueAfterInsert(shouldAcquireValueAfterInsert);
        setShouldUseTransaction(shouldUseTransaction);
    }

    public QuerySequence(String name, int size, boolean shouldAcquireValueAfterInsert, boolean shouldUseTransaction) {
        super(name, size);
        setShouldAcquireValueAfterInsert(shouldAcquireValueAfterInsert);
        setShouldUseTransaction(shouldUseTransaction);
    }

    public QuerySequence(String name, int size, int initialValue,
            boolean shouldAcquireValueAfterInsert, boolean shouldUseTransaction) {
        super(name, size, initialValue);
        setShouldAcquireValueAfterInsert(shouldAcquireValueAfterInsert);
        setShouldUseTransaction(shouldUseTransaction);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QuerySequence other && super.equals(obj)) {
            return (getSelectQuery() == other.getSelectQuery())
                    && (getUpdateQuery() == other.getUpdateQuery())
                    && (shouldAcquireValueAfterInsert() == other.shouldAcquireValueAfterInsert())
                    && (shouldUseTransaction() == other.shouldUseTransaction())
                    && (shouldSkipUpdate() == other.shouldSkipUpdate())
                    && (shouldSelectBeforeUpdate() == other.shouldSelectBeforeUpdate());

        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        ValueReadQuery selectQuery = getSelectQuery();
        DataModifyQuery updateQuery = getUpdateQuery();        
        result = 31 * result + (selectQuery != null ? selectQuery.hashCode() : 0);
        result = 31 * result + (updateQuery != null ? updateQuery.hashCode() : 0);
        result = 31 * result + (shouldAcquireValueAfterInsert() ? 1 : 0);
        result = 31 * result + (shouldUseTransaction() ? 1 : 0);
        result = 31 * result + (shouldSkipUpdate() ? 1 : 0);
        result = 31 * result + (shouldSelectBeforeUpdate() ? 1 : 0);
        return result;
    }

    /**
    * PUBLIC:
    */
    @Override
    public boolean shouldAcquireValueAfterInsert() {
        return shouldAcquireValueAfterInsert;
    }

    /**
    * PUBLIC:
    */
    public void setShouldAcquireValueAfterInsert(boolean shouldAcquireValueAfterInsert) {
        this.shouldAcquireValueAfterInsert = shouldAcquireValueAfterInsert;
    }

    /**
    * PUBLIC:
    */
    @Override
    public boolean shouldUseTransaction() {
        return shouldUseTransaction;
    }

    /**
    * PUBLIC:
    */
    public void setShouldUseTransaction(boolean shouldUseTransaction) {
        this.shouldUseTransaction = shouldUseTransaction;
    }

    /**
    * PUBLIC:
    */
    public void setSelectQuery(ValueReadQuery query) {
        selectQuery = query;
    }

    /**
    * PUBLIC:
    */
    public ValueReadQuery getSelectQuery() {
        return selectQuery;
    }

    /**
    * PUBLIC:
    */
    public void setUpdateQuery(DataModifyQuery query) {
        updateQuery = query;
    }

    /**
    * PUBLIC:
    */
    public DataModifyQuery getUpdateQuery() {
        return updateQuery;
    }

    /**
    * PUBLIC:
    */
    public void setShouldSkipUpdate(boolean shouldSkipUpdate) {
        this.shouldSkipUpdate = shouldSkipUpdate;
    }

    /**
    * PUBLIC:
    */
    public boolean shouldSkipUpdate() {
        return shouldSkipUpdate;
    }

    /**
    * PUBLIC:
    */
    public void setShouldSelectBeforeUpdate(boolean shouldSelectBeforeUpdate) {
        this.shouldSelectBeforeUpdate = shouldSelectBeforeUpdate;
    }

    /**
    * PUBLIC:
    */
    public boolean shouldSelectBeforeUpdate() {
        return shouldSelectBeforeUpdate;
    }

    /**
    * INTERNAL:
    */
    protected ValueReadQuery buildSelectQuery() {
        return null;
    }

    /**
    * INTERNAL:
    */
    protected DataModifyQuery buildUpdateQuery() {
        return null;
    }

    /**
    * INTERNAL:
    */
    protected ValueReadQuery buildSelectQuery(String seqName, Integer size) {
        return null;
    }

    /**
    * INTERNAL:
    */
    protected DataModifyQuery buildUpdateQuery(String seqName, Number sizeOrNewValue) {
        return null;
    }

    /**
    * INTERNAL:
    */
    @Override
    public void onConnect() {
        super.onConnect();
        if (getSelectQuery() == null) {
            setSelectQuery(buildSelectQuery());
            wasSelectQueryCreated = getSelectQuery() != null;
            if (wasSelectQueryCreated) {
                getSelectQuery().setName(getName());
            }
        }
        if ((getUpdateQuery() == null) && !shouldSkipUpdate()) {
            setUpdateQuery(buildUpdateQuery());
            wasUpdateQueryCreated = getUpdateQuery() != null;
            if (wasUpdateQueryCreated) {
                getUpdateQuery().setName(getName());
            }
        }
    }

    /**
    * INTERNAL:
    */
    @Override
    public void onDisconnect() {
        if (wasSelectQueryCreated) {
            setSelectQuery(null);
            wasSelectQueryCreated = false;
        }
        if (wasUpdateQueryCreated) {
            setUpdateQuery(null);
            wasUpdateQueryCreated = false;
        }
        super.onDisconnect();
    }

    /**
    * INTERNAL:
    */
    @Override
    protected Number updateAndSelectSequence(Accessor accessor, AbstractSession writeSession, String seqName, int size) {
        Integer sizeInteger = size;
        if (shouldSkipUpdate()) {
            return (Number)select(accessor, writeSession, seqName, sizeInteger);
        } else {
            if (shouldSelectBeforeUpdate()) {
                Object result = select(accessor, writeSession, seqName, sizeInteger);
                BigDecimal currentValue;
                if (result instanceof Number) {
                    currentValue = BigDecimal.valueOf(((Number)result).longValue());
                } else if (result instanceof String) {
                    currentValue = new BigDecimal((String)result);
                } else if (result instanceof DataRecord) {
                    Object val = ((DataRecord)result).get("text()");
                    currentValue = new BigDecimal((String)val);
                } else {
                    // DatabaseException.errorPreallocatingSequenceNumbers() is thrown by the superclass
                    return null;
                }

                // Increment value
                BigDecimal newValue = currentValue.add(new BigDecimal(size));

                update(accessor, writeSession, seqName, newValue);
                return newValue;
            } else {
                update(accessor, writeSession, seqName, sizeInteger);
                return (Number)select(accessor, writeSession, seqName, sizeInteger);
            }
        }
    }

    /**
    * INTERNAL:
    */
    protected Object select(Accessor accessor, AbstractSession writeSession, String seqName, Integer size) {
        ValueReadQuery query = getSelectQuery();
        if (query != null) {
            if (accessor != null) {
                // PERF: Prepare the query before being cloned.
                // Also BUG: SQLCall could not be prepared concurrently by different queries.
                // Setting user define allow custom SQL query to be prepared without translation row.
                query.setIsUserDefined(true);
                query.checkPrepare(writeSession, null);
                query = (ValueReadQuery)query.clone();
                query.setAccessor(accessor);
            }
        } else {
            query = buildSelectQuery(seqName, size);
            if (accessor != null) {
                query.setAccessor(accessor);
            }
        }
        Vector<Object> args = createArguments(query, seqName, size);
        query.setIsUserDefinedSQLCall(false);
        if (args != null) {
            return writeSession.executeQuery(query, args);
        } else {
            return writeSession.executeQuery(query);
        }
    }

    /**
    * INTERNAL:
    */
    protected void update(Accessor accessor, AbstractSession writeSession, String seqName, Number sizeOrNewValue) {
        DataModifyQuery query = getUpdateQuery();
        if (query != null) {
            if (accessor != null) {
                // PERF: Prepare the query before being cloned.
                // Also BUG: SQLCall could not be prepared concurrently by different queries.
                // Setting user define allow custom SQL query to be prepared without translation row.
                query.setIsUserDefined(true);
                query.checkPrepare(writeSession, null);
                query = (DataModifyQuery)query.clone();
                query.setAccessor(accessor);
            }
        } else {
            query = buildUpdateQuery(seqName, sizeOrNewValue);
            if (query == null) {
                return;
            }
            if (accessor != null) {
                query.setAccessor(accessor);
            }
        }
        Vector<Object> args = createArguments(query, seqName, sizeOrNewValue);
        query.setIsUserDefinedSQLCall(false);
        if (args != null) {
            writeSession.executeQuery(query, args);
        } else {
            writeSession.executeQuery(query);
        }
    }

    /**
    * INTERNAL:
    */
    protected Vector<Object> createArguments(DatabaseQuery query, String seqName, Number sizeOrNewValue) {
        int nArgs = query.getArguments().size();
        if (nArgs > 0) {
            Vector<Object> args = new Vector<>(nArgs);
            args.addElement(seqName);
            if (nArgs > 1) {
                args.addElement(sizeOrNewValue);
            }
            return args;
        } else {
            return null;
        }
    }
}
