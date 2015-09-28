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
    import org.apache.openjpa.jdbc.schema.Column;
    import org.apache.openjpa.jdbc.schema.ForeignKey;
    import org.apache.openjpa.jdbc.sql.DBDictionary;
    import org.apache.openjpa.jdbc.sql.SQLBuffer;
    import org.apache.openjpa.kernel.Seq;
    import org.apache.openjpa.lib.identifier.IdentifierRule;
    import org.apache.openjpa.lib.log.Log;

    import java.sql.*;
    import java.util.*;

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
//        public boolean supportsForeignKeys = true;
//        public boolean supportsParameterInSelect = true;
//        public boolean supportsForeignKeysComposite = true;
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
//        public boolean supportsComments = false;
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
        validationSQL = "SELECT NOW()";
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
//        public String dropTableSQL = "DROP TABLE {0}";
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
        bitTypeName = "SMALLINT";
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
//        public String autoAssignClause = null;
//        public String autoAssignTypeName = null;
//        public boolean supportsAutoAssign = false;
//        public String lastGeneratedKeyQuery = null;
//        public String nextSequenceQuery = null;
//        public String sequenceSQL = null;
//        public String sequenceSchemaSQL = null;
//        public String sequenceNameSQL = null;
//        // most native sequences can be run inside the business transaction
//        public int nativeSequenceType= Seq.TYPE_CONTIGUOUS;
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
                "CHARACTER", "CLOB","CHARACTER LARGE OBJECT","TEXT",
                "BLOB", "BINARY", "VARBINARY"));

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

    @Override
    protected void appendSelectRange(SQLBuffer buf, long start, long end,
                                     boolean subselect) {

        if (end == 0)
            end = Long.MAX_VALUE;
        buf.append(" LIMIT ").append(String.valueOf(start));
        buf.append(", ").append(String.valueOf(end - start));
    }

    @Override
    public int getPreferredType(int type)
    {
        int result;

        switch (type) {
            case Types.BIT:
            case Types.TINYINT:
                result = Types.SMALLINT;
                break;
            case Types.FLOAT:
                result = Types.DOUBLE;
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
    /**
     * Invoke this database's indexOf function.
     *
     * @param buf the SQL buffer to write the indexOf invocation to
     * @param str a query value representing the target string
     * @param find a query value representing the search string
     * @param start a query value representing the start index, or null
     * to start at the beginning
     */
    @Override
    public void indexOf(SQLBuffer buf, FilterValue str, FilterValue find,
                        FilterValue start) {

        buf.append("(POSITION(");   // (POSITION(
        find.appendTo(buf);         // (POSITION(find
        buf.append(" IN ");         // (POSITION(find IN
        if (start != null)
            substring(buf, str, start, null);
        else
            str.appendTo(buf);      // (POSITION(find in str
        buf.append(")");            // (POSITION(find in str)
        if (start != null) {
            buf.append(" - 1  + "); // (POSITION(find in str) - 1 +
            start.appendTo(buf);    // (POSITION(find in str) - 1 + start
        }
        buf.append(")");            // (POSITION(find in str) - 1 + start)
    }

    /**
     * Return a series of SQL statements to drop the given foreign key from
     * its table. Return an empty array if operation not supported.
     * Returns <code>ALTER TABLE &lt;table name&gt; DROP CONSTRAINT
     * &lt;fk name&gt;</code> by default.
     */
    public String[] getDropForeignKeySQL(ForeignKey fk, Connection conn) {
        if (DBIdentifier.isNull(fk.getIdentifier())) {
            String[] retVal;
            DBIdentifier fkName = fk.loadIdentifierFromDB(this,conn);
            retVal = (fkName == null || fkName.getName() == null) ?  new String[0] :
                    new String[]{ "ALTER TABLE "
                            + getFullName(fk.getTable(), false)
                            + " DROP FOREIGN KEY " + toDBName(fkName) };
            return retVal;
        }
        return new String[]{ "ALTER TABLE "
                + getFullName(fk.getTable(), false)
                + " DROP FOREIGN KEY " + toDBName(fk.getIdentifier()) };
    }
}
