package cn.com.wind.mcp.registry.dto.mcptool;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class McpToolDTO {

    private Long id;

    @NotNull(message = "工具编号不能为空")
    private Long toolNum;

    @NotNull(message = "工具版本不能为空")
    private Long toolVersion;

    @NotBlank(message = "有效状态不能为空")
    @Size(min = 1, max = 1, message = "有效状态长度必须为1")
    private String valid;

    @Size(max = 256, message = "工具名称长度不能超过256")
    private String toolName;

    @Size(max = 2000, message = "工具描述长度不能超过2000")
    private String toolDescription;

    private String nameDisplay;

    private String descriptionDisplay;

    private String inputSchema;

    private String outputSchema;

    private String streamOutput;

    @Size(min = 1, max = 1, message = "转换类型长度必须为1")
    private String convertType;

    @Size(min = 1, max = 1, message = "工具类型长度必须为1")
    private String toolType;

    // 不在创建和更新请求中返回的字段
    private String createBy;
    private String updateBy;
}