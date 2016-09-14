package com.example.admin.networktest;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int SHOW_RESPONSE = 0;
    private Button sendRequest;
    private Button httpclientsendRequest;
    private TextView responseText;
    private Handler handler = new Handler(){
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case SHOW_RESPONSE:
                    String response = (String)msg.obj;
                    responseText.setText(response);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendRequest = (Button) findViewById(R.id.send_request);
        httpclientsendRequest = (Button) findViewById(R.id.httpclient_send_request);
        responseText = (TextView) findViewById(R.id.response_text);

        sendRequest.setOnClickListener(this);
        httpclientsendRequest.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.send_request)
        {
            sendRequestWithHttpURLConnection();
        }
        if (v.getId() == R.id.httpclient_send_request)
        {
            sendRequestWithHttpClient();
        }
    }

    private void sendRequestWithHttpClient() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet("http://10.0.2.2/get_data.json");
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    if(httpResponse.getStatusLine().getStatusCode() == 200)
                    {
                        HttpEntity entity = httpResponse.getEntity();
                        String response = EntityUtils.toString(entity, "utf-8");
                        //parseXMLWithPull(response);
                        //parseJSONWithJSONObject(response);
                        parseJSONWithGSON(response);

                        Message message = new Message();
                        message.what =SHOW_RESPONSE;
                        //将服务器返回的结果存放在Message中
                        message.obj = response.toString();
                        handler.sendMessage(message);
                    }

                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseJSONWithGSON(String response) {
        Gson gson = new Gson();
        List<App> appList = gson.fromJson(response, new TypeToken<List<App>>() {}.getType());
        for (App app : appList)
        {
            Log.d("MainActivity", "id is " + app.getId());
            Log.d("MainActivity", "name is " + app.getName());
            Log.d("MainActivity", "version is " + app.getVersion());
        }
    }

    private void parseJSONWithJSONObject(String jsonData) {

        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonobject = jsonArray.getJSONObject(i);
                String id = jsonobject.getString("id");
                String name = jsonobject.getString("name");
                String version = jsonobject.getString("version");
                Log.d("MainActivity", "id is " + id);
                Log.d("MainActivity", "name is " + name);
                Log.d("MainActivity", "version is " + version);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseXMLWithPull(String response) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(response));
            int eventType = xmlPullParser.getEventType();
            String id = "";
            String name = "";
            String version = "";
            while (eventType != xmlPullParser.END_DOCUMENT)
            {
                String nodeName = xmlPullParser.getName();
                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                        if("id".equals(nodeName))
                        {
                            try {
                                id = xmlPullParser.nextText();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else if("id".equals(nodeName))
                        {
                            try {
                                name = xmlPullParser.nextText();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else if ("version".equals(nodeName))
                        {
                            try {
                                version = xmlPullParser.nextText();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    //完成解析某个节点
                    case XmlPullParser.END_TAG:
                    {

                        if ("app".equals(nodeName))
                        {
                            Log.d("MainActivity", "id is " + id);
                            Log.d("MainActivity", "name is " + name);
                            Log.d("MainActivity", "version is " + version);
                        }
                        break;
                    }
                    default:
                        break;
                }
                try {
                    eventType = xmlPullParser.next();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

    }

    private void sendRequestWithHttpURLConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try{
                    try {
                        URL url = new URL("http://www.baidu.com");
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(8000);
                        InputStream in = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder response = new StringBuilder();
                        String line = reader.readLine();
                        while (line != null)
                        {
                            response.append(line);
                            line = reader.readLine();

                        }
                        Message message = new Message();
                        message.what = SHOW_RESPONSE;
                        message.obj = response.toString();
                        handler.sendMessage(message);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
                catch (Exception e){
                    e.printStackTrace();

                }finally {
                    if(connection != null)
                    {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}
