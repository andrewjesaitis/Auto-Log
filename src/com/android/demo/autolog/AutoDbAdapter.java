package com.android.demo.autolog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class AutoDbAdapter {

    private static final String DATABASE_NAME = "data";
    private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_TABLE = "stopList";
	public static final String KEY_ROWID = "_id";
    public static final String DATE = "date";
    public static final String ODO = "odometer";
    public static final String GAL = "gallons";
    public static final String COST = "cost";
    public static final String OCT = "octane";
    public static final String MPG = "mpg";

    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
    	"CREATE TABLE " + DATABASE_TABLE + " ("
        + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + DATE + " INTEGER,"
        + ODO + " INTEGER,"
        + GAL + " REAL,"
        + COST + " REAL,"
        + OCT + " INTEGER,"
        + MPG + " REAL"
        + ");";



    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public AutoDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public AutoDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param date the date of the stop
     * @param odo Odometer reading at the stop
     * @param gal Number of gallons at the stop
     * @param cost Total cost of the stop
     * @param oct Octane of gas used
     * @param mpg Miles per gallon obtained since last stop
     * @return rowId or -1 if failed
     */
    public long createNote(long date, int odo, double gal, double cost, int oct, double mpg) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(DATE, date);
        initialValues.put(ODO, odo);
        initialValues.put(GAL, gal);
        initialValues.put(COST, cost);
        initialValues.put(OCT, oct);
        initialValues.put(MPG, mpg);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllNotes() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, DATE,
                ODO, GAL, COST, OCT, MPG}, null, null, null, null, DATE + " DESC");
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, DATE,
                    ODO, GAL, COST, OCT, MPG}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    public Cursor fetchNewestOdo(long d){
    	 Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {DATE, ODO}, 
    			 DATE + "<" + d, null, null, null, DATE + " DESC", null);
    	 if (mCursor != null) {
             mCursor.moveToFirst();
         }
         return mCursor;
    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateNote(long rowId, long date, int odo, double gal, double cost, int oct, double mpg) {
        ContentValues args = new ContentValues();
        args.put(DATE, date);
        args.put(ODO, odo);
        args.put(GAL, gal);
        args.put(COST, cost);
        args.put(OCT, oct);
        args.put(MPG, mpg);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
