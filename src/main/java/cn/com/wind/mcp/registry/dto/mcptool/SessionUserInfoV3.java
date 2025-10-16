package cn.com.wind.mcp.registry.dto.mcptool;

import java.io.Serializable;

public class SessionUserInfoV3 implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean isExperience;
    private String[] nameValueList;
    private int userID;
    private int accountID;
    private String loginName;
    private String userName;
    private String companyName;
    private String userEmail;
    private String userPhone;
    private String diskID;
    private int bindType;

    public SessionUserInfoV3() {
    }

    public boolean getIsExperience() {
        return this.isExperience;
    }

    public void setIsExperience(boolean isExperience) {
        this.isExperience = isExperience;
    }

    public String[] getNameValueList() {
        return this.nameValueList;
    }

    public void setNameValueList(String[] nameValueList) {
        this.nameValueList = nameValueList;
    }

    public int getUserID() {
        return this.userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getAccountID() {
        return this.accountID;
    }

    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }

    public String getLoginName() {
        return this.loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCompanyName() {
        return this.companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getUserEmail() {
        return this.userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return this.userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getDiskID() {
        return this.diskID;
    }

    public void setDiskID(String diskID) {
        this.diskID = diskID;
    }

    public int getBindType() {
        return this.bindType;
    }

    public void setBindType(int bindType) {
        this.bindType = bindType;
    }
}