package com.example.tingyao.emailapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import static edu.cmu.pocketsphinx.PocketSphinxJNI.Decoder_defaultConfig;
import static edu.cmu.pocketsphinx.PocketSphinxJNI.new_Decoder__SWIG_0;
import static edu.cmu.pocketsphinx.PocketSphinxJNI.new_Decoder__SWIG_1;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;

/**
 * Created by tingyao on 10/21/15.
 */
public class ContinuousDecoder {

    Context context;
    private Config conf;
    private Decoder decoder;

    static {
        System.loadLibrary("pocketsphinx_jni");
    }

    public ContinuousDecoder(Context con){
        context = con;
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(context);
                    File assetDir = assets.syncAssets();
                    setupDecoder(assetDir);
                } catch (IOException e) {
                    Log.e("cmd listen",e.getMessage());
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    System.out.println("exception for command listener");
                } else {
                    System.out.println("after initialization");
                    //switchSearch(KWS_SEARCH);
                }
            }
        }.execute();



    }

    public void setupDecoder(File assetsDir){
        File acousticModel = new File(assetsDir, "en-us-ptm");
        File dictionary = new File(assetsDir, "cmudict-en-us.dict");
        conf = Decoder.defaultConfig();
        conf.setString("-hmm", acousticModel.getPath());
        //conf.setString("-jsgf", "/sdcard/AM/cmd1.gram");
        conf.setString("-dict", dictionary.getPath());
        decoder = new Decoder(conf);
    }
}
