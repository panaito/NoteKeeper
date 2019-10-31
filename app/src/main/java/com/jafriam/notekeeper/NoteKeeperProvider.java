package com.jafriam.notekeeper;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.jafriam.mynotekeeperprovidercontract.NoteKeeperProviderContract;
import com.jafriam.mynotekeeperprovidercontract.NoteKeeperProviderContract.Courses;
import com.jafriam.mynotekeeperprovidercontract.NoteKeeperProviderContract.CoursesIdColumns;
import com.jafriam.mynotekeeperprovidercontract.NoteKeeperProviderContract.Notes;
import com.jafriam.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.jafriam.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

public class NoteKeeperProvider extends ContentProvider {

    private static final String MIME_VENDOR_TYPE = "vnd." +
            NoteKeeperProviderContract.AUTHORITY + ".";
    private NoteKeeperOpenHelper mNoteKeeperOpenHelper;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int COURSES = 0;
    private static final int NOTES = 1;
    private static final int NOTES_EXPANDED = 2;
    private static final int NOTES_ROW = 3;

    static {
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Courses.PATH, COURSES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH, NOTES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH+"/#", NOTES_ROW);
    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase sqLiteDatabase = mNoteKeeperOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        long rowId = -1;
        int numberOfRolesDeleted = 0;

        switch (match){
            case NOTES_ROW:
                String newSelection = NoteInfoEntry._ID +" = ?";
                rowId = ContentUris.parseId(uri);
                String[] newSelectionArgs = new String[]{Long.toString(rowId)};
                numberOfRolesDeleted = sqLiteDatabase.delete(NoteInfoEntry.TABLE_NAME, newSelection,
                        newSelectionArgs);
                break;

            default: throw new UnsupportedOperationException("Operation not supported");
        }
        return numberOfRolesDeleted;
    }

    @Override
    public String getType(Uri uri) {
        String mimeType = null;
        int match = sUriMatcher.match(uri);

        switch (match){
            case COURSES:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Courses.PATH;
                break;

            case NOTES:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE+"/" + MIME_VENDOR_TYPE + Notes.PATH;
                break;

            case NOTES_EXPANDED:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE+ "/" + MIME_VENDOR_TYPE + Notes.PATH_EXPANDED;
                break;

            case NOTES_ROW:
                mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE+ "/" + MIME_VENDOR_TYPE + Notes.PATH;
                break;
        }

        return mimeType;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase sqLiteDatabase = mNoteKeeperOpenHelper.getWritableDatabase();
        long row = -1;
        Uri newRowUri = null;

        int uriMatch = sUriMatcher.match(uri);

        switch (uriMatch){
            case NOTES:
                row = sqLiteDatabase.insert(NoteInfoEntry.TABLE_NAME, null, values);
                newRowUri = ContentUris.withAppendedId(Notes.CONTENT_URI, row);
                break;

            case COURSES:
                row = sqLiteDatabase.insert(CourseInfoEntry.TABLE_NAME, null, values);
                newRowUri = ContentUris.withAppendedId(Courses.CONTENT_URI, row);
                break;


            case NOTES_EXPANDED:
                throw new UnsupportedOperationException("This operation is not supported");
        }

        return newRowUri;
    }

    @Override
    public boolean onCreate() {
        mNoteKeeperOpenHelper = new NoteKeeperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase sqLiteDatabase = mNoteKeeperOpenHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        switch (match){
            case COURSES:
                cursor = sqLiteDatabase.query(CourseInfoEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;

            case NOTES:
                cursor = sqLiteDatabase.query(NoteInfoEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;

            case NOTES_EXPANDED:
                cursor = notesExpandedQuery(sqLiteDatabase, projection, selection, selectionArgs, sortOrder);
                break;

            case NOTES_ROW:
                long rowId = ContentUris.parseId(uri);
                String rowSelection = NoteInfoEntry._ID + " = ?";
                String[] rowSelectionArgs = new String[]{Long.toString(rowId)};
                cursor = sqLiteDatabase.query(NoteInfoEntry.TABLE_NAME, projection, rowSelection,
                        rowSelectionArgs, null, null, null);
                break;
        }

        return cursor;
    }

    private Cursor notesExpandedQuery(SQLiteDatabase sqLiteDatabase, String[] projection, String selection,
                                      String[] selectionArgs, String sortOrder) {
        String[] columns = new String[projection.length];
        for (int index = 0; index < projection.length; index++){
            //if column[index] = projection[index].equals(BaseColumns._ID) do...else
            columns[index] = projection[index].equals(BaseColumns._ID) ||
                    projection[index].equals(CoursesIdColumns.COLUMN_COURSE_ID) ?
                    NoteInfoEntry.getQName(projection[index]) : projection[index];
        }
        String tablesWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " + CourseInfoEntry.TABLE_NAME
                + " ON " + NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + " = "
                + CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);
        return sqLiteDatabase.query(tablesWithJoin, columns, selection, selectionArgs,
                null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        int rowsUpdated = -1;
        SQLiteDatabase sqLiteDatabase = mNoteKeeperOpenHelper.getWritableDatabase();

        switch (match){
            case NOTES_ROW:
                long id = ContentUris.parseId(uri);
                final String newSelection = NoteInfoEntry._ID + " = ?";
                final String[] newSelectionArgs = new String[]{Long.toString(id)};
                rowsUpdated = sqLiteDatabase.update(NoteInfoEntry.TABLE_NAME,
                        values, newSelection, newSelectionArgs);
                break;

            default: throw new UnsupportedOperationException("Operation not supported");
        }

        return rowsUpdated;
    }
}
