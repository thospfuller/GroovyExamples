@GrabConfig(systemClassLoader = true)
@Grab(group="com.h2database", module="h2", version="1.4.200")
@Grab(group='org.hibernate', module='hibernate-core', version='5.4.22.Final')
@Grab(group='org.hibernate', module='hibernate-entitymanager', version='5.4.22.Final')
@Grab(group='org.hibernate', module='hibernate-jpamodelgen', version='5.4.22.Final', scope='provided')
@Grab(group='javax.persistence', module='javax.persistence-api', version='2.2')
@Grab(group='com.atomikos', module='transactions-jdbc', version='5.0.8')
@Grab(group='org.slf4j', module='slf4j-api', version='1.7.30')
@Grab(group='org.slf4j', module='slf4j-simple', version='1.7.30')
import org.hibernate.jpa.HibernatePersistenceProvider
import javax.persistence.spi.ClassTransformer
import javax.persistence.SharedCacheMode
import javax.sql.DataSource
import javax.persistence.spi.PersistenceUnitInfo
import javax.persistence.spi.PersistenceUnitTransactionType
import javax.persistence.ValidationMode

import javax.persistence.Table
import javax.persistence.Column
import javax.persistence.Entity

import com.atomikos.jdbc.AtomikosDataSourceBean

//import java.sql.DriverManager
//
//def cxn = DriverManager.getConnection("jdbc:h2:mem:ExampleDB;DB_CLOSE_DELAY=-1;")

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
 *
 * See also: https://stackoverflow.com/questions/52131659/how-to-enable-xa-for-springboot-h2-datasource
 *
 * https://gist.github.com/rafaeltuelho/fb7fc0d372a0cf85a53e
 */

@Entity
@Table(name="PERSON")
class Person {
    @Column(name="NAME")
    private String name
}

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

        /*
         * https://javadoc.io/doc/com.atomikos/transactions-jdbc/latest/com/atomikos/jdbc/AtomikosDataSourceBean.html
         */
        AtomikosDataSourceBean result = new AtomikosDataSourceBean ()

        def properties = new Properties ()

        properties.setProperty ( "user" , "sa" )
        properties.setProperty ( "password" , "password" )
        properties.setProperty ( "URL" , "jdbc:h2:mem:ExampleDB;DB_CLOSE_DELAY=-1;" )

        result.setXaProperties ( properties )

        result.setUniqueResourceName ("exampleJTADataSource")
        result.setXaDataSourceClassName ("org.h2.jdbcx.JdbcDataSource")
        result.setPoolSize (100)

        return result
    }

    @Override
    public DataSource getNonJtaDataSource() {

        return null
//        AtomikosDataSourceBean result = new AtomikosDataSourceBean ()
//
//        result.setUniqueResourceName ("exampleNonJTADataSource")
//        result.setXaDataSourceClassName ("org.h2.DataSource")
//        result.set
//        result.set
//        result.set
//
//        JdbcDataSource ds = new JdbcDataSource();
//        ds.setURL("jdbc:h2:˜/test");
//        ds.setUser("sa");
//
//        return result
    }

    @Override
    public List<String> getMappingFileNames() {
        return Collections.emptyList()
    }

    @Override
    public List<URL> getJarFileUrls() {
//        try {
//            return Collections.list(getClass().getClassLoader().getResources(""))
//        } catch (IOException ioException) {
//            throw new UncheckedIOException(ioException)
//        }
        return null
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        return null
    }

    @Override
    public List<String> getManagedClassNames() {
        return null//(List<String>) Arrays.asList(Person.class.getName())
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
    DIALECT : "org.hibernate.dialect.H2Dialect",
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

//cxn.close ()

println "...Done!"
