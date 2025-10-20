/**
 * 工具测试功能JavaScript
 */

// 全局变量:当前测试的工具ID
let testToolId = null;

/**
 * 测试工具函数 - 显示参数输入模态框
 * @param {number} toolId - 工具ID
 */
function testTool(toolId) {
    if (!toolId) {
        alert('工具ID不能为空');
        return;
    }

    // 保存当前工具ID
    testToolId = toolId;

    // 初始化参数JSON编辑器
    document.getElementById('testParamsJson').value = '{}';

    // 重置测试结果输出框
    document.getElementById('testResultOutput').innerHTML = `
        <div class="text-muted text-center py-5">
            <i class="fas fa-info-circle me-2"></i>等待测试...
        </div>
    `;

    // 显示参数输入模态框
    const paramsModal = new bootstrap.Modal(document.getElementById('testParamsModal'));
    paramsModal.show();

    console.log('[测试工具] 打开参数输入框, 工具ID:', toolId);
}

/**
 * 执行测试 - 发送测试请求
 */
function executeTest() {
    if (!testToolId) {
        alert('工具ID不能为空');
        return;
    }

    // 获取用户输入的参数JSON
    const paramsJsonStr = document.getElementById('testParamsJson').value;
    let params = {};

    try {
        params = JSON.parse(paramsJsonStr);
    } catch (e) {
        alert('参数JSON格式错误,请检查并修正: ' + e.message);
        return;
    }

    // 显示加载状态在输出参数框中
    document.getElementById('testResultOutput').innerHTML = `
        <div class="text-center py-4">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">测试中...</span>
            </div>
            <p class="mt-3 mb-0">正在执行测试,请稍候...</p>
        </div>
    `;

    console.log('[测试工具] 发送测试请求, 工具ID:', testToolId, '参数:', params);

    // 发送测试请求
    fetch(`/api/tools/${testToolId}/test`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify(params)
    })
        .then(response => {
            console.log('[测试工具] 响应状态:', response.status);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('[测试工具] 成功响应:', data);
            // 显示测试结果
            displayTestResult(data);
        })
        .catch(error => {
            console.error('[测试工具] 失败:', error);
            document.getElementById('testResultOutput').innerHTML = `
                <div class="alert alert-danger mb-0">
                    <h6 class="alert-heading"><i class="fas fa-exclamation-circle me-2"></i>测试失败</h6>
                    <p class="mb-0">错误信息: ${error.message}</p>
                </div>
            `;
        });
}

/**
 * 显示测试结果
 * @param {Object} data - 测试结果数据
 */
function displayTestResult(data) {
    const content = document.getElementById('testResultOutput');
    const success = data.success || data.status === 'success';

    let html = '';

    if (success) {
        html = `
            <div class="alert alert-success mb-0">
                <h6 class="alert-heading"><i class="fas fa-check-circle me-2"></i>测试成功</h6>
                <p class="mb-0">${data.message || '工具测试通过'}</p>
            </div>
        `;
    } else {
        html = `
            <div class="alert alert-danger mb-0">
                <h6 class="alert-heading"><i class="fas fa-times-circle me-2"></i>测试失败</h6>
                <p class="mb-0">${data.message || data.error || '工具测试未通过'}</p>
            </div>
        `;
    }

    // 如果有详细数据,显示JSON格式
    if (data.data) {
        html += `
            <div class="mt-3">
                <h6><i class="fas fa-file-code me-1"></i>详细结果:</h6>
                <pre class="bg-white border rounded p-2 mb-0" style="max-height: 250px; overflow-y: auto; font-size: 0.85rem;"><code>${JSON.stringify(data.data, null, 2)}</code></pre>
            </div>
        `;
    }

    content.innerHTML = html;
}