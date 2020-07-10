@GrabConfig(systemClassLoader=true)

@Grapes(
  @Grab(group="com.h2database", module="h2", version="1.4.200")
)
import org.h2.api.DatabaseEventListener
import java.sql.SQLException
import java.sql.DriverManager

public class ExampleDatabaseEventListener implements DatabaseEventListener {

  public void closingDatabase () {
    println "closingDatabase: method invoked."
  }

  public void exceptionThrown (SQLException sqle, String sql) {
    println "exceptionThrown: method invoked; sqle: $sqle, sql: $sql"
  }

  public void init (String url) {
    println "init: method invoked; url: $url"
  }

  public void opened () {
    println "opened: method invoked."
  }

  public void setProgress (int state, String name, int x, int max) {
    println "setProgress: method invoked; state: $state, name: $name, x: $x, max: $max"
  }
}

def cxn = DriverManager.getConnection("jdbc:h2:mem:EventListenerInH2DatabaseExampleDB;DB_CLOSE_DELAY=-1;DATABASE_EVENT_LISTENER='ExampleDatabaseEventListener';")
def stm = cxn.createStatement()
def resultSet = stm.executeQuery("SELECT 1+1")

if (resultSet.next()) {
  println("next: ${resultSet.getInt(1)}")
}

cxn.close ()

println "...Done!"
