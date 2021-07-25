/**
 *shenmajr.com Inc.
 *copyright (c) 2015-{YEAR} All Rights Reserved.
 */
package com.example.sendmail.common.mail.bo;

import java.io.InputStream;
import java.io.Serializable;

/**
 * 邮件附件信息，不建议作他用
 *@description
 *@author sh00975
 *@version @Id:  AttrInfo.java, sh00975 v 0.1 2020/3/27 10:35
 *
 */
public class AttrInfo implements Serializable {
    private static final long serialVersionUID = 6734819349005572510L;

    /**附件文件名*/
    private String fileName;
    /**附件二进制流*/
    private InputStream is;
    /**附件路径*/
    private String path;
    /**附件类型*/
    private String type;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public InputStream getIs() {
        return is;
    }

    public void setIs(InputStream is) {
        this.is = is;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}