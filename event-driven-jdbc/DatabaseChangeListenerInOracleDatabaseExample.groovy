@GrabConfig(systemClassLoader=true)

@Grapes(
    @Grab(group='com.oracle.database.jdbc', module='ojdbc10', version='19.7.0.0')
)
import oracle.jdbc.dcn.DatabaseChangeListener
import oracle.jdbc.dcn.DatabaseChangeEvent
import oracle.jdbc.driver.OracleConnection
import oracle.jdbc.dcn.DatabaseChangeRegistration
import oracle.jdbc.OracleStatement

import java.sql.DriverManager

import java.util.Properties

public class ExampleDatabaseChangeListener implements DatabaseChangeListener {

    @Override
    public void onDatabaseChangeNotification(DatabaseChangeEvent databaseChangeEvent) {
        println ("databaseChangeEvent: $databaseChangeEvent")       
    }
}

def connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "system", "oracle")

def databaseProperties = new Properties ()

databaseProperties.setProperty(OracleConnection.DCN_NOTIFY_ROWIDS, "true")
databaseProperties.setProperty(OracleConnection.DCN_QUERY_CHANGE_NOTIFICATION, "true")

def databaseChangeRegistration = connection.registerDatabaseChangeNotification(databaseProperties)

databaseChangeRegistration.addListener(new ExampleDatabaseChangeListener ())

def statement = connection.createStatement()

statement.setDatabaseChangeRegistration(databaseChangeRegistration)

def resultSet = statement.executeQuery("select * from example")

while (resultSet.next()) {
  println "resultSet.phrase: ${resultSet.getString('phrase')}"
}

def time = 60 * 60 * 1000

println "Will sleep for $time milliseconds..."

try {
  Thread.sleep (time)
} catch (Throwable thrown) {
  thrown.printStackTrace (System.err)
} finally {
  statement.close ()
  connection.close ()
}

println "...done!"

/*
 * docker run -d -p 1521:1521 oracleinanutshell/oracle-xe-11g
 *
 * docker exec -it 33b3d6438e27 /bin/sh
 *
 * # su 
 * root@33b3d6438e27:/# /u01/app/oracle/product/11.2.0/xe/bin/sqlplus
 *
 * SQL> select instance_name from v$instance;
 *
 * INSTANCE_NAME
 * ----------------
 * XE
 *
 * SQL> grant change notification to system;
 * 
 * CREATE TABLE example(
 *     example_id NUMBER(10) PRIMARY KEY,
 *     phrase VARCHAR2(120) NOT NULL
 * );
 *
 * SQL> insert into example values (1, 'one');
 * SQL> insert into example values (2, 'two');
 * SQL> insert into example values (3, 'three');
 * SQL> insert into example values (4, 'four');
 * SQL> insert into example values (5, 'five');
 * SQL> commit;
 *
 * See also:
 *
 * https://docs.oracle.com/cd/B19306_01/B14251_01/adfns_dcn.htm
 * https://docs.oracle.com/en/database/oracle/oracle-database/12.2/jajdb/oracle/jdbc/dcn/class-use/DatabaseChangeRegistration.html
 * https://docs.oracle.com/cd/E18283_01/appdev.112/e13995/oracle/jdbc/OracleStatement.html#setDatabaseChangeRegistration_oracle_jdbc_dcn_DatabaseChangeRegistration_
 */