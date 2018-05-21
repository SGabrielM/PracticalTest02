package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;

class Pair {
    public String time;
    public String value;
    public Pair (String time, String value) {
        this.time = time;
        this.value = value;
    }
}


public class ServerThread extends Thread {

    private int port = 0;
    private ServerSocket serverSocket = null;

    private HashMap<String, Pair> data = null;

    public ServerThread(int port) {
        this.port = port;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
        this.data = new HashMap<>();
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public synchronized void setData(String city, String weatherForecastInformation, String time) {
        Pair p = new Pair(time, weatherForecastInformation);
        this.data.put(city, p);
    }

    public synchronized HashMap<String, Pair> getData() {
        return data;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Log.i(Constants.TAG, "[SERVER THREAD] Waiting for a client invocation...");
                Socket socket = serverSocket.accept();
                Log.i(Constants.TAG, "[SERVER THREAD] A connection request was received from " + socket.getInetAddress() + ":" + socket.getLocalPort());

                if (socket == null) {
                    Log.e(Constants.TAG, "[SERVER THREAD] Socket is null!");
                    return;
                }

                BufferedReader bufferedReader = Utilities.getReader(socket);
                PrintWriter printWriter = Utilities.getWriter(socket);
                if (bufferedReader == null || printWriter == null) {
                    Log.e(Constants.TAG, "[SERVER THREAD] Buffered Reader / Print Writer are null!");
                    return;
                }

                String operation = bufferedReader.readLine();
                String key = bufferedReader.readLine();
                String value = bufferedReader.readLine();

                Log.i(Constants.TAG, "[SERVER THREAD] Received data : " + operation + " " + key + " " + value);

                Log.i(Constants.TAG, "[SERVER THREAD] Getting the information from the webservice...");
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet= new HttpGet(Constants.WEB_SERVICE_ADDRESS);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String pageSourceCode = httpClient.execute(httpGet, responseHandler);
                Log.e(Constants.TAG, "[SERVER THREAD] This is the page source : " + pageSourceCode);

                if (pageSourceCode == null) {
                    Log.e(Constants.TAG, "[SERVER THREAD] Error getting the information from the webservice!");
                }

                JSONObject obj = new JSONObject(pageSourceCode);
                String currentTime = obj.getString("currentDateTime");

                Log.e(Constants.TAG, "[SERVER THREAD] This is the time I got : " + currentTime);

                if (operation.compareTo("Put") == 0) {
                    this.setData(key, value, currentTime);
                } else if (operation.compareTo("Get") == 0) {
                    Pair returnVal = this.data.get(key);
                    printWriter.println(returnVal.value);
                    printWriter.flush();
                }

            }
        } catch (ClientProtocolException clientProtocolException) {
            Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + clientProtocolException.getMessage());
            if (Constants.DEBUG) {
                clientProtocolException.printStackTrace();
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } catch (JSONException e) {
            Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + e.getMessage());

        }
    }

    public void stopThread() {
        interrupt();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
        }
    }

}
