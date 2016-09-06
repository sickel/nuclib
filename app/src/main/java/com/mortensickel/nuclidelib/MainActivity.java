package com.mortensickel.nuclidelib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class MainActivity extends Activity 
{
	
	private String DB_PATH = null;
    private final static String DB_NAME = "nuclides.db";
    private static final int DB_VERSION = 4;
    private SQLiteDatabase dbNuclides;
	private Double lowprobCutoff=0.01;
    private int energyround=1;
	private ArrayList<String> listItems=new ArrayList<String>();
	private ArrayAdapter<String> adapter; // to keep data for the listview
	private final Integer[] timefactors={1,60,3600,24*3600,36524*24*36};
	public static final String NUCLIDE_SEARCH="com.mortensickel.nuclidelib.SEARCH_NUCLIDE";
	// second, minute, hour, day, year
	// DONE: nuclide search
	// DONE: Search button on keyboard
	// DONE: make strings into resources
	// DONE: better display of result using listview
	// DONE: Formatted strings in listview. 
	// TODO: more info on nuclide
	// DONE: framework for info. more info in db? using iaea app?
	// DONE: half life cut off
	// TODO: menu 
	// TODO: user settable low prob value
	// TODO: user settable rounding
	// TODO: user settable default uncertainty
	// TODO: link to iaea web pages
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		DB_PATH = this.getFilesDir().getPath() + "/databases/";
		try {copyDatabase();}
		catch(Exception e){
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
		// to make the search button work as expected
		OnEditorActionListener oeal=new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					onClickSearch(v);
					return true;
				}
				return false;
			}};
		etEnergy.addTextChangedListener(tw);
		etEnergy.setOnEditorActionListener(oeal);
		etUncert.addTextChangedListener(tw);
		etUncert.setOnEditorActionListener(oeal);
		EditText etFrom=(EditText)findViewById(R.id.etFrom);
		etFrom.setOnEditorActionListener(oeal);
		EditText etTo=(EditText)findViewById(R.id.etTo);
		etTo.setOnEditorActionListener(oeal);
		
		Spinner spnLocale = (Spinner)findViewById(R.id.SpUncerttype);
		spnLocale.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) { 
					calcFromTo();
				} 

				public void onNothingSelected(AdapterView<?> adapterView) {

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
		lv.setOnItemClickListener(new OnItemClickListener(){
			
			final Pattern p = Pattern.compile("<b>(.*?)</b>");
			// nuclide is the first thing between b-tags
			@Override
			public void onItemClick(AdapterView<?> parent,View v,int position, long id){
				Intent intent=new Intent(MainActivity.this,NuclideSearchActivity.class);
			    String data=(String)parent.getItemAtPosition(position);
				Matcher m = p.matcher(data);
				m.find();
				data=m.group(1);
				intent.putExtra(NUCLIDE_SEARCH, data);
				startActivity(intent);
			}
		});
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
	
	public void nuclideSearch(View v){
		Intent intent=new Intent(this,NuclideSearchActivity.class);
		intent.putExtra(NUCLIDE_SEARCH, "");
		startActivity(intent);
	}
	
	private Float retnr(Integer r){
		// returns a float from.the edittext - 0 if empty
		EditText edt = (EditText) findViewById(r);
		String str=edt.getText().toString();
		float retval;
		if(str.equals("")){
			retval=Float.valueOf("0.0");
		}else{
			//TODO wrap this in a try
			retval=Float.valueOf(str);
		}
		return(retval);
	}
	
	

    public void onClickSearch(View v)
    {
		//Button myBtn = (Button)findViewById(R.id.btSearch);
		//v.requestFocus();
		// TODO : hide keyboard when searching
		float min = retnr(R.id.etFrom);
		float max = retnr(R.id.etTo);
		float thalf=retnr(R.id.etThalf);
		Spinner mySpinner=(Spinner) findViewById(R.id.spThalf);
		Integer thalftype = mySpinner.getSelectedItemPosition();
		thalf=timefactors[thalftype]*thalf/(24*3600); // halflife in days as in table from iaea
		boolean lowprob=((CheckBox)findViewById(R.id.cbLowProb)).isChecked();
		
		String sql="select distinct line.nuclide,name,halflife from line,nuclide where nuclide.longname=line.nuclide and line.energy >="+min+" and line.energy <="+max;
		if(!lowprob){
			sql+=" and prob >= "+lowprobCutoff;
		}
		if(thalf>0){
			sql+=" and halflife >="+thalf;
		}
		Cursor c = dbNuclides.rawQuery(sql, null);
		c.moveToFirst();
		listItems.clear();
		adapter.notifyDataSetChanged();
		String headformat="<b>%s</b>: (%s)<br />";
		if (c != null && c.getCount()>0) {
			// Loop through all nuclides
			do {
				String Name = c.getString(0);
				String thalfunit=formatthalf(c.getDouble(2),getApplicationContext());
				String Line=String.format(headformat,c.getString(1),thalfunit);
				// fetches all gammalines for the selected nuclide
				// wants emission probabilities as rounded percentages
				//  TODO: use some.kind of prepared statements if available
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
				listItems.add(Line);
                c2.close();}
			while(c.moveToNext());
            c.close();
		}else{
			Toast.makeText(this, getString(R.string.noDataFound), Toast.LENGTH_LONG).show();
			
		}
		adapter.notifyDataSetChanged();
    }     
   
   
public static String formatthalf(Double thalf,Context c){
	String Unit =c.getString(R.string.days);
	// todo: cleaner formatting using the timeconvertarray
	//thalf = c.getFloat(2);
	if(thalf>=365.24){
		thalf=thalf/365.24;
		Unit = c.getString(R.string.years);
	}else if(thalf<1){
		thalf*=24;
		Unit = c.getString(R.string.hours);
		if(thalf<1){
			thalf*=60;
			Unit=c.getString(R.string.minutes);
			if(thalf<1){
				thalf*=60;
				Unit=c.getString(R.string.second);
			}
		}
	}
	return String.format("%.5g %s",thalf, Unit);
} 
	

    /**
     * Copy DB from ASSETS
     */


    private void copyDatabase() throws Exception {
	File folder = new File(DB_PATH);
	boolean success = true;
	if (!folder.exists()) {
		success = folder.mkdir();
	}
	if(!success){throw new IOException(getString(R.string.errCreateFolder));}
    File outFile = new File(DB_PATH, DB_NAME);
	// TODO: Check if db is the correct version. copy in if updated.
	Boolean doCopy=!outFile.exists();
	File dbversion = new File(getFilesDir(), "DBVERSION");
	try {
		if (!dbversion.exists()){
			doCopy=true;
		}else{
			Integer version=readDbVersion(dbversion);
			doCopy=doCopy || (version != DB_VERSION);
		}
			
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
	if(doCopy){  // Open your local db as the input stream
        saveDbVersion(dbversion); 
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

private void saveDbVersion(File dbversion) throws Exception {
	FileOutputStream out = new FileOutputStream(dbversion);
    out.write(Integer.toString(DB_VERSION).getBytes());
    out.close();
}	
	
private Integer readDbVersion(File dbversion) throws Exception{
	RandomAccessFile f = new RandomAccessFile(dbversion, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return Integer.parseInt(new String(bytes));
		//return(1);
	}

}
