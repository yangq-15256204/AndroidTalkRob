package com.example.talkrob;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private List<Message> msgList = new ArrayList<>();
    private EditText inputText;
    private Button send;
    private RecyclerView msgRecyclerView;
    private MessageAdapter adapter;

    private HTTPRequest tc; // 异步通信
    private String TAG = "TalkRob";

    private String receiveMsgStr;
    private int i = 0;
    private List<String> gojyuList = new ArrayList<String>(Arrays.asList("あ","い","う","え","お","か","き","く","け","こ","さ","し","す","せ","そ","た","ち","つ","て","と","な","に","ぬ","ね","の","は","ひ","ふ","へ","ほ","ま","み","む","め","も","や","ゆ","よ","ら","り","る","れ","ろ","わ","ゐ","を","ん"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMsgs();
        inputText = (EditText) findViewById(R.id.input_text);
        send = (Button) findViewById(R.id.send);
        msgRecyclerView = (RecyclerView) findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = inputText.getText().toString();
                if (!"".equals(content)) {
//                    // 非同期処理タスク
//                    tc = new HTTPRequest(msgList);
//                    tc.execute(content);
//                    // Thread
//                    new curlThread(msgList, content).start();
                    Message sendMsg = new Message(content,Message.TYPE_SENT);
                    msgList.add(sendMsg);
                    adapter.notifyItemInserted(msgList.size()-1);
                    msgRecyclerView.scrollToPosition(msgList.size()-1);
                    inputText.setText("");
                    // 50音メソッド
                    // new gojyuThread(msgList, content).start();
                    if (content.equals(gojyuList.get(i))) {
                        i = i +1;
                        if (i == gojyuList.size()) {
                            msgList.add(new Message("テスト終わりましたよ",Message.TYPE_RECEIVED));
                        }
                        msgList.add(new Message("「" + gojyuList.get(i) +"」を入力してください",Message.TYPE_RECEIVED));
                    } else {
                        msgList.add(new Message("「" + gojyuList.get(i) +"」を入力してください",Message.TYPE_RECEIVED));
                    }
                }
            }
        });
    }

    private void initMsgs() {
        msgList.add(new Message("いらっしゃいませ、ご主人様。ニャー",Message.TYPE_RECEIVED));
        msgList.add(new Message("「あ」を入力してください",Message.TYPE_RECEIVED));
    }

    // Thread
    private class curlThread extends Thread{

        private List<Message> msgList;
        private String sendMsg;
        private curlThread(List<Message> msgList, String msg){
            this.msgList = msgList;
            this.sendMsg = msg;
        }

        @Override
        public void run() {
            System.out.print(Looper.getMainLooper().getThread() == Thread.currentThread());
            String[] cmds={"curl","-XPOST", "https://api.a3rt.recruit-tech.co.jp/talk/v1/smalltalk",
                    "-F", "apikey=DZZNrXj7ge6xwt9qPMqBjgAFFSpfjapJ", "-F", "query="+sendMsg};
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
                receiveMsgStr = convertUnicodeToJp(builder.toString().split("\"")[13]);
                msgList.add(new Message(receiveMsgStr, Message.TYPE_RECEIVED));
            } catch (IOException e) {
                System.out.print("error");
                e.printStackTrace();
            }
        }

        private String convertUnicodeToJp(String str) {
            Pattern pattern = Pattern.compile("(\\\\u(\\w{4}))");
            Matcher matcher = pattern.matcher(str);

            while (matcher.find()) {
                String unicodeFull = matcher.group(1);
                String unicodeNum = matcher.group(2);

                char singleChar = (char) Integer.parseInt(unicodeNum, 16);

                str = str.replace(unicodeFull, singleChar + "");
            }
            return str;
        }
    }
}