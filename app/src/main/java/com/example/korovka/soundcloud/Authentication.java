package com.example.korovka.soundcloud;

import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

/**
 * Created by korovka on 2/19/17.
 */

public class Authentication extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.authentication);
    }

    public void auth(View view) {

     //   ApiWrapper wrapper = new ApiWrapper("client_id", "client_secret", null, null);


    }
}