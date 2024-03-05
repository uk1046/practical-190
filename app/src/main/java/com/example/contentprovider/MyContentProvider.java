package com.example.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MyContentProvider extends ContentProvider {

    private static final String TAG = MyContentProvider.class.getSimpleName();

    private static final int DATA = 1;
    private static final int DATA_ID = 2;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(MyContract.AUTHORITY, MyContract.PATH_DATA, DATA);
        sUriMatcher.addURI(MyContract.AUTHORITY, MyContract.PATH_DATA + "/#", DATA_ID);
    }

    private MyDbHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new MyDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(MyContract.DataEntry.TABLE_NAME);

        int match = sUriMatcher.match(uri);
        switch (match) {
            case DATA:
                // do nothing
                break;
            case DATA_ID:
                queryBuilder.appendWhere(MyContract.DataEntry.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DATA:
                return MyContract.CONTENT_TYPE;
            case DATA_ID:
                return MyContract.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case DATA:
                long _id = db.insert(MyContract.DataEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = ContentUris.withAppendedId(MyContract.DataEntry.CONTENT_URI, _id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case DATA:
                rowsDeleted = db.delete(MyContract.DataEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case DATA_ID:
                String id = uri.getLastPathSegment();
                if (selection == null || selection.isEmpty()) {
                    rowsDeleted = db.delete(MyContract.DataEntry.TABLE_NAME, MyContract.DataEntry.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(MyContract.DataEntry.TABLE_NAME, MyContract.DataEntry.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case DATA:
                rowsUpdated = db.update(MyContract.DataEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case DATA_ID:
                String id = uri.getLastPathSegment();
                if (selection == null || selection.isEmpty()) {
                    rowsUpdated = db.update(MyContract.DataEntry.TABLE_NAME, values, MyContract.DataEntry.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = db.update(MyContract.DataEntry.TABLE_NAME, values, MyContract.DataEntry.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    private static class MyDbHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "myapp.db";
        private static final int DATABASE_VERSION = 1;

        MyDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            final String SQL_CREATE_DATA_TABLE = "CREATE TABLE " +
                    MyContract.DataEntry.TABLE_NAME + " (" +
                    MyContract.DataEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MyContract.DataEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                    MyContract.DataEntry.COLUMN_VALUE + " TEXT NOT NULL " +
                    ");";

            db.execSQL(SQL_CREATE_DATA_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + MyContract.DataEntry.TABLE_NAME);
            onCreate(db);
        }
    }
}
