/**
 * MCP工具注册中心 - 主要JavaScript文件
 * 包含通用功能和交互逻辑
 */

// 应用程序主对象
const MCPRegistry = {
    // 初始化应用
    init: function () {
        this.initEventListeners();
        this.initTooltips();
        this.initFormValidation();
        this.initSearchFeatures();
        console.log('MCP Registry应用已初始化');
    },

    // 初始化事件监听器
    initEventListeners: function () {
        // 删除确认模态框
        this.initDeleteModal();

        // 搜索表单
        this.initSearchForm();

        // JSON编辑器
        this.initJsonEditors();

        // 工具测试
        this.initToolTesting();

        // 页面加载完成后的处理
        document.addEventListener('DOMContentLoaded', () => {
            this.handlePageLoad();
        });
    },

    // 初始化删除确认模态框
    initDeleteModal: function () {
        const deleteModal = document.getElementById('deleteModal');
        if (deleteModal) {
            deleteModal.addEventListener('show.bs.modal', function (event) {
                const button = event.relatedTarget;
                const toolId = button.getAttribute('data-tool-id');
                const toolName = button.getAttribute('data-tool-name');

                const toolNameSpan = document.getElementById('deleteToolName');
                const deleteForm = document.getElementById('deleteForm');

                if (toolNameSpan) toolNameSpan.textContent = toolName;
                if (deleteForm) {
                    const currentPath = window.location.pathname;
                    const basePath = currentPath.includes('mcp-tools') ? '/mcp-tools' : '/origin-http-tools';
                    deleteForm.action = `${basePath}/${toolId}/delete`;
                }
            });
        }
    },

    // 初始化搜索表单
    initSearchForm: function () {
        const searchForms = document.querySelectorAll('form[action*="tools"]');
        searchForms.forEach(form => {
            // 实时搜索功能
            const searchInputs = form.querySelectorAll('input[type="text"]');
            searchInputs.forEach(input => {
                let timeout;
                input.addEventListener('input', () => {
                    clearTimeout(timeout);
                    timeout = setTimeout(() => {
                        if (input.value.length >= 2 || input.value.length === 0) {
                            this.performSearch(form);
                        }
                    }, 500);
                });
            });

            // 选择框变化时自动搜索
            const selects = form.querySelectorAll('select');
            selects.forEach(select => {
                select.addEventListener('change', () => {
                    this.performSearch(form);
                });
            });
        });
    },

    // 执行搜索
    performSearch: function (form) {
        const formData = new FormData(form);
        const params = new URLSearchParams(formData);
        const url = `${form.action}?${params.toString()}`;

        // 使用AJAX加载搜索结果
        fetch(url, {
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
            .then(response => response.text())
            .then(html => {
                // 更新页面内容（这里需要根据实际情况调整）
                console.log('搜索完成');
            })
            .catch(error => {
                console.error('搜索失败:', error);
            });
    },

    // 初始化JSON编辑器
    initJsonEditors: function () {
        const jsonEditors = document.querySelectorAll('.json-editor');
        jsonEditors.forEach(editor => {
            // 添加语法高亮
            this.addJsonSyntaxHighlight(editor);

            // 添加格式化功能
            editor.addEventListener('blur', () => {
                this.formatJson(editor);
            });

            // 添加验证功能
            editor.addEventListener('input', () => {
                this.validateJson(editor);
            });
        });
    },

    // JSON语法高亮
    addJsonSyntaxHighlight: function (editor) {
        // 简单的语法高亮实现
        editor.addEventListener('input', () => {
            // 这里可以集成更复杂的语法高亮库
            this.validateJson(editor);
        });
    },

    // 格式化JSON
    formatJson: function (editor) {
        if (editor.value.trim()) {
            try {
                const json = JSON.parse(editor.value);
                editor.value = JSON.stringify(json, null, 2);
                this.removeValidationError(editor);
            } catch (error) {
                // 静默处理，不打断用户输入
            }
        }
    },

    // 验证JSON
    validateJson: function (editor) {
        if (!editor.value.trim()) {
            this.removeValidationError(editor);
            return true;
        }

        try {
            JSON.parse(editor.value);
            this.removeValidationError(editor);
            return true;
        } catch (error) {
            this.showValidationError(editor, `JSON格式错误: ${error.message}`);
            return false;
        }
    },

    // 显示验证错误
    showValidationError: function (element, message) {
        this.removeValidationError(element);

        element.classList.add('is-invalid');
        const errorDiv = document.createElement('div');
        errorDiv.className = 'invalid-feedback';
        errorDiv.textContent = message;
        element.parentNode.appendChild(errorDiv);
    },

    // 移除验证错误
    removeValidationError: function (element) {
        element.classList.remove('is-invalid');
        const errorDiv = element.parentNode.querySelector('.invalid-feedback');
        if (errorDiv) {
            errorDiv.remove();
        }
    },

    // 初始化工具测试功能
    initToolTesting: function () {
        // 工具测试按钮
        const testButtons = document.querySelectorAll('[onclick*="testTool"]');
        testButtons.forEach(button => {
            button.addEventListener('click', (e) => {
                e.preventDefault();
                this.testTool();
            });
        });
    },

    // 测试工具
    testTool: function () {
        const testResult = document.getElementById('testResult');
        const testContent = document.getElementById('testContent');

        if (!testResult || !testContent) {
            alert('测试功能暂不可用');
            return;
        }

        // 显示加载状态
        testResult.className = 'test-result';
        testResult.style.display = 'block';
        testContent.innerHTML = '<div class="loading me-2"></div>正在测试工具...';

        // 获取工具数据
        const toolData = this.getToolDataFromPage();

        // 发送测试请求
        fetch('/api/tools/test', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify(toolData)
        })
            .then(response => response.json())
            .then(data => {
                this.displayTestResult(data, testResult, testContent);
            })
            .catch(error => {
                this.displayTestError(error, testResult, testContent);
            });
    },

    // 从页面获取工具数据
    getToolDataFromPage: function () {
        // 这里需要根据页面结构获取工具数据
        return {
            id: this.getElementText('[data-tool-id]'),
            name: this.getElementText('h1'),
            method: this.getElementText('.method-badge'),
            url: this.getElementText('code'),
            headers: this.getJsonFromElement('.json-display'),
            parameters: {},
            requestBody: {}
        };
    },

    // 获取元素文本
    getElementText: function (selector) {
        const element = document.querySelector(selector);
        return element ? element.textContent.trim() : '';
    },

    // 从元素获取JSON
    getJsonFromElement: function (selector) {
        const element = document.querySelector(selector);
        if (!element) return {};

        try {
            return JSON.parse(element.textContent);
        } catch (error) {
            return {};
        }
    },

    // 显示测试结果
    displayTestResult: function (data, testResult, testContent) {
        if (data.success) {
            testResult.className = 'test-result test-success';
            testContent.innerHTML = `
                <h5><i class="fas fa-check-circle me-2"></i>测试成功！</h5>
                <div class="row">
                    <div class="col-md-6">
                        <strong>响应状态:</strong> ${data.status || 'N/A'}<br>
                        <strong>响应时间:</strong> ${data.responseTime || 'N/A'}ms<br>
                        <strong>内容类型:</strong> ${data.contentType || 'N/A'}
                    </div>
                    <div class="col-md-6">
                        <strong>响应大小:</strong> ${data.contentLength || 'N/A'} bytes<br>
                        <strong>测试时间:</strong> ${new Date().toLocaleString()}
                    </div>
                </div>
                ${data.response ? `
                    <div class="mt-3">
                        <strong>响应内容:</strong>
                        <pre class="json-display mt-2">${JSON.stringify(data.response, null, 2)}</pre>
                    </div>
                ` : ''}
            `;
        } else {
            this.displayTestError(data, testResult, testContent);
        }
    },

    // 显示测试错误
    displayTestError: function (error, testResult, testContent) {
        testResult.className = 'test-result test-error';
        testContent.innerHTML = `
            <h5><i class="fas fa-exclamation-triangle me-2"></i>测试失败！</h5>
            <strong>错误信息:</strong> ${error.message || error.error || '未知错误'}<br>
            ${error.details ? `<strong>详细信息:</strong> ${error.details}<br>` : ''}
            <strong>测试时间:</strong> ${new Date().toLocaleString()}
        `;
    },

    // 初始化提示工具
    initTooltips: function () {
        // 初始化Bootstrap提示工具
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    },

    // 初始化表单验证
    initFormValidation: function () {
        const forms = document.querySelectorAll('.needs-validation');
        forms.forEach(form => {
            form.addEventListener('submit', (event) => {
                if (!form.checkValidity()) {
                    event.preventDefault();
                    event.stopPropagation();
                }
                form.classList.add('was-validated');
            });
        });
    },

    // 初始化搜索功能
    initSearchFeatures: function () {
        // 搜索建议
        const searchInputs = document.querySelectorAll('input[type="text"][placeholder*="搜索"], input[type="text"][placeholder*="名称"]');
        searchInputs.forEach(input => {
            this.addSearchSuggestions(input);
        });
    },

    // 添加搜索建议
    addSearchSuggestions: function (input) {
        let timeout;
        input.addEventListener('input', () => {
            clearTimeout(timeout);
            timeout = setTimeout(() => {
                if (input.value.length >= 2) {
                    this.fetchSearchSuggestions(input.value, input);
                }
            }, 300);
        });
    },

    // 获取搜索建议
    fetchSearchSuggestions: function (query, input) {
        // 这里可以实现搜索建议功能
        console.log('搜索建议:', query);
    },

    // 页面加载处理
    handlePageLoad: function () {
        // 添加页面加载动画
        document.body.classList.add('loaded');

        // 初始化图表（如果需要）
        this.initCharts();

        // 初始化其他功能
        this.initMiscFeatures();
    },

    // 初始化图表
    initCharts: function () {
        // 这里可以集成图表库，如Chart.js
        console.log('图表初始化');
    },

    // 初始化其他功能
    initMiscFeatures: function () {
        // 返回顶部按钮
        this.initBackToTop();

        // 键盘快捷键
        this.initKeyboardShortcuts();
    },

    // 返回顶部功能
    initBackToTop: function () {
        const backToTopBtn = document.createElement('button');
        backToTopBtn.innerHTML = '<i class="fas fa-arrow-up"></i>';
        backToTopBtn.className = 'btn btn-primary position-fixed';
        backToTopBtn.style.cssText = 'bottom: 20px; right: 20px; z-index: 1000; display: none; border-radius: 50%; width: 50px; height: 50px;';
        backToTopBtn.onclick = () => window.scrollTo({top: 0, behavior: 'smooth'});

        document.body.appendChild(backToTopBtn);

        window.addEventListener('scroll', () => {
            backToTopBtn.style.display = window.scrollY > 300 ? 'block' : 'none';
        });
    },

    // 键盘快捷键
    initKeyboardShortcuts: function () {
        document.addEventListener('keydown', (e) => {
            // Ctrl+/ 显示快捷键帮助
            if (e.ctrlKey && e.key === '/') {
                e.preventDefault();
                this.showKeyboardShortcuts();
            }

            // ESC 关闭模态框
            if (e.key === 'Escape') {
                const modals = document.querySelectorAll('.modal.show');
                modals.forEach(modal => {
                    const modalInstance = bootstrap.Modal.getInstance(modal);
                    if (modalInstance) modalInstance.hide();
                });
            }
        });
    },

    // 显示快捷键帮助
    showKeyboardShortcuts: function () {
        alert('快捷键帮助:\nCtrl+/ - 显示此帮助\nESC - 关闭模态框');
    },

    // 工具函数
    utils: {
        // 防抖函数
        debounce: function (func, wait) {
            let timeout;
            return function executedFunction(...args) {
                const later = () => {
                    clearTimeout(timeout);
                    func(...args);
                };
                clearTimeout(timeout);
                timeout = setTimeout(later, wait);
            };
        },

        // 节流函数
        throttle: function (func, limit) {
            let inThrottle;
            return function () {
                const args = arguments;
                const context = this;
                if (!inThrottle) {
                    func.apply(context, args);
                    inThrottle = true;
                    setTimeout(() => inThrottle = false, limit);
                }
            };
        },

        // 格式化日期
        formatDate: function (date) {
            return new Date(date).toLocaleString('zh-CN');
        },

        // 复制到剪贴板
        copyToClipboard: function (text) {
            navigator.clipboard.writeText(text).then(() => {
                this.showToast('已复制到剪贴板');
            }).catch(err => {
                console.error('复制失败:', err);
            });
        },

        // 显示提示消息
        showToast: function (message, type = 'success') {
            // 简单的提示实现
            const toast = document.createElement('div');
            toast.className = `alert alert-${type} position-fixed`;
            toast.style.cssText = 'top: 20px; right: 20px; z-index: 9999;';
            toast.textContent = message;

            document.body.appendChild(toast);

            setTimeout(() => {
                toast.remove();
            }, 3000);
        }
    }
};

// 页面加载完成后初始化应用
document.addEventListener('DOMContentLoaded', () => {
    MCPRegistry.init();
});

// 导出到全局作用域
window.MCPRegistry = MCPRegistry;