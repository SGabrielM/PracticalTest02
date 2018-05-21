package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;

public class ClientThread extends Thread {

    private String address;
    private int port;
    private String key;
    private String value;
    private TextView weatherForecastTextView;
    private String operation;

    private Socket socket;

    public ClientThread(String address, int port, String key, String value, String op, TextView weatherForecastTextView) {
        this.address = address;
        this.port = port;
        this.key = key;
        this.value= value;
        this.weatherForecastTextView = weatherForecastTextView;
        this.operation = op;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            if (socket == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Could not create socket!");
                return;
            }
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            printWriter.println(operation);
            printWriter.flush();
            printWriter.println(key);
            printWriter.flush();
            printWriter.println(value);
            printWriter.flush();

            if (operation.compareTo("Get") == 0) {
                String returnValue;
                while ((returnValue = bufferedReader.readLine()) != null) {
                    final String returnValueFinal = returnValue;
                    weatherForecastTextView.post(new Runnable() {
                       @Override
                        public void run() {
                           weatherForecastTextView.setText(returnValueFinal);
                       }
                    });
                }
            }

        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

}
