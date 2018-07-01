package io.evercam.androidapp.addeditcamera;

import java.io.Serializable;

import io.evercam.Auth;
import io.evercam.Defaults;
import io.evercam.EvercamException;

public class SelectedModel implements Serializable {
    private String modelId = "";
    private String modelName = "";
    private String vendorId = "";
    private String vendorName = "";
    private String defaultJpgUrl = "";
    private String defaultRtspUrl = "";
    private String defaultUsername = "";
    private String defaultPassword = "";

    public SelectedModel(String modelId, String modelName, String vendorId, String vendorName, Defaults defaults) {
        setModelId(modelId);
        setModelName(modelName);
        setVendorId(vendorId);
        setVendorName(vendorName);

        if (defaults != null) {
            try {
                setDefaultJpgUrl(defaults.getJpgURL());
//                setDefaultRtspUrl(defaults.getH264URL());

                Auth auth = defaults.getAuth(Auth.TYPE_BASIC);
                if (auth != null) {
                    setDefaultUsername(auth.getUsername());
                    setDefaultPassword(auth.getPassword());
                }
            } catch (EvercamException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isUnknown() {
        return getVendorId().isEmpty() || getModelId().isEmpty();
    }

    public String getModelId() {
        return modelId;
    }

    private void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    private void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getVendorId() {
        return vendorId;
    }

    private void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    private void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getDefaultJpgUrl() {
        return defaultJpgUrl;
    }

    private void setDefaultJpgUrl(String defaultJpgUrl) {
        this.defaultJpgUrl = defaultJpgUrl;
    }

    public String getDefaultRtspUrl() {
        return defaultRtspUrl;
    }

    private void setDefaultRtspUrl(String defaultRtspUrl) {
        this.defaultRtspUrl = defaultRtspUrl;
    }

    public String getDefaultUsername() {
        return defaultUsername;
    }

    private void setDefaultUsername(String defaultUsername) {
        this.defaultUsername = defaultUsername;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    private void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    @Override
    public String toString() {
        return "SelectedModel{" +
                "modelId='" + modelId + '\'' +
                ", modelName='" + modelName + '\'' +
                ", vendorId='" + vendorId + '\'' +
                ", defaultJpgUrl='" + defaultJpgUrl + '\'' +
                ", defaultRtspUrl='" + defaultRtspUrl + '\'' +
                ", defaultUsername='" + defaultUsername + '\'' +
                ", defaultPassword='" + defaultPassword + '\'' +
                '}';
    }
}
