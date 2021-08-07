package com.example.sendmail.common.mail.bo;

import java.io.InputStream;
import java.io.Serializable;

public class AttrInfo implements Serializable {
    private static final long serialVersionUID = 6734819349005572510L;

    private String fileName;
    private InputStream is;
    private String path;
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