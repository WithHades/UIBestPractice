package example.uibestpractice;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private List<Msg> msgList = new ArrayList<>();

    private EditText inputText;

    private Button send;

    private RecyclerView msgRecyclerView;

    private  MsgAdapter adapter;

    private String message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMsgs();
        inputText = (EditText)findViewById(R.id.input_text);
        send = (Button)findViewById(R.id.send);
        msgRecyclerView = (RecyclerView)findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);
        send.setOnClickListener(this);
    }
    @Override
    public void onClick(View v){
        final String content = inputText.getText().toString();
        Log.d("log",content);
        Msg msg = new Msg(content,Msg.TYPE_SENT);
        msgList.add(msg);
        adapter.notifyItemInserted(msgList.size() - 1);
        msgRecyclerView.scrollToPosition(msgList.size() - 1);
        inputText.setText("");
        new Thread(new Runnable() {
            @Override
            public void run() {
                message = sendPost(content);
                Log.d("log", message);
                Gson gson = new Gson();
                MsgData msgData = gson.fromJson(message,MsgData.class);
                message = msgData.getText();
                mHandler.sendEmptyMessage(0);
            }
        }).start();
    }
    private void initMsgs(){
        Msg msg1 = new Msg("Hello guy",Msg.TYPE_RECEIVED);
        msgList.add(msg1);
    }
    public String sendPost(final String param) {
        String xmString = "";
        try {
            xmString = URLEncoder.encode(param, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL("http://www.niurenqushi.com/api/simsimi/");
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print("txt=" + xmString);
            out.flush();
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            //Log.d("log",result);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("log",e.toString());
        } finally{
            try{
                if(out != null){
                    out.close();
                }
                if(in != null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Msg msg1 = new Msg(message,Msg.TYPE_RECEIVED);
                    msgList.add(msg1);
                    adapter.notifyItemInserted(msgList.size() - 1);
                    msgRecyclerView.scrollToPosition(msgList.size() - 1);
                    break;
                default:
                    break;
            }
        }
    };
}
