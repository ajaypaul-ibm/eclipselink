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
//      //     30/05/2012-2.4 Guy Pelletier
//       - 354678: Temp classloader is still being used during metadata processing
package org.eclipse.persistence.mappings.foundation;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.DescriptorException;
import org.eclipse.persistence.exceptions.OptimisticLockException;
import org.eclipse.persistence.exceptions.QueryException;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.internal.descriptors.DescriptorIterator;
import org.eclipse.persistence.internal.descriptors.ObjectBuilder;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.identitymaps.CacheKey;
import org.eclipse.persistence.internal.queries.CollectionContainerPolicy;
import org.eclipse.persistence.internal.queries.ContainerPolicy;
import org.eclipse.persistence.internal.queries.JoinedAttributeManager;
import org.eclipse.persistence.internal.queries.ListContainerPolicy;
import org.eclipse.persistence.internal.queries.MapContainerPolicy;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.internal.sessions.AggregateCollectionChangeRecord;
import org.eclipse.persistence.internal.sessions.ChangeRecord;
import org.eclipse.persistence.internal.sessions.MergeManager;
import org.eclipse.persistence.internal.sessions.ObjectChangeSet;
import org.eclipse.persistence.internal.sessions.UnitOfWorkChangeSet;
import org.eclipse.persistence.internal.sessions.UnitOfWorkImpl;
import org.eclipse.persistence.internal.sessions.remote.ObjectDescriptor;
import org.eclipse.persistence.mappings.AggregateMapping;
import org.eclipse.persistence.mappings.ContainerMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.mappings.structures.ArrayCollectionMapping;
import org.eclipse.persistence.mappings.structures.ArrayCollectionMappingHelper;
import org.eclipse.persistence.queries.DeleteObjectQuery;
import org.eclipse.persistence.queries.ObjectBuildingQuery;
import org.eclipse.persistence.queries.ObjectLevelReadQuery;
import org.eclipse.persistence.queries.QueryByExamplePolicy;
import org.eclipse.persistence.queries.WriteObjectQuery;
import org.eclipse.persistence.sessions.CopyGroup;
import org.eclipse.persistence.sessions.remote.DistributedSession;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Define an embedded collection of objects.
 * This is used in structured data-types, such as EIS, NoSQL and object-relational Array (varray, nested table) data-types.
 * The target objects must be aggregate (embedded) and are stored with the parent object.
 */
public abstract class AbstractCompositeCollectionMapping extends AggregateMapping implements ContainerMapping, ArrayCollectionMapping {

    /** The aggregate objects are stored in a single field. */
    protected DatabaseField field;

    /** This determines the type of container used to hold the aggregate objects. */
    private ContainerPolicy containerPolicy;

    /** Allows user defined conversion between the object attribute value and the database value. */
    protected Converter converter;

    /**
     * Default constructor.
     */
    protected AbstractCompositeCollectionMapping() {
        super();
        this.containerPolicy = ContainerPolicy.buildDefaultPolicy();
    }

    /**
     * INTERNAL:
     * Build and return a new element based on the change set.
     */
    @Override
    public Object buildAddedElementFromChangeSet(Object changeSet, MergeManager mergeManager, AbstractSession targetSession) {
        return this.buildElementFromChangeSet(changeSet, mergeManager, targetSession);
    }

    /**
     * Build and return a backup clone of the attribute.
     */
    @Override
    protected Object buildBackupClonePart(Object attributeValue, UnitOfWorkImpl unitOfWork) {
        ContainerPolicy cp = this.getContainerPolicy();
        if (attributeValue == null) {
            return cp.containerInstance();
        }

        Object backupAttributeValue = cp.containerInstance(cp.sizeFor(attributeValue));
        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            Object backupElement = super.buildBackupClonePart(cp.next(iter, unitOfWork), unitOfWork);
            cp.addInto(backupElement, backupAttributeValue, unitOfWork);
        }
        return backupAttributeValue;
    }

    /**
     * INTERNAL:
     * Build and return a change set for the specified element.
     */
    @Override
    public Object buildChangeSet(Object element, ObjectChangeSet owner, AbstractSession session) {
        ObjectBuilder objectBuilder = this.getObjectBuilder(element, session);
        return objectBuilder.compareForChange(element, null, (UnitOfWorkChangeSet)owner.getUOWChangeSet(), session);
    }

    /**
     * Build and return a clone of the attribute.
     */
    @Override
    protected Object buildClonePart(Object original, Object clone, CacheKey cacheKey, Object attributeValue, Integer refreshCascade, AbstractSession clonningSession) {
        ContainerPolicy cp = this.getContainerPolicy();
        if (attributeValue == null) {
            return cp.containerInstance();
        }

        Object clonedAttributeValue = cp.containerInstance(cp.sizeFor(attributeValue));
        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            Object cloneElement = super.buildClonePart(original, clone, cacheKey, cp.next(iter, clonningSession), refreshCascade, clonningSession);
            cp.addInto(cloneElement, clonedAttributeValue, clonningSession);
        }
        return clonedAttributeValue;
    }

    /**
     * Copy of the attribute of the object.
     * This is NOT used for unit of work but for templatizing an object.
     */
    @Override
    protected Object buildCopyOfAttributeValue(Object attributeValue, CopyGroup group) {
        ContainerPolicy cp = this.getContainerPolicy();
        if (attributeValue == null) {
            return cp.containerInstance();
        }

        Object attributeValueCopy = cp.containerInstance(cp.sizeFor(attributeValue));
        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            Object copyElement = super.buildCopyOfAttributeValue(cp.next(iter, group.getSession()), group);
            cp.addInto(copyElement, attributeValueCopy, group.getSession());
        }
        return attributeValueCopy;
    }

    /**
     * Build and return a new element based on the change set.
     */
    protected Object buildElementFromChangeSet(Object changeSet, MergeManager mergeManager, AbstractSession targetSession) {
        ObjectChangeSet objectChangeSet = (ObjectChangeSet)changeSet;
        ObjectBuilder objectBuilder = this.getObjectBuilderForClass(objectChangeSet.getClassType(mergeManager.getSession()), mergeManager.getSession());
        Object result = objectBuilder.buildNewInstance();
        objectBuilder.mergeChangesIntoObject(result, objectChangeSet, null, mergeManager, targetSession);

        return result;
    }

    /**
     * INTERNAL:
     * Build and return a new element based on the specified element.
     */
    @Override
    public Object buildElementFromElement(Object element, MergeManager mergeManager, AbstractSession targetSession) {
        ObjectBuilder objectBuilder = this.getObjectBuilder(element, mergeManager.getSession());
        Object result = objectBuilder.buildNewInstance();
        objectBuilder.mergeIntoObject(result, true, element, mergeManager, targetSession);

        return result;
    }

    /**
     * INTERNAL:
     * In case Query By Example is used, this method builds and returns an expression that
     * corresponds to a single attribute and it's value.
     */
    @Override
    public Expression buildExpression(Object queryObject, QueryByExamplePolicy policy, Expression expressionBuilder, Map processedObjects, AbstractSession session) {
        if (policy.shouldValidateExample()){
            throw QueryException.unsupportedMappingQueryByExample(queryObject.getClass().getName(), this);
        }
        return null;
    }

    /**
     * INTERNAL:
     * Build and return a new element based on the change set.
     */
    @Override
    public Object buildRemovedElementFromChangeSet(Object changeSet, MergeManager mergeManager, AbstractSession targetSession) {
        return this.buildElementFromChangeSet(changeSet, mergeManager, targetSession);
    }

    /**
     * INTERNAL:
     * Cascade perform delete through mappings that require the cascade
     */
    @Override
    public void cascadePerformRemoveIfRequired(Object object, UnitOfWorkImpl uow, Map visitedObjects){
        Object cloneAttribute = null;
        cloneAttribute = getAttributeValueFromObject(object);
        if ( cloneAttribute == null ) {
            return;
        }

        ContainerPolicy cp = getContainerPolicy();
        Object cloneObjectCollection = null;
        cloneObjectCollection = getRealCollectionAttributeValueFromObject(object, uow);
        Object cloneIter = cp.iteratorFor(cloneObjectCollection);
        while (cp.hasNext(cloneIter)) {
            Object objectToCascadeOn = cp.next(cloneIter, uow);
            if (objectToCascadeOn != null && (!visitedObjects.containsKey(objectToCascadeOn)) ) {
                visitedObjects.put(objectToCascadeOn, objectToCascadeOn);
                ObjectBuilder builder = getReferenceDescriptor(objectToCascadeOn.getClass(), uow).getObjectBuilder();
                builder.cascadePerformRemove(objectToCascadeOn, uow, visitedObjects);
            }
        }
    }

    /**
     * INTERNAL:
     * Cascade discover and persist new objects during commit.
     */
    @Override
    public void cascadeDiscoverAndPersistUnregisteredNewObjects(Object object, Map newObjects, Map unregisteredExistingObjects, Map visitedObjects, UnitOfWorkImpl uow, Set cascadeErrors) {
        Object cloneAttribute = getAttributeValueFromObject(object);
        if (cloneAttribute == null ) {
            return;
        }
        ContainerPolicy containerPolicy = getContainerPolicy();
        Object cloneObjectCollection = getRealCollectionAttributeValueFromObject(object, uow);
        Object iterator = containerPolicy.iteratorFor(cloneObjectCollection);
        while (containerPolicy.hasNext(iterator)) {
            Object nextObject = containerPolicy.next(iterator, uow);
            if (nextObject != null) {
                ObjectBuilder builder = getReferenceDescriptor(nextObject.getClass(), uow).getObjectBuilder();
                builder.cascadeDiscoverAndPersistUnregisteredNewObjects(nextObject, newObjects, unregisteredExistingObjects, visitedObjects, uow, cascadeErrors);
            }
        }
    }

    /**
     * INTERNAL:
     * Cascade registerNew for Create through mappings that require the cascade
     */
    @Override
    public void cascadeRegisterNewIfRequired(Object object, UnitOfWorkImpl uow, Map visitedObjects){
        //aggregate objects are not registered but their mappings should be.
        Object cloneAttribute = null;
        cloneAttribute = getAttributeValueFromObject(object);
        if ( cloneAttribute == null ) {
            return;
        }

        ObjectBuilder builder = null;
        ContainerPolicy cp = getContainerPolicy();
        Object cloneObjectCollection = null;
        cloneObjectCollection = getRealCollectionAttributeValueFromObject(object, uow);
        Object cloneIter = cp.iteratorFor(cloneObjectCollection);
        while (cp.hasNext(cloneIter)) {
            Object nextObject = cp.next(cloneIter, uow);
            if (nextObject != null && (! visitedObjects.containsKey(nextObject)) ) {
                visitedObjects.put(nextObject, nextObject);
                builder = getReferenceDescriptor(nextObject.getClass(), uow).getObjectBuilder();
                builder.cascadeRegisterNewForCreate(nextObject, uow, visitedObjects);
            }
        }
    }

    /**
     * Return the fields handled by the mapping.
     */
    @Override
    protected Vector collectFields() {
        Vector fields = new Vector(1);
        fields.addElement(this.getField());
        return fields;
    }

    /**
     * INTERNAL:
     * Compare the non-null elements and return true if they are alike.
     */
    @Override
    public boolean compareElements(Object element1, Object element2, AbstractSession session) {
        if (element1.getClass() != element2.getClass()) {
            return false;
        }
        return this.getObjectBuilder(element1, session).compareObjects(element1, element2, session);
    }

    /**
     * INTERNAL:
     * Compare the non-null elements and return true if they are alike.
     */
    @Override
    public boolean compareElementsForChange(Object element1, Object element2, AbstractSession session) {
        return this.compareElements(element1, element2, session);
    }

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this mapping to actual class-based
     * settings. This method is used when converting a project that has been built
     * with class names to a project with classes.
     */
    @Override
    public void convertClassNamesToClasses(ClassLoader classLoader){
        super.convertClassNamesToClasses(classLoader);

        containerPolicy.convertClassNamesToClasses(classLoader);
    }


    protected ChangeRecord convertToChangeRecord(Object cloneCollection, ObjectChangeSet owner, AbstractSession session) {
        ContainerPolicy cp = getContainerPolicy();
        Object cloneIter = cp.iteratorFor(cloneCollection);
        Vector collectionChanges = new Vector(2);
        while (cp.hasNext(cloneIter)) {
            Object aggregateObject = cp.next(cloneIter, session);

            // For CR#2258 quietly ignore nulls inserted into a collection.
            if (aggregateObject != null) {
                ObjectChangeSet changes = getReferenceDescriptor(aggregateObject.getClass(), session).getObjectBuilder().compareForChange(aggregateObject, null, (UnitOfWorkChangeSet)owner.getUOWChangeSet(), session);
                collectionChanges.addElement(changes);
            }
        }

        //cr 3013 Removed if collection is empty return null block, which prevents recording clear() change
        AggregateCollectionChangeRecord changeRecord = new AggregateCollectionChangeRecord(owner);
        changeRecord.setAttribute(getAttributeName());
        changeRecord.setMapping(this);
        changeRecord.setChangedValues(collectionChanges);
        return changeRecord;
    }

    /**
     * An object has been serialized from the server to the remote client.
     * Replace the transient attributes of the remote value holders
     * with client-side objects.
     */
    @Override
    protected void fixAttributeValue(Object attributeValue, Map<Object, ObjectDescriptor> objectDescriptors, Map<Object, Object> processedObjects, ObjectLevelReadQuery query, DistributedSession session) {
        if (attributeValue == null) {
            return;
        }
        ContainerPolicy cp = this.getContainerPolicy();
        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            super.fixAttributeValue(cp.next(iter, session), objectDescriptors, processedObjects, query, session);
        }
    }

    /**
     * Return the appropriate attribute value.
     * This method is a hack to allow the aggregate collection
     * subclass to override....
     * The intent is to return the aggregate object in the backup clone
     * that corresponds to the one in the working copy.
     * Since we don't know which element in the backup clone
     * collection corresponds any given element in the working copy
     * collection (there is no "primary key"); we simply return null,
     * which will cause a new, empty, instance to be built and used
     * for comparison.
     */
    @Override
    protected Object getAttributeValueFromBackupClone(Object backupClone) {
        return null;
    }

    /**
     * INTERNAL:
     * Return the mapping's containerPolicy.
     */
    @Override
    public ContainerPolicy getContainerPolicy() {
        return containerPolicy;
    }

    /**
     * PUBLIC:
     * Return the converter on the mapping.
     * A converter can be used to convert between the object's value and database value of the attribute.
     */
    public Converter getConverter() {
        return converter;
    }

    /**
     * INTERNAL:
     * Return the field mapped by this mapping.
     */
    @Override
    public DatabaseField getField() {
        return field;
    }

    /**
     * INTERNAL:
     * Convenience method.
     * Return the value of an attribute, unwrapping value holders if necessary.
     * If the value is null, build a new container.
     */
    @Override
    public Object getRealCollectionAttributeValueFromObject(Object object, AbstractSession session) throws DescriptorException {
        Object value = this.getRealAttributeValueFromObject(object, session);
        if (value == null) {
            value = this.getContainerPolicy().containerInstance(1);
        }
        return value;
    }

    /**
     * This is required for ObjectArrayMapping which defines a name for the collection type.
     * Currently this type name is not required or used in general with the SDK.
     */
    protected String getStructureName() {
        return "";
    }

    /**
     * PUBLIC:
     * Indicates if there is a converter on the mapping.
     */
    public boolean hasConverter() {
        return getConverter() != null;
    }

    /**
     * INTERNAL:
     */
    @Override
    public boolean isAbstractCompositeCollectionMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * The mapping is initialized with the given session. This mapping is fully initialized
     * after this.
     */
    @Override
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);
        if (getField() == null) {
            throw DescriptorException.fieldNameNotSetInMapping(this);
        }
        setField(getDescriptor().buildField(getField()));
        setFields(collectFields());
        if (hasConverter()) {
            getConverter().initialize(this, session);
        }
    }

    /**
     * Iterate on the specified attribute value.
     */
    @Override
    protected void iterateOnAttributeValue(DescriptorIterator descriptorIterator, Object attributeValue) {
        if (attributeValue == null) {
            return;
        }
        ContainerPolicy cp = this.getContainerPolicy();
        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            super.iterateOnAttributeValue(descriptorIterator, cp.next(iter, descriptorIterator.getSession()));
        }
    }

    /**
     * INTERNAL:
     * Return whether the element's user-defined Map key has changed
     * since it was cloned from the original version.
     * Aggregate elements cannot change their keys without detection.
     */
    @Override
    public boolean mapKeyHasChanged(Object element, AbstractSession session) {
        return false;
    }

    /**
     * The message is passed to its reference class descriptor.
     */
    @Override
    public void postDeleteAttributeValue(DeleteObjectQuery query, Object attributeValue) throws DatabaseException, OptimisticLockException {
        if (attributeValue == null) {
            return;
        }
        ContainerPolicy cp = this.getContainerPolicy();
        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            super.postDeleteAttributeValue(query, cp.next(iter, query.getSession()));
        }
    }

    /**
     * The message is passed to its reference class descriptor.
     */
    @Override
    public void postInsertAttributeValue(WriteObjectQuery query, Object attributeValue) throws DatabaseException, OptimisticLockException {
        if (attributeValue == null) {
            return;
        }
        ContainerPolicy cp = this.getContainerPolicy();
        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            super.postInsertAttributeValue(query, cp.next(iter, query.getSession()));
        }
    }

    /**
     * The message is passed to its reference class descriptor.
     */
    @Override
    public void postUpdateAttributeValue(WriteObjectQuery query, Object attributeValue) throws DatabaseException, OptimisticLockException {
        if (attributeValue == null) {
            return;
        }
        ContainerPolicy cp = this.getContainerPolicy();
        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            super.postUpdateAttributeValue(query, cp.next(iter, query.getSession()));
        }
    }

    /**
     * The message is passed to its reference class descriptor.
     */
    @Override
    public void preDeleteAttributeValue(DeleteObjectQuery query, Object attributeValue) throws DatabaseException, OptimisticLockException {
        if (attributeValue == null) {
            return;
        }
        ContainerPolicy cp = this.getContainerPolicy();
        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            super.preDeleteAttributeValue(query, cp.next(iter, query.getSession()));
        }
    }

    /**
     * The message is passed to its reference class descriptor.
     */
    @Override
    public void preInsertAttributeValue(WriteObjectQuery query, Object attributeValue) throws DatabaseException, OptimisticLockException {
        if (attributeValue == null) {
            return;
        }
        ContainerPolicy cp = this.getContainerPolicy();
        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            super.preInsertAttributeValue(query, cp.next(iter, query.getSession()));
        }
    }

    /**
     * The message is passed to its reference class descriptor.
     */
    @Override
    public void preUpdateAttributeValue(WriteObjectQuery query, Object attributeValue) throws DatabaseException, OptimisticLockException {
        if (attributeValue == null) {
            return;
        }
        ContainerPolicy cp = this.getContainerPolicy();
        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            super.preUpdateAttributeValue(query, cp.next(iter, query.getSession()));
        }
    }

    /**
     * ADVANCED:
     * Set the mapping's containerPolicy.
     */
    @Override
    public void setContainerPolicy(ContainerPolicy containerPolicy) {
        this.containerPolicy = containerPolicy;
    }

    /**
     * PUBLIC:
     * Set the converter on the mapping.
     * A converter can be used to convert between the object's value and database value of the attribute.
     */
    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    /**
     * Set the field in the mapping.
     */
    public void setField(DatabaseField field) {
        this.field = field;
    }

    /**
     * PUBLIC:
     * Configure the mapping to use an instance of the specified container class
     * to hold the target objects.
     * <p>jdk1.2.x: The container class must implement (directly or indirectly) the Collection interface.
     * <p>jdk1.1.x: The container class must be a subclass of Vector.
     */
    @Override
    public void useCollectionClass(Class<?> concreteContainerClass) {
        this.setContainerPolicy(ContainerPolicy.buildPolicyFor(concreteContainerClass));
    }

    @Override
    public void useCollectionClassName(String concreteContainerClassName) {
        this.setContainerPolicy(new CollectionContainerPolicy(concreteContainerClassName));
    }

    @Override
    public void useListClassName(String concreteContainerClassName) {
        this.setContainerPolicy(new ListContainerPolicy(concreteContainerClassName));
    }

    /**
     * PUBLIC:
     * Configure the mapping to use an instance of the specified container class
     * to hold the target objects. The key used to index the value in the Map
     * is the value returned by a call to the specified zero-argument method.
     * The method must be implemented by the class (or a superclass) of the
     * value to be inserted into the Map.
     * <p>jdk1.2.x: The container class must implement (directly or indirectly) the Map interface.
     * <p>jdk1.1.x: The container class must be a subclass of Hashtable.
     * <p>The referenceClass must be set before calling this method.
     */
    @Override
    public void useMapClass(Class<?> concreteContainerClass, String methodName) {
        // the reference class has to be specified before coming here
        if (this.getReferenceClassName() == null) {
            throw DescriptorException.referenceClassNotSpecified(this);
        }
        ContainerPolicy policy = ContainerPolicy.buildPolicyFor(concreteContainerClass);
        policy.setKeyName(methodName, getReferenceClass());
        this.setContainerPolicy(policy);
    }

    @Override
    public void useMapClassName(String concreteContainerClassName, String methodName) {
        // the reference class has to be specified before coming here
        if (this.getReferenceClassName() == null) {
            throw DescriptorException.referenceClassNotSpecified(this);
        }
        MapContainerPolicy policy = new MapContainerPolicy(concreteContainerClassName);
        policy.setKeyName(methodName, getReferenceClass().getName());
        this.setContainerPolicy(policy);
    }

    /**
     * INTERNAL:
     * Build and return an aggregate collection from the specified row.
     */
    @Override
    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery sourceQuery, CacheKey cacheKey, AbstractSession executionSession, boolean isTargetProtected, Boolean[] wasCacheUsed) throws DatabaseException {
        if (this.descriptor.getCachePolicy().isProtectedIsolation()){
            if (this.isCacheable && isTargetProtected && cacheKey != null){
                //cachekey will be null when isolating to uow
                //used cached collection
                Object result = null;
                Object cached = cacheKey.getObject();
                if (cached != null){
                    if (wasCacheUsed != null){
                        wasCacheUsed[0] = Boolean.TRUE;
                    }
                    Object attributeValue = this.getAttributeValueFromObject(cached);
                    Integer refreshCascade = null;
                    if (sourceQuery != null && sourceQuery.isObjectBuildingQuery() && sourceQuery.shouldRefreshIdentityMapResult()) {
                        refreshCascade = sourceQuery.getCascadePolicy();
                    }
                    return buildClonePart(cached, null, cacheKey, attributeValue, refreshCascade, executionSession);
                }
                return result;

            }else if (!this.isCacheable && !isTargetProtected && cacheKey != null){
                return null;
            }
        }
        if (row.hasSopObject()) {
            return getAttributeValueFromObject(row.getSopObject());
        }
        ContainerPolicy cp = this.getContainerPolicy();

        Object fieldValue = row.getValues(this.getField());

        // BUG#2667762 there could be whitespace in the row instead of null
        if ((fieldValue == null) || (fieldValue instanceof String)) {
            return cp.containerInstance();
        }

        Vector nestedRows = this.getReferenceDescriptor().buildNestedRowsFromFieldValue(fieldValue, executionSession);
        if (nestedRows == null) {
            return cp.containerInstance();
        }

        Object result = cp.containerInstance(nestedRows.size());
        for (Iterator iterator = nestedRows.iterator(); iterator.hasNext();) {
            AbstractRecord nestedRow = (AbstractRecord) iterator.next();

            ClassDescriptor descriptor = this.getReferenceDescriptor();
            if (descriptor.hasInheritance()) {
                Class<?> newElementClass = descriptor.getInheritancePolicy().classFromRow(nestedRow, executionSession);
                descriptor = this.getReferenceDescriptor(newElementClass, executionSession);
            }

            Object element = buildCompositeObject(descriptor, nestedRow, sourceQuery, cacheKey, joinManager, executionSession);
            if (hasConverter()) {
                element = getConverter().convertDataValueToObjectValue(element, executionSession);
            }
            cp.addInto(element, result, sourceQuery.getSession());
        }
        return result;
    }

    protected abstract Object buildCompositeObject(ClassDescriptor descriptor, AbstractRecord nestedRow, ObjectBuildingQuery query, CacheKey parentCacheKey, JoinedAttributeManager joinManger, AbstractSession targetSession);

    /**
     * Return whether the specified object and all its components have been deleted.
     */
    @Override
    protected boolean verifyDeleteOfAttributeValue(Object attributeValue, AbstractSession session) throws DatabaseException {
        if (attributeValue == null) {
            return true;
        }
        ContainerPolicy cp = this.getContainerPolicy();
        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            if (!super.verifyDeleteOfAttributeValue(cp.next(iter, session), session)) {
                return false;
            }
        }
        return true;
    }

    /**
     * INTERNAL:
     * Get the attribute value from the object and add the appropriate
     * values to the specified database row.
     */
    @Override
    public void writeFromObjectIntoRow(Object object, AbstractRecord row, AbstractSession session, WriteType writeType) throws DescriptorException {
        if (this.isReadOnly()) {
            return;
        }

        Object attributeValue = this.getAttributeValueFromObject(object);
        if (attributeValue == null) {
            row.put(this.getField(), null);
            return;
        }

        ContainerPolicy cp = this.getContainerPolicy();

        Vector nestedRows = new Vector(cp.sizeFor(attributeValue));
        for (Object iter = cp.iteratorFor(attributeValue); cp.hasNext(iter);) {
            Object element = cp.next(iter, session);
            // convert the value - if necessary
            if (hasConverter()) {
                element = getConverter().convertObjectValueToDataValue(element, session);
            }
            nestedRows.addElement(buildCompositeRow(element, session, row, writeType));
        }

        Object fieldValue = null;
        if (!nestedRows.isEmpty()) {
            fieldValue = this.getReferenceDescriptor().buildFieldValueFromNestedRows(nestedRows, getStructureName(), session);
        }
        row.put(this.getField(), fieldValue);
    }

    protected abstract AbstractRecord buildCompositeRow(Object attributeValue, AbstractSession session, AbstractRecord record, WriteType writeType);

    /**
     * INTERNAL:
     * Get the attribute value from the object and add the changed
     * values to the specified database row.
     */
    @Override
    public void writeFromObjectIntoRowForUpdate(WriteObjectQuery writeQuery, AbstractRecord row) throws DescriptorException {
        AbstractSession session = writeQuery.getSession();

        //Helper.toDo("bjv: need to figure out how to handle read-only elements...");
        if (session.isClassReadOnly(this.getReferenceClass())) {
            return;
        }

        if (session.isUnitOfWork()) {
            if (this.compareObjects(writeQuery.getObject(), writeQuery.getBackupClone(), session)) {
                return;// nothing has changed - don't put anything in the row
            }
        }
        this.writeFromObjectIntoRow(writeQuery.getObject(), row, session, WriteType.UPDATE);
    }

    /**
     * INTERNAL:
     * Get the attribute value from the object and add the appropriate
     * values to the specified database row.
     */
    @Override
    public void writeFromObjectIntoRowWithChangeRecord(ChangeRecord changeRecord, AbstractRecord row, AbstractSession session, WriteType writeType) throws DescriptorException {
        Object object = ((ObjectChangeSet)changeRecord.getOwner()).getUnitOfWorkClone();
        this.writeFromObjectIntoRow(object, row, session, writeType);
    }

    /**
     * INTERNAL:
     * Write fields needed for insert into the template with null values.
     */
    @Override
    public void writeInsertFieldsIntoRow(AbstractRecord record, AbstractSession session) {
        if (this.isReadOnly()) {
            return;
        }
        record.put(this.getField(), null);
    }

    @Override
    public boolean isCollectionMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Build and return the change record that results
     * from comparing the two direct collection attributes.
     */
    @Override
    public ChangeRecord compareForChange(Object clone, Object backup, ObjectChangeSet owner, AbstractSession session) {
        return (new ArrayCollectionMappingHelper(this)).compareForChange(clone, backup, owner, session);
    }

    /**
     * INTERNAL:
     * Compare the attributes belonging to this mapping for the objects.
     */
    @Override
    public boolean compareObjects(Object object1, Object object2, AbstractSession session) {
        return (new ArrayCollectionMappingHelper(this)).compareObjects(object1, object2, session);
    }

    /**
     * INTERNAL:
     * Merge changes from the source to the target object.
     */
    @Override
    public void mergeChangesIntoObject(Object target, ChangeRecord changeRecord, Object source, MergeManager mergeManager, AbstractSession targetSession) {
        (new ArrayCollectionMappingHelper(this)).mergeChangesIntoObject(target, changeRecord, source, mergeManager, targetSession);
    }

    /**
     * INTERNAL:
     * Merge changes from the source to the target object.
     * Simply replace the entire target collection.
     */
    @Override
    public void mergeIntoObject(Object target, boolean isTargetUnInitialized, Object source, MergeManager mergeManager, AbstractSession targetSession) {
        (new ArrayCollectionMappingHelper(this)).mergeIntoObject(target, isTargetUnInitialized, source, mergeManager, targetSession);
    }

    /**
     * ADVANCED:
     * This method is used to have an object add to a collection once the changeSet is applied
     * The referenceKey parameter should only be used for direct Maps.
     */
    @Override
    public void simpleAddToCollectionChangeRecord(Object referenceKey, Object changeSetToAdd, ObjectChangeSet changeSet, AbstractSession session) {
        (new ArrayCollectionMappingHelper(this)).simpleAddToCollectionChangeRecord(referenceKey, changeSetToAdd, changeSet, session);
    }

    /**
     * ADVANCED:
     * This method is used to have an object removed from a collection once the changeSet is applied
     * The referenceKey parameter should only be used for direct Maps.
     */
    @Override
    public void simpleRemoveFromCollectionChangeRecord(Object referenceKey, Object changeSetToRemove, ObjectChangeSet changeSet, AbstractSession session) {
        (new ArrayCollectionMappingHelper(this)).simpleRemoveFromCollectionChangeRecord(referenceKey, changeSetToRemove, changeSet, session);
    }

    /**
     * INTERNAL
     * Called when a DatabaseMapping is used to map the key in a collection.  Returns the key.
     */
    public Object createMapComponentFromRow(AbstractRecord dbRow, ObjectBuildingQuery query, CacheKey parentCacheKey, AbstractSession session, boolean isTargetProtected){
        return valueFromRow(dbRow, null, query, parentCacheKey, query.getExecutionSession(), isTargetProtected, null);
    }

}
