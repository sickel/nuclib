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
import android.content.Intent;

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
    
		Intent intent = getIntent();
		String message = intent.getStringExtra(MainActivity.NUCLIDE_SEARCH);
		/*TextView textView = new TextView(this);
		textView.setTextSize(40);
		textView.setText(message);
		ViewGroup layout = (ViewGroup) findViewById(R.id.llHolder);
		layout.addView(textView);*/
		
		adapter=new ArrayAdapter<String>(NuclideSearchActivity.this,R.layout.listitem,listItems)
		{ public View getView(int position, View view, ViewGroup viewGroup)
            {
                TextView v = (TextView)super.getView(position, view, viewGroup);
                String n = this.getItem(position);
                v.setText(Html.fromHtml(n));
				// default not selectable to make it clickable
				v.setTextIsSelectable(true);
				return v;
            }};
		ListView lv=(ListView)findViewById(R.id.lvNuclide);
		lv.setAdapter(adapter);
		if(!message.equals("")){
			querynuclide(message);
		}
		
	}
	public void closeActivity(View v){
		finish();
	}
	
	public void nuclideSearchButton(View v){
		String sql=SQLFromForm();
		runSQL(sql);
	}
	
	private void querynuclide(String nuclide){
		String sql="select * from nuclide where name='"+nuclide+"'";
		runSQL(sql);
	}
	
	
	private void runSQL(String sql){
		Cursor c = dbNuclides.rawQuery(sql, null);
		int probround=2;
		listItems.clear();
		adapter.notifyDataSetChanged();
		if (c != null && c.getCount()>0) {
			c.moveToFirst();
			String linetemplate="<b><sup>%d</sup>%s</b><br />T 1/2: %s <br/>"+getString(R.string.gammaLineProb)+" ";
			do {
				String thalf=MainActivity.formatthalf(c.getDouble(6),getApplicationContext());
				String Line=  String.format(linetemplate,c.getInt(4),c.getString(8),thalf);
				// TODO: fix display of metastables
				String sql2 = "select energy,round(prob*100,"+probround+") as prob from line where nuclide='"+c.getString(1)+"' ";
				sql2 +=" order by prob desc";
				Cursor c2=dbNuclides.rawQuery(sql2,null);
				if (c2 != null && c2.getCount()>0){
				c2.moveToFirst();
				do{
					String nrgy=c2.getString(0);
					String GammaLine=nrgy+" ("+c2.getString(1)+"%) ";
					Line+=GammaLine;
				}while(c2.moveToNext());
				}
				listItems.add(Line);
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
			Element = Element.substring(0,1).toUpperCase() + Element.substring(1).toLowerCase();
			
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
