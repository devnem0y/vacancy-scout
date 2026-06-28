package com.devnem0y.vacancy_scout.debug;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.sql.SQLException;

@RestController
@RequestMapping("/debug")
public class SchemaDebugController {

    private final JdbcTemplate jdbcTemplate;

    public SchemaDebugController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/schema")
    public String printSchema() throws SQLException {
        ResultSet tables = jdbcTemplate.getDataSource().getConnection()
                .getMetaData().getTables(null, "public", null, new String[]{"TABLE"});

        StringBuilder sb = new StringBuilder();
        sb.append("TABLES:\n");
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            sb.append("- ").append(tableName).append("\n");

            ResultSet columns = jdbcTemplate.getDataSource().getConnection()
                    .getMetaData().getColumns(null, "public", tableName, null);
            sb.append("  COLUMNS:\n");
            while (columns.next()) {
                String colName = columns.getString("COLUMN_NAME");
                String colType = columns.getString("DATA_TYPE") + " (" + columns.getString("TYPE_NAME") + ")";
                sb.append("    - ").append(colName).append(": ").append(colType).append("\n");
            }
        }

        String result = sb.toString();
        System.out.println("=== DEBUG SCHEMA ===");
        System.out.println(result);
        System.out.println("====================");

        return "Schema printed to logs. Check Runtime Logs in Amvera.";
    }
}
