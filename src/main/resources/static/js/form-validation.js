/**
 * 表单验证JavaScript文件
 * 提供通用的表单验证功能
 */

const FormValidator = {
    // 初始化表单验证
    init: function () {
        this.initBootstrapValidation();
        this.initCustomValidation();
        this.initRealTimeValidation();
        console.log('表单验证器已初始化');
    },

    // 初始化Bootstrap验证
    initBootstrapValidation: function () {
        const forms = document.querySelectorAll('.needs-validation');

        Array.from(forms).forEach(form => {
            form.addEventListener('submit', (event) => {
                if (!form.checkValidity()) {
                    event.preventDefault();
                    event.stopPropagation();

                    // 滚动到第一个错误字段
                    const firstInvalidField = form.querySelector(':invalid');
                    if (firstInvalidField) {
                        firstInvalidField.scrollIntoView({
                            behavior: 'smooth',
                            block: 'center'
                        });
                        firstInvalidField.focus();
                    }
                }

                form.classList.add('was-validated');
            }, false);
        });
    },

    // 初始化自定义验证
    initCustomValidation: function () {
        // URL验证
        this.initUrlValidation();

        // JSON验证
        this.initJsonValidation();

        // 端口号验证
        this.initPortValidation();

        // 邮箱验证
        this.initEmailValidation();

        // 密码强度验证
        this.initPasswordValidation();
    },

    // URL验证
    initUrlValidation: function () {
        const urlInputs = document.querySelectorAll('input[type="url"], input[name*="url"], input[id*="url"]');

        urlInputs.forEach(input => {
            input.addEventListener('blur', () => {
                this.validateUrl(input);
            });

            input.addEventListener('input', () => {
                this.clearValidationState(input);
            });
        });
    },

    // 验证URL
    validateUrl: function (input) {
        const value = input.value.trim();

        if (!value) {
            return true; // 空值由required属性处理
        }

        try {
            const url = new URL(value);
            if (!['http:', 'https:'].includes(url.protocol)) {
                this.setInvalid(input, 'URL必须以http://或https://开头');
                return false;
            }

            this.setValid(input);
            return true;
        } catch (error) {
            this.setInvalid(input, '请输入有效的URL地址');
            return false;
        }
    },

    // JSON验证
    initJsonValidation: function () {
        const jsonInputs = document.querySelectorAll('textarea[name*="json"], textarea[name*="config"], textarea[name*="parameters"]');

        jsonInputs.forEach(input => {
            input.addEventListener('blur', () => {
                this.validateJson(input);
            });

            input.addEventListener('input', () => {
                this.clearValidationState(input);
                // 实时验证（防抖）
                clearTimeout(input.jsonValidationTimeout);
                input.jsonValidationTimeout = setTimeout(() => {
                    this.validateJson(input, true);
                }, 1000);
            });
        });
    },

    // 验证JSON
    validateJson: function (input, silent = false) {
        const value = input.value.trim();

        if (!value) {
            this.clearValidationState(input);
            return true;
        }

        try {
            JSON.parse(value);
            if (!silent) {
                this.setValid(input);
            }
            return true;
        } catch (error) {
            if (!silent) {
                this.setInvalid(input, `JSON格式错误: ${error.message}`);
            }
            return false;
        }
    },

    // 端口号验证
    initPortValidation: function () {
        const portInputs = document.querySelectorAll('input[name*="port"], input[id*="port"]');

        portInputs.forEach(input => {
            input.addEventListener('blur', () => {
                this.validatePort(input);
            });

            input.addEventListener('input', () => {
                this.clearValidationState(input);
            });
        });
    },

    // 验证端口号
    validatePort: function (input) {
        const value = input.value.trim();

        if (!value) {
            return true;
        }

        const port = parseInt(value);
        if (isNaN(port) || port < 1 || port > 65535) {
            this.setInvalid(input, '端口号必须在1-65535之间');
            return false;
        }

        this.setValid(input);
        return true;
    },

    // 邮箱验证
    initEmailValidation: function () {
        const emailInputs = document.querySelectorAll('input[type="email"]');

        emailInputs.forEach(input => {
            input.addEventListener('blur', () => {
                this.validateEmail(input);
            });
        });
    },

    // 验证邮箱
    validateEmail: function (input) {
        const value = input.value.trim();

        if (!value) {
            return true;
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(value)) {
            this.setInvalid(input, '请输入有效的邮箱地址');
            return false;
        }

        this.setValid(input);
        return true;
    },

    // 密码强度验证
    initPasswordValidation: function () {
        const passwordInputs = document.querySelectorAll('input[type="password"]');

        passwordInputs.forEach(input => {
            input.addEventListener('input', () => {
                this.validatePasswordStrength(input);
            });
        });
    },

    // 验证密码强度
    validatePasswordStrength: function (input) {
        const value = input.value;
        const strengthIndicator = input.parentNode.querySelector('.password-strength');

        if (!value) {
            if (strengthIndicator) {
                strengthIndicator.style.display = 'none';
            }
            return true;
        }

        const strength = this.calculatePasswordStrength(value);

        if (strengthIndicator) {
            this.updatePasswordStrengthIndicator(strengthIndicator, strength);
        }

        if (strength.score < 2) {
            this.setInvalid(input, '密码强度太弱，请使用更复杂的密码');
            return false;
        }

        this.setValid(input);
        return true;
    },

    // 计算密码强度
    calculatePasswordStrength: function (password) {
        let score = 0;
        const feedback = [];

        // 长度检查
        if (password.length >= 8) score++;
        else feedback.push('至少8个字符');

        // 包含小写字母
        if (/[a-z]/.test(password)) score++;
        else feedback.push('包含小写字母');

        // 包含大写字母
        if (/[A-Z]/.test(password)) score++;
        else feedback.push('包含大写字母');

        // 包含数字
        if (/\d/.test(password)) score++;
        else feedback.push('包含数字');

        // 包含特殊字符
        if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) score++;
        else feedback.push('包含特殊字符');

        const levels = ['很弱', '弱', '中等', '强', '很强'];

        return {
            score: score,
            level: levels[score] || '很弱',
            feedback: feedback
        };
    },

    // 更新密码强度指示器
    updatePasswordStrengthIndicator: function (indicator, strength) {
        indicator.style.display = 'block';

        const colors = ['#dc3545', '#fd7e14', '#ffc107', '#28a745', '#20c997'];
        const color = colors[strength.score] || colors[0];

        indicator.innerHTML = `
            <div class="password-strength-bar">
                <div class="password-strength-fill" style="width: ${(strength.score / 5) * 100}%; background-color: ${color};"></div>
            </div>
            <small class="text-muted">密码强度: ${strength.level}</small>
            ${strength.feedback.length > 0 ? `<small class="text-muted d-block">建议: ${strength.feedback.join(', ')}</small>` : ''}
        `;
    },

    // 初始化实时验证
    initRealTimeValidation: function () {
        // 必填字段验证
        const requiredInputs = document.querySelectorAll('input[required], textarea[required], select[required]');

        requiredInputs.forEach(input => {
            input.addEventListener('blur', () => {
                this.validateRequired(input);
            });

            input.addEventListener('input', () => {
                if (input.classList.contains('is-invalid')) {
                    this.validateRequired(input);
                }
            });
        });

        // 长度验证
        const lengthInputs = document.querySelectorAll('input[minlength], input[maxlength], textarea[minlength], textarea[maxlength]');

        lengthInputs.forEach(input => {
            input.addEventListener('input', () => {
                this.validateLength(input);
            });
        });
    },

    // 验证必填字段
    validateRequired: function (input) {
        const value = input.value.trim();

        if (!value) {
            this.setInvalid(input, '此字段为必填项');
            return false;
        }

        this.setValid(input);
        return true;
    },

    // 验证长度
    validateLength: function (input) {
        const value = input.value;
        const minLength = parseInt(input.getAttribute('minlength'));
        const maxLength = parseInt(input.getAttribute('maxlength'));

        if (minLength && value.length < minLength) {
            this.setInvalid(input, `最少需要${minLength}个字符`);
            return false;
        }

        if (maxLength && value.length > maxLength) {
            this.setInvalid(input, `最多允许${maxLength}个字符`);
            return false;
        }

        this.setValid(input);
        return true;
    },

    // 设置字段为无效状态
    setInvalid: function (input, message) {
        input.classList.remove('is-valid');
        input.classList.add('is-invalid');

        // 移除现有的错误消息
        const existingFeedback = input.parentNode.querySelector('.invalid-feedback');
        if (existingFeedback) {
            existingFeedback.remove();
        }

        // 添加新的错误消息
        const feedback = document.createElement('div');
        feedback.className = 'invalid-feedback';
        feedback.textContent = message;
        input.parentNode.appendChild(feedback);
    },

    // 设置字段为有效状态
    setValid: function (input) {
        input.classList.remove('is-invalid');
        input.classList.add('is-valid');

        // 移除错误消息
        const existingFeedback = input.parentNode.querySelector('.invalid-feedback');
        if (existingFeedback) {
            existingFeedback.remove();
        }
    },

    // 清除验证状态
    clearValidationState: function (input) {
        input.classList.remove('is-valid', 'is-invalid');

        const existingFeedback = input.parentNode.querySelector('.invalid-feedback');
        if (existingFeedback) {
            existingFeedback.remove();
        }
    },

    // 验证整个表单
    validateForm: function (form) {
        let isValid = true;
        const inputs = form.querySelectorAll('input, textarea, select');

        inputs.forEach(input => {
            if (!this.validateField(input)) {
                isValid = false;
            }
        });

        return isValid;
    },

    // 验证单个字段
    validateField: function (input) {
        let isValid = true;

        // 必填验证
        if (input.hasAttribute('required')) {
            isValid = this.validateRequired(input) && isValid;
        }

        // 类型特定验证
        const type = input.type || input.tagName.toLowerCase();
        switch (type) {
            case 'url':
                isValid = this.validateUrl(input) && isValid;
                break;
            case 'email':
                isValid = this.validateEmail(input) && isValid;
                break;
            case 'password':
                isValid = this.validatePasswordStrength(input) && isValid;
                break;
            case 'textarea':
                if (input.name.includes('json') || input.name.includes('config')) {
                    isValid = this.validateJson(input) && isValid;
                }
                break;
        }

        // 长度验证
        if (input.hasAttribute('minlength') || input.hasAttribute('maxlength')) {
            isValid = this.validateLength(input) && isValid;
        }

        // 端口号验证
        if (input.name.includes('port') || input.id.includes('port')) {
            isValid = this.validatePort(input) && isValid;
        }

        return isValid;
    },

    // 重置表单验证状态
    resetForm: function (form) {
        form.classList.remove('was-validated');

        const inputs = form.querySelectorAll('input, textarea, select');
        inputs.forEach(input => {
            this.clearValidationState(input);
        });

        // 清除密码强度指示器
        const strengthIndicators = form.querySelectorAll('.password-strength');
        strengthIndicators.forEach(indicator => {
            indicator.style.display = 'none';
        });
    }
};

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    FormValidator.init();
});

// 导出到全局作用域
window.FormValidator = FormValidator;