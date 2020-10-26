@Grab(group="com.h2database", module="h2", version="1.4.200")
@Grab(group='org.hibernate', module='hibernate-core', version='5.4.22.Final')
@Grab(group='org.hibernate', module='hibernate-entitymanager', version='5.4.22.Final')
@Grab(group='javax.persistence', module='javax.persistence-api', version='2.2')
@Grab(group='org.hibernate', module='hibernate-jpamodelgen', version='5.4.22.Final', scope='provided')
import org.hibernate.jpa.HibernatePersistenceProvider
import javax.persistence.spi.ClassTransformer
import javax.persistence.SharedCacheMode
import javax.sql.DataSource
import javax.persistence.spi.PersistenceUnitInfo
import javax.persistence.spi.PersistenceUnitTransactionType
import javax.persistence.ValidationMode
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl
import org.hibernate.dialect.H2Dialect
import java.sql.DriverManager

import static org.hibernate.cfg.AvailableSettings.*

/* If you see this:
 *
 * Caught: java.lang.NoClassDefFoundError: Unable to load class org.apache.groovy.jaxb.extensions.JaxbExtensions due to
 *         missing dependency javax/xml/bind/JAXBContext
 *
 * Then for Intellij IDEA:
 *   File -> Project Structure -> Dependencies -> + -> (add the JAXB dependencies in the groovy lib directory)
 */

/* https://docs.jboss.org/hibernate/orm/5.2/javadocs/index.html
 * https://stackoverflow.com/questions/1989672/create-jpa-entitymanager-without-persistence-xml-configuration-file
 */

class ExamplePersistenceUnitInfo implements PersistenceUnitInfo {

    @Override
    public String getPersistenceUnitName() {
        return "ApplicationPersistenceUnit"
    }

    @Override
    public String getPersistenceProviderClassName() {
        return "org.hibernate.jpa.HibernatePersistenceProvider"
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return PersistenceUnitTransactionType.RESOURCE_LOCAL
    }

    @Override
    public DataSource getJtaDataSource() {
        return null
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return null
    }

    @Override
    public List<String> getMappingFileNames() {
        return Collections.emptyList()
    }

    @Override
    public List<URL> getJarFileUrls() {
        try {
            return Collections.list(this.getClass()
                    .getClassLoader()
                    .getResources(""))
        } catch (IOException e) {
            throw new UncheckedIOException(e)
        }
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        return null
    }

    @Override
    public List<String> getManagedClassNames() {
        return Collections.emptyList()
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return false
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return null
    }

    @Override
    public ValidationMode getValidationMode() {
        return null
    }

    @Override
    public Properties getProperties() {
        return new Properties()
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        return null
    }

    @Override
    public ClassLoader getClassLoader() {
        return null
    }

    @Override
    public void addTransformer(ClassTransformer transformer) {

    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        return null
    }
}

def properties = [
    JPA_JDBC_DRIVER : "org.h2.Driver",
    JPA_JDBC_URL : "jdbc:h2:mem:ExampleDB;DB_CLOSE_DELAY=-1;",
    DIALECT : H2Dialect.class,
    HBM2DDL_AUTO : "create",
    SHOW_SQL : true,
    QUERY_STARTUP_CHECKING : true,
    GENERATE_STATISTICS : true,
    USE_REFLECTION_OPTIMIZER : true,
    USE_SECOND_LEVEL_CACHE : false,
    USE_QUERY_CACHE : false,
    USE_STRUCTURED_CACHE : false,
    STATEMENT_BATCH_SIZE : 20
] as Map<String, Object>

def entityManagerFactory = new HibernatePersistenceProvider().createContainerEntityManagerFactory(
    new ExamplePersistenceUnitInfo (),
    properties
)

def entityManager = entityManagerFactory.createEntityManager()

//def cxn = DriverManager.getConnection("jdbc:h2:mem:ExampleDB;DB_CLOSE_DELAY=-1;")

def entityManagerFactoryBuilder = new EntityManagerFactoryBuilderImpl ()

//@Entity
//@Table(name="PERSON")
//class Person {
//    @Column(name="NAME")
//    private String name
//}


//cxn.close ()

println "...Done!"
