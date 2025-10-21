package cn.com.wind.mcp.registry.dto.mcptool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 工具响应DTO
 * 用于统一返回工具信息的格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolResponseDTO {
    private Long id;
    private String name;
    private String displayName;
    private String description;
    private String provider;
    private String type;
    private String usageCount;
    private List<String> tags;
    private Map<String, Object> inputSchema;
    private Map<String, Object> outputSchema;
    private String usage;
}