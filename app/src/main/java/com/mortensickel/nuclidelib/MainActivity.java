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
import android.util.*;public class MainActivity extends Activity 
{
	
	public String DB_PATH = null;
    public final static String DB_NAME = "nuclides.db"; //take note this is in your asset folder
    private static final int DB_VERSION = 1;
    private SQLiteDatabase dbNuclides;
	public Double lowprobCutoff=0.01;
    public int energyround=1;
	
	// TODO: nuclide search
	// TODO: make strings into resources
	// TODO: better display of results
	// TODO: more info on nuclides
	// TODO: half life cut off
	// TODO: user settable low prob value
	// TODO: user settable rounding
	// TODO: user settable default uncertainty
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		DB_PATH = "/data/data/" + this.getPackageName() + "/databases/";
		try {copyDatabase();}
		catch(IOException e){
			Log.e("tag", "Failed to copy asset file: " + DB_NAME, e);
			Toast.makeText(this, "io error - could not copy database", Toast.LENGTH_LONG).show();
		}
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
		dbNuclides=openOrCreateDatabase(DB_NAME,MODE_PRIVATE,null);
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
		float min = retnr(R.id.etFrom);
		float max = retnr(R.id.etTo);
		boolean lowprob=((CheckBox)findViewById(R.id.cbLowProb)).isChecked();
		String sql="select distinct nuclide from line where energy >="+min+" and energy <="+max;
		if(!lowprob){
			sql+=" and prob >= "+lowprobCutoff;
		}
		//sql="select distinct nuclide from line";
		//sql="select count(*) from line";
		//Toast.makeText(this, sql, Toast.LENGTH_LONG).show();
		Cursor c = dbNuclides.rawQuery(sql, null);

		//int Column1 = c.getColumnIndex("nuclide");
		//int Column2 = c.getColumnIndex("Field2");
		String Data="";
		// Check if our result was valid.
		c.moveToFirst();
		if (c != null && c.getCount()>0) {
			// Loop through all Results
			do {
				String Name = c.getString(0);
				if(Data.length()>1){
					Data=Data+"\n\n";
				}
				Data =Data+Name+":";
				String sql2 = "select energy,round(prob*100,"+energyround+") as prob from line where nuclide='"+Name+"' ";
				if(!lowprob){
					sql2+=" and prob >="+lowprobCutoff;
				}
				sql2 +=" order by prob desc";
				Cursor c2=dbNuclides.rawQuery(sql2,null);
				c2.moveToFirst();
				do{
					Data=Data+c2.getString(0)+" ("+c2.getString(1)+"%) ";
				}while(c2.moveToNext());
			}while(c.moveToNext());
		}
		EditText etResult=(EditText)findViewById(R.id.etResult);
		etResult.setText(Data);
		//Toast.makeText(this, Data, Toast.LENGTH_LONG).show();
		
    } 
	



    

        
    /* Creates a empty database on the system and rewrites it with your own
     * database.
     * */
 /*  public void createDataBase() throws IOException {

        dbNuclides.getReadableDatabase();

        try {
            copyDatabase();
        } catch (IOException e) {

            throw new Error("Error copying database");

        }
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
		Toast.makeText(this, "database copied", Toast.LENGTH_LONG).show();
}
    }

    /**
     * Opens Database . Method is synhcronized
     * @throws SQLException
     */
 /*   public synchronized void openDatabase() throws SQLException {
        String dbPath = DB_PATH + DB_NAME;
        myDatabase = SQLiteDatabase.openDatabase(dbPath, null,
												 SQLiteDatabase.OPEN_READWRITE);
    }

    /**
     * Closes database. 
     * Method is synchronized for protection
     */
 /*   @Override
    public synchronized void close() {
        if (myDatabase != null) {
            myDatabase.close();
        }
        super.close();
    }


    /**
     * Check if the database exists
     * 
     * @param cntx
     * @return true / false
     */
/*    public boolean checkDatabase(Context cntx) {
        File dbFile = cntx.getDatabasePath(DB_NAME);
//      Log.e("zeus", "Check db returns : " + dbFile.exists());
        return dbFile.exists();

    }*/
}
