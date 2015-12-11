Apache OpenJPA - README.txt
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

Thank you for downloading this release of Apache OpenJPA.

The following files can be found in the openjpa-project subdirectory:
    BUILDING.txt
    CHANGES.txt
    RELEASE-NOTES.html

For documentation and project information, please visit our project site:
    http://openjpa.apache.org/


Informaiton for the NuoDB branch
1. You will likely need to install the NuoDB jdbc driver into your local repository. 
2. Install NuoDB
3. Code is writtent assuming a database named 'openpja2' with a schema 'test'.  Username of 'dba' and password of 'dba'


Maven command to build:
mvn clean install -Dcheckstyle.skip=true -Ptest-nuodb -Dopenjpa.nuodb.username=dba -Dopenjpa.nuodb.password=dba -Dnuodb.version=3.0.0 -Dopenjpa.nuodb.url=jdbc:com.nuodb://192.168.149.134:48004/openjpa2?schema=test\&isolation=read_committed -DfailIfNoTests=false 2>&1 | tee ../build.log

-Dcheckstyle.skip=true   I didn't add in copyright notices etc
-Ptest-nuodb   Profile to config for using NuoDB
-Dnuodb.version=2.4.0  The version of NuoDB you are using.  This should match the version of the JDBC driver you installed in your local maven repository
-Dopenjpa.nuodb.url   Modify to point to your host


