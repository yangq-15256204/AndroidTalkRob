package com.example.talkrob;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPRequest extends AsyncTask<String, Void, String> {


    private List<Message> msgList;
    private String result;
    public HTTPRequest(List<Message> msgList) {
        super();
        this.msgList = msgList;
    }

    @Override
    protected String doInBackground(String... strings) {
        result = null;
        try {
            HttpURLConnection conn = null;
            URL url = new URL("https://api.a3rt.recruit-tech.co.jp/talk/v1/smalltalk");
//            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxyhost", 9999));
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);

            String data = "apikey=DZZNrXj7ge6xwt9qPMqBjgAFFSpfjapJ&query=" + strings[0];

            try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                dos.writeBytes(data);
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder buf = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        buf.append(line);
                    }
                    return convertUnicodeToJp(buf.toString());
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "分からん、" +
                "ネット接続ができない";
    }

    @Override
    protected void onPostExecute(String s) {
        if (s != null) {
            msgList.add(new Message(s,Message.TYPE_RECEIVED));
        }
    }

    private static String convertUnicodeToJp(String str) {
        Pattern pattern = Pattern.compile("(\\\\u(\\w{4}))");
        Matcher matcher = pattern.matcher(str);

        while (matcher.find()) {
            String unicodeFull = matcher.group(1); // 匹配出的每个字的unicode，比如\u67e5
            String unicodeNum = matcher.group(2); // 匹配出每个字的数字，比如\u67e5，会匹配出67e5

            char singleChar = (char) Integer.parseInt(unicodeNum, 16);

            str = str.replace(unicodeFull, singleChar + "");
        }
        return str;
    }
}
