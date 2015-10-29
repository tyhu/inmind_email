package com.example.tingyao.emailapp;


import android.os.Bundle;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.HashMap;


public class EmailTest extends AppCompatActivity {

    //public ContinuousDecoder cDecoder;

    EmailNLG emailNLG;
    public CommandListener commandListener;
    //public AndroidCommandListener commandListener;
    public Handler commandHandler;
    Context context;
    private String command;

    //UserInterface
    Button sendButton;
    Button voiceCmdButton;
    Button stopButton;
    EditText textCmd;
    TextView tView;

    //tmp, should be handled by DM
    MyHttpConnect conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_test);

        //user interface
        sendButton = (Button)findViewById(R.id.text_send);
        voiceCmdButton = (Button) findViewById(R.id.voice_cmd);
        stopButton = (Button) findViewById(R.id.stop_voice);
        textCmd = (EditText)findViewById(R.id.cmdtext);
        tView = (TextView)findViewById(R.id.textView);

        context = getApplicationContext();
        emailNLG = new EmailNLG(context);

        //cDecoder = new ContinuousDecoder(context);

        commandHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.arg1==0){
                    //start keyword
                    commandListener.Search("cmd1",7000);
                    emailNLG.speakRaw("Yes?");
                }
                if (msg.arg1==1){
                    command = msg.obj.toString();
                    tView.setText("action for: \n" + command);
                    if(command.equals("check in box") || command.equals("check inbox")){
                        checkInBox();
                        commandListener.Search("cmd_start", -1);
                    }
                    else if(command.equals("reply email")){
                        replyEmail();
                    }
                    else if(command.equals("read the email from")){
                        ReadFirstEmail();
                        commandListener.Search("cmd_start",-1);
                    }
                    else if(command.equals("summarize them")){
                        Summarize();
                        commandListener.Search("cmd_start",-1);
                    }
                }
                if (msg.arg1==2){
                    commandListener.Search("cmd_start",-1);
                }
                if (msg.arg1==3){
                    emailNLG.speakRaw("Your email has been sent.");
                    commandListener.Search("cmd_start",-1);
                }
                return false;
            }
        });

        //tmp, should be handled by DM
        //allow main thread execute network operation
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        conn = new MyHttpConnect("http://128.237.200.161:9000");

        commandListener = new CommandListener(context, commandHandler);
        //commandListener = new AndroidCommandListener(context, commandHandler);

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                command = textCmd.getText().toString();
                tView.setText("action for "+command);
                textCmd.setText("");
            }
        });

        voiceCmdButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                commandListener.Search("cmd_start",-1);
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                commandListener.StopSearch();
            }
        });


    }

    public void Summarize() {
        String responseStr = PostCmd("summarize");
        CommandParser parse = new CommandParser(responseStr);
        String[] senderlst = parse.GetStringLst("Senderlst");
        System.out.println("length: "+senderlst.length);
        emailNLG.SummerizeSend(senderlst);

    }

    public String PostToServer(String params){
        String responseStr = "";
        try{
            HttpURLConnection response = (HttpURLConnection)conn.PostToServer(params);
            System.out.println("connection success!");
            int responseCode = response.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(response.getInputStream()));
                while ((line=br.readLine()) != null) {
                    responseStr+=line;
                }

            }
        }catch (IOException e){
            Log.e("Main","connection error");
        }
        return responseStr;
    }

    public String PostCmd(String cmd){
        HashMap<String, String> keyValuePairs = new HashMap<String,String>();
        keyValuePairs.put("Command", cmd);
        String params = conn.SetParams(keyValuePairs);

        return PostToServer(params);
    }

    public void checkInBox(){
        String responseStr = PostCmd("check-in-box");
        CommandParser parse = new CommandParser(responseStr);
        int msgnum = parse.GetInt("unread_num");
        emailNLG.InformUnread(msgnum);
        //communicate to email server
    }

    public void replyEmail() {
        emailNLG.speakRaw("Say terminate when you finish, you can start to speak now");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        commandListener.Search("cmd_final",-1);
    }

    public void ReadFirstEmail() {
        HashMap<String, String> keyValuePairs = new HashMap<String,String>();
        keyValuePairs.put("Command", "read");
        keyValuePairs.put("MsgId", "first");
        String params = conn.SetParams(keyValuePairs);
        String responseStr = PostToServer(params);
        CommandParser parse = new CommandParser(responseStr);
        String emailcontent = parse.GetString("email-content");
        emailNLG.speakRaw(emailcontent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_email_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
