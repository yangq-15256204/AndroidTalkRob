package com.example.talkrob;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestCurl {
    private String TAG = "TalkRob";
    public String TestCurl(String s){
        String[] cmds={"curl","-XPOST", "https://api.a3rt.recruit-tech.co.jp/talk/v1/smalltalk",
                "-F", "apikey=DZZNrXj7ge6xwt9qPMqBjgAFFSpfjapJ", "-F", "query="+s};
        return convertUnicodeToJp(execCurl(cmds).split("\"")[13]);
    }


    public static String execCurl(String[] cmds) {
        ProcessBuilder process = new ProcessBuilder(cmds);
        Process p;
        try {
            p = process.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            return builder.toString();

        } catch (IOException e) {
            System.out.print("error");
            e.printStackTrace();
        }
        return null;

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