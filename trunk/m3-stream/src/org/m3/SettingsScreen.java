package org.m3;

import android.preference.PreferenceActivity;
import android.os.Bundle;

public class SettingsScreen extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Настройки и их разметка загружаются из XML-файла
        addPreferencesFromResource(R.xml.preferences);
    }

}
