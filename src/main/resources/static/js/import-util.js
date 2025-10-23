/**
 * 工具导入相关的公共工具函数
 * 用于处理JSON文件读取、验证和数据传递
 */

/**
 * 工具导入管理器
 */
const ImportToolManager = {
    /**
     * 读取并解析JSON文件
     * @param {File} file - 用户选择的JSON文件
     * @returns {Promise<Object>} 解析后的JSON对象
     */
    readJsonFile(file) {
        return new Promise((resolve, reject) => {
            if (!file) {
                reject(new Error('未选择文件'));
                return;
            }

            if (!file.name.endsWith('.json')) {
                reject(new Error('请选择JSON格式的文件'));
                return;
            }

            const reader = new FileReader();

            reader.onload = function (e) {
                try {
                    const jsonData = JSON.parse(e.target.result);
                    resolve(jsonData);
                } catch (error) {
                    reject(new Error('JSON文件格式错误: ' + error.message));
                }
            };

            reader.onerror = function () {
                reject(new Error('文件读取失败'));
            };

            reader.readAsText(file);
        });
    },

    /**
     * 基础验证导入的JSON数据结构
     * @param {Object} jsonData - 导入的JSON数据
     * @returns {Object} {valid: boolean, error: string}
     */
    validateImportData(jsonData) {
        // 1. 检查基本结构
        if (!jsonData || typeof jsonData !== 'object') {
            return {valid: false, error: '数据格式错误'};
        }

        // 2. 检查必需字段
        if (!jsonData.mcpTool) {
            return {valid: false, error: '缺少MCP工具信息(mcpTool)'};
        }

        const mcpTool = jsonData.mcpTool;

        // 3. 检查MCP工具的必需字段
        const requiredFields = [
            'toolName',
            'toolDescription',
            'convertType',
            'inputSchema',
            'outputSchema'
        ];

        for (const field of requiredFields) {
            if (!mcpTool[field]) {
                return {valid: false, error: `MCP工具缺少必需字段: ${field}`};
            }
        }

        // 4. 验证convertType的值 (只接受数字值: '1'=HTTP, '2'=Expo, '3'=Manual)
        const convertType = mcpTool.convertType;
        const validConvertTypes = ['1', '2', '3'];
        if (!validConvertTypes.includes(convertType)) {
            return {valid: false, error: `convertType值无效: ${convertType}, 必须为 '1'(HTTP), '2'(Expo), '3'(Manual)`};
        }

        // 5. 验证JSON Schema格式
        try {
            if (mcpTool.inputSchema) {
                JSON.parse(mcpTool.inputSchema);
            }
            if (mcpTool.outputSchema) {
                JSON.parse(mcpTool.outputSchema);
            }
        } catch (e) {
            return {valid: false, error: 'inputSchema或outputSchema不是有效的JSON格式'};
        }

        return {valid: true, error: null};
    },

    /**
     * 将导入数据存储到sessionStorage
     * @param {Object} jsonData - 导入的JSON数据
     */
    storeImportData(jsonData) {
        try {
            sessionStorage.setItem('importToolData', JSON.stringify(jsonData));
            return true;
        } catch (e) {
            console.error('存储导入数据失败:', e);
            return false;
        }
    },

    /**
     * 从sessionStorage获取导入数据
     * @returns {Object|null} 导入的JSON数据或null
     */
    getImportData() {
        try {
            const data = sessionStorage.getItem('importToolData');
            if (data) {
                return JSON.parse(data);
            }
            return null;
        } catch (e) {
            console.error('获取导入数据失败:', e);
            return null;
        }
    },

    /**
     * 清除sessionStorage中的导入数据
     */
    clearImportData() {
        try {
            sessionStorage.removeItem('importToolData');
        } catch (e) {
            console.error('清除导入数据失败:', e);
        }
    },

    /**
     * 处理导入流程
     * 读取文件 -> 验证 -> 存储 -> 跳转
     * @param {File} file - 用户选择的JSON文件
     * @param {string} targetUrl - 跳转的目标URL (默认为 /tool-wizard/unified-add)
     * @returns {Promise<void>}
     */
    async handleImport(file, targetUrl = '/tool-wizard/unified-add') {
        try {
            // 1. 读取JSON文件
            const jsonData = await this.readJsonFile(file);

            // 2. 基础验证
            const validationResult = this.validateImportData(jsonData);
            if (!validationResult.valid) {
                throw new Error(validationResult.error);
            }

            // 3. 存储到sessionStorage
            if (!this.storeImportData(jsonData)) {
                throw new Error('无法存储导入数据');
            }

            // 4. 跳转到录入页面
            window.location.href = `${targetUrl}?mode=import`;

        } catch (error) {
            throw error;
        }
    },

    /**
     * 显示通知消息
     * @param {string} message - 消息内容
     * @param {string} type - 消息类型: success, error, warning, info
     */
    showNotification(message, type = 'info') {
        const alertClass = {
            'success': 'alert-success',
            'error': 'alert-danger',
            'warning': 'alert-warning',
            'info': 'alert-info'
        }[type] || 'alert-info';

        const iconClass = {
            'success': 'fa-check-circle',
            'error': 'fa-exclamation-triangle',
            'warning': 'fa-exclamation-circle',
            'info': 'fa-info-circle'
        }[type] || 'fa-info-circle';

        const alert = document.createElement('div');
        alert.className = `alert ${alertClass} alert-dismissible fade show`;
        alert.innerHTML = `
            <i class="fas ${iconClass} me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

        // 插入到页面顶部
        const main = document.querySelector('main');
        if (main) {
            main.insertBefore(alert, main.firstChild);
            // 滚动到顶部
            window.scrollTo({top: 0, behavior: 'smooth'});
        }

        // 3秒后自动移除
        setTimeout(() => {
            alert.remove();
        }, 3000);
    }
};

// 导出为全局对象
window.ImportToolManager = ImportToolManager;
