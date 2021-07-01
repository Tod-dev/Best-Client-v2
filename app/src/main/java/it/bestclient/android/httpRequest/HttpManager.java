package it.bestclient.android.httpRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpManager {
    public static String getData(RequestPackage requestPackage) {

        BufferedReader reader = null;
        String uri = requestPackage.getUrl();

        if (requestPackage.getMethod().equals("GET")) {
            uri += "?" + requestPackage.getEncodedParams();
            //As mentioned before, this only executes if the request method has been
            //set to GET
        }

        try {
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(requestPackage.getMethod());

            if (requestPackage.getMethod().equals("POST")) {
                con.setDoOutput(true);
                OutputStreamWriter writer =
                        new OutputStreamWriter(con.getOutputStream());
                writer.write(requestPackage.getEncodedParams());
                writer.flush();
            }

            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
    /*
    private void requestData() {
        Log.d("REQUEST DATA:","SONO IN REQUEST DATA");
        RequestPackage requestPackage = new RequestPackage();
        requestPackage.setMethod("GET");
        requestPackage.setUrl(uri);

        Downloader downloader = new Downloader(); //Instantiation of the Async task
        //that’s defined below

        downloader.execute(requestPackage);
    }
*/
    /*
    private static class Downloader extends AsyncTask<RequestPackage, String, String> {
        @Override
        protected String doInBackground(RequestPackage... params) {
            return HttpManager.getData(params[0]);
        }

        //The String that is returned in the doInBackground() method is sent to the
        // onPostExecute() method below. The String should contain JSON data.
        @Override
        protected void onPostExecute(String result) {
            try {
                Log.d("DOWNLOADER:","SONO IN DOWNLOADER : "+result);
                //We need to convert the string in result to a JSONObject
                if(result == null) return;
                JSONObject jsonObject = new JSONObject(result);
                Log.d("JSON:",jsonObject.toString());
                //The “ask” value below is a field in the JSON Object that was
                //retrieved from the BitcoinAverage API. It contains the current
                //bitcoin price
                long unixTime = jsonObject.getLong("unixtime");
                unixTime *= 1000; //timestamp in ms
                Log.d("Data:",""+unixTime);
                BigToAvg.update(unixTime);

                //Now we can use the value in the mPriceTextView
                //mPriceTextView.setText(price);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}*/

