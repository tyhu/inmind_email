package com.example.tingyao.emailapp;

import android.content.Context;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by tingyao on 10/28/15.
 */
public class EmailNLG {
    TTSController tts;
    public EmailNLG(Context context){
        tts = new TTSController(context);
    }

    public void InformUnread(int num){
        if(num==-1)
            tts.speakThis("Connection Error, please check your internet");
        else if(num==0)
            tts.speakThis("You have no unread email");
        else if(num<6){
            String numStr = String.valueOf(num);
            tts.speakThis("You have "+numStr+", unread email");
        }
        else
            tts.speakThis("You have more than 5 unread email");
    }
    public void SummerizeSend(String[] senderlst){
        List<String> slst = Arrays.asList(senderlst);
        Set<String> unique = new HashSet<String>(slst);
        String nlgText = "";
        int count;
        for (String key : unique) {
            count = Collections.frequency(slst, key);
            nlgText = describeEmailSet(count, key, nlgText);
        }
        tts.speakThis(nlgText);
    }

    public String describeEmailSet(int count, String sender, String currentText){
        String countStr= String.valueOf(count);
        String beV=" is";
        String msgStr="";
        if(count>1) beV=" are";
        if(currentText.equals("")) msgStr=" message";
        return currentText+countStr+msgStr+beV+" from "+sender+",";
    }

    public void speakRaw(String msg){ tts.speakThis(msg); }
}
