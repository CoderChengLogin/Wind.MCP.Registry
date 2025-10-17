/**
 * Toast通知组件
 * 用于显示操作成功/失败/警告等临时消息
 *
 * @author Claude
 * @version 1.0.0
 */

/**
 * 显示Toast通知
 *
 * @param {string} message - 通知消息内容
 * @param {string} type - 通知类型: 'success'(成功-绿色), 'error'(错误-红色), 'warning'(警告-黄色), 'info'(信息-蓝色)
 * @param {number} duration - 显示时长（毫秒），默认3000ms
 */
function showToast(message, type = 'info', duration = 3000) {
    // 确保Toast容器存在
    let toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toastContainer';
        toastContainer.className = 'toast-container';
        document.body.appendChild(toastContainer);
    }

    // 创建Toast元素
    const toast = document.createElement('div');
    toast.className = `toast toast-${type} show`;

    // 根据类型选择图标
    let icon = 'fa-info-circle';
    switch (type) {
        case 'success':
            icon = 'fa-check-circle';
            break;
        case 'error':
            icon = 'fa-exclamation-circle';
            break;
        case 'warning':
            icon = 'fa-exclamation-triangle';
            break;
        case 'info':
        default:
            icon = 'fa-info-circle';
            break;
    }

    // 设置Toast内容
    toast.innerHTML = `
        <div class="toast-icon">
            <i class="fas ${icon}"></i>
        </div>
        <div class="toast-message">${message}</div>
        <button type="button" class="toast-close" onclick="closeToast(this)">
            <i class="fas fa-times"></i>
        </button>
    `;

    // 添加到容器
    toastContainer.appendChild(toast);

    // 触发进入动画
    setTimeout(() => {
        toast.classList.add('toast-enter');
    }, 10);

    // 自动关闭
    const timer = setTimeout(() => {
        closeToast(toast.querySelector('.toast-close'));
    }, duration);

    // 鼠标悬停时暂停自动关闭
    toast.addEventListener('mouseenter', () => {
        clearTimeout(timer);
    });

    // 鼠标离开后延迟关闭
    toast.addEventListener('mouseleave', () => {
        setTimeout(() => {
            closeToast(toast.querySelector('.toast-close'));
        }, 1000);
    });
}

/**
 * 关闭Toast通知
 *
 * @param {HTMLElement} closeButton - 关闭按钮元素
 */
function closeToast(closeButton) {
    const toast = closeButton.closest('.toast');
    if (!toast) return;

    // 添加退出动画
    toast.classList.remove('toast-enter');
    toast.classList.add('toast-exit');

    // 动画结束后移除元素
    setTimeout(() => {
        toast.remove();

        // 如果容器为空，移除容器
        const container = document.getElementById('toastContainer');
        if (container && container.children.length === 0) {
            container.remove();
        }
    }, 300);
}

/**
 * 便捷方法：显示成功消息
 *
 * @param {string} message - 消息内容
 * @param {number} duration - 显示时长（毫秒），默认3000ms
 */
function showSuccess(message, duration = 3000) {
    showToast(message, 'success', duration);
}

/**
 * 便捷方法：显示错误消息
 *
 * @param {string} message - 消息内容
 * @param {number} duration - 显示时长（毫秒），默认4000ms（错误消息显示时间稍长）
 */
function showError(message, duration = 4000) {
    showToast(message, 'error', duration);
}

/**
 * 便捷方法：显示警告消息
 *
 * @param {string} message - 消息内容
 * @param {number} duration - 显示时长（毫秒），默认3500ms
 */
function showWarning(message, duration = 3500) {
    showToast(message, 'warning', duration);
}

/**
 * 便捷方法：显示信息消息
 *
 * @param {string} message - 消息内容
 * @param {number} duration - 显示时长（毫秒），默认3000ms
 */
function showInfo(message, duration = 3000) {
    showToast(message, 'info', duration);
}
