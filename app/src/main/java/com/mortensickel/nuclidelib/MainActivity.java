package com.mortensickel.nuclidelib;

import android.app.*;
import android.os.*;
import android.widget.EditText;
import android.text.*;
import android.widget.*; 
import android.view.*;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.*;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import android.util.*;
import java.util.ArrayList;
import android.view.inputmethod.*;
import android.widget.TextView.*;
import android.content.Intent;
import android.widget.AdapterView.*;
import java.security.*;
import java.util.regex.*;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.content.*;
import android.preference.*;
public class MainActivity extends Activity 
{
	
	public String DB_PATH = null;
    public final static String DB_NAME = "nuclides.db";
    private static final int DB_VERSION = 4;
    private SQLiteDatabase dbNuclides;
	public Double lowprobCutoff=0.01;
    public int energyround=1;
	ArrayList<String> listItems=new ArrayList<>();
	ArrayAdapter<String> adapter; // to keep data for the listview
	private Integer[] timefactors={1,60,3600,24*3600,36524*24*36};
	public static String NUCLIDE_SEARCH="com.mortensickel.nuclidelib.SEARCH_NUCLIDE";
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
	// DONE: link to iaea web pages
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		// String datadir=getApplicationInfo().dataDir;
		DB_PATH = "/data/data/" + this.getPackageName() + "/databases/";
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
		lv.setOnItemClickListener(new OnItemClickListener(){
			
			Pattern p = Pattern.compile("<b>(.*?)</b>");
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
		loadPref();
		  }
		private void loadPref(){
		SharedPreferences shpref=PreferenceManager.getDefaultSharedPreferences(this);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
			
		
		}

		@Override
		public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
		{
			// TODO: Implement this method
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}

		@Override
		protected void onResume()
		{
			// TODO: Implement this method
			super.onResume();
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
	//		lowprobCutoff = (double)sharedPref.getFloat("pref_key_lowprobcutoff", 1)/100;
			readPrefs();
			//XlowprobCutoff=
		}
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}
	
	@Override  
    public boolean onOptionsItemSelected(MenuItem item) {  
        switch (item.getItemId()) {  
            case R.id.mnuLowprob:  
				//Toast.makeText(getApplicationContext(),"Set lowprob",Toast.LENGTH_LONG).show();  
				Intent intent=new Intent();
				intent.setClass(MainActivity.this,SetPreferenceActivity.class);
				startActivityForResult(intent,0);
				return true;     
			case R.id.mnuAbout:  
              //  Toast.makeText(getApplicationContext(),"About",Toast.LENGTH_LONG).show();
				showAbout();
				return true; 
			case R.id.mnuNuclidesearch:
				nuclideSearch(null);
			default:  
                return super.onOptionsItemSelected(item);  
        }  
   
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO: Implement this method
		super.onActivityResult(requestCode, resultCode, data);
		//Toast.makeText(getApplicationContext(),"Back!",Toast.LENGTH_LONG).show();
		readPrefs();
	}  
	
	private void readPrefs(){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String ret=sharedPref.getString("pref_key_lowprobcutoff", "1");
		lowprobCutoff = Double.parseDouble(ret)/100;
		ret=sharedPref.getString("pref_key_rounding","4");
		energyround=Integer.parseInt(ret);
		ret=sharedPref.getString("pref_key_uncert","5");
		EditText et=(EditText)findViewById(R.id.etUncert);
		et.setText(ret);
	}
	

	protected void showAbout() {
        // Inflate the about message contents
        View messageView = getLayoutInflater().inflate(R.layout.about, null, false);

        // When linking text, force to always use default color. This works
        // around a pressed color state bug.
        TextView textView = (TextView) messageView.findViewById(R.id.about_credits);
      //  int defaultColor = textView.getTextColors().getDefaultColor();
       // textView.setTextColor(defaultColor);
	    try{
		PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		String version = this.getText(R.string.versionText).toString();
		version=String.format(version,pInfo.versionName);
		textView=(TextView)messageView.findViewById(R.id.tvVersion);
		textView.setText(version);
		}
		catch(Exception e){
			// really dosn't matter if it fails
		}
		
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.spectrum);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
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
	
	public void onClickList(View v){
		Intent intent=new Intent(this,NuclideSearchActivity.class);
		intent.putExtra(NUCLIDE_SEARCH, "Cs137");
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
		sql+=" order by z,a";
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
						// marks the found lines
						nrgy="<b>"+nrgy+"</b>";
					}
					String GammaLine=nrgy+" ("+c2.getString(1)+"%) ";
					Line+=GammaLine;
				}while(c2.moveToNext());
				listItems.add(Line);}
			while(c.moveToNext());
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
	return String.format("%.4g %s",thalf, Unit);
} 
	

    /**
     * Copy DB from ASSETS
     */
	


public void copyDatabase() throws Exception {
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

void saveDbVersion(File dbversion) throws Exception {
	FileOutputStream out = new FileOutputStream(dbversion);
    out.write(Integer.toString(DB_VERSION).getBytes());
    out.close();
}	
	
Integer readDbVersion(File dbversion) throws Exception{
	RandomAccessFile f = new RandomAccessFile(dbversion, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return Integer.parseInt(new String(bytes));
		//return(1);
	}


	
	

	
	}
