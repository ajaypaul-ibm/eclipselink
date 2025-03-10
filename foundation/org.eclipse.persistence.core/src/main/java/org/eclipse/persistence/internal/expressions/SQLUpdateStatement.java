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

import org.eclipse.persistence.exceptions.ValidationException;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.queries.SQLCall;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

/**
 * <p><b>Purpose</b>: Print UPDATE statement.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Print UPDATE statement.
 * </ul>
 *    @author Dorin Sandu
 *    @since TOPLink/Java 1.0
 */
public class SQLUpdateStatement extends SQLModifyStatement {

    /**
     * Append the string containing the SQL insert string for the given table.
     */
    @Override
    protected SQLCall buildCallWithoutReturning(AbstractSession session) {
        SQLCall call = new SQLCall();
        call.returnNothing();

        Writer writer = new CharArrayWriter(100);
        try {
            writer.write("UPDATE ");
            if (getHintString() != null) {
                writer.write(getHintString());
                writer.write(" ");
            }
            writer.write(getTable().getQualifiedNameDelimited(session.getPlatform()));
            writer.write(" SET ");

            ExpressionSQLPrinter printer = null;

            Vector fieldsForTable = new Vector();
            Iterator iterator = getModifyRow().getValues().iterator();
            Vector values = new Vector();
            for (Enumeration fieldsEnum = getModifyRow().keys(); fieldsEnum.hasMoreElements();) {
                DatabaseField field = (DatabaseField)fieldsEnum.nextElement();
                Object value = iterator.next();
                if (field.getTable().equals(getTable()) || (!field.hasTableName())) {
                    fieldsForTable.addElement(field);
                    values.addElement(value);
                }
            }

            if (fieldsForTable.isEmpty()) {
                return null;
            }

            for (int i = 0; i < fieldsForTable.size(); i++) {
                DatabaseField field = (DatabaseField)fieldsForTable.elementAt(i);
                writer.write(field.getNameDelimited(session.getPlatform()));
                writer.write(" = ");
                if(values.elementAt(i) instanceof Expression exp) {
                    // the value in the modify row is an expression - assign it.
                    if(printer == null) {
                        printer = new ExpressionSQLPrinter(session, getTranslationRow(), call, false, getBuilder());
                        printer.setWriter(writer);
                    }
                    printer.printExpression(exp);

                } else {
                    // the value in the modify row is ignored, the parameter corresponding to the key field will be assigned.
                    call.appendModify(writer, field);
                }

                if ((i + 1) < fieldsForTable.size()) {
                    writer.write(", ");
                }
            }

            if (!(getWhereClause() == null)) {
                writer.write(" WHERE ");
                if(printer == null) {
                    printer = new ExpressionSQLPrinter(session, getTranslationRow(), call, false, getBuilder());
                    printer.setWriter(writer);
                }
                printer.printExpression(getWhereClause());
            }

            call.setSQLString(writer.toString());
            return call;
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
    }
}
