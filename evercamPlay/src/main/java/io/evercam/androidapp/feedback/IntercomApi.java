package io.evercam.androidapp.feedback;

import android.util.Log;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.json.JSONException;
import org.json.JSONObject;

public class IntercomApi {
    private final static String TAG = "IntercomApi";
    public static String WEB_API_KEY = "";
    public static String ANDROID_API_KEY = "";
    public static String APP_ID = "";

    public static boolean hasApiKey() {
        return !WEB_API_KEY.isEmpty() && !APP_ID.isEmpty();
    }

    public static String getIntercomIdByUsername(String username) {
        final String URL_USERS = "https://api.intercom.io/users";

        try {
            HttpResponse<JsonNode> response = Unirest.get(URL_USERS).queryString("user_id",
                    username).basicAuth(IntercomApi.APP_ID, IntercomApi.WEB_API_KEY)
                    .header("Accept", "application/json").asJson();
            if (response.getStatus() == 200) {
                JSONObject jsonObject = response.getBody().getObject();
                return jsonObject.getString("id");
            } else {
                Log.e(TAG, response.getStatus() + " " + response.getBody().toString());
            }
        } catch (UnirestException | JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean sendMessage(String intercomId, String message) {
        final String URL_MESSAGES = "https://api.intercom.io/messages";

        try {
            JSONObject fromJsonObject = new JSONObject();
            fromJsonObject.put("type", "user");
            fromJsonObject.put("id", intercomId);
            JSONObject bodyJsonObject = new JSONObject();
            bodyJsonObject.put("from", fromJsonObject);
            bodyJsonObject.put("body", message);

            HttpResponse<JsonNode> response = Unirest.post(URL_MESSAGES).basicAuth(IntercomApi.APP_ID, IntercomApi.WEB_API_KEY)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(bodyJsonObject.toString()).asJson();

            if (response.getStatus() == 200) {
                return true;
            } else {
                Log.e(TAG, response.getStatus() + " " + response.getBody().toString());
            }
        } catch (UnirestException | JSONException e) {
            e.printStackTrace();
        }

        return false;
    }
}
