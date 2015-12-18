# Axway Changes
The Axway mirror of Apache/openjpa has a handful of changes.

# Summary of Axway Changes
1. Axway found and fixed a number of issues in the 2.1.1 release. These chanages are in the 2.1.1-AXWAY branch
2. Some work has been done to add NuoDB support. The source changes are in the nuodb branch.  These changes have been merged into the branch 2.1.x-nuodb which is based on the 2.1.x branch.  These changed have been merged into the branch 2.1.1-axway-nuodb 
3. The Axway changes in 2.1.1-AXWAY have been reconciled with 2.4.0. Any changes Axway made in 2.1.1 that where not addressed in 2.4.0 have been merged into 2.4.0-AXWAY

# Branch Structure
```
                                             -------- 2.1.1-AXWAY-fixtests
                                            /
                *---------2.1.1-AXWAY------*
               /                     2.1.1-AXWAY-3
              /
             /            ---------2.1.x-nuodb
            /            /
       ----*------------ 2.1.x                    
      /  2.1.1                                   
     /          2.2.0             2.3.0          2.4.0
-----------------*-----------------*---------------*----- trunk
                  \                 \               \    \
                   --- 2.2.x         --- 2.3.x       \    -----nuodb
                                                      \
                                                       ---- 2.4.0-AXWAY
```

# Branch Details

## 2.1.1
### Branch 2.1.1-AXWAY
The branch 2.1.1-AXWAY contains Axway fixes to version 2.1.1.

### Branch 2.1.1-AXWAY-fixtests
The changes to ApplicationIds.create()in the 2.1.1-AXWAY branch was modified to treat 0 as a special ID.
This broke a number of unit and integration tests.
This branch is intended to be constantly rebased from the 2.1.1-AXWAY branch.

Branch 2.1.1-AXWAY-fixtests was created to modify failing tests in order to document which tests failed.  Some tests where modified to use a non-zero ID which should keep the test valid.  Other tests were simply commented out. These tests should be researched and the code fixed.  However the code related to this caching and creating IDs has been changed in subsequent versions. Perhaps, if Interchange can move to a newer version, the issue has been solved in a different manner w/o introducing other problems.

All modifications have been commented with:
> FIXME Axway - ApplicationIds.create: avoid using 0 as an ID work around bug introduced in ApplicationIds.create


## Tag 2.4.0
OpenJPA release 2.4.0

### 2.4.0-AWAY 
2.1.1-AXWAY was rebased onto 2.4.0

#### How the rebase was done
1. Create branch 2.4.0-AXWAY:  "git branch 2.4.0-AXWAY 2.1.1-AXWAY"
2. Perform the rebase and resolve conflicts: "git rebase --onto 2.4.0 2.4.0 2.4.0-AXWAY"
2.1 When resolving conflicts compared result to 2.1.1-AXWAY, 2.4.0, and trunk
2.2 After rebase, compare 2.4.0 to 2.4.0-AXWAY to verify nothing was lost from 2.4.0
2.3 After rebase, compare 2.1.1-AXWAY to 2.4.0-AXWAY to verify changes exist in 2.4.0-AXWAY
2.4 Identify all changed files in 2.1.1-AXWAY and verify contents of of post rebase
to verify everthing was addressed.

Result is 2.4.0-AXWAY starts with tag 2.4.0 and applies the changes we made on
2.1.1-AXWAY since it branched from tag 2.1.1

#### List of Java files changed by Axway in 2.1.1-AXWAY
- openjpa-jdbc/src/main/java/org/apache/openjpa/jdbc/kernel/PreparedQueryImpl.java
- openjpa-jdbc/src/main/java/org/apache/openjpa/jdbc/kernel/exps/CompareEqualExpression.java
- openjpa-jdbc/src/main/java/org/apache/openjpa/jdbc/kernel/exps/CompareExpression.java
- openjpa-jdbc/src/main/java/org/apache/openjpa/jdbc/kernel/exps/ContainsExpression.java
- openjpa-jdbc/src/main/java/org/apache/openjpa/jdbc/kernel/exps/PCPath.java
- openjpa-kernel/src/main/java/org/apache/openjpa/datacache/AbstractDataCache.java
- openjpa-kernel/src/main/java/org/apache/openjpa/datacache/DataCacheManagerImpl.java
- openjpa-kernel/src/main/java/org/apache/openjpa/datacache/DataCacheStoreManager.java
- openjpa-kernel/src/main/java/org/apache/openjpa/enhance/AsmAdaptor.java
- openjpa-kernel/src/main/java/org/apache/openjpa/enhance/PCEnhancer.java
- openjpa-kernel/src/main/java/org/apache/openjpa/meta/MetaDataRepository.java
- openjpa-kernel/src/main/java/org/apache/openjpa/util/ApplicationIds.java
- openjpa-persistence/src/main/java/org/apache/openjpa/persistence/PersistenceProductDerivation.java
- openjpa-slice/src/main/java/org/apache/openjpa/slice/jdbc/DistributedDataSource.java

#### Reconcilation of Axway changes in 2.1.1-AXWAY vs 2.4.0-Axway
- AbstractDataCache.java - Change not in trunk. Keeping the Axway change. 
Comment from the code: 
> todo - report this bug to JPA.  Deletes were coming first and then updates were putting them back in the cache.  (Bob K) 

- ApplicationIds.java - How the cache is handle has changed.  No longer need this modication.  
See changes to DataCacheStoreManager.java
NOTE: would be best if we could construct a test case to demonstrate this in 2.1.1 and verify that the problem doesn't exist 2.4.x
In the abscence of this we will need to have some focused application testing

- AsmAdapter.java - trunk has changes

- DataCacheManagerImpl.java -  Trunk contains changes  (OPENJPA-2470)

- DataCacheStoreManager.java - Trunk has changes that address why we changed
ApplicationIds.java.  

- MetaDataRepostitory.java - Trunk doesn't have have change our change, but it has numerous other changes. Keeping our change. Unclear if it is still needed w/o further
analysis. Comment from the code:
> Todo - report to JPA.  It doesn't seem like annotations can be used if pre-load is set to true. I added this if condition for _factory.load.  Notice JPA's comment that this might not be necessary.  Bob K.

- PCEnhancer.java - Looks like trunk incorporates changes.  Trunk as numerous other enhancements and changes

- PersistentProductDerivation.java - Changes have have been incorporated into trunk.  
Trunk has additional changes as well

- CompareEqualExpression.java - Change NOT in trunk.   Keeping change.  
See CompareExpression.java.where we made similar change which is in trunk.
This needs to get submitted to OpenJPA

- CompareExpression.java - Change is in trunk.  
See CompareEqualExpression.java.  Why was this change made in trunk but not in CompareEqualExpression.java? Just an oversight?

- PreparedQueryImpl - Change is NOT in the trunk.  Keeping the change. 
A synchronization issue. Is this a sign that Interchange using a class that is not intended to be multi-thread safe across threads?

- ContainsExpress.java - Change is NOT in the trunk.  NOTE: Our change looks suspect. We've changed the semantics of the class call sql.setContainsId(contains);  instead of sql.setContainsId(count.toString());   The notes from the Axway change indicate a race condition. Is this class intended to be multi-thread safe? Is Integrator utilizing this class in multiple thread manner when the class is not intended to be be multi-thread safe?  See PCPath.java

- PCPath.java - Change is NOT in the trunk.  See ContainsExpression.java

- DistributedDataSource.java - Trunk has a more correct implementation of getParentLogger() and other minor changes.

## NuoDB work
### Branch nuodb
This branch is based of the trunk and is a work in progress to enable OpenJPA to work with NuoDB. The intent is to keep this branch fresh by rebase from trunk 

### Branch 2.1.x-nuodb
Nuodb changes from nuodb applied to the HEAD of the 2.1.x branch.
The intent is to keep this branch fresh be rebasing from 2.1.x.

### Branch 2.1.1-axway-nuodb
Nuodb changes merged from 2.1.x-nuodb.
Minor changes to work with the Axway changes introduced on the 2.1.1-AXWAY branch.


