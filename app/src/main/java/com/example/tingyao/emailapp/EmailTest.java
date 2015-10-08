package com.example.tingyao.emailapp;


import android.os.Bundle;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;




public class EmailTest extends AppCompatActivity {

    TTSController tts;
    public CommandListener commandListener;
    public Handler commandHandler;
    Context context;
    private String command;

    //UserInterface
    Button sendButton;
    Button voiceCmdButton;
    Button stopButton;
    EditText textCmd;
    TextView tView;


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
        tts = new TTSController(context);

        commandHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.arg1==0){
                    //start keyword
                    commandListener.Search("cmd1",7000);
                    tts.speakThis("Yes?");
                }
                if (msg.arg1==1){
                    command = msg.obj.toString();
                    tView.setText("action for: \n"+command);
                    if(command.equals("check in box")){
                        checkInBox();
                    }
                    else if(command.equals("reply email")){
                        replyEmail();
                    }
                    else if(command.equals("read first email")){
                        ReadFirstEmail();
                    }
                    commandListener.Search("cmd_start",-1);
                }
                if (msg.arg1==2){
                    commandListener.Search("cmd_start",-1);
                }
                return false;
            }
        });

        commandListener = new CommandListener(context, commandHandler);

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

    public void checkInBox(){
        tts.speakThis("You have a new email from Mom");
        //communicate to email server
    }
    public void replyEmail(){
        tts.speakThis("Say terminate when you finish, you can start to speak now");
    }

    public void ReadFirstEmail(){
        tts.speakThis("Mom said, How are you today?");
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
