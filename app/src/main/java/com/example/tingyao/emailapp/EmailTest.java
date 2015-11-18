package com.example.tingyao.emailapp;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
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
import java.io.File;
import java.io.FileInputStream;
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
    public BingRecognizer bingRecognizer;
    public Handler commandHandler;
    Context context;
    private String command;

    //UserInterface

    Button voiceCmdButton;
    Button stopButton;
    Button asrButton;

    TextView tView;

    //tmp, should be handled by DM
    MyHttpConnect conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_test);

        //user interface
        asrButton = (Button) findViewById(R.id.asr_test);
        voiceCmdButton = (Button) findViewById(R.id.voice_cmd);
        stopButton = (Button) findViewById(R.id.stop_voice);
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
                else if (msg.arg1==1){
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
                        //ReadFirstEmail();
                        //commandListener.Search("cmd_start",-1);
                        commandListener.Search("contact",-1);
                    }
                    else if(command.equals("summarize them")){
                        Summarize();
                        commandListener.Search("cmd_start",-1);
                    }
                }
                else if (msg.arg1==2){
                    if(CheckUrgentEmail())
                        commandListener.Search("cmd_reply_only",-1);
                    else{
                        System.out.println("no urgent email");
                        //commandListener.Search("cmd_start", -1);
                        commandListener.Search("cmd_start", 10000);
                    }
                }
                else if (msg.arg1==3){
                    tView.setText("terminate");
                    emailNLG.speakRaw("Your email has been sent.");
                    commandListener.Search("cmd_start",-1);
                }
                else if (msg.arg1==4){
                    ReadEmailFrom(msg.obj.toString());
                    commandListener.Search("cmd_start",-1);
                }
                else if (msg.arg1==5){
                    PlayBack();
                }
                else if (msg.arg1==6){
                    commandListener.StopSearch();
                    //emailNLG.speakRaw("You are distracted. May I continue?");
                    emailNLG.speakRaw("You are distracted. System shutting down");
                }
                else if(msg.arg1==7){
                    ReadSchedulingMsg();
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
        conn = new MyHttpConnect("http://128.2.212.80:9000");

        commandListener = new CommandListener(context, commandHandler);
        //commandListener = new AndroidCommandListener(context, commandHandler);
        bingRecognizer = new BingRecognizer("dmeexdia","wNUXY7NvpIw1ugB4zVcUPhVQS6Lv9MFNPWa6qWIkIFY=");



        voiceCmdButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                //commandListener.Search("cmd_start",-1);
                commandListener.Search("cmd_start", 10000);
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                commandListener.StopSearch();

            }
        });
        asrButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try{
                    String asrOutput = bingRecognizer.BingSuperRecognition();
                    tView.setText("output from bingASR: \n"+asrOutput);
                }catch(IOException e){Log.e("EmailTest",e.getMessage());}
            }
        });
        //asrButton.setEnabled(false);
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
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        commandListener.SuperSearch("cmd_final", -1);
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

    public void ReadSchedulingMsg(){
        HashMap<String, String> keyValuePairs = new HashMap<String,String>();
        keyValuePairs.put("Command", "read");
        keyValuePairs.put("MsgId", "schedule");
        String params = conn.SetParams(keyValuePairs);
        String responseStr = PostToServer(params);
        CommandParser parse = new CommandParser(responseStr);
        String emailcontent = parse.GetString("email-content");
        emailNLG.speakRaw(emailcontent);
    }

    public void ReadEmailFrom(String sender){
        HashMap<String, String> keyValuePairs = new HashMap<String,String>();
        keyValuePairs.put("Command", "read");
        keyValuePairs.put("MsgId", "name");
        keyValuePairs.put("Name", sender);
        String params = conn.SetParams(keyValuePairs);
        String responseStr = PostToServer(params);
        CommandParser parse = new CommandParser(responseStr);
        String emailcontent = parse.GetString("email-content");
        emailNLG.speakRaw(emailcontent);
    }

    public boolean CheckUrgentEmail(){
        HashMap<String, String> keyValuePairs = new HashMap<String,String>();
        keyValuePairs.put("Command", "check-urgent");
        String params = conn.SetParams(keyValuePairs);
        String responseStr = PostToServer(params);
        CommandParser parse = new CommandParser(responseStr);
        String emailcontent = parse.GetString("email-content");
        if(emailcontent.equals("none"))
            return false;
        else{
            emailNLG.stateUrgentEmail(emailcontent);
            return true;
        }
    }

    public void PlayBack(){
        AudioTrack trackplay=new AudioTrack(AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,1280, AudioTrack.MODE_STREAM);
        //trackplay.setStereoVolume((float) volume,(float) volume);
        byte[] buffer = new byte[1280];
        int byteread;
        short tmpshort;
        File historyRaw=new File("/sdcard/history.raw");
        //connectAudioToServer();

        try(FileInputStream in = new FileInputStream(historyRaw)){
            trackplay.play();
            while((byteread = in.read(buffer))!=-1 ){

                //amplification
                for(int i = 0;i<640;i++){
                    tmpshort = (short) ((buffer[2*i] << 8) | buffer[2*i+1]);
                    //tmpshort *= 2;
                    buffer[2*i]=(byte) (tmpshort >>> 8);
                    buffer[2*i+1]=(byte) (tmpshort >>> 0);
                }

                trackplay.write(buffer,0,1280);
            }
            trackplay.release();
        }
        catch (Exception ex){}
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
