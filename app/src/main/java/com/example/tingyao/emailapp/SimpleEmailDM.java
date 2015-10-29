package com.example.tingyao.emailapp;

import android.os.Handler;

/**
 * Created by tingyao on 10/27/15.
 */
public class SimpleEmailDM {
    private String addr="128.237.136.19:9000";
    private MyHttpConnect httpConn;
    private Handler interfaceHandler;

    //state description
    //focus email
    //Distraction
    //

    public SimpleEmailDM(Handler handler){
        this.interfaceHandler = handler;
        httpConn = new MyHttpConnect(addr);
    }

    public void TakeAction(String sysinput){

    }
}
