package cn.com.wind.mcp.registry.util;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.wind.mcp.registry.dto.mcptool.SessionUserInfoV3;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class SessionDataUtil {

    private static final Pattern VALID_IP_PATTERN = Pattern.compile(
        "^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,"
            + "2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$");

    @Value("${spring.profiles.active}")
    private static String env;

    public static boolean isNullOrEmpty(String value) {
        return value == null || "".equals(value.trim()) || "unknown".equals(value.trim());
    }

    public static boolean isValidIp(String ip) {
        Matcher matcher = VALID_IP_PATTERN.matcher(ip);
        return matcher.find();
    }

    public static String getIpAddress(HttpServletRequest request) {

        if (request == null) {
            log.info("getIpAddress attributes == null");
            return "127.0.0.1";
        }

        String text = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (!isNullOrEmpty(text)) {
            if (!text.contains(".")) {
                text = null;
            } else if (text.contains(",")) {
                text = text.replace("'", "");
                String[] array = text.split("[,|;]");
                for (String s : array) {
                    if (isValidIp(s) &&
                        !s.startsWith("10.") &&
                        !s.startsWith("192.168") &&
                        !s.startsWith("172.16.")) {
                        return s;
                    }
                }
            } else {
                if (isValidIp(text)) {
                    return text;
                }
                text = null;
            }
        }

        if (isNullOrEmpty(text)) {
            text = request.getHeader("REMOTE_ADDR");
        }

        if (isNullOrEmpty(text)) {
            text = request.getRemoteAddr();
        }

        return text;
    }

    /**
     * 获取wind.sessionid
     *
     * @param request
     * @return
     */
    public static String getWindSessionId(HttpServletRequest request) {

        try {
            if (env == "indev") {
                return WindSessionIdUtils.getWindSessionID2(request);
            }
            return WindSessionIdUtils.getWindSessionId(request);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static int getInternalUserId(HttpServletRequest request) {
        if (request == null || request.getSession() == null) {
            log.info("getInternalUserId attributes == null");
            return 0;
        }
        try {
            String wftSessionId = WindSessionIdUtils.getWindSessionId(request);
            //log.info("wftSessionId:" + wftSessionId);
            SessionUserInfoV3 userInfoV3 = ((SessionUserInfoV3)request.getSession().getAttribute(
                "__user__" + wftSessionId));
            if (userInfoV3 == null) {
                return 0;
            } else {
                //    log.info("userInfoV3.getUserID:" + userInfoV3.getUserID());
                return userInfoV3.getUserID();
            }
        } catch (UnsupportedEncodingException e) {
            log.info("getInternalUserId Exception", e);
            return 0;
        }
    }

    /**
     * 获取当前请求语言类型
     *
     * @param request
     * @return 当前请求语言类型
     */
    public static String getLan(HttpServletRequest request) {

        if (request == null) {
            return "cn";
        }
        String lan = request.getHeader("wind-language");
        if (StrUtil.isBlank(lan)) {
            lan = request.getParameter("lan");
        }
        return StrUtil.isBlank(lan) ? "cn" :
            ("cn".equals(lan) || "zh-CN".equals(lan)) || "zh".equals(lan) ? "cn" : "en";
    }

}