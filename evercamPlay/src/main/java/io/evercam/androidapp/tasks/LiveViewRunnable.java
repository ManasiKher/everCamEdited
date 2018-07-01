package io.evercam.androidapp.tasks;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//import org.phoenixframework.channels.Channel;
//import org.phoenixframework.channels.ChannelEvent;
//import org.phoenixframework.channels.Envelope;
//import org.phoenixframework.channels.IMessageCallback;
//import org.phoenixframework.channels.ISocketCloseCallback;
//import org.phoenixframework.channels.Socket;
//import org.phoenixframework.channels.*;

import java.io.IOException;
import java.lang.ref.WeakReference;

import io.evercam.API;
import io.evercam.androidapp.PhoenixChannel.Channel;
import io.evercam.androidapp.PhoenixChannel.ChannelEvent;
import io.evercam.androidapp.PhoenixChannel.Envelope;
import io.evercam.androidapp.PhoenixChannel.IMessageCallback;
import io.evercam.androidapp.PhoenixChannel.ISocketCloseCallback;
import io.evercam.androidapp.PhoenixChannel.Socket;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.video.VideoActivity;


public class LiveViewRunnable implements Runnable {

    private final static String TAG = "LiveViewRunnable";
    private final String HOST = "wss://media.evercam.io/socket/websocket";
    private final String ENVELOPE_KEY_TIMESTAMP = "timestamp";
    private final String ENVELOPE_KEY_IMAGE = "image";
    private final String EVENT_SNAPSHOT_TAKEN = "snapshot-taken";

    private Socket mSocket;
    private Channel mChannel;
    private String mCameraId;

    //Check if it's the first image so that the progress bar should be hidden
    private boolean isFirstImage = true;

    private final Handler mHandler;
    private WeakReference<VideoActivity> mVideoActivityReference;

    public LiveViewRunnable(VideoActivity videoActivity, String cameraId) {
        mCameraId = cameraId;
        mHandler = new Handler(Looper.getMainLooper());
        mVideoActivityReference = new WeakReference<>(videoActivity);
    }

    @Override
    public void run() {
        if (API.hasUserKeyPair()) {
            connectWebSocket();
        }
    }

    private VideoActivity getActivity() {
        return mVideoActivityReference.get();
    }

    private void connectWebSocket() {
        try {

            mSocket = new Socket(getHostWithAuth(HOST));
            mSocket.connect();

            mSocket.onClose(new ISocketCloseCallback() {
                @Override
                public void onClose() {
                    Log.e(TAG, "socket:onClose");
                }
            });

            JsonNode jsonNode = new ObjectMapper().valueToTree(API.userKeyPairMap());
            mChannel = mSocket.chan("cameras:" + mCameraId, jsonNode);

            mChannel.join()
                    .receive("ignore", new IMessageCallback() {
                        @Override
                        public void onMessage(Envelope envelope) {
                            Log.d(TAG, "receive:ignore " + envelope.toString());
                        }
                    })
                    .receive("ok", new IMessageCallback() {
                        @Override
                        public void onMessage(Envelope envelope) {
                            Log.d(TAG, "receive:ok " + envelope.toString());
                        }
                    });

            mChannel.on(EVENT_SNAPSHOT_TAKEN, new IMessageCallback() {

//                @Override
//                public void onMessage(Envelope envelope) {
//                    System.out.println("NEW MESSAGE: " + envelope.toString());
//                }

                @Override
                public void onMessage(Envelope envelope) {

                    if (isFirstImage) {
                        isFirstImage = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (getActivity() != null) {
                                    getActivity().onFirstJpgLoaded();
                                }
                            }
                        });
                    }
                    Log.d(TAG, "Timestamp: " + envelope.getPayload().get(ENVELOPE_KEY_TIMESTAMP).toString());

                    Log.d(TAG, "Payload: " + envelope.getPayload());
                    String base64String = envelope.getPayload().get(ENVELOPE_KEY_IMAGE).toString();
                    //Log.d(TAG, "Data: " + base64String);

                    byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                    int screenWidth = getActivity().getResources().getDisplayMetrics().widthPixels;
                    final Bitmap bitmap = Commons.decodeBitmapFromResource(decodedString, screenWidth);
                    //Log.d(TAG, "Bitmap size: " +bitmap.getWidth() + " " +  bitmap.getHeight());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity() != null) {
                                getActivity().updateImage(bitmap, mCameraId);
                            }
                        }
                    });
                }

            });
            mChannel.on(ChannelEvent.CLOSE.getPhxEvent(), new IMessageCallback() {
                @Override
                public void onMessage(Envelope envelope) {
                    System.out.println("CLOSED: " + envelope.toString());
                }
            });

            mChannel.on(ChannelEvent.ERROR.getPhxEvent(), new IMessageCallback() {
                @Override
                public void onMessage(Envelope envelope) {
                    System.out.println("ERROR: " + envelope.toString());
                }
            });

//            mChannel.onClose(new IMessageCallback() {
//                @Override
//                public void onMessage(Envelope envelope) {
//                    Log.d(TAG, "Channel Closed");
//                }
//            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "WebSocketError: " + e.toString());
            e.printStackTrace();
        }
    }

    public void disconnect() {
        isFirstImage = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mSocket != null) {
                        mSocket.remove(mChannel);
                        mSocket.disconnect();
                        mSocket = null;
                    }
                    mChannel = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void runOnUiThread(Runnable runnable) {
        mHandler.post(runnable);
    }

    private String getHostWithAuth(String host) {
        Uri.Builder url = Uri.parse(host).buildUpon();
        url.appendQueryParameter("api_key", API.getUserKeyPair()[0]);
        url.appendQueryParameter("api_id", API.getUserKeyPair()[1]);
        return url.build().toString();
    }
}
