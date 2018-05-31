package com.example.adrija.keystrokeanalysis;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import static com.example.adrija.keystrokeanalysis.R.layout.user_consent;

/**
 * Created by Adrija on 19-05-2018.
 */

public class UserConsent extends AppCompatActivity{

    Button grUsAcc,enInSet,selInMet,done;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(user_consent);

        grUsAcc=findViewById(R.id.btnGrant_usage_access_settings);
        enInSet=findViewById(R.id.btnEnable_in_settings);
        selInMet=findViewById(R.id.btnSelect_ip_method);
        done=findViewById(R.id.btnDone1);


        grUsAcc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS), 0);
            }
        });

       enInSet.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS), 0);
                }
        });

       selInMet.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (mgr != null) {
                    mgr.showInputMethodPicker();
                }
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });
    }

}
