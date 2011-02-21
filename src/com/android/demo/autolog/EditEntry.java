
package com.android.demo.autolog;

import java.util.Calendar;

import com.android.autolog.R;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

public class EditEntry extends Activity {

	private TextView mDateDisplay;
	private Button mPickDate;
	private EditText mOdoTxt;
	private EditText mGalTxt;
	private EditText mCostTxt;
	private EditText mOctTxt;
	
    private int mYear;
    private int mMonth;
    private int mDay;
    private long time;
	    
    private Long mRowId;
    
    static final int DATE_DIALOG_ID = 0;
    static final int ALERT_DIALOG_ID = 1;
    
    private AutoDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new AutoDbAdapter(this);
        mDbHelper.open();
        
        setContentView(R.layout.entry_edit);
        setTitle(R.string.edit_entry);

     // capture our View elements
        mDateDisplay = (TextView) findViewById(R.id.dateDisplay);
        mPickDate = (Button) findViewById(R.id.pickDate);
        mOdoTxt = (EditText) findViewById(R.id.odoTxt);
        mGalTxt = (EditText) findViewById(R.id.galTxt);
        mCostTxt = (EditText) findViewById(R.id.costTxt);
        mOctTxt = (EditText) findViewById(R.id.octTxt);
        
        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(AutoDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(AutoDbAdapter.KEY_ROWID)
                                    : null;
        }
        
        populateFields();
        
        // add a click listener to the pick date button
        mPickDate.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });
        // add a click listener to the confirm button
        confirmButton.setOnClickListener(new View.OnClickListener() {

        	public void onClick(View view) {
        	    setResult(RESULT_OK);
        	    finish();
        	}

        });
    }
    
    private void populateFields() {
    	final Calendar c = Calendar.getInstance();
        if (mRowId != null) {
            Cursor entry = mDbHelper.fetchNote(mRowId);
            startManagingCursor(entry);
            
            //set up date from UNIX timestamp
            c.setTimeInMillis(entry.getLong(entry.getColumnIndexOrThrow(AutoDbAdapter.DATE)));
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            time = c.getTimeInMillis();
            // display the current date
            updateDisplay();
            
            mOdoTxt.setText(entry.getString(entry.getColumnIndexOrThrow(AutoDbAdapter.ODO)));
            mGalTxt.setText(entry.getString(entry.getColumnIndexOrThrow(AutoDbAdapter.GAL)));
            mCostTxt.setText(entry.getString(entry.getColumnIndexOrThrow(AutoDbAdapter.COST)));
            mOctTxt.setText(entry.getString(entry.getColumnIndexOrThrow(AutoDbAdapter.OCT))); 
        }else{ //not a saved entry, so we need to set date to current
        	// get the current date
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            time = c.getTimeInMillis();

            // display the current date (this method is below)
            updateDisplay();
        }
    }
    
    private void saveState() {
    	//convert the shitty calendar date to the much cooler UNIX timestamp
        Calendar cal = Calendar.getInstance();
        cal.set(mYear, mMonth, mDay);
        Log.d("Debug", "time1: " + time);
        //convert other values to correct format
        int odo = Integer.parseInt(mOdoTxt.getText().toString());
        double gal = Double.parseDouble(mGalTxt.getText().toString());
        double cost = Double.parseDouble(mCostTxt.getText().toString());
        int oct = Integer.parseInt(mOctTxt.getText().toString());
        
        //Calculate MPG
        double mpg = 0;
//        if(mRowId != null){
//        	Cursor entry = mDbHelper.fetchNote(mRowId);
//            startManagingCursor(entry);
//            
//            //set up date from UNIX timestamp
//            dateStamp = entry.getLong(entry.getColumnIndexOrThrow(AutoDbAdapter.DATE));
//            Log.d("Debug", "dateStamp2: " + dateStamp);
//        }
        Cursor c = mDbHelper.fetchNewestOdo(time);
        startManagingCursor(c);
		if (c != null && c.getCount() > 0) {
			Log.d("Debug", "Count: " +c.getCount());
			int prevOdo = c.getInt(c.getColumnIndexOrThrow(AutoDbAdapter.ODO));
			mpg = (odo - prevOdo) / gal;
		}
        Log.d("Debug", "MPG: " + mpg);
        

        if (mRowId == null) {
            long id = mDbHelper.createNote(time, odo, gal, cost, oct, mpg);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateNote(mRowId, time, odo, gal, cost, oct, mpg);
        }
    }
    
 // updates the date in the TextView
    private void updateDisplay() {
        mDateDisplay.setText(
                new StringBuilder() // Month is 0 based so add 1
                .append(mMonth + 1).append("-").append(mDay).append("-").append(mYear).append(" "));
    }
    
    // the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year,
                        int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDisplay();
                }


            };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
//            case ALERT_DIALOG_ID:
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setMessage("Elements: " + CarLog.stopList.size() + "Date: " + mDateDisplay + "\nOdometer: " + mOdo + "\nGallons: " + mGal + "\nCost: " + mPrice + "\nOcatane: " + mOct)
//                       .setCancelable(false)
//                       .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        });
//                AlertDialog alert = builder.create();
//                return alert;
        }
        return null;
    }
    
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(AutoDbAdapter.KEY_ROWID, mRowId);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }
}
