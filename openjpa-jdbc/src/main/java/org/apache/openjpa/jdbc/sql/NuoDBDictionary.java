package org.apache.openjpa.jdbc.sql;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

    import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
    import org.apache.openjpa.jdbc.identifier.DBIdentifier;
    import org.apache.openjpa.jdbc.identifier.DBIdentifierUtil;
    import org.apache.openjpa.jdbc.kernel.exps.FilterValue;
    import org.apache.openjpa.jdbc.schema.Table;
    import org.apache.openjpa.jdbc.schema.Column;
    import org.apache.openjpa.jdbc.schema.Sequence;
    import org.apache.openjpa.jdbc.schema.ForeignKey;
    import org.apache.openjpa.jdbc.schema.PrimaryKey;
    import org.apache.openjpa.jdbc.sql.DBDictionary;
    import org.apache.openjpa.jdbc.sql.SQLBuffer;
    import org.apache.openjpa.kernel.Seq;
    import org.apache.openjpa.lib.identifier.IdentifierRule;
    import org.apache.openjpa.lib.log.Log;
    import org.apache.openjpa.lib.jdbc.ReportingSQLException;
    import org.apache.openjpa.util.StoreException;
    import org.apache.openjpa.util.OpenJPAException;

    import java.sql.*;
    import java.util.*;
    import java.text.MessageFormat;

public class NuoDBDictionary extends DBDictionary
{
    public static final String DELIMITER_BACK_TICK = "`";

    public NuoDBDictionary()
    {
        // schema data
        platform = "NuoDB";
//        public String databaseProductName = "";
//        public String databaseProductVersion = "";
//        public String driverVendor = null;
//        public boolean createPrimaryKeys = true;
//        public String constraintNameMode = CONS_NAME_BEFORE;
//        public int maxTableNameLength = 128;
//        public int maxColumnNameLength = 128;
//        public int maxConstraintNameLength = 128;
//        public int maxIndexNameLength = 128;
//        public int maxIndexesPerTable = Integer.MAX_VALUE;
        supportsAnyAllSome = false;
//        public boolean supportsForeignKeys = true;
        supportsForeignKeys = /*true;*/ false; // support DDL but not DML
//        public boolean supportsParameterInSelect = true;
//        public boolean supportsForeignKeysComposite = true;
        supportsForeignKeysComposite = /*true*/ false;
//        public boolean supportsUniqueConstraints = true;
        supportsDeferredConstraints = false;
//        public boolean supportsRestrictDeleteAction = true;
//        public boolean supportsCascadeDeleteAction = true;
        supportsNullDeleteAction = false;
//        public boolean supportsNullUniqueColumn = true;
//        public boolean supportsDefaultDeleteAction = true;
//        public boolean supportsRestrictUpdateAction = true;
//        public boolean supportsCascadeUpdateAction = true;
//        public boolean supportsNullUpdateAction = true;
//        public boolean supportsDefaultUpdateAction = true;
//        public boolean supportsAlterTableWithAddColumn = true;
//        public boolean supportsAlterTableWithDropColumn = true;
        supportsAlterTableWithDropColumn = false;
//        public boolean supportsComments = false;
        supportsComments = true;
//        public Boolean supportsGetGeneratedKeys = null;
//        public String reservedWords = null;
//        public String systemSchemas = null;
//        public String systemTables = null;
//        public String selectWords = null;
//        public String fixedSizeTypeNames = null;
//        public String schemaCase = SCHEMA_CASE_UPPER;
//        public boolean setStringRightTruncationOn = true;
//        public boolean fullResultCollectionInOrderByRelation = false;
//
//        // sql
//        public boolean disableAlterSeqenceIncrementBy=false;
        validationSQL = "SELECT NOW() FROM DUAL";
//        public String closePoolSQL = null;
//        public String initializationSQL = null;
//        public int joinSyntax = SYNTAX_SQL92;

//        public String outerJoinClause = "LEFT OUTER JOIN";
//        public String innerJoinClause = "INNER JOIN";
        crossJoinClause = "JOIN";
        requiresConditionForCrossJoin = true;
//        public String forUpdateClause = "FOR UPDATE";
//        public String tableForUpdateClause = null;
//        public String distinctCountColumnSeparator = null;
//        public boolean supportsSelectForUpdate = true;
//        public boolean supportsLockingWithDistinctClause = true;
//        public boolean supportsLockingWithMultipleTables = true;
//        public boolean supportsLockingWithOrderClause = true;
//        public boolean supportsLockingWithOuterJoin = true;
//        public boolean supportsLockingWithInnerJoin = true;
//        public boolean supportsLockingWithSelectRange = true;
//        public boolean supportsQueryTimeout = true;
//        public boolean allowQueryTimeoutOnFindUpdate = false; //OPENJPA-2517
//        public boolean simulateLocking = false;
//        public boolean supportsSubselect = true;
//        public boolean supportsCorrelatedSubselect = true;
//        public boolean supportsHaving = true;
        supportsSelectStartIndex = false;
        supportsSelectEndIndex = false;
//        public int rangePosition = RANGE_POST_SELECT;
        requiresAliasForSubselect = true;
//        public boolean requiresTargetForDelete = false;
//        public boolean allowsAliasInBulkClause = true;
//        public boolean supportsMultipleNontransactionalResultSets = true;
//        public boolean requiresSearchStringEscapeForLike = false;
//        public String searchStringEscape = "\\";
//        public boolean requiresCastForMathFunctions = false;
//        public boolean requiresCastForComparisons = false;
//        public boolean supportsModOperator = false;
//        public boolean supportsXMLColumn = false;
//        public boolean supportsCaseConversionForLob = false;
//        public boolean reportsSuccessNoInfoOnBatchUpdates = false;
//        public boolean supportsSelectFromFinalTable = false;
//        public boolean supportsSimpleCaseExpression = true;
//        public boolean supportsGeneralCaseExpression = true;
//        public boolean useWildCardForCount = false;
        useWildCardForCount = true;
//
//        /**
//         * Some Databases append whitespace after the schema name
//         */
//        public boolean trimSchemaName = false;
//
//        // functions
//        public String castFunction = "CAST({0} AS {1})";
//        public String toLowerCaseFunction = "LOWER({0})";
//        public String toUpperCaseFunction = "UPPER({0})";
//        public String stringLengthFunction = "CHAR_LENGTH({0})";
//        public String bitLengthFunction = "(OCTET_LENGTH({0}) * 8)";
//        public String trimLeadingFunction = "TRIM(LEADING {1} FROM {0})";
//        public String trimTrailingFunction = "TRIM(TRAILING {1} FROM {0})";
//        public String trimBothFunction = "TRIM(BOTH {1} FROM {0})";
//        public String concatenateFunction = "({0}||{1})";
//        public String concatenateDelimiter = "'OPENJPATOKEN'";
//        public String substringFunctionName = "SUBSTRING";
//        public String currentDateFunction = "CURRENT_DATE";
//        public String currentTimeFunction = "CURRENT_TIME";
//        public String currentTimestampFunction = "CURRENT_TIMESTAMP";
        dropTableSQL = "DROP TABLE {0} IF EXISTS CASCADE";
//
//        // types
//        public boolean storageLimitationsFatal = false;
//        public boolean storeLargeNumbersAsStrings = false;
//        public boolean storeCharsAsNumbers = true;
//        public boolean trimStringColumns = false;
//        public boolean useGetBytesForBlobs = false;
//        public boolean useSetBytesForBlobs = false;
//        public boolean useGetObjectForBlobs = false;
//        public boolean useGetStringForClobs = false;
//        public boolean useSetStringForClobs = false;
//        public boolean useJDBC4SetBinaryStream = true;//OPENJPA-2067
//        public int maxEmbeddedBlobSize = -1;
//        public int maxEmbeddedClobSize = -1;
//        public int inClauseLimit = -1;
//        public int datePrecision = MILLI;
//
//        /**
//         * @deprecated Use 'dateMillisecondBehavior' instead.
//         */
//        @Deprecated
//        public boolean roundTimeToMillisec = true;
//
//    /*
//     * This defines how the milliseconds of a Date field are handled
//     * when the Date is retrieved from the database, as follows:
//     *
//     * ROUND: This is the default.  The
//     * Date will be rounded to the nearest millisecond.
//     * DROP: The milliseconds will be dropped, thus rounding is not
//     * performed.  As an example, a date of '2010-01-01 12:00:00.687701'
//     * stored in the database will become '2010-01-01 12:00:00.000' in
//     * the Date field of the entity.
//     * RETAIN: The milliseconds will not be rounded and retained.  As an
//     * example, a date of '2010-01-01 12:00:00.687701' stored in the
//     * database will become '2010-01-01 12:00:00.687' in the Date field
//     * of the entity.
//     */
//        public enum DateMillisecondBehaviors { DROP, ROUND, RETAIN };
//        private DateMillisecondBehaviors dateMillisecondBehavior;
//
//        /**
//         * Defines how {@code Boolean} and {@code boolean} values get represented
//         * in OpenJPA. Default to {@code INT_10}.
//         * for backward compatibility.
//         */
//        protected BooleanRepresentation booleanRepresentation = BooleanRepresentationFactory.INT_10;
//
//        public int characterColumnSize = 255;
//        public String arrayTypeName = "ARRAY";
//        public String bigintTypeName = "BIGINT";
//        public String binaryTypeName = "BINARY";
        bitTypeName = "BOOLEAN";
//        public String blobTypeName = "BLOB";
//        public String booleanTypeName = "BOOLEAN";
        charTypeName = "CHAR";
//        public String clobTypeName = "CLOB";
//        public String dateTypeName = "DATE";
//        public String decimalTypeName = "DECIMAL";
//        public String distinctTypeName = "DISTINCT";
//        public String doubleTypeName = "DOUBLE";
        floatTypeName = "DOUBLE";
//        public String integerTypeName = "INTEGER";
//        public String javaObjectTypeName = "JAVA_OBJECT";
        longVarbinaryTypeName = "VARBINARY";
        longVarcharTypeName = "VARCHAR";
//        public String nullTypeName = "NULL";
//        public String numericTypeName = "NUMERIC";
//        public String otherTypeName = "OTHER";
//        public String realTypeName = "REAL";
//        public String refTypeName = "REF";
//        public String smallintTypeName = "SMALLINT";
//        public String structTypeName = "STRUCT";
//        public String timeTypeName = "TIME";
//        public String timestampTypeName = "TIMESTAMP";
        tinyintTypeName = "SMALLINT";
//        public String varbinaryTypeName = "VARBINARY";
        //varcharTypeName = "STRING"; // String will not limit size of varchar
        varcharTypeName = "VARCHAR";
        xmlTypeName = "CLOB";
//        public String xmlTypeEncoding = "UTF-8";
//        public String getStringVal = "";
//
//        // schema metadata
        useSchemaName = true;
//        public String tableTypes = "TABLE";
//        public boolean supportsSchemaForGetTables = true;
//        public boolean supportsSchemaForGetColumns = true;
//        public boolean supportsNullTableForGetColumns = true;
//        public boolean supportsNullTableForGetPrimaryKeys = false;
//        public boolean supportsNullTableForGetIndexInfo = false;
//        public boolean supportsNullTableForGetImportedKeys = false;
//        public boolean useGetBestRowIdentifierForPrimaryKeys = false;
//        public boolean requiresAutoCommitForMetaData = false;
//        public boolean tableLengthIncludesSchema = false;
//
//        // auto-increment
//        public int maxAutoAssignNameLength = 31;
        autoAssignClause = "GENERATED BY DEFAULT AS IDENTITY";
        autoAssignTypeName = "BIGINT";
        supportsAutoAssign = true;
        lastGeneratedKeyQuery =   "SELECT LAST_INSERT_ID() FROM DUAL";
        nextSequenceQuery = "SELECT NEXT VALUE FOR {0} FROM DUAL";
        sequenceSQL = "SELECT SCHEMA AS SEQUENCE_SCHEMA ,SEQUENCENAME AS SEQUENCE_NAME FROM SYSTEM.SEQUENCES";
        sequenceSchemaSQL = "SCHEMA = ?";
        sequenceNameSQL = "SEQUENCENAME = ?";
//        // most native sequences can be run inside the business transaction
//        public int nativeSequenceType= Seq.TYPE_CONTIGUOUS;
        nativeSequenceType= Seq.TYPE_DEFAULT;
//
//        /**
//         * This variable was used in 2.1.x and prior releases to indicate that
//         * OpenJPA should not use the CACHE clause when getting a native
//         * sequence; instead the INCREMENT BY clause gets its value equal to the
//         * allocationSize property.  Post 2.1.x, code was added to allow
//         * said functionality by default (see OPENJPA-1376).  For forward
//         * compatibility, this variable should not be removed.
//         */
//        @Deprecated
//        public boolean useNativeSequenceCache = true;
//
//        /**
//         * If a user sets the previous variable (useNativeSequenceCache) to false, we should log a
//         * warning indicating that the variable no longer has an effect due to the code changes
//         * of OPENJPA-1376.  We only want to log the warning once per instance, thus this
//         * variable will be used to indicate if the warning should be printed or not.
//         */
//        @Deprecated
//        private boolean logNativeSequenceCacheWarning = true;
//
//        protected JDBCConfiguration conf = null;
//        protected Log log = null;
//        protected boolean connected = false;
//        protected boolean isJDBC3 = false;
        isJDBC4 = true;
//        protected final Set<String> reservedWordSet = new HashSet<String>();
//        // reservedWordSet subset that CANNOT be used as valid column names
//        // (i.e., without surrounding them with double-quotes)
//        protected Set<String> invalidColumnWordSet = new HashSet<String>();
//        protected final Set<String> systemSchemaSet = new HashSet<String>();
//        protected final Set<String> systemTableSet = new HashSet<String>();
//        protected final Set<String> fixedSizeTypeNameSet = new HashSet<String>();
//        protected final Set<String> typeModifierSet = new HashSet<String>();
//
//        // NamingConfiguration properties
//        private boolean delimitIdentifiers = false;
        supportsDelimitedIdentifiers = true;
        leadingDelimiter = DELIMITER_BACK_TICK;
        trailingDelimiter = DELIMITER_BACK_TICK;
//        public String nameConcatenator = "_";
//        public String delimitedCase = SCHEMA_CASE_PRESERVE;
//        public String catalogSeparator = ".";
//        protected String defaultSchemaName = null;
//        private String conversionKey = null;
//
//        // Naming utility and naming rules
//        private DBIdentifierUtil namingUtil = null;
//        private Map<String, IdentifierRule> namingRules = new HashMap<String, IdentifierRule>();
//        private IdentifierRule defaultNamingRule = null;  // cached for performance
//
//        /**
//         * If a native query begins with any of the values found here then it will
//         * be treated as a select statement.
//         */
//        protected final Set<String> selectWordSet = new HashSet<String>();
//
//        // when we store values that lose precision, track the types so that the
//        // first time it happens we can warn the user
//        private Set<Class<?>> _precisionWarnedTypes = null;
//
//        // batchLimit value:
//        // -1 = unlimited
//        // 0  = no batch
//        // any positive number = batch limit
//        public int batchLimit = NO_BATCH;

        platform = "NuoDB";
        requiresSearchStringEscapeForLike = false;
        useSetBytesForBlobs = true;
        useGetBytesForBlobs = true;
        useSetStringForClobs = true;
        useGetStringForClobs = true;

        supportsSelectStartIndex = true;
        supportsSelectEndIndex = true;

        setLeadingDelimiter(DELIMITER_BACK_TICK);
        setTrailingDelimiter(DELIMITER_BACK_TICK);

        fixedSizeTypeNameSet.addAll(Arrays.asList(
                "CHARACTER", "CLOB","CHARACTER LARGE OBJECT","TEXT","STRING",
                "BLOB", "BINARY", "VARBINARY"));
        fixedSizeTypeNameSet.remove("NUMERIC");

        reservedWordSet.addAll(Arrays.asList("ALL", "AS", "BETWEEN", "BITS", "BOTH", "BREAK",
                "BY", "CALL", "CASCADE", "CASE", "CATCH", "COLLATE", "COLUMN", "CONSTRAINT",
                "CONTAINING", "CONTINUE", "CREATE", "CURRENT", "CURRENT_DATE", "CURRENT_TIME",
                "CURRENT_TIMESTAMP", "DEFAULT", "DELETE", "DESCRIBE", "DISTINCT", "ELSE", "END",
                "END_FOR", "END_FUNCTION", "END_IF", "END_PROCEDURE", "END_TRIGGER", "END_TRY",
                "END_WHILE", "ENUM", "ESCAPE", "EXECUTE", "EXISTS", "FALSE", "FETCH", "FOR",
                "FOREIGN", "FOR_UPDATE", "FROM", "FULL", "GENERATED", "GROUP", "HAVING",
                "IDENTITY", "IF", "IN", "INNER", "INOUT", "INSERT", "INTO", "IS", "JOIN", "KEY",
                "LEADING", "LEFT", "LIKE", "LIMIT", "LOGICAL_AND", "LOGICAL_NOT", "LOGICAL_OR",
                "MAX", "MAXVALUE", "MIN", "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEXT",
                "NEXT_VALUE", "NOT_BETWEEN", "NOT_CONTAINING", "NOT_IN", "NOT_LIKE",
                "NOT_STARTING", "NTEXT", "NULL", "NUMERIC", "NVARCHAR", "OCTETS", "OFF",
                "OFFSET", "ON", "ONLY", "ORDER", "OUT", "PRIMARY", "REAL", "RECORD_BATCHING",
                "REFERENCES", "REGEXP", "RESTART", "RESTRICT", "RETURN", "RIGHT", "ROLLBACK",
                "ROWS", "SELECT", "SET", "SHOW", "SMALLDATETIME", "SMALLINT", "STARTING", "STRING",
                "STRING_TYPE", "THEN", "THROW", "TINYBLOB", "TINYINT", "TO", "TRAILING",
                "TRUE", "TRY", "UNION", "UNIQUE", "UNKNOWN", "UPDATE", "USING", "VAR", "VER",
                "WHEN", "WHERE", "WHILE", "WITH"));
        // reservedWordSet subset that CANNOT be used as valid column names
        // (i.e., without surrounding them with double-quotes)
        invalidColumnWordSet.addAll(Arrays.asList("ALL", "AS", "BETWEEN", "BITS", "BOTH", "BREAK",
                "BY", "CALL", "CASCADE", "CASE", "CATCH", "COLLATE", "COLUMN", "CONSTRAINT",
                "CONTAINING", "CONTINUE", "CREATE", "CURRENT", "CURRENT_DATE", "CURRENT_TIME",
                "CURRENT_TIMESTAMP", "DEFAULT", "DELETE", "DESCRIBE", "DISTINCT", "ELSE", "END",
                "END_FOR", "END_FUNCTION", "END_IF", "END_PROCEDURE", "END_TRIGGER", "END_TRY",
                "END_WHILE", "ENUM", "ESCAPE", "EXECUTE", "EXISTS", "FALSE", "FETCH", "FOR",
                "FOREIGN", "FOR_UPDATE", "FROM", "FULL", "GENERATED", "GROUP", "HAVING",
                "IDENTITY", "IF", "IN", "INNER", "INOUT", "INSERT", "INTO", "IS", "JOIN", "KEY",
                "LEADING", "LEFT", "LIKE", "LIMIT", "LOGICAL_AND", "LOGICAL_NOT", "LOGICAL_OR",
                "MAX", "MAXVALUE", "MIN", "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEXT",
                "NEXT_VALUE", "NOT_BETWEEN", "NOT_CONTAINING", "NOT_IN", "NOT_LIKE",
                "NOT_STARTING", "NTEXT", "NULL", "NUMERIC", "NVARCHAR", "OCTETS", "OFF",
                "OFFSET", "ON", "ONLY", "ORDER", "OUT", "PRIMARY", "REAL", "RECORD_BATCHING",
                "REFERENCES", "REGEXP", "RESTART", "RESTRICT", "RETURN", "RIGHT", "ROLLBACK",
                "ROWS", "SE-j-jLECT", "SET", "SHOW", "SMALLDATETIME", "SMALLINT", "STARTING",
                "STRING", "STRING_TYPE", "THEN", "THROW", "TINYBLOB", "TINYINT", "TO", "TRAILING",
                "TRUE", "TRY", "UNION", "UNIQUE", "UNKNOWN", "UPDATE", "USING", "VAR", "VER",
                "WHEN", "WHERE", "WHILE", "WITH"));

        systemSchemaSet.addAll(Arrays.asList("SYSTEM"));

    }

    /*
        @Override
    public void setByte(PreparedStatement stmnt, int idx, byte val, Column col)
            throws SQLException
   {
        stmnt.setInt(idx, (int)val);
    }

    @Override
    public byte getByte(ResultSet rs, int column) throws SQLException
    {
        return (byte)rs.getInt(column);
    }
    */

    @Override
    protected void appendSelectRange(SQLBuffer buf, long start, long end,
                                     boolean subselect) {
        long len = end-start;
        if (len <= 0) {
            len = Long.MAX_VALUE;
        }
        buf.append(String.format(" LIMIT %d ",len));
        if (start != 0) {
            buf.append(String.format("OFFSET %d ",start));
        }
    }


    // From migrator doc:
    //  Bit, newSize(0)             BOOLEAN
    //  Bit, newSize(1)             BOOLEAN
    //  Integer                     INTEGER
    //  BigInt                      BIGINT
    //  Numeric                     NUMERIC( P , S )
    //  Decimal                     DECIMAL( P , S )
    //  Real                        REAL
    //  Float                       FLOAT
    //  Double                      DOUBLE
    //  Char                        CHAR( N )
    //  Varchar                     VARCHAR( N )
    //  LongVarchar                 VARCHAR( N )
    //  Date                        DATE
    //  Time, newScale(0)           TIME
    //  Time                        TIME( S )
    //  TimeStamp, newScale(0)      TIMESTAMP
    //  TimeStamp                   TIMESTAMP( S )
    //  Binary                      BINARY( N )
    //  VarBinary                   VARBINARY( N )
    //  LongVarBinary               VARBINARY( N )
    //  Null                        NULL
    //  NCLOB                       NCLOB
    //  VarChar                     STRING
    //  SmallInt                    INTEGER
    //  SmallInt Unsigned           INTEGER
    //  Int Unsigned                BIGINT
    //  BigInt Unsigned             NUMERIC( N , 1 )
    @Override
    public int getPreferredType(int type)
    {
        int result;

        switch (type) {
            case Types.BIT:
                result = Types.BOOLEAN;
                break;
            case Types.TINYINT:
                result = Types.SMALLINT;
                break;
            case Types.SMALLINT:
                result = Types.INTEGER;
                break;
            case Types.LONGNVARCHAR:
                result = Types.VARCHAR;
                break;
            default:
                // No need to change type
                result = type;
        }

        return type;
    }

    // LOCATE(<string>,<instring>,<startpos>)
    @Override
    public void indexOf(SQLBuffer buf, FilterValue str, FilterValue find,
        FilterValue start) {
        buf.append("LOCATE(");
        find.appendTo(buf);
        buf.append(", ");
        str.appendTo(buf);
        if (start != null) {
            buf.append(", ");
            start.appendTo(buf);
        }
        buf.append(")");
    }

    // ALTER TABLE <tablename> DROP FOREIGN KEY (<col1>,...) REFRENCES <tablename>
    @Override
    public String[] getDropForeignKeySQL(ForeignKey fk, Connection conn) {

        StringBuilder dropTable = new StringBuilder();
        dropTable.append("ALTER TABLE ");
        dropTable.append(getFullName(fk.getTable(), false));
        dropTable.append(" DROP FOREIGN KEY (");
        boolean first = true;
        for (Column col : fk.getColumns()) {
            if (!first) {
                dropTable.append(",");
            }
            first = false;
            dropTable.append(col.getIdentifier().getName());
        }
        dropTable.append(") REFERENCES ");
        dropTable.append(fk.getPrimaryKeyTableIdentifier().getName());
        return new String[] { dropTable.toString() };
    }

    // At moment we don't support FK - so have not implemented
    // This function is not called because supportsForeignKeys = false
    @Override
    public String[] getAddForeignKeySQL(ForeignKey fk)
    {
        log.warn("getAddForeignKeySQL(ForeignKey fk) not implemented");
        return new String[0];
    }

    @Override
    public String[] getDropPrimaryKeySQL(PrimaryKey pk) {
        if (DBIdentifier.isNull(pk.getIdentifier()))
            return new String[0];
        String _pk = "DROP INDEX \"" + getFullName(pk.getTable(), false) + "\"";
        return new String[] { _pk };
    }

    @Override
    public String[] getDropSequenceSQL(Sequence seq) {
        // NOT seeing getDropSequenceSQL being called...
        log.warn("DAB;  getDropSequenceSQL(Sequence seq): ");
        String [] dropStmt = super.getDropSequenceSQL(seq);
        for (String stmt : dropStmt) {
            log.warn("   " + stmt);
        }
        return dropStmt;
    }

    @Override
    public String[] getCreateSequenceSQL(Sequence seq) {
        // NuoDB does not support INCREMENT BY
        seq.setIncrement(0);
        seq.setAllocate(0);
        return super.getCreateSequenceSQL(seq);
    }

    @Override
    public String getAlterSequenceSQL(Sequence seq) {
        seq.setIncrement(0);
        seq.setAllocate(0);
        return super.getAlterSequenceSQL(seq);
    }

    @Override
    protected String getSequencesSQL(String schemaName, String sequenceName) {
        return getSequencesSQL(DBIdentifier.newSchema(schemaName),
                DBIdentifier.newSequence(sequenceName));
    }

    @Override
    protected String getSequencesSQL(DBIdentifier schemaName, DBIdentifier sequenceName) {
        StringBuilder buf = new StringBuilder();
        buf.append(sequenceSQL);
        if (!DBIdentifier.isNull(schemaName) || !DBIdentifier.isNull(sequenceName))
            buf.append(" WHERE ");
        if (!DBIdentifier.isNull(schemaName)) {
            buf.append(sequenceSchemaSQL);
            if (!DBIdentifier.isNull(sequenceName))
                buf.append(" AND ");
        }
        if (!DBIdentifier.isNull(sequenceName))
            buf.append(sequenceNameSQL);
        return buf.toString();
    }
    @Override
    public String getTypeName(Column col) {
        String typeName=super.getTypeName(col);
        if ("NUMERIC".equals(typeName)) {
            typeName="NUMBER";
        }
        return typeName;
    }

    @Override
    public boolean isFatalException(int subtype, SQLException ex) {
        if (subtype == StoreException.LOCK  && ex.getErrorCode() == -48) {
            return false;
        }
        if (ex.getErrorCode() == 0 && ex.getSQLState() == null)
            return false;
        return super.isFatalException(subtype, ex);
    }

    @Override
    protected int matchErrorState(Map<Integer,Set<String>> errorStates, SQLException ex) {
        int state = super.matchErrorState(errorStates, ex);
        if ("HY008".equals(ex.getSQLState())) {
            if (conf != null && conf.getLockTimeout() != -1) {
                state = StoreException.LOCK;
            } else {
                state = StoreException.QUERY;
            }
        }
        return state;
    }


}
