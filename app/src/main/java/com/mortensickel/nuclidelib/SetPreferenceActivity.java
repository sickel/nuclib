package com.mortensickel.nuclidelib;

import android.app.Activity;
import android.os.Bundle;
import android.os.*;
import android.preference.*;
import android.preference.PreferenceScreen;

public class SetPreferenceActivity extends Activity
{

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
		
	}
	

}
