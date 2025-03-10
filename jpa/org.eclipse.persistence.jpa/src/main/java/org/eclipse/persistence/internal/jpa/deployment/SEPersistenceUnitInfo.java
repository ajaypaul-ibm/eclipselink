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
//     06/16/2009-2.0 Guy Pelletier
//       - 277039: JPA 2.0 Cache Usage Settings
package org.eclipse.persistence.internal.jpa.deployment;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.sql.DataSource;

import jakarta.persistence.PersistenceConfiguration;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.PersistenceUnitTransactionType;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.internal.jpa.jdbc.DataSourceImpl;

/**
 * Internal implementation of the PersistenceUnitInfo detailed in the EJB 3.0 specification
 * Used by our Java SE implementation so common method calls can be used in setting
 * of Container and non-Container EntityManagerFactories.
 */
public class SEPersistenceUnitInfo implements jakarta.persistence.spi.PersistenceUnitInfo {

    // What about 2.0 in 1.0 container here ...
    protected SharedCacheMode cacheMode;
    protected ValidationMode validationMode;
    protected String persistenceUnitName;
    protected String persistenceProviderClassName;
    protected DataSource jtaDataSource;
    protected DataSource nonJtaDataSource;
    protected PersistenceUnitTransactionType persistenceUnitTransactionType;
    protected List<String> mappingFiles;

    // names of jars specified in persistence.xml. they are later on used
    // to build jar-file URL.
    private final Collection<String> jarFiles = new ArrayList<>();
    protected List<URL> jarFileUrls;
    protected List<String> managedClassNames;
    // persistence.xml root URL
    protected URL persistenceUnitRootUrl;
    // PersistenceConfiguration hash of programmatically defined PU, value is null for PU defined in persistence.xml
    private String configHash;

    protected boolean excludeUnlistedClasses = true;

    // Persistence.xml loaded from the canonical model processor will
    // populate the properties into this collection.
    protected List<SEPersistenceUnitProperty> persistenceUnitProperties = new ArrayList<>();
    // Persistence.xml loaded from the metadata processor will populate the
    // properties into this properties map.
    protected Properties properties;

    protected ClassLoader tempClassLoader;
    protected ClassLoader realClassLoader;

    public SEPersistenceUnitInfo(){
        mappingFiles = new ArrayList<>();
        managedClassNames = new ArrayList<>();
        properties = new Properties();
        persistenceUnitTransactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;
        // don't initialize jarFileUrls as it is lazily initialized
    }

    /**
     * Creates internal implementation of the {@link PersistenceUnitInfo}
     * from {@link PersistenceConfiguration} content.
     *
     * @param configuration configuration of a persistence unit
     * @param rootURL root {@link URL} of the persistence unit
     */
    public SEPersistenceUnitInfo(PersistenceConfiguration configuration, URL rootURL) {
        persistenceUnitName = configuration.name();
        persistenceProviderClassName = configuration.provider();
        if (configuration.jtaDataSource() != null && !configuration.jtaDataSource().isEmpty()) {
            jtaDataSource = new DataSourceImpl(configuration.jtaDataSource(), null, null, null);
        }
        if (configuration.nonJtaDataSource() != null && !configuration.nonJtaDataSource().isEmpty()) {
            nonJtaDataSource = new DataSourceImpl(configuration.nonJtaDataSource(), null, null, null);
        }
        cacheMode = configuration.sharedCacheMode();
        validationMode = configuration.validationMode();
        persistenceUnitTransactionType = configuration.transactionType();
        mappingFiles = configuration.mappingFiles();
        managedClassNames = configuration.managedClasses()
                .stream()
                .map(Class::getName)
                .toList();
        properties = new Properties();
        properties.putAll(configuration.properties());
        if (properties.contains(PersistenceUnitProperties.CLASSLOADER)) {
            realClassLoader = (ClassLoader) properties.get(PersistenceUnitProperties.CLASSLOADER);
        } else {
            realClassLoader = Thread.currentThread().getContextClassLoader();
        }
        persistenceUnitRootUrl = rootURL;
        configHash = Integer.toString(persistenceConfigurationHashCode(configuration));
    }

    // TODO: Remove when fixed by implementing hashCode in jakarta.persistence API
    private static int persistenceConfigurationHashCode(PersistenceConfiguration configuration) {
        return Objects.hash(configuration.name(),
                                configuration.provider(),
                                configuration.jtaDataSource(),
                                configuration.nonJtaDataSource(),
                                configuration.sharedCacheMode(),
                                configuration.validationMode(),
                                configuration.transactionType(),
                                configuration.managedClasses(),
                                configuration.mappingFiles(),
                                configuration.properties());
    }

    /**
    * @return The name of the persistence unit.
    * Corresponds to the &lt;name&gt; element in the persistence.xml file.
    */
    @Override
    public String getPersistenceUnitName(){
        return persistenceUnitName;
    }

    public void setPersistenceUnitName(String persistenceUnitName){
        this.persistenceUnitName = persistenceUnitName;
    }

    /**
     * Used with the OX mapping file for the Canonical model processor.
     */
    public List<SEPersistenceUnitProperty> getPersistenceUnitProperties() {
       return persistenceUnitProperties;
    }

    /**
     * Used with the OX mapping file for the Canonical model processor.
     */
    public void setPersistenceUnitProperties(List<SEPersistenceUnitProperty> persistenceUnitProperties) {
       this.persistenceUnitProperties = persistenceUnitProperties;
    }

    /**
    * @return The fully qualified name of the persistence provider
    * implementation class.
    * Corresponds to the &lt;provider&gt; element in the persistence.xml
    * file.
    */
    @Override
    public String getPersistenceProviderClassName(){
        return persistenceProviderClassName;
    }

    public void setPersistenceProviderClassName(String persistenceProviderClassName){
        this.persistenceProviderClassName = persistenceProviderClassName;
    }

    /**
     * Get the transaction type of the persistence unit.
     * The transaction type corresponds to the transaction-type attribute
     * in the {@code persistence.xml} file.
     *
     * @return The transaction type of the entity managers created
     *         by the EntityManagerFactory.
     * @deprecated Use {@link PersistenceUnitTransactionType}
     *             instead of {@link jakarta.persistence.spi.PersistenceUnitTransactionType}
     */
    @Override
    @Deprecated
    @SuppressWarnings("removal")
    public jakarta.persistence.spi.PersistenceUnitTransactionType getTransactionType() {
        return switch (persistenceUnitTransactionType) {
            case JTA -> jakarta.persistence.spi.PersistenceUnitTransactionType.JTA;
            case RESOURCE_LOCAL -> jakarta.persistence.spi.PersistenceUnitTransactionType.RESOURCE_LOCAL;
        };
    }

    /**
     * Specify the transaction type of the persistence unit.
     *
     * @param transactionType the transaction type of the entity managers
     *                        created by the EntityManagerFactory.
     * @deprecated Use {@link PersistenceUnitTransactionType}
     *             instead of {@link jakarta.persistence.spi.PersistenceUnitTransactionType}
     */
    @Deprecated
    @SuppressWarnings("removal")
    public void setTransactionType(jakarta.persistence.spi.PersistenceUnitTransactionType transactionType) {
        persistenceUnitTransactionType = switch (transactionType) {
            case JTA -> PersistenceUnitTransactionType.JTA;
            case RESOURCE_LOCAL -> PersistenceUnitTransactionType.RESOURCE_LOCAL;
        };
    }

    /**
     * Specify the transaction type for the persistence unit.
     *
     * @param transactionType the transaction type
     */
    public void setTransactionType(PersistenceUnitTransactionType transactionType){
        persistenceUnitTransactionType = transactionType;
    }

    /**
    * @return the JTA-enabled data source to be used by the
    * persistence provider.
    * The data source corresponds to the &lt;jta-data-source&gt;
    * element in the persistence.xml file or is provided at
    * deployment or by the container.
    */
    @Override
    public DataSource getJtaDataSource(){
        return jtaDataSource;
    }

    public void setJtaDataSource(DataSource jtaDataSource){
        this.jtaDataSource = jtaDataSource;
    }

    /**
    * @return The non-JTA-enabled data source to be used by the
    * persistence provider for accessing data outside a JTA
    * transaction.
    * The data source corresponds to the named &lt;non-jta-data-source&gt;
    * element in the persistence.xml file or provided at
    * deployment or by the container.
    */
    @Override
    public DataSource getNonJtaDataSource(){
        return nonJtaDataSource;
    }

    public void setNonJtaDataSource(DataSource nonJtaDataSource){
        this.nonJtaDataSource = nonJtaDataSource;
    }

    /**
    * @return The list of mapping file names that the persistence
    * provider must load to determine the mappings for the entity
    * classes. The mapping files must be in the standard XML
    * mapping format, be uniquely named and be resource-loadable
    * from the application classpath. This list will not include
    * the orm.xml file if one was specified.
    * Each mapping file name corresponds to a &lt;mapping-file&gt;
    * element in the persistence.xml file.
    */
    @Override
    public List<String> getMappingFileNames(){
        return mappingFiles;
    }

    public void setMappingFileNames(List<String> mappingFiles){
        this.mappingFiles = mappingFiles;
    }
    /**
    * @return The list of JAR file URLs that the persistence
    * provider must examine for managed classes of the persistence
    * unit. Each jar file URL corresponds to a named &lt;jar-file&gt;
    * element in the persistence.xml file.
    */
    @Override
    public List<URL> getJarFileUrls(){
        if (jarFileUrls == null) { // lazy initialization
            List<URL> jarFileUrls = new ArrayList<>(jarFiles.size());
            for (String jarFile : jarFiles) {
                try {
                    // build a URL relative to the PU Root
                    URL jarFileURL = new URL(persistenceUnitRootUrl, jarFile);
                    jarFileUrls.add(jarFileURL);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
            synchronized(this) {
                this.jarFileUrls = jarFileUrls;
            }
        }
        return Collections.unmodifiableList(jarFileUrls);
    }

    public void setJarFileUrls(List<URL> jarFileUrls){
        this.jarFileUrls = jarFileUrls;
    }

    /**
    * @return The URL for the jar file that is the root of the
    * persistence unit. If the persistence unit is rooted in
    * the WEB-INF/classes directory, this will be the URL of
    * that directory.
    */
    @Override
    public URL getPersistenceUnitRootUrl(){
        return persistenceUnitRootUrl;
    }

    public void setPersistenceUnitRootUrl(URL persistenceUnitRootUrl){
        this.persistenceUnitRootUrl = persistenceUnitRootUrl;
    }

    /**
     * Get programmatically defined persistence unit hash.
     *
     * @return persistence unit hash or {@code null} if this unit was not defined programmatically
     */
    String getConfigHash() {
        return configHash;
    }

    /**
    * @return The list of the names of the classes that the
    * persistence provider must add it to its set of managed
    * classes. Each name corresponds to a named &lt;class&gt; element
    * in the persistence.xml file.
    */
    @Override
    public List<String> getManagedClassNames(){
        return managedClassNames;
    }

    public void setManagedClassNames(List<String> managedClassNames){
        this.managedClassNames = managedClassNames;
    }
    /**
    * @return Whether classes in the root of the persistence
    * unit that have not been explicitly listed are to be
    * included in the set of managed classes.
    * This value corresponds to the &lt;exclude-unlisted-classes&gt;
    * element in the persistence.xml file.
    */
    @Override
    public boolean excludeUnlistedClasses(){
        return excludeUnlistedClasses;
    }

    public void setExcludeUnlistedClasses(boolean excludeUnlistedClasses){
        this.excludeUnlistedClasses = excludeUnlistedClasses;
    }
    /**
    * @return Properties object. Each property corresponds
    * to a &lt;property&gt; element in the persistence.xml file
    */
    @Override
    public Properties getProperties(){
        return properties;
    }

    public void setProperties(Properties properties){
        this.properties = properties;
    }
    /**
    * @return ClassLoader that the provider may use to load any
    * classes, resources, or open URLs.
    */
    @Override
    public ClassLoader getClassLoader(){
        return realClassLoader;
    }
    /**
    * Add a transformer supplied by the provider that will be
    * called for every new class definition or class redefinition
    * that gets loaded by the loader returned by the
    * PersistenceUnitInfo.getClassLoader method. The transformer
    * has no effect on the result returned by the
    * PersistenceUnitInfo.getNewTempClassLoader method.
    * Classes are only transformed once within the same classloading
    * scope, regardless of how many persistence units they may be
    * a part of.
    *
    * @param transformer A provider-supplied transformer that the
    * Container invokes at class-(re)definition time
    */
    @Override
    public void addTransformer(ClassTransformer transformer){
        // not required for our Java SE implementation
    }

    /**
    * Return a ClassLoader that the provider may use to temporarily
    * load any classes, resources, or open URLs. The scope and
    * classpath of this loader is exactly the same as that of the
    * loader returned by PersistenceUnitInfo.getClassLoader. None of the
    * classes loaded by this class loader will be visible to
    * application components.
    *
    * @return Temporary ClassLoader with same visibility as current
    * loader
    */
    @Override
    public ClassLoader getNewTempClassLoader(){
        return tempClassLoader;
    }

    public void setNewTempClassLoader(ClassLoader loader){
        this.tempClassLoader = loader;
    }

    /**
     * @see PersistenceUnitInfo#getSharedCacheMode()
     * @since Java Persistence 2.0
     */
    public void setSharedCacheMode(String sharedCacheMode) {
        // If user enters an invalid caching type valueOf will throw an illegal
        // argument exception, e.g.
        // java.lang.IllegalArgumentException: No enum const class jakarta.persistence.SharedCacheMode.ALLBOGUS
        this.cacheMode = SharedCacheMode.valueOf(sharedCacheMode);
    }

    /**
     * @see PersistenceUnitInfo#getValidationMode()
     * @since Java Persistence 2.0
     */
    public void setValidationMode(String validationMode) {
        // If user enters an invalid validation mode valueOf will throw an illegal
        // argument exception, e.g.
        // java.lang.IllegalArgumentException: No enum const class jakarta.persistence.ValidationMode.ALLBOGUS
        this.validationMode = ValidationMode.valueOf(validationMode);
    }

    public void setClassLoader(ClassLoader loader) {
        this.realClassLoader = loader;
    }

    public Collection<String> getJarFiles() {
        return jarFiles;
    }

    /**
     * @see PersistenceUnitInfo#getPersistenceXMLSchemaVersion()
     * @since Java Persistence 2.0
     */
    @Override
    public String getPersistenceXMLSchemaVersion() {
        // TODO
        throw new PersistenceException("Not Yet Implemented");
    }

    /**
     * @see PersistenceUnitInfo#getSharedCacheMode()
     * @since Java Persistence 2.0
     */
    @Override
    public SharedCacheMode getSharedCacheMode() {
        return cacheMode;
    }

    /**
     * @see PersistenceUnitInfo#getValidationMode()
     * @since Java Persistence 2.0
     */
    @Override
    public ValidationMode getValidationMode() {
        return validationMode;
    }
}
