1. 为实现系统核心功能，请执行以下数据库结构调整：
    - 删除以下冗余表及相关代码逻辑：tool_usage_stats、operation_log、orm_user
    - 创建provider表替代orm_user作为系统登录用户表
    - 建立完整工具调用流程的数据存储机制：
        * 用户需先录入origin_tool_http表数据，再录入关联的mcp_tool表数据
        * 对于HTTP类型工具，需同步录入http_template_converter表数据
        * 同理处理expo协议（是我发明的一款通信协议）工具：先录入origin_tool_expo，再派生对应的mcp_tool

2. 测试模块优化要求：
    - 从基础DAO层和entity模块开始编写测试用例
    - 确保实体类与数据库字段完全一致
    - 逐步向上完善service层的测试用例

3. 代码清理要求：
    - 彻底删除orm_user、orm_role、orm_user_role相关代码逻辑

4. 系统验证要求：
    - 完成上述修改后，需完整测试系统功能：
        * 验证页面操作（增删改查）是否正常
        * 测试所有接口调用功能
        * 建议下载MCP工具进行端到端流程验证
    - 确保最终系统可展示完整运行效果