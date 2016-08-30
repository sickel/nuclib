package com.mortensickel.nuclidelib;
import android.app.*;
import android.os.*;
import android.database.sqlite.*;
import android.database.Cursor;
import android.widget.TextView;
import android.widget.*;
import android.view.*;
import java.util.ArrayList;
import android.text.*;

public class NuclideSearchActivity extends Activity
{
	private SQLiteDatabase dbNuclides;
	private static String DB_NAME="nuclides.db";
	private ArrayList<String> listItems=new ArrayList<String>();
	private ArrayAdapter<String> adapter; // to keep data for the listview
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nuclide);
		dbNuclides=openOrCreateDatabase(DB_NAME,MODE_PRIVATE,null);
    
	/*	Intent intent = getIntent();
		String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
		TextView textView = new TextView(this);
		textView.setTextSize(40);
		textView.setText(message);

		ViewGroup layout = (ViewGroup) findViewById(R.id.activity_display_message);
		layout.addView(textView);*/
		
		adapter=new ArrayAdapter<String>(NuclideSearchActivity.this,R.layout.listitem,listItems)
		{ public View getView(int position, View view, ViewGroup viewGroup)
            {
                View v = super.getView(position, view, viewGroup);
                String n = this.getItem(position);
                ((TextView)v).setText(Html.fromHtml(n));
				return v;
            }};
		ListView lv=(ListView)findViewById(R.id.lvNuclide);
		lv.setAdapter(adapter);
		
	}
	
	public void nuclideSearchButton(View v){
		String sql=SQLFromForm();
		//Toast.makeText(getApplicationContext(),sql,Toast.LENGTH_LONG).show();
		runSQL(sql);
	}
	
	private void runSQL(String sql){
		Cursor c = dbNuclides.rawQuery(sql, null);
		
		listItems.clear();
		adapter.notifyDataSetChanged();
		if (c != null && c.getCount()>0) {
			c.moveToFirst();
	// Loop through all nuclides
			do {
				String Name = c.getString(1);
				listItems.add(Name);
			}while(c.moveToNext());
			adapter.notifyDataSetChanged();
		}else{
			Toast.makeText(getApplicationContext(),getString(R.string.noDataFound),Toast.LENGTH_LONG).show();
			
		}
	}
	
	private String SQLFromForm(){
		String sql="select * from nuclide";
		String whereinit =" where ";
		String where=whereinit;
		String Element=((TextView)findViewById(R.id.etElement)).getText().toString();
		if (!Element.isEmpty()){
			where+="element=\""+Element+"\"";
			}
		String Massnumber=((TextView)findViewById(R.id.etMassnumber)).getText().toString();
		if(!Massnumber.isEmpty()){
			if(!where.equals(whereinit)){
				where+=" and ";
			}
			where+="A="+Massnumber;
		}
		if(!where.equals(whereinit)){
			sql+=where;
		}
		return sql;
		}
}
