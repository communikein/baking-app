package it.communikein.bakingapp.data.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {

    private static final String DATASET_URL = "https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json";

    private static final String KEY_SERVER_RESPONSE = "SERVER_RESPONSE";
    public static final String KEY_DATA = "DATA";

    public static URL getRecipesUrl() {
        try {
            return new URL(DATASET_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Bundle getResponseFromHttpUrl(URL url) throws IOException {
        Bundle data = new Bundle();

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        int responseCode = urlConnection.getResponseCode();
        if (responseCode == 200) {
            try {
                InputStream in = urlConnection.getInputStream();

                Scanner scanner = new Scanner(in);
                scanner.useDelimiter("\\A");

                boolean hasInput = scanner.hasNext();
                String response = null;
                if (hasInput) {
                    response = scanner.next();
                }
                scanner.close();

                data.putString(KEY_DATA, response);
            } finally {
                urlConnection.disconnect();
            }
        }
        data.putInt(KEY_SERVER_RESPONSE, responseCode);

        return data;
    }

    public static boolean isDeviceOnline(Context context){
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr != null) {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            return (networkInfo != null && networkInfo.isConnected());
        }
        else
            return false;
    }

}
