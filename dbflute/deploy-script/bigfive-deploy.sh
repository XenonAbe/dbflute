cd ..
ant -f build.xml reflect-to-basic
ant -f build.xml reflect-to-spring
ant -f build.xml reflect-to-guice
ant -f build.xml reflect-to-mysql
ant -f build.xml reflect-to-postgresql

cd ../dbflute-basic-example/dbflute_exampledb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh

cd ../../dbflute-spring-example/dbflute_exampledb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh

cd ../../dbflute-guice-example/dbflute_exampledb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh

cd ../../dbflute-mysql-example/dbflute_exampledb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh
. outside-sql-test.sh
. clientbhv-generate.sh
. clientbhv-sql2entity.sh
. clientbhv-outside-sql-test.sh

cd ../../dbflute-postgresql-example/dbflute_exampledb
rm ./log/*.log
. jdbc.sh
. doc.sh
. generate.sh
. sql2entity.sh
. outside-sql-test.sh
. clientbhv-generate.sh
. clientbhv-sql2entity.sh
. clientbhv-outside-sql-test.sh
