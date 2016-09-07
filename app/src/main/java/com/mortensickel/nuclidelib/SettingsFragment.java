package com.mortensickel.nuclidelib;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

	public class SettingsFragment extends PreferenceFragment{
		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
		}
	}
