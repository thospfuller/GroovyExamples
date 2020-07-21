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
        println ("***** databaseChangeEvent: $databaseChangeEvent")       
    }
}

final def connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "system", "oracle")

def databaseProperties = new Properties ()

/*
 * [6] When the notification type is OCN, any DML transaction that changes one or more registered objects generates
 *     one notification for each object when it commits.
 *
 *     When the notification type is QRCN, any DML transaction that changes the result of one or more registered
 *     queries generates a notification when it commits. The notification includes the query IDs of the queries whose
 *     results changed.
 *
 *     For either notification type, the notification includes:
 *
 *     Name of each changed table
 *
 *     Operation type (INSERT, UPDATE, or DELETE)
 *
 *     ROWID of each changed row, if the registration was created with the ROWID option and the number of modified rows
 *     was not too large. For more information, see ROWID Option."
 */
databaseProperties.setProperty(OracleConnection.DCN_NOTIFY_ROWIDS, "true")
databaseProperties.setProperty(OracleConnection.DCN_QUERY_CHANGE_NOTIFICATION, "true")

final def databaseChangeRegistration = connection.registerDatabaseChangeNotification(databaseProperties)

databaseChangeRegistration.addListener(new ExampleDatabaseChangeListener ())

final def statement = connection.createStatement()

statement.setDatabaseChangeRegistration(databaseChangeRegistration)

//new Thread ({

  def ctr = 0

  while (true) {

    def resultSet = statement.executeQuery("select * from example where example_id = 1")

    while (resultSet.next()) {
      println "[@ $ctr}] resultSet.phrase: ${resultSet.getString('phrase')}"
    }

    Thread.sleep (5 * 1000)

    ctr++
  }
//}).start ()

final def time = 60 * 60 * 1000

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
 * Run the script and then execute the following in sqlplus:
 *
insert into example values (1, 'one');
insert into example values (2, 'two');
insert into example values (3, 'three');
insert into example values (4, 'four');
insert into example values (5, 'five');
commit;

update example set phrase = 'one / 1' where example_id = 1;
 *
 * See also:
 *
 * Oracle Continuous Query Notification Example
 *
 * [1] https://docs.oracle.com/cd/B19306_01/B14251_01/adfns_dcn.htm
 * [2] https://docs.oracle.com/en/database/oracle/oracle-database/12.2/jajdb/oracle/jdbc/dcn/class-use/DatabaseChangeRegistration.html
 * [3] https://docs.oracle.com/cd/E18283_01/appdev.112/e13995/oracle/jdbc/OracleStatement.html#setDatabaseChangeRegistration_oracle_jdbc_dcn_DatabaseChangeRegistration_
 * [4] https://docs.oracle.com/en/database/oracle/oracle-database/12.2/jjdbc/continuos-query-notification.html#GUID-6CA108CC-658D-447D-8B8A-ABBBF975871FsetDatabaseChangeRegistration
 * [5] https://docs.oracle.com/en/database/oracle/oracle-database/12.2/jjdbc/continuos-query-notification.html#GUID-17D0D7C5-77C9-420D-9D13-F668C1056792
 * - Object Change Notification (OCN)
 * - Query Result Change Notification (QRCN)
 *
 * [6] https://docs.oracle.com/cd/B28359_01/appdev.111/b28424/adfns_cqn.htm#CHDHIADC
 * - Events that Generate Notifications
 *
 * [7] https://docs.oracle.com/cd/E18283_01/appdev.112/e13995/oracle/jdbc/OracleConnection.html && DCN_NOTIFY_ROWIDS
 */