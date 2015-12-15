# Axway Changes
See the end of this text for a picture of the branch structure

## Branch 2.1.1-AXWAY
The branch 2.1.1-AXWAY contains Axway fixes to version 2.1.1.

## Branch 2.1.1-AXWAY-fixtests
The changes to ApplicationIds.create()in the 2.1.1-AXWAY branch was modified to
treat 0 as a special ID.
This broke a number of unit and integration tests.
This branch is intended to be constantly rebased from the 2.1.1-AXWAY branch.

Branch 2.1.1-AXWAY-fixtests was created to modify failing tests in order to 
document which tests failed.  Some tests where modified to use a non-zero
ID which should keep the test valid.  Other tests where simply commented
out. These tests should be researched and the code fixed.  However the code
related to this caching and creating IDs has been changed in subsequent versions.
Perhaps, if Interchange can move to a newer version, the issue has been solved
in a different manner w/o introducing other problems.

All modifications have been commented with:
> FIXME Axway - ApplicationIds.create: avoid using 0 as an ID work around bug introduced in ApplicationIds.create

## 2.1.1-AXWAY vs 2.4.0 / trunk
This section will review all the Axway changes to determine if the fix has found
its way into the most recent version of OpenJPA (Currently 2.4.0)
### 5c9448e - INT - Modifications to run in Interchange (rkrier 1 year, 6 months ago)
AbstractDataCache.java - 

## Branch nuodb
This branch is based of the trunk and is a work in progress to enable OpenJPA to
work with NuoDB. The intent is to keep this branch fresh by rebase from trunk 

## Branch 2.1.x-nuodb
Nuodb changes from nuodb applied to the HEAD of the 2.1.x branch.
The intent is to keep this branch fresh be rebasing from 2.1.x.

## Branch 2.1.1-axway-nuodb
Nuodb changes merged from 2.1.x-nuodb.
Minor changes to work with the Axway changes introduced on the 2.1.1-AXWAY branch.


Branch Structure
```

                                                -------- 2.1.1-AXWAY-fixtests
                                              /
               *---------2.1.1-AXWAY-------*
               /                     2.1.1-AXWAY-3
              /
             /            /---------2.1.x-nuodb
            /            /
      ----*------------- 2.1.x                    
      /  2.1.1                                   
     /          2.2.0             2.3.0          2.4.0
-----------------*-----------------*---------------*----- trunk
                  \                 \               \    \
                   \-- 2.2.x         \-- 2.3.x       \    \----nuodb
                                                      \
                                                       \--- 2.4.0-AXWAY
```
