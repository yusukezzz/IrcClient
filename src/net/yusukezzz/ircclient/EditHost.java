package net.yusukezzz.ircclient;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class EditHost extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        String hostname = i.getStringExtra("hostname");
        getPreferenceManager().setSharedPreferencesName(hostname);
        addPreferencesFromResource(R.xml.edithost);
//        setResult(RESULT_OK);
//        finish();
    }
}
