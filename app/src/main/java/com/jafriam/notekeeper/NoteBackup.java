package com.jafriam.notekeeper;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.jafriam.mynotekeeperprovidercontract.NoteKeeperProviderContract;
import com.jafriam.mynotekeeperprovidercontract.NoteKeeperProviderContract.Notes;

/**
 * Created by Belema Ogan on 12/15/2018.
 */

class NoteBackup {
    public static final String ALL_COURSES = "ALL_COURSES";
    private static final String TAG = NoteBackup.class.getSimpleName();

    public static void doBackup(Context context, String backupCourseId) {

        String[] columns = {
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT
        };

        String selection = null;
        String[] selectionArgs = null;

        if (!backupCourseId.equals(ALL_COURSES)){
            selection = Notes.COLUMN_COURSE_ID + " = ?";
            selectionArgs = new String[]{backupCourseId};
        }

        Cursor cursor = context.getContentResolver().query(Notes.CONTENT_URI, columns, selection,
                selectionArgs, null);

        int courseIdIndex = cursor.getColumnIndex(Notes.COLUMN_COURSE_ID);
        int noteTitleIndex = cursor.getColumnIndex(Notes.COLUMN_NOTE_TITLE);
        int noteTextIndex = cursor.getColumnIndex(Notes.COLUMN_NOTE_TEXT);

        Log.i(TAG, "Backup Begins on thread - "+ Thread.currentThread().getId());
        while (cursor.moveToNext()){
            String courseId = cursor.getString(courseIdIndex);
            String noteTitle = cursor.getString(noteTitleIndex);
            String noteText = cursor.getString(noteTextIndex);

            if (!noteTitle.equals("")){
                Log.i(TAG, "Backup  note: "+ courseId + " | " + noteTitle + " | "+ noteText);
                simulateLongRunningTask();
            }
        }

        Log.i(TAG, "Backup complete");
        cursor.close();
    }

    private static void simulateLongRunningTask() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
