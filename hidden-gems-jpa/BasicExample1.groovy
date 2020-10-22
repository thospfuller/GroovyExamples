@GrabConfig(systemClassLoader=true)

/* If you see this:
 *
 * Caught: java.lang.NoClassDefFoundError: Unable to load class org.apache.groovy.jaxb.extensions.JaxbExtensions due to
 *         missing dependency javax/xml/bind/JAXBContext
 *
 * Then for Intellij IDEA:
 *   File -> Project Structure -> Dependencies -> + -> (add the JAXB dependencies in the groovy lib directory)
 */

@Grapes(
        @Grab(group="com.h2database", module="h2", version="1.4.200")
)
import java.sql.SQLException
import java.sql.DriverManager

def configuration = '''
'''

def cxn = DriverManager.getConnection("jdbc:h2:mem:ExampleDB;DB_CLOSE_DELAY=-1;")
def stm = cxn.createStatement()
def resultSet = stm.executeQuery("SELECT 1+1")

if (resultSet.next()) {
    println("next: ${resultSet.getInt(1)}")
}

cxn.close ()

println "...Done!"
