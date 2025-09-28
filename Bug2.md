#### 1.在“一体化工具录入”页面http://localhost:8081/tool-wizard/unified-add需要进行以下修改，具体如下：

在第二步：填写源工具信息的表单里面，只能按顺序填写以下字段

| 表单显示  | 是否必填 | 对应底层数据库字段 |
| --------- | -------- | ------------------ |
| 名称      | 是       | name_display       |
| 功能描述  | 是       | desc_display       |
| 请求url   | 是       | req_url            |
| 请求方式  | 是       | req_method         |
| 请求头    | 是       | req_headers        |
| 输入参数  | 是       | input_schema       |
| 输出参数  | 是       | output_schema      |
| 服务方app | 是       | provider_app_num   |

在第三步：填写MCP工具信息里面，只能按顺序填写以下字段

| 表单显示           | 是否必填 | 对应底层数据库字段  |
| ------------------ | -------- | ------------------- |
| MCP工具英文名      | 是       | tool_name           |
| MCP工具描述        | 是       | tool_description    |
| 多语言名称         | 是       | name_display        |
| 多语言描述         | 是       | description_display |
| input_json_schema  | 是       | input_schema        |
| output_json_schema | 是       | output_schema       |
| 是否流式输出       | 是       | stream_output       |
| 转换模板类型       | 是       | convert_type        |
| MCP工具类型        | 是       | tool_type           |

在第四步：配置模板转换器里面，只能按顺序填写以下字段

| 表单显示         | 是否必填 | 对应底层数据库字段 |
| ---------------- | -------- | ------------------ |
| 请求路径         | 是       | req_url            |
| 请求方式         | 是       | req_method         |
| 请求头（jinja2） | 是       | req_headers        |
| 请求体（jinja2） | 是       | req_body           |
| 响应体（jinja2） | 是       | resp_body          |

#### 2.一键保存工具信息，需要能生效保存到数据库，并且数据库表里的其余字段根据系统关联关系自动生成

#### 3.MCP工具列表页面能查看到的详情字段，记得保持和录入页面一致

