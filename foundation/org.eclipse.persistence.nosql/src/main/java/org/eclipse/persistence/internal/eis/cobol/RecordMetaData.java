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
package org.eclipse.persistence.internal.eis.cobol;

import java.util.*;

/**
* <b>Purpose</b>:  This class contains meta information for a record.
*/
public class RecordMetaData implements CompositeObject {

    /** a collection of fields that the record contains */
    private Vector<FieldMetaData> myFields;

    /** the name of the record */
    private String myName;

    /** constructor */
    public RecordMetaData() {
        initialize();
    }

    public RecordMetaData(String name) {
        initialize(name);
    }

    public RecordMetaData(String name, Vector<FieldMetaData> fields) {
        initialize(name, fields);
    }

    private void initialize() {
        myFields = new Vector<>();
    }

    private void initialize(String name) {
        myName = name;
        myFields = new Vector<>();
    }

    private void initialize(String name, Vector<FieldMetaData> fields) {
        myName = name;
        myFields = fields;
    }

    /** getter for record name */
    @Override
    public String getName() {
        return myName;
    }

    /** setter for record name */
    public void setName(String newName) {
        myName = newName;
    }

    /** getter for myFields */
    @Override
    public Vector<FieldMetaData> getFields() {
        return myFields;
    }

    /** setter for myFields */
    @Override
    public void setFields(Vector<FieldMetaData> newFields) {
        myFields = newFields;
    }

    /** adds the field to the collection */
    @Override
    public void addField(FieldMetaData newField) {
        myFields.addElement(newField);
    }

    /** since a record is by defintion is composite, this always returns true */
    public boolean isComposite() {
        return true;
    }

    /** retrieves the <code>FieldMetaData</code> with the corresponding name if it exists */
    @Override
    public FieldMetaData getFieldNamed(String fieldName) {
        Iterator<FieldMetaData> iterator = getFields().iterator();
        while (iterator.hasNext()) {
            FieldMetaData field = iterator.next();
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }
}
