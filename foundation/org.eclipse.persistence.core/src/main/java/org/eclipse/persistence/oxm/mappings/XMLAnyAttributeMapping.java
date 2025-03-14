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
package org.eclipse.persistence.oxm.mappings;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.exceptions.XMLMarshalException;
import org.eclipse.persistence.internal.descriptors.DescriptorIterator;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.helper.Helper;
import org.eclipse.persistence.internal.identitymaps.CacheKey;
import org.eclipse.persistence.internal.oxm.Namespace;
import org.eclipse.persistence.internal.oxm.XMLObjectBuilder;
import org.eclipse.persistence.internal.oxm.XPathEngine;
import org.eclipse.persistence.internal.oxm.mappings.AnyAttributeMapping;
import org.eclipse.persistence.internal.oxm.mappings.XMLContainerMapping;
import org.eclipse.persistence.internal.queries.ContainerPolicy;
import org.eclipse.persistence.internal.queries.JoinedAttributeManager;
import org.eclipse.persistence.internal.queries.MappedKeyMapContainerPolicy;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.internal.sessions.ChangeRecord;
import org.eclipse.persistence.internal.sessions.MergeManager;
import org.eclipse.persistence.internal.sessions.ObjectChangeSet;
import org.eclipse.persistence.internal.sessions.UnitOfWorkImpl;
import org.eclipse.persistence.internal.sessions.remote.ObjectDescriptor;
import org.eclipse.persistence.mappings.AttributeAccessor;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.oxm.NamespaceResolver;
import org.eclipse.persistence.oxm.XMLConstants;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.oxm.XMLField;
import org.eclipse.persistence.oxm.mappings.nullpolicy.AbstractNullPolicy;
import org.eclipse.persistence.oxm.record.DOMRecord;
import org.eclipse.persistence.oxm.record.XMLRecord;
import org.eclipse.persistence.queries.ObjectBuildingQuery;
import org.eclipse.persistence.queries.ObjectLevelReadQuery;
import org.eclipse.persistence.sessions.remote.DistributedSession;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p><b>Purpose</b>:The XMLAnyAttributeMapping is used to map to an attribute in an object to any xml attributes contained
 * on a specific element in the XML Document. The attribute in the object will contain a map of attribute values keyed
 * on QName. In the case that one or more of the attributes found on the specified element is already mapped to another
 * attribute in the object, that attribute will be ignored during the unmarshal operation.
 *
 * <p><b>Setting the XPath</b>: TopLink XML mappings make use of XPath statements to find the relevant
 * data in an XML document.  The XPath statement is relative to the context node specified in the descriptor.
 * The XPath may contain node type, path, and positional information.  The XPath is specified on the
 * mapping using the <code>setXPath</code> method.  Note that for XML Any Attribute Mappings the XPath
 * is optional. Not setting the xpath, will cause the mapping to look for any attribute children directly owned by the
 * current Element.
 *
 */
public class XMLAnyAttributeMapping extends DatabaseMapping implements XMLMapping,AnyAttributeMapping<AbstractSession, AttributeAccessor, ContainerPolicy, ClassDescriptor, DatabaseField, XMLRecord> {
    private AbstractNullPolicy wrapperNullPolicy;
    private XMLField field;
    private MappedKeyMapContainerPolicy containerPolicy;
    private boolean isDefaultEmptyContainer = XMLContainerMapping.EMPTY_CONTAINER_DEFAULT;
    private boolean isNamespaceDeclarationIncluded;
    private boolean isSchemaInstanceIncluded;
    private boolean isWriteOnly;
    private boolean reuseContainer;

    public XMLAnyAttributeMapping() {
        this.containerPolicy = new MappedKeyMapContainerPolicy(HashMap.class);
        this.containerPolicy.setKeyMapping(new XMLDirectMapping());
        this.isNamespaceDeclarationIncluded = true;
        this.isSchemaInstanceIncluded = true;
    }

    /**
    * INTERNAL:
    * Clone the attribute from the clone and assign it to the backup.
    */
    @Override
    public void buildBackupClone(Object clone, Object backup, UnitOfWorkImpl unitOfWork) {
        throw DescriptorException.invalidMappingOperation(this, "buildBackupClone");
    }

    /**
    * INTERNAL:
    * Clone the attribute from the original and assign it to the clone.
    */
    @Override
    public void buildClone(Object original, CacheKey cacheKey, Object clone, Integer refreshCascade, AbstractSession cloningSession) {
        throw DescriptorException.invalidMappingOperation(this, "buildClone");
    }

    @Override
    public void buildCloneFromRow(AbstractRecord Record, JoinedAttributeManager joinManager, Object clone, CacheKey sharedCacheKey, ObjectBuildingQuery sourceQuery, UnitOfWorkImpl unitOfWork, AbstractSession executionSession) {
        throw DescriptorException.invalidMappingOperation(this, "buildCloneFromRow");
    }

    /**
     * INTERNAL:
     * Cascade perform delete through mappings that require the cascade
     */
    @Override
    public void cascadePerformRemoveIfRequired(Object object, UnitOfWorkImpl uow, Map visitedObjects) {
        //objects referenced by this mapping are not registered as they have
        // no identity, this is a no-op.
    }

    /**
      * INTERNAL:
      * Cascade registerNew for Create through mappings that require the cascade
      */
    @Override
    public void cascadeRegisterNewIfRequired(Object object, UnitOfWorkImpl uow, Map visitedObjects) {
        //Our current XML support does not make use of the UNitOfWork.
    }

    @Override
    public Object clone() {
        // Bug 3037701 - clone the AttributeAccessor
        XMLAnyAttributeMapping mapping = null;
        mapping = (XMLAnyAttributeMapping) super.clone();
        mapping.setContainerPolicy(this.getContainerPolicy());
        mapping.setField(this.getField());
        return mapping;
    }

    /**
    * INTERNAL:
    * This method was created in VisualAge.
    * @return prototype.changeset.ChangeRecord
    */
    @Override
    public ChangeRecord compareForChange(Object clone, Object backup, ObjectChangeSet owner, AbstractSession session) {
        throw DescriptorException.invalidMappingOperation(this, "compareForChange");
    }

    /**
    * INTERNAL:
    * Compare the attributes belonging to this mapping for the objects.
    */
    @Override
    public boolean compareObjects(Object firstObject, Object secondObject, AbstractSession session) {
        throw DescriptorException.invalidMappingOperation(this, "compareObjects");
    }

    /**
    * INTERNAL:
    * An object has been serialized from the server to the client.
    * Replace the transient attributes of the remote value holders
    * with client-side objects.
    */
    @Override
    public void fixObjectReferences(Object object, Map<Object, ObjectDescriptor> objectDescriptors, Map<Object, Object> processedObjects, ObjectLevelReadQuery query, DistributedSession session) {
        throw DescriptorException.invalidMappingOperation(this, "fixObjectReferences");
    }

    /**
    * INTERNAL:
    * Return the mapping's containerPolicy.
    */
    @Override
    public ContainerPolicy getContainerPolicy() {
        return containerPolicy;
    }

    @Override
    public DatabaseField getField() {
        return field;
    }

    @Override
    public void initialize(AbstractSession session) throws DescriptorException {
        if (getField() != null) {
            setField(getDescriptor().buildField(getField()));
        }
        ContainerPolicy cp = getContainerPolicy();
        if (cp != null && cp.getContainerClass() == null) {
            Class<Object> cls = session.getDatasourcePlatform().getConversionManager().convertClassNameToClass(cp.getContainerClassName());
            cp.setContainerClass(cls);
        }
    }

    /**
    * INTERNAL:
    * Iterate on the appropriate attribute value.
    */
    @Override
    public void iterate(DescriptorIterator iterator) {
        throw DescriptorException.invalidMappingOperation(this, "iterate");
    }

    public void setXPath(String xpath) {
        this.field = new XMLField(xpath);
    }

    /**
    * INTERNAL:
    * Merge changes from the source to the target object.
    */
    @Override
    public void mergeChangesIntoObject(Object target, ChangeRecord changeRecord, Object source, MergeManager mergeManager, AbstractSession targetSession) {
        throw DescriptorException.invalidMappingOperation(this, "mergeChangesIntoObject");
    }

    /**
    * INTERNAL:
    * Merge changes from the source to the target object.
    */
    @Override
    public void mergeIntoObject(Object target, boolean isTargetUninitialized, Object source, MergeManager mergeManager, AbstractSession targetSession) {
        throw DescriptorException.invalidMappingOperation(this, "mergeIntoObject");
    }

    public void setContainerPolicy(ContainerPolicy cp) {
        if (!cp.isMappedKeyMapPolicy()) {
            throw DescriptorException.invalidContainerPolicy(cp, this.getClass());
        }
        if (((MappedKeyMapContainerPolicy)cp).getKeyMapping() == null) {
            ((MappedKeyMapContainerPolicy)cp).setKeyMapping(new XMLDirectMapping());
        }
        this.containerPolicy = (MappedKeyMapContainerPolicy) cp;
    }

    @Override
    public void setField(DatabaseField field) {
        this.field = (XMLField) field;
    }

    @Override
    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery sourceQuery, CacheKey cacheKey, AbstractSession executionSession, boolean isTargetProtected, Boolean[] wasCacheUsed) throws DatabaseException {
        XMLRecord record = (XMLRecord) row;

        if (getField() != null) {
            //Get the nested row represented by this field to build the collection from
            Object nested = record.get(getField());
            if (nested instanceof Vector) {
                nested = ((Vector) nested).firstElement();
            }
            if (!(nested instanceof XMLRecord)) {
                return null;
            }
            record = (XMLRecord) nested;
        }
        return buildObjectValuesFromDOMRecord((DOMRecord) record, executionSession, sourceQuery);
    }

    private Object buildObjectValuesFromDOMRecord(DOMRecord record, AbstractSession session, ObjectBuildingQuery query) {
        //This DOMRecord represents the root node of the AnyType instance
        //Grab ALL children to populate the collection.
        ContainerPolicy cp = getContainerPolicy();

        Object container = null;
        if (reuseContainer) {
            Object currentObject = record.getCurrentObject();
            Object value = getAttributeAccessor().getAttributeValueFromObject(currentObject);
            container = value != null ? value : cp.containerInstance();
        } else {
            container = cp.containerInstance();
        }

        org.w3c.dom.Element root = (Element) record.getDOM();
        NamedNodeMap attributes = root.getAttributes();
        Attr next;
        String localName;
        int numberOfAtts = attributes.getLength();
        for (int i = 0; i < numberOfAtts; i++) {
            next = (Attr) attributes.item(i);
            localName = next.getLocalName();
            if (null == localName) {
                localName = next.getName();
            }
            String namespaceURI = next.getNamespaceURI();
            boolean includeAttribute = true;
            if (!isNamespaceDeclarationIncluded && javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI)){
                includeAttribute = false;
            } else if (!isSchemaInstanceIncluded && javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(namespaceURI)){
                includeAttribute = false;
            }

            if (includeAttribute){
                String value = next.getValue();
                QName key = new QName(namespaceURI, localName);
                cp.addInto(key, value, container, session);
            }
        }
        return container;
    }

    protected XMLDescriptor getDescriptor(XMLRecord xmlRecord, AbstractSession session) throws XMLMarshalException {
        XMLContext xmlContext = xmlRecord.getUnmarshaller().getXMLContext();
        QName rootQName = new QName(xmlRecord.getNamespaceURI(), xmlRecord.getLocalName());
        XMLDescriptor xmlDescriptor = xmlContext.getDescriptor(rootQName);
        if (null == xmlDescriptor) {
            throw XMLMarshalException.noDescriptorWithMatchingRootElement(xmlRecord.getLocalName());
        }
        return xmlDescriptor;
    }

    @Override
    public void writeFromObjectIntoRow(Object object, AbstractRecord row, AbstractSession session, WriteType writeType) throws DescriptorException {
        if (this.isReadOnly()) {
            return;
        }
        Object attributeValue = this.getAttributeValueFromObject(object);
        writeSingleValue(attributeValue, object, (XMLRecord) row, session);
    }

    protected AbstractRecord buildCompositeRow(Object attributeValue, AbstractSession session, AbstractRecord parentRow) {
        XMLDescriptor referenceDescriptor = (XMLDescriptor) session.getDescriptor(attributeValue.getClass());
        if ((referenceDescriptor != null) && (referenceDescriptor.getDefaultRootElement() != null)) {
            XMLObjectBuilder objectBuilder = (XMLObjectBuilder) referenceDescriptor.getObjectBuilder();
            return objectBuilder.buildRow(attributeValue, session, referenceDescriptor.buildField(referenceDescriptor.getDefaultRootElement()), (XMLRecord) parentRow);
        }
        return null;
    }

    @Override
    public boolean isXMLMapping() {
        return true;
    }

    @Override
    public List<DatabaseField> getFields() {
        return this.collectFields();
    }

    public void useMapClass(Class<?> concreteMapClass) {
        if (!Helper.classImplementsInterface(concreteMapClass, Map.class)) {
            throw DescriptorException.illegalContainerClass(concreteMapClass);
        }
        this.containerPolicy.setContainerClass(concreteMapClass);
    }

    @Override
    public void writeSingleValue(Object attributeValue, Object parent, XMLRecord row, AbstractSession session) {
        ContainerPolicy cp = this.getContainerPolicy();
        if ((attributeValue == null) || (cp.sizeFor(attributeValue) == 0)) {
            return;
        }
        DOMRecord record = (DOMRecord) row;
        if (record.getDOM().getNodeType() != Node.ELEMENT_NODE) {
            return;
        }
        DOMRecord recordToModify = record;
        Element root = (Element) record.getDOM();
        if (field != null) {
            root = (Element) XPathEngine.getInstance().create((XMLField) getField(), root, session);
            recordToModify = new DOMRecord(root);
        }

        List extraNamespaces = new ArrayList();
        NamespaceResolver nr = recordToModify.getNamespaceResolver();
        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            Map.Entry entry = (Map.Entry)cp.nextEntry(iter, session);
            Object key = entry.getKey();
            if ((key != null) && key instanceof QName attributeName) {
                Object value = entry.getValue();
                String namespaceURI = attributeName.getNamespaceURI();
                String qualifiedName = attributeName.getLocalPart();

                if (nr != null) {
                    String prefix = nr.resolveNamespaceURI(attributeName.getNamespaceURI());
                    if ((prefix != null) && !prefix.isEmpty()) {
                        qualifiedName = prefix + XMLConstants.COLON + qualifiedName;
                    } else if (attributeName.getNamespaceURI() != null && !attributeName.getNamespaceURI().isEmpty()) {
                        String generatedPrefix = nr.generatePrefix();
                        qualifiedName = generatedPrefix + XMLConstants.COLON + qualifiedName;
                        nr.put(generatedPrefix, attributeName.getNamespaceURI());
                        extraNamespaces.add(new Namespace(generatedPrefix, attributeName.getNamespaceURI()));
                        recordToModify.getNamespaceResolver().put(generatedPrefix, attributeName.getNamespaceURI());
                    }
                }
                if (namespaceURI != null) {
                    root.setAttributeNS(namespaceURI, qualifiedName, value.toString());
                } else {
                    root.setAttribute(attributeName.getLocalPart(), value.toString());
                }
            }
        }

        ((XMLObjectBuilder) descriptor.getObjectBuilder()).writeExtraNamespaces(extraNamespaces, recordToModify);
        recordToModify.removeExtraNamespacesFromNamespaceResolver(extraNamespaces, session);
    }

    /**
     * INTERNAL:
     * Indicates the name of the Map class to be used.
     *
     */
    @Override
    public void useMapClassName(String concreteMapClassName) {
        MappedKeyMapContainerPolicy policy = new MappedKeyMapContainerPolicy(concreteMapClassName);
        policy.setKeyMapping(new XMLDirectMapping());
        this.setContainerPolicy(policy);
    }

    @Override
    public boolean isNamespaceDeclarationIncluded() {
        return isNamespaceDeclarationIncluded;
    }

    @Override
    public void setNamespaceDeclarationIncluded(boolean isNamespaceDeclarationIncluded) {
        this.isNamespaceDeclarationIncluded = isNamespaceDeclarationIncluded;
    }

    @Override
    public boolean isSchemaInstanceIncluded() {
        return isSchemaInstanceIncluded;
    }

    @Override
    public void setSchemaInstanceIncluded(boolean isSchemaInstanceIncluded) {
        this.isSchemaInstanceIncluded = isSchemaInstanceIncluded;
    }

    @Override
    public boolean isWriteOnly() {
        return isWriteOnly;
    }

    @Override
    public void setIsWriteOnly(boolean b) {
        this.isWriteOnly = b;
    }

    @Override
    public void setAttributeValueInObject(Object object, Object value) throws DescriptorException {
        if(isWriteOnly()) {
            return;
        }
        super.setAttributeValueInObject(object, value);
    }


    @Override
    public void preInitialize(AbstractSession session) throws DescriptorException {
        getAttributeAccessor().setIsWriteOnly(this.isWriteOnly());
        getAttributeAccessor().setIsReadOnly(this.isReadOnly());
        super.preInitialize(session);
    }

    /**
     * Return true if the original container on the object should be used if
     * present.  If it is not present then the container policy will be used to
     * create the container.
     */
    @Override
    public boolean getReuseContainer() {
        return reuseContainer;
    }

    /**
     * Specify whether the original container on the object should be used if
     * present.  If it is not present then the container policy will be used to
     * create the container.
     */
    @Override
    public void setReuseContainer(boolean reuseContainer) {
        this.reuseContainer = reuseContainer;
    }

    /**
     * INTERNAL
     * Return true if an empty container should be set on the object if there
     * is no presence of the collection in the XML document.
     * @since EclipseLink 2.3.3
     */
    @Override
    public boolean isDefaultEmptyContainer() {
        return isDefaultEmptyContainer;
    }

    /**
     * INTERNAL
     * Indicate whether by default an empty container should be set on the
     * field/property if the collection is not present in the XML document.
     * @since EclipseLink 2.3.3
     */
    @Override
    public void setDefaultEmptyContainer(boolean defaultEmptyContainer) {
        this.isDefaultEmptyContainer = defaultEmptyContainer;
    }

    @Override
    public AbstractNullPolicy getWrapperNullPolicy() {
        return this.wrapperNullPolicy;
    }

    @Override
    public void setWrapperNullPolicy(AbstractNullPolicy policy) {
        this.wrapperNullPolicy = policy;
    }

}
