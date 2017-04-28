package co.aospa.mandy.utils.server;

import android.app.Activity;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by willi on 14.04.17.
 */

public class MandyApi {

    public interface ApiListener {
        void onReturn(String output, int code);

        void onFailure();
    }

    private static final String DOMAIN = "https://grarak.com/mandy/api";

    private final String mURL;
    private final Activity mActivity;

    private HttpURLConnection mConnection;

    public MandyApi(String path, Activity activity) {
        this(path, "v1", activity);
    }

    public MandyApi(String path, String version, Activity activity) {
        mURL = DOMAIN + "/" + version + "/" + path;
        mActivity = activity;
    }

    public void get(String api, @NonNull ApiListener listener) {
        send(null, "GET", api, listener);
    }

    public void post(String request, ApiListener listener) {
        post(request, null, listener);
    }

    public void post(String request, String api, ApiListener listener) {
        send(request, "POST", api, listener);
    }

    private void send(final String request, final String method, final String api,
                      final ApiListener apiListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mConnection = null;
                try {
                    URL url = new URL(api == null ? mURL : mURL + "?key=" + api);
                    mConnection = (HttpURLConnection) url.openConnection();
                    mConnection.setRequestMethod(method);
                    mConnection.setRequestProperty("Content-Type", "application/json");
                    mConnection.setConnectTimeout(10000);
                    mConnection.setReadTimeout(10000);
                    if (request != null) {
                        mConnection.setFixedLengthStreamingMode(request.length());
                        mConnection.setDoOutput(true);

                        DataOutputStream outputStream = new DataOutputStream(mConnection.getOutputStream());
                        outputStream.writeBytes(request);
                        outputStream.flush();

                        outputStream.close();
                    }

                    final int responseCode = mConnection.getResponseCode();
                    InputStream inputStream =
                            responseCode == HttpURLConnection.HTTP_OK ?
                                    mConnection.getInputStream() : mConnection.getErrorStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    final StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    reader.close();

                    if (apiListener != null) {
                        if (mActivity != null) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    apiListener.onReturn(stringBuilder.toString(), responseCode);
                                }
                            });
                        } else {
                            apiListener.onReturn(stringBuilder.toString(), responseCode);
                        }
                    }
                } catch (IOException ignored) {
                    if (apiListener != null) {
                        if (mActivity != null) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    apiListener.onFailure();
                                }
                            });
                        } else {
                            apiListener.onFailure();
                        }
                    }
                } finally {
                    disconnect();
                }
            }
        }).start();
    }

    public void disconnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mConnection != null) {
                    mConnection.disconnect();
                }
            }
        }).start();
    }

}
