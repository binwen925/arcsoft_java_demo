package com.zbw.arcsoft;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class AFD_FSDK_Version extends Structure {
    public int lCodebase;
    public int lMajor;
    public int lMinor;
    public int lBuild;
    public String Version;
    public String BuildDate;
    public String CopyRight;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(new String[]{"lCodebase", "lMajor", "lMinor", "lBuild", "Version", "BuildDate", "CopyRight"});
    }

    private String format(String field, String value) {
        return String.format("%30s\t:\t%s%n", field, value);
    }

    private String format(String field, int value) {
        return String.format("%30s\t:\t%d%n", field, value);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(format("Codebase", this.lCodebase));
        builder.append(format("Major", this.lMajor));
        builder.append(format("Minor", this.lMinor));
        builder.append(format("Build", this.lBuild));
        builder.append(format("Version", this.Version));
        builder.append(format("BuildDate", this.BuildDate));
        builder.append(format("CopyRight", this.CopyRight));

        return builder.toString();
    }
}
