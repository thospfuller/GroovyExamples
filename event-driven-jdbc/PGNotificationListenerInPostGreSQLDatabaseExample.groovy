// https://mvnrepository.com/artifact/com.impossibl.pgjdbc-ng/pgjdbc-ng
@Grapes(
    @Grab(group='com.impossibl.pgjdbc-ng', module='pgjdbc-ng', version='0.8.4')
)
import com.impossibl.postgres.api.jdbc.PGConnection
import com.impossibl.postgres.api.jdbc.PGNotificationListener
import com.impossibl.postgres.jdbc.PGDataSource

PGDataSource dataSource = new PGDataSource();
dataSource.setHost("0.0.0.0")
dataSource.setPort(5432)
dataSource.setDatabaseName("testdb")
dataSource.setUser("postgres")
dataSource.setPassword("password")

final def pgNotificationListener = new PGNotificationListener () { 

  @Override
  public void notification(int processId, String channelName, String payload) {
    println("processId $processId, channelName: $channelName, payload: $payload")
  }
}

final def connection = (PGConnection) dataSource.getConnection()

connection.addNotificationListener(pgNotificationListener)

final def statement = connection.createStatement()

statement.execute("LISTEN examplechannel")
statement.close()

def time = 60 * 60 * 1000

println "Will sleep for $time milliseconds..."

try {
  Thread.sleep (time)
} catch (Throwable thrown) {
  thrown.printStackTrace (System.err)
} finally {
  connection.close ()
}

print "...done!"

/*
 * The -p 5432:5432 maps localhost:5432 to the [PostgreSQL container ip]:5432
 * docker run -p 5432:5432 --name cl-postgres -e POSTGRES_PASSWORD=password -d postgres
 *
 * docker exec -it 816f2432df70 /bin/sh
 *
 * https://hub.docker.com/_/postgres
 *
 * From [1] "A key limitation of the JDBC driver is that it cannot receive asynchronous notifications and must poll the backend to check if any notifications were issued."
 * 
 * [1] https://jdbc.postgresql.org/documentation/94/listennotify.html
 * 
 * So an alternative is to use [2]
 *
 * [2] https://impossibl.github.io/pgjdbc-ng/
 *
 * https://stackoverflow.com/questions/21632243/how-do-i-get-asynchronous-event-driven-listen-notify-support-in-java-using-a-p
 *
 * root@cb9222b1f718:/# psql -U postgres
 *
 * postgres=# create database testdb;
 *
 * postgres=# \connect testdb;
 *
 * create table example (phrase text);
 *
 * Note: pg_notify takes the channelName, payload and also note that according to https://www.postgresql.org/docs/9.0/sql-notify.html
 *       "There is no NOTIFY statement in the SQL standard".
 *
CREATE OR REPLACE FUNCTION notify_change() RETURNS TRIGGER AS $$
    BEGIN
        --
        -- WARNING: Case is VERY IMPORTANT here! If we use 'exampleChannel' PG converts this to
        --          examplechannel and no events will be received!!
        --
        --  UPDATE: [to be confirmed] Case can be handled in PostgreSQL by using double quotes.
        --
        --          In theory, if you had the following line as the listener, it would work in camelCase.
        --
        --          statement.execute('LISTEN "exampleChannel"');
        --
        --          The same applies to any identifier in Postgres.
        --
        PERFORM pg_notify('examplechannel', NEW.phrase);
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;
 *
create trigger table_change
  AFTER INSERT OR UPDATE OR DELETE ON example
  FOR EACH ROW EXECUTE PROCEDURE notify_change();
 *
 *
 * insert into example (phrase) values ('hello world');
 *
 * 
 *
 * [3] https://medium.com/better-programming/connect-from-local-machine-to-postgresql-docker-container-f785f00461a7
 *
 * docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' 816f2432df70
 *
 * [4] https://wiki.postgresql.org/wiki/PgNotificationHelper
 */
