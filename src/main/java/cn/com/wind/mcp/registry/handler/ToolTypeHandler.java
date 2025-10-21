package cn.com.wind.mcp.registry.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ToolType字段类型处理器
 * 负责在数据库CHAR(1)值和业务逻辑String值之间转换
 * <p>
 * 数据库存储: '1', '2'
 * 业务逻辑: "1" (tool), "2" (agent)
 *
 * @author system
 * @date 2025-10-17
 */
@MappedTypes(String.class)
@MappedJdbcTypes(JdbcType.CHAR)
public class ToolTypeHandler extends BaseTypeHandler<String> {

    /**
     * 设置非空参数：将业务值转换为数据库值
     *
     * @param ps        PreparedStatement
     * @param i         参数索引
     * @param parameter 业务值 ("1", "2", "tool", "agent")
     * @param jdbcType  JDBC类型
     * @throws SQLException SQL异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        // 业务值 → 数据库值
        String dbValue = convertToDbValue(parameter);
        ps.setString(i, dbValue);
    }

    /**
     * 获取可空结果：将数据库值转换为业务值
     *
     * @param rs         ResultSet
     * @param columnName 列名
     * @return 业务值 ("1", "2")
     * @throws SQLException SQL异常
     */
    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String dbValue = rs.getString(columnName);
        return convertToBusinessValue(dbValue);
    }

    /**
     * 获取可空结果：将数据库值转换为业务值
     *
     * @param rs          ResultSet
     * @param columnIndex 列索引
     * @return 业务值 ("1", "2")
     * @throws SQLException SQL异常
     */
    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String dbValue = rs.getString(columnIndex);
        return convertToBusinessValue(dbValue);
    }

    /**
     * 获取可空结果：将数据库值转换为业务值
     *
     * @param cs          CallableStatement
     * @param columnIndex 列索引
     * @return 业务值 ("1", "2")
     * @throws SQLException SQL异常
     */
    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String dbValue = cs.getString(columnIndex);
        return convertToBusinessValue(dbValue);
    }

    /**
     * 将业务值转换为数据库值
     *
     * @param businessValue 业务值 ("1", "2", "tool", "agent")
     * @return 数据库值 ('1', '2')
     */
    private String convertToDbValue(String businessValue) {
        if (businessValue == null) {
            return null;
        }

        String lowerValue = businessValue.toLowerCase().trim();

        // 业务值 → 数据库值
        if (lowerValue.equals("1") || lowerValue.equals("tool")) {
            return "1";
        } else if (lowerValue.equals("2") || lowerValue.equals("agent")) {
            return "2";
        }

        // 默认为tool
        return "1";
    }

    /**
     * 将数据库值转换为业务值
     *
     * @param dbValue 数据库值 ('1', '2')
     * @return 业务值 ("1", "2")
     */
    private String convertToBusinessValue(String dbValue) {
        if (dbValue == null) {
            return null;
        }

        // 去除空格进行比较
        String trimmedValue = dbValue.trim();

        switch (trimmedValue) {
            case "1":
                return "1";  // tool
            case "2":
                return "2";  // agent
            // 兼容历史数据：如果数据库中已经是字符串形式(旧数据),也要正确转换
            case "tool":
            case "Tool":
            case "TOOL":
                return "1";
            case "agent":
            case "Agent":
            case "AGENT":
                return "2";
            default:
                // 非标准值：记录警告并返回原值(避免数据丢失)
                System.err.println("ToolTypeHandler警告: 数据库中存在非标准的tool_type值: '" + dbValue + "'");
                return dbValue;
        }
    }
}
