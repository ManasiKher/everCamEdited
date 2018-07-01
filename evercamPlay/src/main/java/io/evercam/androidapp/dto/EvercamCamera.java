package io.evercam.androidapp.dto;

import android.location.Location;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import io.evercam.Camera;
import io.evercam.EvercamException;


public class EvercamCamera {
    public ImageLoadingStatus loadingStatus = ImageLoadingStatus.not_started;

    private final String TAG = "EvercamCamera";
    private boolean isLocal = false;
    public Camera camera = null;
    private int id = -1;

    private String cameraId = "";
    private String name = "";
    private String user = ""; // The user's user name
    private String realOwner = "";// The owner of camera
    private boolean canEdit = false;
    private boolean canDelete = false;
    private String rights = "";
    private String username = "";
    private String password = "";
    private String timezone = "";
    private String vendor = "";
    private String modelId = "";
    private String modelName = "";
    private String mac = "";
    private String externalSnapshotUrl = "";
    private String internalSnapshotUrl = "";
    private String externalRtspUrl = "";
    private String internalRtspUrl = "";
    private boolean isOnline = false;
    private boolean hasCredentials = false;
    private String thumbnailUrl = "";
    private String hlsUrl = "";

    // Fields for edit camera
    private String internalHost = "";
    private String externalHost = "";
    private int internalHttp = 0;
    private int internalRtsp = 0;
    private int externalHttp = 0;
    private int externalRtsp = 0;

    private boolean isPublic;
    private boolean isDiscoverable;
    private double latitude;
    private double longitude;

    public EvercamCamera() {

    }

    public EvercamCamera convertFromEvercam(io.evercam.Camera camera) {
        this.camera = camera;
        try {
            //location":{"lng":-6.0426519,"lat":52.9679717},
            cameraId = camera.getId();
            name = camera.getName();
            if (AppData.defaultUser != null) {
                username = AppData.defaultUser.getUsername();
            }
            realOwner = camera.getOwner();
            rights = camera.getRights().toString();
            canEdit = camera.getRights().canEdit();
            canDelete = camera.getRights().canDelete();
            if (camera.hasCredentials()) {
                hasCredentials = true;
                username = camera.getUsername();
                password = camera.getPassword();
            }
            timezone = camera.getTimezone();
            vendor = camera.getVendorName();
            modelId = camera.getModelId();
            modelName = camera.getModelName();
            mac = camera.getMacAddress();
            externalSnapshotUrl = camera.getExternalJpgUrl();
            internalSnapshotUrl = camera.getInternalJpgUrl();
            externalRtspUrl = camera.getExternalH264Url();
            internalRtspUrl = camera.getInternalH264Url();
            isOnline = camera.isOnline();
            internalHost = camera.getInternalHost();
            externalHost = camera.getExternalHost();
            internalHttp = camera.getInternalHttpPort();
            internalRtsp = camera.getInternalRtspPort();
            externalHttp = camera.getExternalHttpPort();
            externalRtsp = camera.getExternalRtspPort();
            thumbnailUrl = camera.getThumbnailUrl();
            hlsUrl = camera.getProxyUrl().getHls();

            /*latitude        =       camera.getLocation().getLat();
            longitude       =       camera.getLocation().getLng();*/
            latitude        =       camera.getLat();
            longitude       =       camera.getLng();

            isDiscoverable = camera.isDiscoverable();
            isPublic = camera.isPublic();
        } catch (EvercamException e) {
            Log.e(TAG, e.getMessage());
        }
        return this;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public String getCameraId() {
        return cameraId;
    }

    public String getExternalSnapshotUrl() {
        return externalSnapshotUrl;
    }

    public String getInternalSnapshotUrl() {
        return internalSnapshotUrl;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getModelId() {
        return modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public String getVendor() {
        return vendor;
    }

    public String getUser() {
        return user;
    }

    public String getRealOwner() {
        return realOwner;
    }

    public boolean canEdit() {
        return canEdit;
    }

    public boolean canDelete() {
        return canDelete;
    }

    public int getCanEditInt() {
        return canEdit() ? 1 : 0;
    }

    public int getCanDeleteInt() {
        return canDelete() ? 1 : 0;
    }

    public boolean hasCredentials() {
        return hasCredentials;
    }

    public int getHasCredentialsInt() {
        return hasCredentials() ? 1 : 0;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public void setExternalSnapshotUrl(String externalSnapshotUrl) {
        this.externalSnapshotUrl = externalSnapshotUrl;
    }

    public void setInternalSnapshotUrl(String internalSnapshotUrl) {
        this.internalSnapshotUrl = internalSnapshotUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setRealOwner(String realOwner) {
        this.realOwner = realOwner;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getExternalRtspUrl() {
        return externalRtspUrl;
    }

    public String getInternalRtspUrl() {
        return internalRtspUrl;
    }

    public void setExternalRtspUrl(String externalRtspUrl) {
        this.externalRtspUrl = externalRtspUrl;
    }

    public void setInternalRtspUrl(String internalRtspUrl) {
        this.internalRtspUrl = internalRtspUrl;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public String getCode() {
        return "";
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean isLocal) {
        this.isLocal = isLocal;
    }

    public void setHasCredentials(boolean hasCredentials) {
        this.hasCredentials = hasCredentials;
    }

    public String getInternalHost() {
        return internalHost;
    }

    public String getExternalHost() {
        return externalHost;
    }

    public int getInternalHttp() {
        return internalHttp;
    }

    public int getInternalRtsp() {
        return internalRtsp;
    }

    public int getExternalHttp() {
        return externalHttp;
    }

    public int getExternalRtsp() {
        return externalRtsp;
    }

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }

    public String getJpgPath() {
        try {
            // TODO: Wrap this in the wrapper or API response
            if (!internalSnapshotUrl.isEmpty()) {
                return new URL(internalSnapshotUrl).getPath();
            } else if (!externalSnapshotUrl.isEmpty()) {
                return new URL(externalSnapshotUrl).getPath();
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, e.toString());
        }
        return "";
    }

    public String getH264Path() {
        try {
            if (!internalRtspUrl.isEmpty()) {
                return new URI(internalRtspUrl).getPath();
            } else if (!externalRtspUrl.isEmpty()) {
                return new URI(externalRtspUrl).getPath();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void setInternalHost(String internalHost) {
        this.internalHost = internalHost;
    }

    public void setExternalHost(String externalHost) {
        this.externalHost = externalHost;
    }

    public void setInternalHttp(int internalHttp) {
        this.internalHttp = internalHttp;
    }

    public void setInternalRtsp(int internalRtsp) {
        this.internalRtsp = internalRtsp;
    }

    public void setExternalHttp(int externalHttp) {
        this.externalHttp = externalHttp;
    }

    public void setExternalRtsp(int externalRtsp) {
        this.externalRtsp = externalRtsp;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public boolean isHikvision() {
        if (getVendor().toLowerCase(Locale.UK).contains("hikvision")) {
            return true;
        }
        return false;
    }

    public boolean hasRtspUrl() {
        return !getExternalRtspUrl().isEmpty();
    }

    public boolean hasThumbnailUrl() {
        return getThumbnailUrl() != null && !getThumbnailUrl().isEmpty();
    }

    public void setHlsUrl(String hlsUrl) {
        this.hlsUrl = hlsUrl;
    }

    public String getHlsUrl() {
        return hlsUrl;
    }

    public boolean hasHlsUrl() {
        return !hlsUrl.isEmpty();
    }

    public boolean hasModel() {
        return !getModelId().isEmpty();
    }

    public boolean isDiscoverable() {
        return isDiscoverable;
    }

    public void setIsDiscoverable(boolean isDiscoverable) {
        this.isDiscoverable = isDiscoverable;
    }

    public int getDiscoverableInt() {
        return isDiscoverable() ? 1 : 0;
    }

    public int getIsOnlineInt() {
        return isOnline ? 1 : 0;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public int getPublicInt() {
        return isPublic ? 1 : 0;
    }

    public boolean isOwned() {
        return getRealOwner().equals(getUser());
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public  void setLongitude (double longitude){
        this.longitude = longitude;
    }

    public double getLatitude(){
        return latitude;
    }

    public  double getLongitude(){
        return longitude;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        EvercamCamera other = (EvercamCamera) obj;
        if (cameraId.equals(other.cameraId) && externalRtspUrl.equals(other.externalRtspUrl) &&
                internalRtspUrl.equals(other.internalRtspUrl) && externalSnapshotUrl.equals(other
                .externalSnapshotUrl) && internalSnapshotUrl.equals(other.internalSnapshotUrl) &&
                mac.equals(other.mac) && modelId.equals(other.modelId) && modelName.equals(other.modelName)
                && name.equals(other.name) &&
                user.equals(other.user) && password.equals(other.password) && timezone.equals
                (other.timezone) && username.equals(other.username) && vendor.equals(other
                .vendor) && internalHost.equals(other.internalHost) && externalHost.equals(other
                .externalHost) && internalHttp == other.internalHttp && externalHttp == other
                .externalHttp && internalRtsp == other.internalRtsp && externalRtsp == other
                .externalRtsp && realOwner.equals(other.realOwner) && canEdit == other.canEdit &&
                canDelete == other.canDelete && isOnline == other.isOnline() && rights == other.rights &&
                isPublic == other.isPublic && isDiscoverable == other.isDiscoverable &&
                hlsUrl == other.hlsUrl) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "EvercamCamera [loadingStatus=" + loadingStatus + ", id=" + id + ", " +
                "cameraId=" + cameraId + ", name=" + name + ", user=" + user + ", " +
                "realOwner=" + realOwner + ", canEdit=" + canEdit + ", " +
                "canDelete=" + canDelete + ", rights=" + rights + ", username=" + username + ", " +
                "password=" + password + ", timezone=" + timezone + ", vendor=" + vendor + ", " +
                "modelId=" + modelId + ", " +
                "modelName=" + modelName + ", mac=" + mac + ", externalSnapshotUrl=" +
                externalSnapshotUrl + ", internalSnapshotUrl=" + internalSnapshotUrl + ", " +
                "externalRtspUrl=" + externalRtspUrl + ", internalRtspUrl=" + internalRtspUrl +
                ", isOnline=" + isOnline + ", hasCredentials=" + hasCredentials + ", " +
                "internalHost=" + internalHost + ", externalHost=" + externalHost + ", " +
                "internalHttp=" + internalHttp + ", internalRtsp=" + internalRtsp + ", " +
                "externalHttp=" + externalHttp + ", externalRtsp=" + externalRtsp + ", " +
                "thumbnailUrl=" + thumbnailUrl + ", hlsUrl=" + hlsUrl + "]";
    }
}
