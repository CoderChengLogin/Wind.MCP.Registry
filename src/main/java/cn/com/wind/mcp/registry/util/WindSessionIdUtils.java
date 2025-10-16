package cn.com.wind.mcp.registry.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class WindSessionIdUtils {
    public WindSessionIdUtils() {
    }

    public static String getWindSessionId(HttpServletRequest httpRequest) throws UnsupportedEncodingException {
        String wftSessionId = httpRequest.getHeader("wind.sessionid");
        String wftSessionIdNew = httpRequest.getHeader("windsessionid");
        if (!StrUtil.isBlank(wftSessionIdNew)) {
            return wftSessionIdNew;
        } else if (!StrUtil.isBlank(wftSessionId)) {
            return wftSessionId;
        } else {
            wftSessionId = httpRequest.getParameter("wind.sessionid");
            wftSessionIdNew = httpRequest.getParameter("windsessionid");
            if (!StrUtil.isBlank(wftSessionIdNew)) {
                return wftSessionIdNew;
            } else if (!StrUtil.isBlank(wftSessionId)) {
                return wftSessionId;
            } else {
                Cookie cookie = CookieUtils.getCookie(httpRequest, "windsessionid");
                if (cookie == null) {
                    cookie = CookieUtils.getCookie(httpRequest, "wind.sessionid");
                }

                return cookie != null ? URLDecoder.decode(cookie.getValue(), "utf-8") : null;
            }
        }
    }

    public static String getWindSessionID2(HttpServletRequest httpRequest) throws UnsupportedEncodingException {
        String wftSessionID2 = httpRequest.getHeader("windsessionid2");
        if (!StrUtil.isBlank(wftSessionID2)) {
            return wftSessionID2;
        } else {
            wftSessionID2 = httpRequest.getParameter("windsessionid2");
            if (!StrUtil.isBlank(wftSessionID2)) {
                return wftSessionID2;
            } else {
                Cookie cookie = CookieUtils.getCookie(httpRequest, "windsessionid2");
                return cookie != null ? URLDecoder.decode(cookie.getValue(), "utf-8") : null;
            }
        }
    }
}