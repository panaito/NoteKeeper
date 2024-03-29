package com.jafriam.notekeeper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import com.jafriam.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.jafriam.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

public class NoteKeeperOpenHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "NoteKeeper.db";
    public static final int DATABASE_VERSION = 2;

    public NoteKeeperOpenHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CourseInfoEntry.SQL_CREATE_TABLE);
        db.execSQL(NoteInfoEntry.SQL_CREATE_TABLE);
        db.execSQL(CourseInfoEntry.SQL_CREATE_INDEX1);
        db.execSQL(NoteInfoEntry.SQL_CREATE_INDEX1);

        DatabaseDataWorker dataWorker = new DatabaseDataWorker(db);
        dataWorker.insertCourses();
        dataWorker.insertSampleNotes();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2){
            db.execSQL(CourseInfoEntry.SQL_CREATE_INDEX1);
            db.execSQL(NoteInfoEntry.SQL_CREATE_INDEX1);
        }
    }
}
