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
package org.eclipse.persistence.eis;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.InteractionSpec;
import jakarta.resource.cci.MappedRecord;
import org.eclipse.persistence.eis.interactions.EISInteraction;
import org.eclipse.persistence.eis.interactions.QueryStringInteraction;
import org.eclipse.persistence.exceptions.QueryException;
import org.eclipse.persistence.exceptions.ValidationException;
import org.eclipse.persistence.internal.databaseaccess.DatasourceCall;
import org.eclipse.persistence.internal.databaseaccess.DatasourcePlatform;
import org.eclipse.persistence.internal.expressions.SQLStatement;
import org.eclipse.persistence.internal.helper.ClassConstants;
import org.eclipse.persistence.internal.helper.ConversionManager;
import org.eclipse.persistence.internal.oxm.XMLConversionManager;
import org.eclipse.persistence.internal.security.PrivilegedAccessHelper;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.queries.Call;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Vector;

/**
 * <p>An <code>EISPlatform</code> defines any EIS adapter specific behavior.
 * This may include:
 * <ul>
 * <li>Instantiation of the adapter InteractionSpec
 * <li>Conversion from an adapter custom Record
 * <li>Perform platform specific record access
 * <li>Provide XML DOM record conversion
 * <li>Provide sequence support
 * </ul>
 *
 * <p><code>EISPlatform</code> also supports behavior for specifying the record
 * types supported and automatic data-conversion to strings.
 *
 * <p>Although use of the generic <code>EISPlatform</code> may be sufficient,
 * some adapters may require that a specific platform be defined.
 *
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class EISPlatform extends DatasourcePlatform {
    protected boolean isIndexedRecordSupported;
    protected boolean isMappedRecordSupported;
    protected boolean isDOMRecordSupported;

    /** Can be used for adapters that only support String data/XML. */
    protected boolean shouldConvertDataToStrings;

    /** Allows for usage of transaction to be disabled if not supported. */
    protected boolean supportsLocalTransactions;

    /** Can be used if a commit is required after every interaction outside of a local transaction. */
    protected boolean requiresAutoCommit;

    /** Can be used to convert from an adapter specific record. */
    protected RecordConverter recordConverter;

    /** Used to reflectively provide XML record support as DOMRecord is not part of the JCA-CCI spec. */
    protected Method domMethod;

    /** For XML usage, an XMLConversionManager instance is required */
    protected XMLConversionManager xmlConversionManager;

    /**
     * Default constructor.
     */
    public EISPlatform() {
        super();
        setIsMappedRecordSupported(true);
        setIsIndexedRecordSupported(true);
        setIsDOMRecordSupported(false);
        setShouldConvertDataToStrings(false);
        setSupportsLocalTransactions(true);
        setRequiresAutoCommit(false);
    }

    /**
     * Return the record converter.
     */
    public RecordConverter getRecordConverter() {
        return recordConverter;
    }

    /**
     * Set the record converter.
     * Can be used to convert from an adapter specific record.
     */
    public void setRecordConverter(RecordConverter recordConverter) {
        this.recordConverter = recordConverter;
    }

    /**
     * Return if this platform requires auto commit of the local transaction
     * for interactions outside of an interaction.
     */
    public boolean requiresAutoCommit() {
        return requiresAutoCommit;
    }

    /**
     * Set if this platform requires auto commit of the local transaction
     * for interactions outside of an interaction.
     */
    public void setRequiresAutoCommit(boolean requiresAutoCommit) {
        this.requiresAutoCommit = requiresAutoCommit;
    }

    /**
     * Return if this platform supports local transactions.
     */
    public boolean supportsLocalTransactions() {
        return supportsLocalTransactions;
    }

    /**
     * Set if this platform supports local transactions.
     */
    public void setSupportsLocalTransactions(boolean supportsLocalTransactions) {
        this.supportsLocalTransactions = supportsLocalTransactions;
    }

    /**
     * Return if this platform supports JCA IndexedRecord.
     */
    public boolean isIndexedRecordSupported() {
        return isIndexedRecordSupported;
    }

    /**
     * Set if this platform supports JCA IndexedRecord.
     */
    public void setIsIndexedRecordSupported(boolean isIndexedRecordSupported) {
        this.isIndexedRecordSupported = isIndexedRecordSupported;
    }

    /**
     * Return if this platform supports JCA MappedRecord.
     */
    public boolean isMappedRecordSupported() {
        return isMappedRecordSupported;
    }

    /**
     * Set if this platform supports JCA MappedRecord.
     */
    public void setIsMappedRecordSupported(boolean isMappedRecordSupported) {
        this.isMappedRecordSupported = isMappedRecordSupported;
    }

    /**
     * Return if this platform supports XML/DOM Records.
     */
    public boolean isDOMRecordSupported() {
        return isDOMRecordSupported;
    }

    /**
     * Set if this platform supports XML/DOM Records.
     */
    public void setIsDOMRecordSupported(boolean isDOMRecordSupported) {
        this.isDOMRecordSupported = isDOMRecordSupported;
    }

    /**
     * Return if all data set into the adapter should be first converted to strings.
     */
    public boolean shouldConvertDataToStrings() {
        return shouldConvertDataToStrings;
    }

    /**
     * Set if all data set into the adapter should be first converted to strings.
     */
    public void setShouldConvertDataToStrings(boolean shouldConvertDataToStrings) {
        this.shouldConvertDataToStrings = shouldConvertDataToStrings;
    }

    /**
     * Allow the platform to build the interaction spec based on properties defined in the interaction.
     */
    public InteractionSpec buildInteractionSpec(EISInteraction interaction) {
        return interaction.getInteractionSpec();
    }

    /**
     * Allow the platform to create the appropriate type of record for the interaction.
     */
    public jakarta.resource.cci.Record createInputRecord(EISInteraction interaction, EISAccessor accessor) {
        jakarta.resource.cci.Record input = interaction.createInputRecord(accessor);
        if (getRecordConverter() != null) {
            input = getRecordConverter().converterToAdapterRecord(input);
        }
        return input;
    }

    /**
     * Allow the platform to create the appropriate type of record for the interaction.
     * If an output record is not required then null is returned.
     */
    public jakarta.resource.cci.Record createOutputRecord(EISInteraction interaction, AbstractRecord translationRow, EISAccessor accessor) {
        return null;
    }

    /**
     * INTERNAL:
     * Allow the platform to handle record to row conversion.
     */
    public AbstractRecord buildRow(jakarta.resource.cci.Record record, EISInteraction interaction, EISAccessor accessor) {
        jakarta.resource.cci.Record output = record;
        if (getRecordConverter() != null) {
            output = getRecordConverter().converterFromAdapterRecord(output);
        }
        return interaction.buildRow(output, accessor);
    }

    /**
     * Allow the platform to handle record to row conversion.
     */
    public Vector<AbstractRecord> buildRows(jakarta.resource.cci.Record record, EISInteraction interaction, EISAccessor accessor) {
        jakarta.resource.cci.Record output = record;
        if (getRecordConverter() != null) {
            output = getRecordConverter().converterFromAdapterRecord(output);
        }
        return interaction.buildRows(output, accessor);
    }

    /**
     * Allow the platform to handle the creation of the DOM record.
     * By default create a mapped record an assume it implements DOM as well.
     */
    public jakarta.resource.cci.Record createDOMRecord(String recordName, EISAccessor accessor) {
        try {
            return accessor.getRecordFactory().createMappedRecord(recordName);
        } catch (ResourceException exception) {
            throw EISException.resourceException(exception, accessor, null);
        }
    }

    /**
     * INTERNAL:
     * Allow the platform to handle the creation of the Record for the DOM record.
     * By default instantiate an EISDOMRecord which introspects the record for a getDOM method.
     */
    public AbstractRecord createDatabaseRowFromDOMRecord(jakarta.resource.cci.Record record, EISInteraction call, EISAccessor accessor) {
        return new EISDOMRecord(record);
    }

    /**
     * Retrieves the field value from the record.
     * This allows for the platform to perform any platform specific translation or conversion.
     */
    public Object getValueFromRecord(String key, MappedRecord record, EISAccessor accessor) {
        return record.get(key);
    }

    /**
     * Stores the XML DOM value into the record.
     * This must be implemented by the platform if it support XML/DOM records.
     */
    public void setDOMInRecord(Element dom, jakarta.resource.cci.Record record, EISInteraction call, EISAccessor accessor) {
        if (domMethod == null) {
            Class<?>[] argumentTypes = new Class<?>[1];
            argumentTypes[0] = Element.class;
            try {
                domMethod = PrivilegedAccessHelper.callDoPrivilegedWithException(
                        () -> PrivilegedAccessHelper.getMethod(record.getClass(), "setDom", argumentTypes, false)
                );
            } catch (Exception notFound) {
                domMethod = PrivilegedAccessHelper.callDoPrivilegedWithException(
                        () -> PrivilegedAccessHelper.getMethod(record.getClass(), "setDOM", argumentTypes, false),
                        EISException::new
                );
            }
        }
        PrivilegedAccessHelper.callDoPrivilegedWithException(
                () -> PrivilegedAccessHelper.invokeMethod(domMethod, record, new Object[] {dom}),
                EISException::new
        );
    }

    /**
     * Stores the field value into the record.
     * This allows for the platform to perform any platform specific translation or conversion.
     */
    public void setValueInRecord(String key, Object value, MappedRecord record, EISAccessor accessor) {
        Object recordValue = value;
        if (shouldConvertDataToStrings() && !(value instanceof jakarta.resource.cci.Record) && !(value instanceof Collection)) {
            recordValue = getConversionManager().convertObject(value, ClassConstants.STRING);
        }
        record.put(key, recordValue);
    }

    /**
     * Add the parameter.
     * Convert the parameter to a string and write it.
     * Convert rows to XML strings.
     */
    @Override
    public void appendParameter(Call call, Writer writer, Object parameter) {
        if (parameter instanceof Vector<?> records) {

            // May be a collection of record.
            for (int index = 0; index < records.size(); index++) {
                appendParameter(call, writer, (records).elementAt(index));
            }
        } else if (parameter instanceof org.eclipse.persistence.oxm.record.DOMRecord) {
            String xml = ((org.eclipse.persistence.oxm.record.DOMRecord)parameter).transformToXML();

            // For some reason the transform always prints the XML header, so trim it off.
            int start = xml.indexOf('>');
            xml = xml.substring(start + 1);
            try {
                writer.write(xml);
            } catch (IOException exception) {
                throw ValidationException.fileError(exception);
            }
        } else {
            super.appendParameter(call, writer, parameter);
        }
    }

    /**
     * The platform holds its own instance of conversion manager to allow customization.
     */
    @Override
    public ConversionManager getConversionManager() {
        // For XML we need an XMLConversionManager instance
        if (isDOMRecordSupported()) {
            // Lazy init for serialization.
            if (xmlConversionManager == null) {
                // Clone the default to allow customers to easily override the conversion manager
                xmlConversionManager = (XMLConversionManager) XMLConversionManager.getDefaultXMLManager().clone();
                xmlConversionManager.setLoader(super.getConversionManager().getLoader());
            }
            return xmlConversionManager;
        }
        // For non-XML, return the ConversionManager instance from DatasourcePlatform
        return super.getConversionManager();
    }

    /**
     * INTERNAL:
     * Override this method to throw an exception by default.
     * Platforms that support dynamic querying can override this to generate an EISInteraction.
     */
    public DatasourceCall buildCallFromStatement(SQLStatement statement, DatabaseQuery query, AbstractSession session) {
        throw QueryException.noCallOrInteractionSpecified();
    }

    /**
     * INTERNAL:
     * Return the correct call type for the native query string.
     * This allows EIS platforms to use different types of native calls.
     */
    @Override
    public DatasourceCall buildNativeCall(String queryString) {
        return new QueryStringInteraction(queryString);
    }
}
