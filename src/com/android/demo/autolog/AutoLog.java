/**
 * Car Log
 * 
 * This application keeps track of your gas stops, so you can see if your gas milege is being adversely affected.
 * 
 * This program is licensed under the GNU general public license. Please be respectful of the open source community and support your developers.
 * 
 * @author Andrew Jesaitis
 * @version 0.0.1
 * @date January 10, 2011
 * 
 */
package com.android.demo.autolog;

import java.text.DecimalFormat;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.android.autolog.R;

public class AutoLog extends ListActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private AutoDbAdapter mDbHelper;
    
    private Context mCtx;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_list);
        mDbHelper = new AutoDbAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
        mCtx = this;
    }

    private void fillData() {
        // Get all of the rows from the database and create the item list
        Cursor NotesCursor = mDbHelper.fetchAllNotes();
        startManagingCursor(NotesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{AutoDbAdapter.DATE, AutoDbAdapter.ODO, AutoDbAdapter.GAL, AutoDbAdapter.COST, AutoDbAdapter.MPG};

        // and an array of the fields we want to bind those fields to (in this case just text1)

        int[] to = new int[]{R.id.text1, R.id.text2, R.id.text3, R.id.text4, R.id.text5};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = 
            new SimpleCursorAdapter(this, R.layout.entry_row, NotesCursor, from, to);
        
        //Set up decimal format, so we can round to two places
        final DecimalFormat df = new DecimalFormat("#.##");
        
        //turn the dates back into MM/DD/YYYY from UNIX Epoch
        notes.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            	if(columnIndex == cursor.getColumnIndexOrThrow(AutoDbAdapter.DATE)){
            		((TextView) view).setText(android.text.format.DateUtils.formatDateTime(mCtx, cursor.getLong(columnIndex), android.text.format.DateUtils.FORMAT_NUMERIC_DATE));
            	}else if(columnIndex == cursor.getColumnIndexOrThrow(AutoDbAdapter.ODO)){
            		((TextView) view).setText("Odo: " + cursor.getInt(columnIndex));
            	}else if(columnIndex == cursor.getColumnIndexOrThrow(AutoDbAdapter.GAL)){
            		((TextView) view).setText(df.format(cursor.getDouble(columnIndex))+" gal");
            	}else if(columnIndex == cursor.getColumnIndexOrThrow(AutoDbAdapter.COST)){
            		((TextView) view).setText("$" + df.format(cursor.getDouble(columnIndex)));
            	}else if(columnIndex == cursor.getColumnIndexOrThrow(AutoDbAdapter.MPG)){
            	    ((TextView) view).setText(df.format(cursor.getDouble(columnIndex))+" mpg");	
            	}
            	return true;
            }
        });
        setListAdapter(notes);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert)
        	.setIcon(android.R.drawable.ic_menu_add);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case INSERT_ID:
                createNote();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Item Actions");
        menu.add(0, DELETE_ID, 0, R.string.menu_delete)
        	.setIcon(android.R.drawable.ic_menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteNote(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createNote() {
        Intent i = new Intent(this, EditEntry.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, EditEntry.class);
        i.putExtra(AutoDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
}
