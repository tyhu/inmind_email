package com.example.tingyao.emailapp;

import java.util.HashMap;

/**
 * Created by tingyao on 10/28/15.
 */
public class CommandParser {
    HashMap<String, String> map;
    public CommandParser(String response){
        map = new HashMap<String,String>();
        System.out.println("response: "+response);
        String [] cmds = response.split("\\|");
        for(int i=0;i<cmds.length;i++){
            String cmd = cmds[i];
            System.out.println("cmd: "+cmd);
            if(!cmd.equals(""))
                map.put(cmd.split(":")[0],cmd.split(":")[1]);
        }
    }
    public int GetInt(String key){
        if(map.containsKey(key))
            return Integer.parseInt(map.get(key));
        else
            return -1;
    }
    public String[] GetStringLst(String key){
        System.out.println(map.get(key));
        return map.get(key).split("\\.");
    }
    public String GetString(String key){ return map.get(key); }
}
