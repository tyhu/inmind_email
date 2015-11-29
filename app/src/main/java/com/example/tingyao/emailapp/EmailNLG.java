package com.example.tingyao.emailapp;

import android.content.Context;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by tingyao on 10/28/15.
 */
public class EmailNLG {
    TTSController tts;
    HashMap<String,String> senderMapT2F;
    HashMap<String,String> senderMapF2T;
    public EmailNLG(Context context){
        tts = new TTSController(context);

        senderMapT2F = new HashMap<String,String>();
        senderMapF2T = new HashMap<String,String>();

        senderMapT2F.put("agent","mom");
        senderMapT2F.put("Ting-Yao","steve");
        senderMapT2F.put("tony","tony");
        senderMapT2F.put("Schedule","Schedule");

        senderMapF2T.put("mom","Agent");
        senderMapF2T.put("steve","Ting-Yao");
        senderMapF2T.put("tony","tony");
        senderMapF2T.put("Schedule","Schedule");
    }

    public void InformUnread(int num){
        if(num==-1)
            tts.speakThis("Connection Error, please check your internet");
        else if(num==0)
            tts.speakThis("You have no unread email");
        else if(num==1)
            tts.speakThis("You have one, unread emails");
        else if(num<6){
            String numStr = String.valueOf(num);
            tts.speakThis("You have "+numStr+", unread emails");
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
        String prep = " from ";
        if(count>1) beV=" are";
        if(currentText.equals("")) msgStr=" message";
        if(sender.equals("Schedule")) prep=" about ";
        return currentText+countStr+msgStr+beV+prep+senderMapT2F.get(sender.split(" ")[0])+",";
    }

    public void stateUrgentEmail(String msg){
        String nlgText="you have an urgent email, ";
        nlgText+=msg;
        tts.speakThis(nlgText);
    }

    public void speakRaw(String msg){ tts.speakThis(msg); }
    public boolean isSpeaking(){ return tts.Speaking(); }

    public String SenderF2T(String fsender){
        return senderMapF2T.get(fsender);
    }
}
