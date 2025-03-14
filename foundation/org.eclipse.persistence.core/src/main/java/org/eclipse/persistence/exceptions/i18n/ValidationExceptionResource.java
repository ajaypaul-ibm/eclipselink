/*
 * Copyright (c) 1998, 2024 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998, 2024 IBM Corporation and/or its affiliates. All rights reserved.
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
//     04/05/2011-2.3 Guy Pelletier
//       - 337323: Multi-tenant with shared schema support (part 3)
//     04/21/2011-2.3 Guy Pelletier
//       - 337323: Multi-tenant with shared schema support (part 5)
//     06/30/2011-2.3.1 Guy Pelletier
//       - 341940: Add disable/enable allowing native queries
//     07/11/2011-2.4 Guy Pelletier
//       - 343632: Can't map a compound constraint because of exception:
//                 The reference column name [y] mapped on the element [field x]
//                 does not correspond to a valid field on the mapping reference
//     09/09/2011-2.3.1 Guy Pelletier
//       - 356197: Add new VPD type to MultitenantType
//     22/05/2012-2.4 Guy Pelletier
//       - 380008: Multitenant persistence units with a dedicated emf should force tenant property specification up front.
//     10/09/2012-2.5 Guy Pelletier
//       - 374688: JPA 2.1 Converter support
//     10/25/2012-2.5 Guy Pelletier
//       - 374688: JPA 2.1 Converter support
//     10/30/2012-2.5 Guy Pelletier
//       - 374688: JPA 2.1 Converter support
//     11/28/2012-2.5 Guy Pelletier
//       - 374688: JPA 2.1 Converter support
//     09/24/2014-2.6 Rick Curtis
//       - 443762 : Misc message cleanup.
//     12/18/2014-2.6 Rick Curtis
//       - 454189 : Misc message cleanup.#2
//     09/07/2018-3.0 Dmitry Polienko
//       - 326728: Fix persistence root calculation for WAR files
package org.eclipse.persistence.exceptions.i18n;

import java.util.ListResourceBundle;

/**
 * INTERNAL:
 * English ResourceBundle for ValidationException messages.
 *
 * Creation date: (12/6/00 9:47:38 AM)
 * @author Xi Chen
 */
public final class ValidationExceptionResource extends ListResourceBundle {
    static final Object[][] contents = {
                                           { "7001", "You must login to the ServerSession before acquiring ClientSessions." },
                                           { "7002", "The pool named [{0}] does not exist." },
                                           { "7003", "Max size must be greater than min size." },
                                           { "7004", "Pools must be configured before login." },
                                           { "7008", "The Java type [{0}] is not a valid database type." },
                                           { "7009", "Missing descriptor for [{0}].  Verify that the descriptor has been properly registered with the Session." },
                                           { "7010", "Start index is out of range." },
                                           { "7011", "Stop index is out of range." },
                                           { "7012", "Fatal error occurred." },
                                           { "7013", "You are using the deprecated SessionManager API and no EclipseLink.properties file could be found in your classpath.  No sessions could be read in from file." },
                                           { "7017", "Child descriptors do not have an identity map, they share their parents" },
                                           { "7018", "File error." },
                                           { "7023", "Incorrect login instance provided.  A DatabaseLogin must be provided." },
                                           { "7024", "Invalid merge policy." },
                                           { "7025", "The only valid keys for DatabaseRows are Strings and DatabaseFields." },
                                           { "7027", "The sequence named [{0}] is setup incorrectly.  Its increment does not match its pre-allocation size." },
                                           { "7028", "writeObject() is not allowed within a UnitOfWork." },
                                           { "7030", "You cannot set read pool size after login." },
                                           { "7031", "You cannot add descriptors to a SessionBroker." },
                                           { "7032", "There is no session registered for the class [{0}]." },
                                           { "7033", "There is no session registered with the name [{0}]." },
                                           { "7038", "Error while logging message to Session log." },
                                           { "7039", "Cannot remove from the set of read-only classes in a nested UnitOfWork. {0}A nested UnitOfWork''s set of read-only classes must be equal to or a superset of its parent''s set of read-only classes." },
                                           { "7040", "Cannot change the set of read-only classes in a UnitOfWork after that UnitOfWork has been used. {0}Changes to the read-only set must be made when acquiring the UnitOfWork or immediately after." },
                                           { "7042", "Database platform class [{0}] not found." },
                                           { "7043", "[{0}] does not have any tables to create on the database." },
                                           { "7044", "The container class specified [{0}] cannot be used as a container because it does not implement Collection or Map." },
                                           { "7047", "The container specified [{0}] does not require keys.  You tried to use the method [{1}]." },
                                           { "7048", "Neither the instance method or field named [{0}] exists for the item class [{1}], and therefore cannot be used to create a key for the Map." },
                                           { "7051", "Missing attribute [{1}] for descriptor [{0}], called from [{2}]" },
                                           { "7052", "An attempt was made to use [{0}] (with the key method [{1}]) as a container for a DirectCollectionMapping.  The useMapClass() method cannot be used, only the useCollectionClass() API is supported for DirectCollectionMappings." },
                                           { "7053", "release() attempted on a Session that is not a ClientSession.  Only ClientSessions may be released." },
                                           { "7054", "acquire() attempted on a Session that is not a ServerSession.  ClientSessions may only be acquired from ServerSessions." },
                                           { "7055", "Optimistic Locking is not supported with stored procedure generation." },
                                           { "7056", "The wrong object was registered into the UnitOfWork.  The object [{0}] should be the object from the parent cache [{1}]." },
                                           { "7058", "Invalid Connector [{0}] (must be of type DefaultConnector)." },
                                           { "7059", "Invalid data source name [{0}]." },
                                           { "7060", "Cannot acquire data source [{0}]." },
                                           { "7061", "Exception occurred within JTS." },
                                           { "7062", "Field-level locking is not supported outside of a UnitOfWork.  To use field-level locking, a UnitOfWork must be used for ALL writing." },
                                           { "7063", "Exception occurred within EJB container." },
                                           { "7064", "Exception occurred in reflective EJB primary key extraction.  Ensure your primary key object is defined correctly. {2}Key: [{0}] {2}Bean: [{1}]" },
                                           { "7065", "The remote class for the bean cannot be loaded or found.  Ensure that the correct class loader is set. {2}Bean: [{0}] {2}Remote Class: [{1}]" },
                                           { "7066", "Cannot create or remove beans unless a JTS transaction is present. {1}Bean: [{0}]" },
                                           { "7068", "The project class [{0}] was not found for the project [{1}] using the default class loader." },
                                           { "7071", "Cannot use input/output parameters without using binding." },
                                           { "7072", "The database platform class [{0}] was not found for the project [{1}] using the default class loader." },
                                           { "7073", "The Oracle object type with type name [{0}] is not defined." },
                                           { "7074", "The Oracle object type name [{0}] is not defined." },
                                           { "7075", "Maximum size is not defined for the Oracle VARRAY type [{0}].  A maximum size must be defined." },
                                           { "7076", "When generating the project class, the project''s descriptors must not be initialized. {1}Descriptor: [{0}]" },
                                           { "7077", "The home interface [{0}], specified during creation of the BMPWrapperPolicy, does not contain a correct findByPrimaryKey() method.  A findByPrimaryKey() method must exist that takes the PrimaryKey class for this bean." },
                                           { "7079", "The descriptor for [{0}] was not found in the session [{1}].  Check the project being used for this session." },
                                           { "7080", "A FinderException was thrown when trying to load [{0}], of class [{1}], with primary key [{2}]." },
                                           { "7081", "The aggregate object [{0}] cannot be directly registered in the UnitOfWork.  It must be associated with the source (owner) object." },
                                           { "7084", "The file [{0}] is not a valid type for reading.  ProjectReader must be given the deployed XML Project file." },
                                           { "7086", "The session type [{0}] of the session name [{1}] was not defined properly." },
                                           { "7087", "The session type [{0}] was not found for the [{1}] using default class loader." },
                                           { "7088", "Cannot create an instance of the external transaction controller [{0}], specified in the properties file." },
                                           { "7089", "An exception occurred looking up or invoking the session amendment method [{0}] on the class [{1}] with parameters [{2}]." },
                                           { "7091", "Cannot set listener classes." },
                                           { "7092", "Cannot add a query whose types conflict with an existing query. Query To Be Added: [{0}] is named: [{1}] with arguments [{2}].The existing conflicting query: [{3}] is named: [{4}] with arguments: [{5}]." },
                                           { "7093", "In the query named [{0}], the class [{2}] for query argument named [{1}] cannot be found. Include the missing class on your classpath." },
                                           { "7095", "The sessions.xml resource [{0}] was not found on the resource path.  Check that the resource name/path and classloader passed to the SessionManager.getSession are correct.  The sessions.xml should be included in the root of the application''s deployed jar, if the sessions.xml is deployed in a sub-directory in the application''s jar ensure that the correct resource path using \"/\" not \"\\\" is used." },
                                           { "7096", "Cannot use commit() method to commit UnitOfWork again." },
                                           { "7097", "Operation not supported: [{0}]." },
                                           { "7099", "The deployment project XML resource [{0}] was not found on the resource path.  Check that the resource name/path and classloader passed to the XMLProjectReader are correct.  The project XML should be included in the root of the application''s deployed jar, if the project XML is deployed in a sub-directory in the application''s jar ensure that the correct resource path using \"/\" not \"\\\" is used." },
                                           { "7100", "Could not find the session with the name [{0}] in the session.xml file [{1}]" },
                                           { "7101", "No \"meta-inf/eclipselink-ejb-jar.xml\" could be found in your classpath.  The CMP session could not be read in from file." },
                                           { "7102", "Encountered a null value for a cache key while attempting to remove" +
                                                   "{2}an object from the identity map: [{0}]" +
                                                   "{2}containing an object of class: [{1}] (or a class in this hierarchy)" +
                                                   "{2}The most likely cause of this situation is that the object has already been garbage-" +
                                                   "{2}collected and therefore does not exist within the identity map." +
                                                   "{2}Consider using an alternative identity map to prevent this situation." +
                                                   "{2}Refer to the EclipseLink documentation for more details regarding identity maps ." },
                                           { "7103", "A null reference was encountered while attempting to invoke" +
                                                   "{1}method: [{0}] on an object which uses proxy indirection." +
                                                   "{1}Check that this object is not null before invoking its methods." },
                                           { "7104", "Sequencing login should not use External Transaction Controller." },
                                           { "7105", "Error encountered converting encrypting class: [{0}]" },
                                           { "7106", "Error encountered during string encryption." },
                                           { "7107", "Error encountered during string decryption." },
                                           { "7360", "Database password was encrypted by deprecated algorithm.\nIt's recommended to re-encrypt it by `passwordUpdate.sh` from eclipselink.zip bundle"},
                                           { "7108", "This operation is not supported for non-relational platforms." },
                                           { "7109", "The login in the project used to create the session is null, it must be a valid login." },
                                           { "7110", "At present HistoricalSession only works with Oracle 9R2 or later databases, as it uses Oracle''s Flashback feature." },
                                           { "7111", "You may not acquire a HistoricalSession from a UnitOfWork, another HistoricalSession, a ServerSession, or a ServerSessionBroker.  You may acquire one from a regular session, a ClientSession, or a ClientSessionBroker." },
                                           { "7112", "You have specified that EclipseLink uses the feature : {0}, but this feature is not available in the currently running JDK version :{1}." },
                                           { "7113", "{0} does not support call with returning." },
                                           { "7114", "Isolated data is not currently supported within a client session broker. Session named {0} contains descriptors representing isolated data." },
                                           { "7115", "A Exclusive Connection cannot be used for ClientSession reads without isolated data.  Update the ConnectionPolicy used to remove ExclusiveConnection configuration or the project to set certain data to be exclusive." },
                                           { "7116", "Invalid arguments are used.  Please refer to public API of the calling method and use valid values for the arguments." },
                                           { "7117", "There is an attempt to use more than one cursor in SQLCall {0}" },
                                           { "7118", "setCustomSQLArgumentType method was called on SQLCall {0}, but this call does not use custom SQL" },
                                           { "7119", "Unprepared SQLCall {0} attempted translation" },
                                           { "7120", "Parameter {0} in SQLCall {1} cannot be used as a cursor, because it is has parameter type other than OUT" },
                                           { "7121", "{0} does not support stored functions" },
                                           { "7122", "The exclusive connection associated with the session is unavailable for the query on {0}" },
                                           { "7123", "A successful writeChanges() has been called on this UnitOfWork.  As the commit process has been started but not yet finalized, the only supported operations now are commit, commitAndResume, release, any non-object level query or SQLCall execution.  The operation {0} is not allowed at this time." },
                                           { "7124", "An unsuccessful writeChanges() has been called on this UnitOfWork.  Given the danger that partial changes have been written to the datastore but not rolled back (if inside external transaction), the only supported operations now are release, global transaction rollback, any non-object level query or SQLCall execution.  The operation {0} was attempted." },
                                           { "7125", "Once the UnitOfWork has been committed and/or released, no further operation should be performed on it.  The operation {0} was attempted on it." },
                                           { "7126", "writeChanges cannot be called on a nested UnitOfWork.  A nested UnitOfWork never writes changes directly to the datastore, only the parent UnitOfWork does." },
                                           { "7127", "You can only writes changes to the datastore once, just as you can only call commit once." },
                                           { "7128", "Session [{0}] is already logged in." },
                                           { "7129", "The method''s arguments cannot have null value." },
                                           { "7130", "Nested UnitOfWork is not supported for attribute change tracking." },
                                           { "7131", "{0} is the wrong type.  The collection change event type has to be add or remove." },
                                           { "7132", "{0} is the wrong event class.  Only PropertyChangeEvent and CollectionChangeEvent are supported." },
                                           { "7133", "Old commit is not supported for attribute change tracking." },
                                           { "7134", "Server platform {0} is read-only after login." },
                                           { "7135", "You cannot commit and resume a UnitOfWork containing any modify all queries" },
                                           { "7136", "Nested UnitOfWork is not supported for a modify all query" },
                                           { "7137", "The object is partially fetched (using fetch group), the unfetched attribute ({0}) is not editable." },
                                           { "7139", "Modify all queries cannot be issued within a UnitOfWork containing other write operations." },
                                           { "7140", "Sequence type {0} does not have method {1}." },
                                           { "7141", "{0} sequence is of type DefaultSequence, which cannot be used in setDefaultSequence method." },
                                           { "7142", "{0} sequence cannot be set as default, because a sequence with that name has been already added" },
                                           { "7143", "{0} sequence cannot be added, because a sequence with that name has been already set as default." },
                                           { "7144", "{0}: platform {1} does not support {2}." },
                                           { "7145", "{2} attempts to connect to sequence {0}, but it is already connected to {1}. Likely the two sessions share the DatasourcePlatform object" },
                                           { "7146", "QuerySequence {1} does not have select query." },
                                           { "7147", "Platform {0} cannot create platform default sequence - it does not override createPlatformDefaultSequence method" },
                                           { "7148", "commitAndResume() cannot be used with a JTA/synchronized UnitOfWork." },
                                           { "7149", "The composite primary key attribute [{2}] of type [{4}] on entity class [{0}] should be of the same type as defined on its primary key class [{1}]. That is, it should be of type [{3}]." },
                                           { "7150", "Invalid composite primary key specification. The names of the primary key fields or properties in the primary key class [{1}] and those of the entity bean class [{0}] must correspond and their types must be the same. Also, ensure that you have specified ID elements for the corresponding attributes in XML and/or an @Id on the corresponding fields or properties of the entity class." },
                                           { "7151", "The type [{1}] for the attribute [{0}] on the entity class [{2}] is not a valid type for an enumerated mapping. The attribute must be defined as a Java enum."},
                                           { "7153", "Mapping annotations cannot be applied to fields or properties that have a @Transient specified. [{0}] is in violation of this restriction." },
                                           { "7154", "The attribute [{3}] in entity class [{2}] has a mappedBy value of [{1}] which does not exist in its owning entity class [{0}]. If the owning entity class is a @MappedSuperclass, this is invalid, and your attribute should reference the correct subclass." },
                                           { "7155", "The type [{1}] for the attribute [{0}] on the entity class [{2}] is not a valid type for a serialized mapping. The attribute type must implement the Serializable interface."},
                                           { "7156", "Unable to find the class named [{0}]. Ensure the class name/path is correct and available to the classloader."},
                                           { "7157", "Entity class [{0}] must use a @JoinColumn instead of @Column to map its relationship attribute [{1}]."},
                                           { "7158", "Error encountered when building the @NamedQuery [{1}] from entity class [{0}]."},
                                           { "7159", "The map key [{0}] on the entity class [{1}] could not be found for the mapping [{2}]."},
                                           { "7160", "@OneToMany for attribute name [{1}] in entity class [{0}] should not have JoinColumn(s) specified. " +
                                                     "Where a @OneToMany is not mapped by another entity (i.e. it is the owning side and is uni-directional) a @JoinTable must be specified, not @JoinColumn(s). " +
                                                     "If @JoinTable is not specified, a default join table will be used instead; Specify @JoinTable only if the default configuration should be overridden." },
                                           { "7161", "Entity class [{0}] has no primary key specified. It should define either an @Id, @EmbeddedId or an @IdClass." +
                                                     " If you have defined PK using any of these annotations then make sure that you do not have " +
                                                     "mixed access-type (both fields and properties annotated) in your entity class hierarchy."},
                                           { "7162", "Entity class [{0}] has multiple @EmbeddedId annotations specified (on attributes [{1}] and [{2}]). Only one @EmbeddedId can be specified per Entity."},
                                           { "7163", "Entity class [{0}] has both an @EmbdeddedId (on attribute [{1}]) and an @Id (on attribute [{2}]. Both ID types cannot be specified on the same entity."},
                                           { "7164", "The type [{1}] for the attribute [{0}] on the entity class [{2}] is not a valid type for a lob mapping. For a lob of type BLOB, the attribute must be defined as a java.sql.Blob, byte[], Byte[] or a Serializable type. For a lob of type CLOB, the attribute must be defined as a java.sql.Clob, char[], Character[] or String type."},
                                           { "7165", "The type [{1}] for the attribute [{0}] on the entity class [{2}] is not a valid type for a temporal mapping. The attribute must be defined as java.util.Date or java.util.Calendar."},
                                           { "7166", "A table generator that uses the reserved name [{0}] for its \"name\" has been found in [{1}]. It cannot use this name because it is reserved for defaulting a sequence generator \"sequence name\"."},
                                           { "7167", "A sequence generator that uses the reserved name [{0}] for its \"sequence name\" has been found in [{1}]. It cannot use this name because it is reserved for defaulting a table generator''s \"name\"."},
                                           { "7168", "The attribute [{0}] of type [{1}] on the entity class [{2}] is not valid for a version property. The following types are supported: int, Integer, short, Short, long, Long, Timestamp."},
                                           { "7169", "Class [{0}] has two @GeneratedValues: for fields [{1}] and [{2}]. Only one is allowed."} ,
                                           { "7172", "Error encountered when instantiating the class [{0}]."},
                                           { "7173", "A property change event has been fired on a property with name [{1}] in [{0}].  However this property does not exist."},
                                           { "7174", "The getter method [{1}] on entity class [{0}] does not have a corresponding setter method defined."},
                                           { "7175", "The mapping [{0}] does not support cascading version optimistic locking."},
                                           { "7176", "The mapping [{0}] does not support cascading version optimistic locking because it has a custom query."},
                                           { "7177", "The aggregate descriptor [{0}] has privately-owned mappings. Aggregate descriptors do not support cascading version optimistic locking."},
                                           { "7178", "OracleOCIProxyConnector requires OracleOCIConnectionPool datasource."},
                                           { "7179", "OracleJDBC_10_1_0_2ProxyConnectionCustomizer requires datasource producing OracleConnections."},
                                           { "7180", "OracleJDBC_10_1_0_2ProxyConnectionCustomizer requires Oracle JDBC version 10.1.0.2 or higher so that OracleConnection declares openProxySession method."},
                                           { "7181", "OracleJDBC_10_1_0_2ProxyConnectionCustomizer requires PersistenceUnitProperties.ORACLE_PROXY_TYPE property value to be either Integer or convertable to Integer: for instance OracleConnection.PROXYTYPE_USER_NAME or Integer.toString(OracleConnection.PROXYTYPE_USER_NAME)"},
                                           { "7182", "EC - Could not find driver class [{0}]"},
                                           { "7183", "Error closing persistence.xml file."},
                                           { "7184", "[{0}] system property not specified. It must be set to a class that defines a \"getContainerConfig()\" method."},
                                           { "7185", "Cannot find class [{0}] specified in [{1}]"},
                                           { "7186", "Cannot invoke method [{0}] on class [{1}] specified in [{2}]"},
                                           { "7187", "[{0}] should define a public static method [{1}] that has no parameters and returns Collection"},
                                           { "7188", "Non-null class list is required."},
                                           { "7189", "Cannot create temp classloader from current loader: [{0}]"},
                                           { "7190", "[{0}] failed"},
                                           { "7191", "The entity class [{0}] was not found using class loader [{1}]." },
                                           { "7192", "ClassFileTransformer [{0}] throws an exception when performing transform() on class [{1}]." },
                                           { "7193", "Jar files in persistence XML are not supported in this version of EclipseLink." },
                                           { "7194", "Could not bind: [{0}] to: [{1}]." },
                                           { "7195", "Exception configuring EntityManagerFactory." },
                                           { "7196", "[{0}] of type [{1}] cannot be casted to [{2}]."},
                                           { "7197", "Null or zero primary key encountered in UnitOfWork clone [{0}], primary key [{1}]. Set descriptor''s IdValidation or the \"eclipselink.id-validation\" property."},
                                           { "7198", "Class: [{0}] was not found while converting from class names to classes."},
                                           { "7199", "A primary table was not defined for entity {0} in the entity-mappings file: {1}.  A primary table is required to process an entity relationship."},
                                           { "7200", "The attribute [{1}] was not found on the embeddable class [{0}]. It is referenced in an attribute-override for the embedded attribute [{3}] on class [{2}]."},
                                           { "7201", "An exception occurred parsing the entity-mappings file: {0}."},
                                           { "7202", "Attribute-override name {0} is invalid - make sure that an attribute with the same name exists in the embeddable {1}."},
                                           { "7203", "The mapping element [{1}] for the class [{2}] has an unsupported collection type [{0}]. Only Set, List, Map and Collection are supported."},
                                           { "7207", "Attribute [{1}] in entity class [{0}] has an invalid type for a lob of type BLOB. The attribute must be defined as a java.sql.Blob, byte[], Byte[] or a Serializable type."},
                                           { "7208", "Attribute [{1}] in entity class [{0}] has an invalid type for a lob of type CLOB. The attribute must be defined as a java.sql.Clob, char[], Character[] or String type."},
                                           { "7212", "The attribute [{0}] from the entity class [{1}] does not specify a temporal type. A temporal type must be specified for persistent fields or properties of type java.util.Date and java.util.Calendar."},
                                           { "7213", "Circular mappedBy references have been specified (Class: [{0}], attribute: [{1}] and Class: [{2}], attribute: [{3}]. This is not valid, only one side can be the owner of the relationship. Therefore, specify a mappedBy value only on the non-owning side of the relationship."},
                                           { "7214", "The target entity of the relationship attribute [{0}] on the class [{1}] cannot be determined.  When not using generics, ensure the target entity is defined on the relationship mapping."},
                                           { "7215", "Could not load the field named [{0}] on the class [{1}]. Ensure there is a corresponding field with that name defined on the class."},
                                           { "7216", "Could not load the method for the property name [{0}] on the class [{1}]. Ensure there is corresponding get method for that property name defined on the class."},
                                           { "7217", "The order by value [{0}], specified on the element [{2}] from entity [{3}], is invalid. No property or field with that name exists on the target entity [{1}]."},
                                           { "7218", "[{0}] does not override getCreateTempTableSqlPrefix method. DatabasePlatforms that support temporary tables must override this method."},
                                           { "7219", "[{0}] does not override valueFromRowInternalWithJoin method, but its isJoiningSupported method returns true. Foreign reference mapping that supports joining must override this method."},
                                           { "7220", "The @JoinColumns on the annotated element [{0}] from the entity class [{1}] is incomplete. When the source entity class uses a composite primary key, a @JoinColumn must be specified for each join column using the @JoinColumns. Both the name and the referencedColumnName elements must be specified in each such @JoinColumn."},
                                           { "7222", "An incomplete @PrimaryKeyJoinColumns was specified on the annotated element [{0}]. When specifying @PrimaryKeyJoinColumns for an entity that has a composite primary key, a @PrimaryKeyJoinColumn must be specified for each primary key join column using the @PrimaryKeyJoinColumns. Both the name and the referencedColumnName elements must be specified in each such @PrimaryKeyJoinColumn."},
                                           { "7223", "A @PrimaryKeyJoinColumns was found on the annotated element [{0}]. When the entity uses a single primary key, only a single (or zero) @PrimaryKeyJoinColumn should be specified."},
                                           { "7224", "The method [{1}] on the listener class [{0}] is an invalid callback method."},
                                           { "7225", "The method [{1}] could not be found on the listener class [{0}]."},
                                           { "7226", "The method [{1}] on the listener class [{0}] has an invalid modifier. Callback methods cannot be static or final."},
                                           { "7227", "The listener class [{0}] has multiple lifecycle callback methods for the same lifecycle event ([{1}] and [{2}])."},
                                           { "7228", "The Callback method [{1}] on the listener class [{0}] has an incorrect signature. It should not have any parameters."},
                                           { "7229", "The Callback method [{3}] on the entity listener class [{2}] has an incorrect signature. The method must take 1 parameter which must be assignable from the entity class. Here, the parameter class [{1}] is not assignable from the entity class [{0}]."},
                                           { "7231", "Cannot persist detached object [{0}]. {3}Class> {1} Primary Key> {2}"},
                                           { "7232", "The entity class [{0}] contains multiple @Id declarations, but does not define any <id> elements in the entity mappings instance document.  Please ensure that if there are multiple @Id declarations for a given entity class, the corresponding <entity> definition contains an <id> element for each."},
                                           { "7233", "Mapping metadata cannot be applied to properties/methods that take arguments. The attribute [{0}] from class [{1}] is in violation of this restriction. Ensure the method has no arguments if it is mapped with annotations or in an XML mapping file." },
                                           { "7234", "DDL generation requires that the class transformer, used with the transformation mapping of attribute [{1}] from the descriptor [{0}], specify a specific return type for its [{2}] method (and not Object). DDL generation requires this specific return type so that it may generate the correct field type." },
                                           { "7235", "The class transformer used with the transformation mapping of attribute [{1}] from the descriptor [{0}] does not implement the [{2}] method. This method is part of the FieldTransformer interface and must be implemented. Note also, when implemented, its return type should also be an explicit type (and not Object) when using DDL generation." },
                                           { "7237", "Entity name must be unique in a persistence unit. Entity name [{0}] is used for the entity classes [{1}] and [{2}]."},
                                           { "7238", "The table generator specified in [{2}] with name == [{0}] conflicts with the sequence generator with the same name specified in [{1}]."},
                                           { "7240", "The table generator specified in [{2}] with pk column value == [{0}] conflicts with the sequence generator specified in [{1}] with sequence name == [{0}]. They cannot use the same value."},
                                           { "7242", "An attempt was made to traverse a relationship using indirection that had a null Session.  This often occurs when an entity with an uninstantiated LAZY relationship is serialized and that relationship is traversed after serialization.  To avoid this issue, instantiate the LAZY relationship prior to serialization."},
                                           { "7243", "Missing meta data for class [{0}]. Ensure the class is not being excluded from your persistence unit by a <exclude-unlisted-classes>true</exclude-unlisted-classes> setting. If this is the case, you will need to include the class directly by adding a <class>[{0}]</class> entry for your persistence-unit."},
                                           { "7244", "An incompatible mapping has been encountered between [{0}] and [{1}]. This usually occurs when the cardinality of a mapping does not correspond with the cardinality of its backpointer."},
                                           { "7245", "The embeddable class [{0}] is used in classes with conflicting access types. Class [{1}] uses access [{2}] and class [{3}] uses access [{4}]. When sharing an embeddable object between classes, the access types of those embedding class must be the same."},
                                           { "7246", "The Entity class [{0}] has an embedded attribute [{1}] of type [{2}] which is NOT an Embeddable class. Probable reason: missing @Embeddable or missing <embeddable> in orm.xml if metadata-complete = true"},
                                           { "7247", "A circular reference was discovered processing derived IDs on the following Entity classes: [{0}] "},
                                           { "7249", "Entity [{0}] uses [{1}] as embedded ID class whose access-type has been determined as [{2}]. But [{1}] does not define any [{2}]. It is likely that you have not provided sufficient metadata in your ID class [{1}]."},
                                           { "7250", "[{0}] uses a non-entity [{1}] as target entity in the relationship attribute [{2}]."},
                                           { "7251", "The attribute [{1}] of class [{0}] is mapped to a primary key column in the database. Updates are not allowed."},
                                           { "7252", "There are multiple mapping files called [{1}] in classpath for persistence unit named [ {0} ]."},
                                           { "7253", "There is no mapping file called [{1}] in classpath for persistence unit named [{0}]."},
                                           { "7254", "The converter with name [{1}] in the class [{0}] has mapped the data value [{2}] to multiple object values. A conversion value must map each data value only once."},
                                           { "7255", "The class [{0}] specifies a @Convert on [{1}]. This is invalid. A @Convert is only supported with a @Basic, @BasicCollection, @BasicMap and @ElementCollection. For to-many mappings that use a map, you may use a @MapKeyConvert only."},
                                           { "7256", "The converter with name [{1}] used with the element [{2}] in the class [{0}] was not found within the persistence unit. Please ensure you have provided the correct converter name."},
                                           { "7257", "Unable to instantiate data value with type [{2}] and value [{1}] from the object type converter named [{0}]"},
                                           { "7258", "Unable to instantiate object value with type [{2}] and value [{1}] from the object type converter named [{0}]"},
                                           { "7259", "Unable to determine the data type for the attribute [{1}] from the entity class [{0}] that uses the converter named [{2}]. A type must be specified using the data type on the converter or your attribute should use a generic specification."},
                                           { "7260", "Unable to determine the object type for the attribute [{1}] from the entity class [{0}] that uses the converter named [{2}]. A type must be specified using the object type on the converter or your attribute should use a generic specification."},
                                           { "7261", "The type [{1}] for the attribute [{0}] on the entity class [{2}] is not a valid type for a basic collection mapping. The attribute must be of type Collection.class, List.class or Set.class."},
                                           { "7262", "The type [{1}] for the attribute [{0}] on the entity class [{2}] is not a valid type for a basic map mapping. The attribute must be of type Map.class."},
                                           { "7263", "Class [{0}] has an incomplete optimistic locking specification. For an optimistic locking policy of type SELECTED_COLUMNS, the selected columns must be specified and the names of those columns cannot be omitted."},
                                           { "7264", "Class [{0}] has an incomplete optimistic locking specification. For an optimistic locking policy of type VERSION_COLUMN, a @Version must be specified on the version field or property."},
                                           { "7265", "A @Cache annotation is not allowed on an embeddable class."},
                                           { "7266", "The @Cache annotation on class [{0}] has both expiry() and expiryTimeOfDay() specified. Only one of the two may be specified in a @Cache annotation setting."},
                                           { "7267", "The specified exception handler class [{0}] is invalid, the class must exist in the classpath and implement the interface ExceptionHandler."},
                                           { "7268", "The specified session event listener class [{0}] is invalid, the class must exist in the classpath and implement the interface SessionEventListener."},
                                           { "7270", "The specified cache statements size value [{0}] is invalid [{1}]."},
                                           { "7271", "The specified boolean value [{0}] for setting native sql is invalid, the value must either be \"true\" or \"false\"."},
                                           { "7272", "The specified boolean value [{0}] for SQL statement cache enable is invalid, the value must be either \"true\" or \"false\"."},
                                           { "7273", "The specified boolean value [{0}] for copying descriptor named queries into the session is invalid, the value must either be \"true\" or \"false\"."},
                                           { "7274", "An Exception was thrown while trying to create logging file [{0}]:[{1}]."},
                                           { "7275", "Unable to instantiate the exception handler class [{0}] specified in the property eclipselink.exception-handler [{1}]."},
                                           { "7276", "Unable to instantiate the session event listener class [{0}] specified in the property eclipselink.session-event-listener [{1}]."},
                                           { "7277", "The name of the logging file was not specified."},
                                           { "7278", "The specified boolean value [{0}] for the persistence property [{1}] is invalid, the value must either be \"true\" or \"false\"."},
                                           { "7282", "The StructConverter: {0} may not be defined on mapping {1}.  StructConverters may only be used on direct mappings." },
                                           { "7283", "Two StructConverters were added for class {0}.  Only one StructConverter may be added per class." },
                                           { "7284", "The class [{0}] is not a valid comparator. The class must implement the Comparator interface."},
                                           { "7285", "The specified profiler class [{0}] is invalid, the class must exist in the classpath and implement the interface SessionProfiler."},
                                           { "7286", "Unable to instantiate the profiler class [{0}] specified in the property eclipselink.profiler [{1}]."},
                                           { "7287", "The read transformer specified for the element [{0}] does not implement required interface AttributeTransformer."},
                                           { "7288", "The read transformer specified for the element [{0}] has both class and method. Either class or method is required, but not both of them."},
                                           { "7289", "The read transformer specified for the element [{0}] has neither class nor method. Either class or method is required, but not both of them."},
                                           { "7290", "The write transformer specified for the column [{1}] of element [{0}] does not implement required interface FieldTransformer."},
                                           { "7291", "The write transformer specified for the column [{1}] of element [{0}] has both class and method. Either class or method is required, but not both of them."},
                                           { "7292", "The write transformer specified for the column [{1}] of element [{0}] has neither class nor method. Either class or method is required, but not both of them."},
                                           { "7293", "The write transformer specified for the element [{0}] either does not have a column or the column does not have name."},
                                           { "7294", "The variable one to one element [{1}] has multiple entities mapped to the same discriminator [{0}]. Each entity that implements the variable one to one interface must have its own unique discriminator."},
                                           { "7295", "The CloneCopyPolicy specified on class [{0}] does not specify either a method or a workingCopyMethod.  It is required that one of these be specified."},
                                           { "7296", "Class [{0}] has multiple CopyPolicy Annotations.  Only one CopyPolicyAnnotation is allowed per class."},
                                           { "7297", "An exception was thrown while reflectively instantiating Class [{0}].  This usually means that this class is specified in your meta data and for some reason java cannot instanitate it reflectively with a no-args constructor.  Check the chained exception for more information.  Note: To see the chained exception you may need to increase your logging level."},
                                           { "7298", "The mapping [{2}] from the embedded ID class [{3}] is an invalid mapping for this class. An embeddable class that is used with an embedded ID specification (attribute [{0}] from the source [{1}]) can only contain basic mappings. Either remove the non basic mapping or change the embedded ID specification on the source to be embedded."},
                                           { "7299", "Conflicting annotations with the same name [{0}] were found. The first one [{1}] was found within [{2}] and the second [{3}] was found within [{4}]. Named annotations must be unique across the persistence unit." },
                                           { "7300", "Conflicting XML elements [{1}] with the same name [{0}] were found. The first was found in the mapping file [{2}] and the second in the mapping file [{3}]. Named XML elements must be unique across the persistence unit." },
                                           { "7301", "Conflicting annotations were found. The first one [{0}] was found within [{1}] and the second [{2}] was found within [{3}]. Please correct this by removing the annotation which does not apply." },
                                           { "7302", "Conflicting XML elements [{0}] were found for the element [{1}]. The first was found in the mapping file [{2}] and the second in the mapping file [{3}]. Please correct this by removing the XML element which does not apply." },
                                           { "7303", "PersistenceUnitProperties.ORACLE_PROXY_TYPE property set to [{0}], required for this proxy type property [{1}] not found." },
                                           { "7304", "PersistenceUnitProperties.ORACLE_PROXY_TYPE property set to unknown type [{0}], known types are [{1}], [{2}], [{3}]." },
                                           { "7305", "An exception was thrown while processing the mapping file from URL: [{0}]." },
                                           { "7306", "The annotated element [{0}] from the class [{1}] has an incorrect explicit access type specified. It should specify an access type of [{2}]." },
                                           { "7307", "Missing a logging context string for the context [{0}]. This is an internal exception that occurred retrieving a log message for Jakarta Persistence metadata processing, please report a bug." },
                                           { "7308", "The specified value [{0}] for for the persistence property [{1}] is invalid - [{2}]."},
                                           { "7309", "The attribute named [{1}] from the embeddable class [{0}] is not a valid mapping to use with an attribute-override for the attribute [{3}] on class [{2}]." },
                                           { "7310", "The target class of the element collection attribute [{0}] on the class [{1}] cannot be determined.  When not using generics, ensure the target class is defined on the element collection mapping."},
                                           { "7311", "An invalid target class is being used with the element collection attribute [{0}] on the class [{1}].  Only basic types and embeddable classes are allowed."},
                                           { "7312", "An invalid embeddable class [{0}] is being used with the element collection attribute [{1}] on the class [{2}]. See section 2.6 of the spec: \" An embeddable class (including an embeddable class within another embeddable class) contained within an element collection must not contain an element collection, nor may it contain a relationship to an entity other than a many-to-one or one-to-one relationship. The embeddable class must be on the owning side of such a relationship and the relationship must be mapped by a foreign key mapping.\" The mapping for the attribute [{3}] on the embeddable class is in violation of this."},
                                           { "7313", "The attribute [{1}] was not found on the embeddable class [{0}]. It is referenced in an association-override for the embedded attribute [{3}] on class [{2}]."},
                                           { "7314", "The mapping [{0}] is being used to map the key in a MappedKeyMapContainerPolicy and uses indirection.  Mappings used for Map Keys cannot use indirection."},
                                           { "7315", "The map key class of the element collection attribute [{0}] on the class [{1}] cannot be determined.  When specifying a convert key with an element collection, ensure you are using a generic definition so that a class type can be determined for the converter."},
                                           { "7316", "The MapsId value [{0}] from the mapping attribute [{1}] is invalid. An equivalent attribute with that name must be defined on the IdClass [{2}]"},
                                           { "7317", "List order field is not supported for [{0}]." },
                                           { "7318", "[{0}] has list order set, but CollectionChangeEvent.REMOVE was sent without index." },
                                           { "7319", "The attribute named [{1}] from the embeddable class [{0}] is not a valid relationship to use with the association-override named [{2}] from [{3}]. An association-override may be specified only when the embeddable is on the owning side of the relationship." },
                                           { "7320", "The attribute [{0}] from the class [{1}] (or inherited from a mapped superclass) is not a valid type to use with an order column specification. When specifying an order column, the attribute must be of type List." },
                                           { "7321", "The field [{1}] from the derived id mapping [{2}] from class [{3}] is an invalid id field from the reference class [{0}]. Ensure there is a corresponding id mapping to that field. " },
                                           { "7322", "The reference column name [{0}] from the association-override named [{1}] on the attribute [{2}] from class [{3}] is not a valid mapped primary key field. Ensure there is a corresponding id mapping to that field" },
                                           { "7323", "The table with name [{1}] from [{2}] has multiple unique constraints with the name [{0}]. This is not allowed, unique constraint names must be unique across all tables." },
                                           { "7324", "The entity class [{1}] specifies both a @ClassExtractor and discriminator metadata. When using a @ClassExtractor, a @DiscriminatorColumn and/or @DiscriminatorValue should not be specified on this class nor should any discriminator value metadata be defined on its subclasses." },
                                           { "7325", "The sql result set mapping [{0}] used with the named query [{1}] from [{2}] is not a recognized sql result set mapping. Ensure the name is correct and that a sql result set mapping with that name exists." },
                                           { "7326", "The attribute [{0}] from the class [{1}] mapped in [{2}] which uses VIRTUAL access does not specify an attribute-type. When using VIRTUAL access, an attribute-type must be specified. Note: For a one-to-one or a many-to-one, the attribute-type is specified using the target-entity. For a variable-one-to-one it is specified using the target-class." },
                                           { "7327", "The embeddable class [{0}] is used in classes with conflicting access methods. Class [{1}] uses access methods [{2}] and class [{3}] uses access methods [{4}]. When sharing an embeddable object between classes, the access methods of those embedding class must be the same."},
                                           { "7328", "When using VIRTUAL access a DynamicClassLoader must be provided when creating the EntityManagerFactory using the eclipselink property [eclipselink.classloader]. That is, createEntityManagerFactory(String persistenceUnitName, Map properties) and add a new DynamicClassLoader() to the Map properties."},
                                           { "7329", "Attribute {1} of {0} is not mapped."},
                                           { "7330", "Attribute {1} of {0} references a nested fetch group but either not mapped with ForeignReferenceMapping or the mapping does not have reference descriptor."},
                                           { "7331", "Attribute {1} of {0} references a nested fetch group but the target class does not support fetch groups."},
                                           { "7332", "The derived composite primary key attribute [{2}] of type [{4}] from [{1}] should be of the same type as its parent id field from [{0}]. That is, it should be of type [{3}]." },
                                           { "7334", "Class [{0}] has an incomplete primary key specification. When specifying primary key columns, the names of those columns must be specified."},
                                           { "7335", "Duplicate partition value [{1}] given for @ValuePartitioning named [{0}]"},
                                           { "7336", "Multiple context properties [{2}] and [{3}] specified for the same tenant discriminator field [{1}] for class [{0}]"},
                                           { "7337", "The mapped tenant discriminator column [{1}] on the class [{0}] must be marked as read only. In Jakarta Persistence API this is done by setting insertable=false and updatable=false on the column e.g. @Column(name=\"TENANT_ID\", insertable=false, updatable=false)."},
                                           { "7338", "You cannot add sequences to a SessionBroker." },
                                           { "7339", "Alias [{0}] is used by both [{1}] and [{2}] classes. Descriptor alias must be unique." },
                                           { "7340", "There are multiple mapping files called [{0}] in classpath."},
                                           { "7341", "No eclipselink-orm.xml was specified for the XMLMetadataSource.  Please specify one using either persistence unit property eclipselink.metadata-source.xml.file or eclipselink.metadata-source.xml.url"},
                                           { "7342", "The specified boolean value [{0}] for setting allow native sql queries is invalid, the value must either be \"true\" or \"false\"."},
                                           { "7343", "Multiple VPD identifiers (tenant discriminator context property) have been specified. Entity [{1}] uses [{0}] and Entity [{3}] uses [{2}]. The Multitenant VPD strategy allows only one tenant discriminator column for each entity and its context property must be consistent across all the Multitenant VPD entities."},
                                           { "7344", "VPD (connections and DDL generation) is not supported for the platform: [{0}]."},
                                           { "7345", "{0} file specified for XMLMetadataSource is not found"},
                                           { "7346", "The multitenant context property [{0}] has not been provided. When the persistence unit property ({1}) is set to false, all multitenant context properties must be provided up front. This can be done through the persistence unit definition directly or by passing a properties map containing all the multitenant context properties on the create EntityManagerFactory call."},
                                           { "7347", "The class [{0}] specifies type level convert metadata without specifying an attribute name for each. An attribute name must be provided for all type level convert metadata to ensure the correct application to a super class attribute."},
                                           { "7348", "The embedded mapping [{1}] from [{0}] does not specify an attribute name to which the convert is to be applied. You must specify an attribute name on the Embeddable."},
                                           { "7350", "The convert attribute name [{3}] from the mapping [{1}] from the class [{0}] was not found on the embeddable class [{2}]. Please ensure the attribute exists and is correctly named." },
                                           { "7351", "The converter class [{2}] specified on the mapping attribute [{1}] from the class [{0}] was not found. Please ensure the converter class name is correct and exists with the persistence unit definition." },
                                           { "7352", "The converter class [{0}] must implement the Jakarta Persistence jakarta.persistence.AttributeConverter<X, Y> interface to be a valid converter class." },
                                           { "7353", "The mapping attribute [{1}] from the class [{0}] is not a valid mapping type for a convert specification." },
                                           { "7354", "The mapping attribute [{1}] from the class [{0}] is not a valid mapping type for a map key convert specification." },
                                           { "7355", "The mapping attribute [{1}] from the class [{0}] is not a valid mapping type for a convert using an attribute name specification. An attribute name should only be specified to traverse an Embedded mapping type." },
                                           { "7356", "Procedure: [{1}] cannot be executed because {0} does not currently support multiple out parameters"},
                                           { "7357", "The \"[{0}]\" URL for the \"[{1}]\" resource does not belong to a valid persistence root, as defined by the Jakarta Persistence specification"},
                                           { "7358", "Incorrect ASM service name provided."},
                                           { "7359", "No any ASM service available."},
 };

    /**
     * Default constructor.
     */
    public ValidationExceptionResource() {
        // for reflection
    }

    /**
     * Return the lookup table.
     */
    @Override
    protected Object[][] getContents() {
        return contents;
    }
}
