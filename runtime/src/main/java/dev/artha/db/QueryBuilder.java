package dev.artha.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.*;
import java.util.*;

/**
 * Fluent Query Builder for safe database operations.
 * All queries use prepared statements to prevent SQL injection.
 */
public class QueryBuilder {
    private final Connection connection;
    private String table;
    private String type = "SELECT"; // SELECT, INSERT, UPDATE, DELETE
    private List<String> selectColumns = new ArrayList<>();
    private List<WhereClause> whereClauses = new ArrayList<>();
    private String orderByColumn;
    private String orderByDirection = "ASC";
    private Integer limitValue;
    private Integer offsetValue;
    private Map<String, Object> insertData;
    private Map<String, Object> updateData;

    private static class WhereClause {
        String column;
        String operator;
        Object value;

        WhereClause(String column, String operator, Object value) {
            this.column = column;
            this.operator = operator;
            this.value = value;
        }
    }

    public QueryBuilder(Connection connection, String table) {
        this.connection = connection;
        this.table = table;
    }

    /**
     * Start a SELECT query
     */
    public QueryBuilder select(String... columns) {
        this.type = "SELECT";
        if (columns.length > 0) {
            this.selectColumns = Arrays.asList(columns);
        }
        return this;
    }

    /**
     * Add WHERE clause
     */
    public QueryBuilder where(String column, String operator, Object value) {
        whereClauses.add(new WhereClause(column, operator, value));
        return this;
    }

    /**
     * Add WHERE clause with = operator
     */
    public QueryBuilder where(String column, Object value) {
        return where(column, "=", value);
    }

    /**
     * Add ORDER BY
     */
    public QueryBuilder orderBy(String column, String direction) {
        this.orderByColumn = column;
        this.orderByDirection = direction.toUpperCase();
        return this;
    }

    /**
     * Add ORDER BY ASC
     */
    public QueryBuilder orderBy(String column) {
        return orderBy(column, "ASC");
    }

    /**
     * Add LIMIT
     */
    public QueryBuilder limit(int limit) {
        this.limitValue = limit;
        return this;
    }

    /**
     * Add OFFSET
     */
    public QueryBuilder offset(int offset) {
        this.offsetValue = offset;
        return this;
    }

    /**
     * Execute SELECT and return list of results
     */
    public <T> List<T> get(Class<T> clazz) throws SQLException {
        String sql = buildSelectSQL();
        List<T> results = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setWhereParameters(stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                ObjectMapper mapper = new ObjectMapper();
                while (rs.next()) {
                    Map<String, Object> row = resultSetToMap(rs);
                    T obj = mapper.convertValue(row, clazz);
                    results.add(obj);
                }
            }
        }

        return results;
    }

    /**
     * Execute SELECT and return first result
     */
    public <T> T first(Class<T> clazz) throws SQLException {
        this.limitValue = 1;
        List<T> results = get(clazz);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Find a single record by ID
     */
    public <T> T find(Object id, Class<T> clazz) throws SQLException {
        return where("id", id).first(clazz);
    }

    /**
     * Save an object (Insert or Update)
     * Assumes "id" field determines if it's new or existing.
     */
    public int save(Object entity) throws SQLException {
        ObjectMapper mapper = new ObjectMapper();
        // Convert object to map
        Map<String, Object> map = mapper.convertValue(entity,
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                });

        Object id = map.get("id");

        // Remove nulls if necessary? Usually insert/update handles them.

        if (id != null && (id instanceof Number && ((Number) id).longValue() > 0)) {
            return where("id", id).update(map);
        } else {
            // Remove null/empty id to let DB auto-increment
            if (map.containsKey("id") && (map.get("id") == null
                    || (map.get("id") instanceof Number && ((Number) map.get("id")).longValue() == 0))) {
                map.remove("id");
            }
            return insert(map);
        }
    }

    /**
     * Execute SELECT and return list of maps
     */
    public List<Map<String, Object>> get() throws SQLException {
        String sql = buildSelectSQL();
        List<Map<String, Object>> results = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setWhereParameters(stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(resultSetToMap(rs));
                }
            }
        }

        return results;
    }

    /**
     * INSERT data
     */
    public int insert(Map<String, Object> data) throws SQLException {
        this.type = "INSERT";
        this.insertData = data;

        String sql = buildInsertSQL();
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int index = 1;
            for (Object value : data.values()) {
                stmt.setObject(index++, value);
            }

            stmt.executeUpdate();

            // Return generated ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return -1;
    }

    /**
     * UPDATE data
     */
    public int update(Map<String, Object> data) throws SQLException {
        this.type = "UPDATE";
        this.updateData = data;

        String sql = buildUpdateSQL();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int index = 1;

            // Set update values
            for (Object value : data.values()) {
                stmt.setObject(index++, value);
            }

            // Set WHERE values
            for (WhereClause where : whereClauses) {
                stmt.setObject(index++, where.value);
            }

            return stmt.executeUpdate();
        }
    }

    /**
     * DELETE records
     */
    public int delete() throws SQLException {
        this.type = "DELETE";

        String sql = buildDeleteSQL();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setWhereParameters(stmt);
            return stmt.executeUpdate();
        }
    }

    // SQL Building Methods

    private String buildSelectSQL() {
        StringBuilder sql = new StringBuilder("SELECT ");

        if (selectColumns.isEmpty()) {
            sql.append("*");
        } else {
            sql.append(String.join(", ", selectColumns));
        }

        sql.append(" FROM ").append(table);

        appendWhereClauses(sql);

        if (orderByColumn != null) {
            sql.append(" ORDER BY ").append(orderByColumn).append(" ").append(orderByDirection);
        }

        if (limitValue != null) {
            sql.append(" LIMIT ").append(limitValue);
        }

        if (offsetValue != null) {
            sql.append(" OFFSET ").append(offsetValue);
        }

        return sql.toString();
    }

    private String buildInsertSQL() {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(table).append(" (");

        String columns = String.join(", ", insertData.keySet());
        String placeholders = String.join(", ", Collections.nCopies(insertData.size(), "?"));

        sql.append(columns).append(") VALUES (").append(placeholders).append(")");

        return sql.toString();
    }

    private String buildUpdateSQL() {
        StringBuilder sql = new StringBuilder("UPDATE ").append(table).append(" SET ");

        List<String> setParts = new ArrayList<>();
        for (String column : updateData.keySet()) {
            setParts.add(column + " = ?");
        }
        sql.append(String.join(", ", setParts));

        appendWhereClauses(sql);

        return sql.toString();
    }

    private String buildDeleteSQL() {
        StringBuilder sql = new StringBuilder("DELETE FROM ").append(table);
        appendWhereClauses(sql);
        return sql.toString();
    }

    private void appendWhereClauses(StringBuilder sql) {
        if (!whereClauses.isEmpty()) {
            sql.append(" WHERE ");
            List<String> whereParts = new ArrayList<>();
            for (WhereClause where : whereClauses) {
                whereParts.add(where.column + " " + where.operator + " ?");
            }
            sql.append(String.join(" AND ", whereParts));
        }
    }

    private void setWhereParameters(PreparedStatement stmt) throws SQLException {
        int index = 1;
        for (WhereClause where : whereClauses) {
            stmt.setObject(index++, where.value);
        }
    }

    private Map<String, Object> resultSetToMap(ResultSet rs) throws SQLException {
        Map<String, Object> row = new HashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            Object value = rs.getObject(i);
            row.put(columnName, value);
        }

        return row;
    }
}
