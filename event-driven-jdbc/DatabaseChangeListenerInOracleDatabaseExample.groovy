@GrabConfig(systemClassLoader=true)

//
// https://docs.oracle.com/cd/E11882_01/appdev.112/e13995/index.html?oracle/jdbc/dcn/DatabaseChangeRegistration.html
//
// https://mvnrepository.com/artifact/com.oracle.database.jdbc/ojdbc6
@Grapes(
    @Grab(group='com.oracle.database.jdbc', module='ojdbc6', version='11.2.0.4')
)
import oracle.jdbc.dcn.DatabaseChangeListener
import oracle.jdbc.dcn.DatabaseChangeEvent
import oracle.jdbc.driver.OracleConnection
import oracle.jdbc.dcn.DatabaseChangeRegistration
import oracle.jdbc.OracleStatement

import java.sql.DriverManager

import java.util.Properties

//
// Note that the thin driver supports this example.
//
//
// SEE THE WARNING BELOW ABOUT RUNNING THIS SCRIPT ON LOCALHOST WITH ORACLE DB IN DOCKER, ALSO ON LOCALHOST.
//
final def connection = DriverManager.getConnection("jdbc:oracle:thin:@192.168.1.232:1521:xe", "system", "oracle")

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

public class ExampleDatabaseChangeListener implements DatabaseChangeListener {

    @Override
    public void onDatabaseChangeNotification(DatabaseChangeEvent databaseChangeEvent) {
        println ("***** databaseChangeEvent: $databaseChangeEvent")
        println ("***** databaseChangeEvent.source: ${databaseChangeEvent.source}")
        println ("***** databaseChangeEvent.queryChangeDescription: ${databaseChangeEvent.queryChangeDescription}")
        println ("***** databaseChangeEvent.tableChangeDescription: ${databaseChangeEvent.tableChangeDescription.each {println '\n  - nextTableChangeDescription: $it' } }")
    }
}

databaseChangeRegistration.addListener(new ExampleDatabaseChangeListener ())

final def statement = connection.createStatement()

statement.setDatabaseChangeRegistration(databaseChangeRegistration)

try {

  resultSet = statement.executeQuery("select * from example")

  while (resultSet.next())
    {} // println "resultSet.phrase: ${resultSet.getString('phrase')}"

} catch (Throwable thrown) {
  thrown.printStackTrace (System.err)
} finally {
  //resultSet?.close ()
}

println "databaseChangeRegistration.userName: ${databaseChangeRegistration.userName}"

databaseChangeRegistration.tables.each {
    println "tables: $it"
}

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

/* WARNING: I'm copy-pasting the below message because this is very important when running Oracle in Docker and then
 *          running this script on localhost. This caused me a few hours of time trying to figure out why the
 *          notification wasn't being received and ONLY APPLIES IF YOU'RE RUNNING DOCKER ON THE SAME MACHINE AS THIS
 *          SCRIPT IS BEING EXECUTED ON! In fact, I'm not bothering with this at the moment and am running Docker with
 *          Oracle on another machine entirely.
 *
 *          Note also that I've not been able to get this running ON THE SAME MACHINE using:
 *
 *          docker run -d -p 1521:1521 -p [47632:47632] oracleinanutshell/oracle-xe-11g
 *
 * FROM:
 *
 * https://stackoverflow.com/questions/26003506/databasechangeregistration-in-remote-server
 *
 * "You can check active listeners in the Oracle database running the following query:
 *
 * Select * FROM USER_CHANGE_NOTIFICATION_REGS
 * I the query does not return any rows probably the database server can't access the jdbc driver listener port.
 *
 * By default the Oracle JDBC driver listens at port 47632 for notifications. You will need to ensure that it is possible to connect to that port from the database server. You may need to add a rule in the firewall to accept incoming requests to that port.
 *
 * This port can be changed with the NTF_LOCAL_TCP_PORT option:
 *
 * prop.setProperty(OracleConnection.NTF_LOCAL_TCP_PORT, "15000");"
 *
 * -----
 *
 * NOT WORKING, SEE THE NOTES ABOVE:
 *
 *   docker run -d -p 1521:1521 -p 47632:47632 oracleinanutshell/oracle-xe-11g
 *
 * This works but must be launched on another machine:
 *
 *   docker run -d -p 1521:1521 oracleinanutshell/oracle-xe-11g
 *
 * docker exec -it 33b3d6438e27 /bin/sh
 *
 sqlplus sys as SYSDBA
 
 * # su 
 * root@33b3d6438e27:/# /u01/app/oracle/product/11.2.0/xe/bin/sqlplus
 *
 * SQL> Select * FROM USER_CHANGE_NOTIFICATION_REGS
 *
 * SQL> select instance_name from v$instance;
 *
 * INSTANCE_NAME
 * ----------------
 * XE
 *
 * SQL> select version from v$instance;
 *
 * VERSION
 * -----------------
 * 11.2.0.2.0
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

update example set phrase = 'one / 111111111' where example_id = 1;
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
 *
 * [8] ORA-29970: Specified registration id does not exist, https://support.oracle.com/knowledge/Oracle%20Database%20Products/971412_1.html
 *     DBMS_CQ_NOTIFICATION.DEREGISTER();
 *
 * [9] https://docs.oracle.com/cd/E11882_01/appdev.112/e13995/oracle/jdbc/OracleDriver.html
 *
 */