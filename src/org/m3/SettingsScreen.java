package org.m3;

import android.preference.PreferenceActivity;
import android.os.Bundle;

public class SettingsScreen extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Settings and their markup loads from XML file
        addPreferencesFromResource(R.xml.preferences);
    }
}