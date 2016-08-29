package com.mortensickel.nuclidelib;

import android.app.*;
import android.os.*;
import android.widget.EditText;
import android.text.*;
import android.widget.*; 
import android.view.*;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import android.util.*;
import java.util.ArrayList;

public class MainActivity extends Activity 
{
	
	public String DB_PATH = null;
    public final static String DB_NAME = "nuclides.db"; //take note this is in your asset folder
    private static final int DB_VERSION = 1;
    private SQLiteDatabase dbNuclides;
	public Double lowprobCutoff=0.01;
    public int energyround=1;
	ArrayList<String> listItems=new ArrayList<String>();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter<String> adapter;
	// TODO: nuclide search
	// DONE: make strings into resources
	// DONE: better display of result using listview
	// DONE: Formatted strings in listview. 
	// TODO: more info on nuclides
	// TODO: half life cut off
	// TODO: user settable low prob value
	// TODO: user settable rounding
	// TODO: user settable default uncertainty
	// TODO: link to iaea web pages
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		DB_PATH = "/data/data/" + this.getPackageName() + "/databases/";
		try {copyDatabase();}
		catch(IOException e){
			Log.e("tag", getString(R.string.copy_asset_error)+ DB_NAME, e);
			
			Toast.makeText(this, getString(R.string.ioErrorDatabase), Toast.LENGTH_LONG).show();
		}
		EditText etEnergy = (EditText)findViewById(R.id.etEnergy);
		EditText etUncert = (EditText)findViewById(R.id.etUncert);
	    TextWatcher tw = new TextWatcher(){
			public void afterTextChanged(Editable s) {}

			public void beforeTextChanged(CharSequence s, int start,int count, int after) {
			} 

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				calcFromTo();
			}
		
		};
		etEnergy.addTextChangedListener(tw);
		etUncert.addTextChangedListener(tw);
		Spinner spnLocale = (Spinner)findViewById(R.id.SpUncerttype);
		spnLocale.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) { 
					calcFromTo();
				} 

				public void onNothingSelected(AdapterView<?> adapterView) {
					return;
				} 
			}); 
		adapter=new ArrayAdapter<String>(MainActivity.this,R.layout.listitem,listItems)
		{ public View getView(int position, View view, ViewGroup viewGroup)
            {
                View v = super.getView(position, view, viewGroup);
                String n = this.getItem(position);
                ((TextView)v).setText(Html.fromHtml(n));
				return v;
            }};
		ListView lv=(ListView)findViewById(R.id.lvResult);
		lv.setAdapter(adapter);
		dbNuclides=openOrCreateDatabase(DB_NAME,MODE_PRIVATE,null);
    }
	
	private void calcFromTo(){
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
		listItems.clear();
		adapter.notifyDataSetChanged();
		
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
		//Button myBtn = (Button)findViewById(R.id.btSearch);
		//v.requestFocus();
		// TODO : hide keyboard
		float min = retnr(R.id.etFrom);
		float max = retnr(R.id.etTo);
		boolean lowprob=((CheckBox)findViewById(R.id.cbLowProb)).isChecked();
		
		String sql="select distinct line.nuclide,name from line,nuclide where nuclide.longname=line.nuclide and line.energy >="+min+" and line.energy <="+max;
		if(!lowprob){
			sql+=" and prob >= "+lowprobCutoff;
		}
		Cursor c = dbNuclides.rawQuery(sql, null);
		c.moveToFirst();
		listItems.clear();
		adapter.notifyDataSetChanged();
		if (c != null && c.getCount()>0) {
			// Loop through all nuclides
			do {
				String Name = c.getString(0);
				String Line="<b>"+c.getString(1)+":</b><br />\n";
				// fetches all gammalines for the selected nuclide
				// wants emission probabilities as rounded percentages
				//  TODO: use some.kind of prepared statements
				String sql2 = "select energy,round(prob*100,"+energyround+") as prob from line where nuclide='"+Name+"' ";
				if(!lowprob){
					sql2+=" and prob >="+lowprobCutoff;
				}
				sql2 +=" order by prob desc";
				Cursor c2=dbNuclides.rawQuery(sql2,null);
				c2.moveToFirst();
				do{
					String nrgy=c2.getString(0);
					if(c2.getFloat(0) >=min && c2.getFloat(0)<=max){
						nrgy="<b>"+nrgy+"</b>";
					}
					String GammaLine=nrgy+" ("+c2.getString(1)+"%) ";
					Line+=GammaLine;
				}while(c2.moveToNext());
				listItems.add(Line);}
			while(c.moveToNext());
		}
		adapter.notifyDataSetChanged();
    }     
   
    
    /**
     * Copy DB from ASSETS
     */

public void copyDatabase() throws IOException {
	File folder = new File(DB_PATH);
	boolean success = true;
	if (!folder.exists()) {
		success = folder.mkdir();
	}
	if(!success){throw new IOException("could not create folder");}
    File outFile = new File(DB_PATH, DB_NAME);
	// TODO: Check if db is the correct version. copy in if updated.
	if(!outFile.exists()){  // Open your local db as the input stream
        InputStream myInput = this.getAssets().open(DB_NAME);
        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFile);

        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
		Toast.makeText(this, getString(R.string.databaseCopied), Toast.LENGTH_LONG).show();
}
    }

}
