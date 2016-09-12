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
import java.util.regex.*;
import android.widget.TextView.*;

import android.view.inputmethod.*;

public class NuclideSearchActivity extends Activity
{
	private SQLiteDatabase dbNuclides;
	private static String DB_NAME="nuclides.db";
	private ArrayList<String> listItems=new ArrayList<String>();
	private ArrayAdapter<String> adapter; // to keep data for the listview
	private final String basesql="select longname,element,A,halflife,meta, ref from nuclide ";
	private final int SQL_LONGNAME=0;
	private final int SQL_ELEMENT=1;
	private final int SQL_A=2;
	private final int SQL_THALF=3;
	private final int SQL_META=4;
	private final int SQL_REF=5;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nuclide);
		dbNuclides=openOrCreateDatabase(DB_NAME,MODE_PRIVATE,null);
    
		Intent intent = getIntent();
		String message = intent.getStringExtra(MainActivity.NUCLIDE_SEARCH);	
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
			Pattern p = Pattern.compile("([A-Z][a-z]?)([0-9]+)");
			Matcher m = p.matcher(message);
			m.find();
			String e=m.group(1);
			((TextView)findViewById(R.id.etElement)).setText(e);
			String n=m.group(2);
			((TextView)findViewById(R.id.etMassnumber)).setText(n);
			querynuclide(message);
		}
		OnEditorActionListener oeal=new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					nuclideSearchButton(v);
					return true;
				}
				return false;
			}};
		EditText et=(EditText)findViewById(R.id.etElement);
		et.setOnEditorActionListener(oeal);
		et=(EditText)findViewById(R.id.etMassnumber);
		et.setOnEditorActionListener(oeal);
		
		//MainActivity.loadPrefs(this);
	}
	
	
	
	public void closeActivity(View v){
		finish();
	}
	
	public void nuclideSearchButton(View v){
		String sql=SQLFromForm();
		runSQL(sql);
	}
	
	private void querynuclide(String nuclide){
		String sql=basesql + "where name='"+nuclide+"'";
		runSQL(sql);
	} 
	
	
	private void runSQL(String sql){
		//TODO check if sql already has an order by
		sql=sql+" order by z,n";
		Cursor c = dbNuclides.rawQuery(sql, null);
		int probround=2;
		listItems.clear();
		adapter.notifyDataSetChanged();
		if (c != null && c.getCount()>0) {
			c.moveToFirst();
			String linetemplate="<b><sup>%s</sup>%s</b><br />T<sub>1/2</sub>: %s (%s) <br/>"+getString(R.string.gammaLineProb)+" ";
			do {
				String thalf=MainActivity.formatthalf(c.getDouble(SQL_THALF),getApplicationContext());
				String Line=  String.format(linetemplate,c.getString(SQL_A)+c.getString(SQL_META),c.getString(SQL_ELEMENT),thalf,c.getString(SQL_REF));
				// TODO: fix display of metastables
				String sql2 = "select energy,round(prob*100,"+probround+") as prob from line where nuclide='"+c.getString(SQL_LONGNAME)+"' ";
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
		String sql=basesql;
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
