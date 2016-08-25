package com.mortensickel.nuclidelib;

import android.app.*;
import android.os.*;
import android.widget.EditText;
import android.text.*;
import android.widget.*;
import android.view.*;

public class MainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		EditText etEnergy = (EditText)findViewById(R.id.etEnergy);
		EditText etUncert = (EditText)findViewById(R.id.etUncert);
	    TextWatcher tw = new TextWatcher(){
			public void afterTextChanged(Editable s) {}

			public void beforeTextChanged(CharSequence s, int start,
										  int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start,
									  int before, int count) {
			
				float energy = retnr(R.id.etEnergy);
				float uncert = retnr(R.id.etUncert);
				Spinner mySpinner=(Spinner) findViewById(R.id.SpUncerttype);
				String text = mySpinner.getSelectedItem().toString();
				if (text.equals("%")){
					uncert=uncert*energy/100;
				}
				EditText edt = (EditText) findViewById(R.id.etFrom);
				edt.setText(Float.toString(Math.max(0,energy-uncert)));
				edt = (EditText) findViewById(R.id.etTo);
				edt.setText(Float.toString(energy+uncert));			
			}
		
		};
		etEnergy.addTextChangedListener(tw);
		etUncert.addTextChangedListener(tw);
		
    }
	
	Float retnr(Integer r){
		EditText edt = (EditText) findViewById(r);
		String str=edt.getText().toString();
		float retval;
		
		if(str.equals("")){
			retval=Float.valueOf("0.0");
		}else{
			retval=Float.valueOf(str);
		}
		return(retval);
	}
	
	

    public void onClickSearch(View v)
    {
        Toast.makeText(this, "Clicked on Button", Toast.LENGTH_LONG).show();
    } 
	
}
