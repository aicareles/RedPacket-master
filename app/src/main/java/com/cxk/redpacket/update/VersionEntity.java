package com.cxk.redpacket.update;


import org.json.JSONException;
import org.json.JSONObject;

public class VersionEntity {
    private OutputType outputType;
    private ApkData apkData;
    private String path;

    public OutputType getOutputType() {
        return outputType;
    }

    public void setOutputType(OutputType outputType) {
        this.outputType = outputType;
    }

    public ApkData getApkData() {
        return apkData;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setApkData(ApkData apkData) {
        this.apkData = apkData;
    }

    public static VersionEntity fromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            VersionEntity entity = new VersionEntity();

            JSONObject outputTypeObject = jsonObject.getJSONObject("outputType");
            OutputType outputType = new OutputType();
            outputType.setType(outputTypeObject.optString("type"));
            entity.setOutputType(outputType);

            JSONObject apkDataObject = jsonObject.getJSONObject("apkData");
            ApkData apkData = new ApkData();
            apkData.setType(apkDataObject.optString("type"));
            apkData.setVersionCode(apkDataObject.optInt("versionCode"));
            apkData.setVersionName(apkDataObject.optString("versionName"));
            apkData.setEnabled(apkDataObject.optBoolean("enabled"));
            apkData.setOutputFile(apkDataObject.optString("outputFile"));
            apkData.setFullName(apkDataObject.optString("fullName"));
            apkData.setBaseName(apkDataObject.getString("baseName"));
            apkData.setSize(apkDataObject.getInt("size"));
            apkData.setUpdateContent(apkDataObject.getString("updateContent"));
            entity.setApkData(apkData);

            entity.setPath(jsonObject.optString("path"));

            return entity;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject outputTypeObject = new JSONObject();
            outputTypeObject.put("type", outputType.getType());
            jsonObject.put("outputType", outputTypeObject);

            JSONObject apkDataObject = new JSONObject();
            apkDataObject.put("type", apkData.getType());
            apkDataObject.put("versionCode", apkData.getVersionCode());
            apkDataObject.put("versionName", apkData.getVersionName());
            apkDataObject.put("enabled", apkData.isEnabled());
            apkDataObject.put("outputFile", apkData.getOutputFile());
            apkDataObject.put("fullName", apkData.getFullName());
            apkDataObject.put("baseName", apkData.getBaseName());
            jsonObject.put("apkData", apkDataObject);

            jsonObject.put("path", getPath());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
