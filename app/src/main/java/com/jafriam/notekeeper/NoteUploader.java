package com.jafriam.notekeeper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.jafriam.mynotekeeperprovidercontract.NoteKeeperProviderContract;
import com.jafriam.mynotekeeperprovidercontract.NoteKeeperProviderContract.Notes;


/**
 * Created by Belema Ogan on 12/17/2018.
 */

public class NoteUploader {
    private final String TAG = NoteUploader.class.getSimpleName();

    private final Context mContext;
    private boolean mCanceled;

    public NoteUploader(Context context) {
        mContext = context;
    }

    public boolean isCanceled(){ return mCanceled; }

    public void cancel(){
        mCanceled = true;
    }

    public void doUpload(Uri dataUri){
        String[] columns = {
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT
        };

        Cursor cursor = mContext.getContentResolver().query(dataUri, columns, null,
                null, null);
        int courseIdIndex = cursor.getColumnIndex(Notes.COLUMN_COURSE_ID);
        int noteTitleIndex = cursor.getColumnIndex(Notes.COLUMN_NOTE_TITLE);
        int noteTextIndex = cursor.getColumnIndex(Notes.COLUMN_NOTE_TEXT);

        Log.i(TAG, "Upload Started - "+ dataUri);
        mCanceled = false;
        while (!mCanceled && cursor.moveToNext()){
            String courseId = cursor.getString(courseIdIndex);
            String noteTitle = cursor.getString(noteTitleIndex);
            String noteText = cursor.getString(noteTextIndex);

            if (!noteTitle.equals("")){
                Log.i(TAG, "Uploading "+courseId + " | " + noteTitle + " | "+ noteText);
                simulateLongRunningTask();
            }
        }

        if (isCanceled()){
            Log.i(TAG, "Upload has been canceled");
        } else {
            Log.i(TAG, "Upload complete");
        }

        cursor.close();
    }

    private void simulateLongRunningTask() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
