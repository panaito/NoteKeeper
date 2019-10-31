package com.jafriam.notekeeper;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.jafriam.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

import java.util.List;

/**
 * Created by Ogan Belema
 **/

public class NoteListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int NOTES_LOADER = 1;
    private NoteRecyclerAdapter mNoteRecyclerAdapter;

    public static final String COURSE_ID = "com.jafriam.notekeeper.COURSE_ID";
    private List<NoteInfo> mNoteInfoList;
    private String mCourseId;
    private NoteKeeperOpenHelper mNoteKeeperOpenHelper;

    //private ArrayAdapter<NoteInfo> mAdapterNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNoteKeeperOpenHelper = new NoteKeeperOpenHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab_add_new_note);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NoteListActivity.this, NoteActivity.class));
            }
        });

        initializeDisplayContent();
    }

    private void initializeDisplayContent() {

        final RecyclerView recyclerViewNotes = findViewById(R.id.list_items);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewNotes.setLayoutManager(layoutManager);

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra(COURSE_ID)){
            mCourseId = intent.getStringExtra(COURSE_ID);

            mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, null);
            recyclerViewNotes.setAdapter(mNoteRecyclerAdapter);
            //loadNotesWithId();
            getSupportLoaderManager().restartLoader(NOTES_LOADER, null, this);
        }

    }

    private void loadNotesWithId() {
        String selection = NoteInfoEntry.COLUMN_COURSE_ID+"=?";
        String[] selectionArgs = new String[]{mCourseId};
        String orderBy = NoteInfoEntry.COLUMN_COURSE_ID+", "+NoteInfoEntry.COLUMN_NOTE_TITLE;

        SQLiteDatabase sqLiteDatabase = mNoteKeeperOpenHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(NoteInfoEntry.TABLE_NAME, null, selection,
                selectionArgs, null, null, orderBy);
        mNoteRecyclerAdapter.changeCursor(cursor);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mNoteRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        return new CursorLoader(this){
            @Override
            public Cursor loadInBackground() {
                String selection = NoteInfoEntry.COLUMN_COURSE_ID+"=?";
                String[] selectionArgs = new String[]{mCourseId};
                String orderBy = NoteInfoEntry.COLUMN_COURSE_ID+", "+NoteInfoEntry.COLUMN_NOTE_TITLE;

                SQLiteDatabase sqLiteDatabase = mNoteKeeperOpenHelper.getReadableDatabase();
                return sqLiteDatabase.query(NoteInfoEntry.TABLE_NAME, null, selection,
                        selectionArgs, null, null, orderBy);

            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == NOTES_LOADER){
            mNoteRecyclerAdapter.changeCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == NOTES_LOADER){
            mNoteRecyclerAdapter.changeCursor(null);
        }
    }
}
