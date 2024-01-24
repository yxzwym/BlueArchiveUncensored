package com.guisei.bluearchiveuncensored.config;

/**
 * 蔚蓝档案包名管理
 */
public enum PackEnum {

    SCHALE("沙勒", "com.RoamingStar.BlueArchive"),

    ;// =================================================================================

    /**
     * 渠道名
     */
    private String channel;
    /**
     * 包名
     */
    private String packName;

    PackEnum(String channel, String packName) {
        this.channel = channel;
        this.packName = packName;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }
}
