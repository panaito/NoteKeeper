package com.jafriam.notekeeper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.jafriam.mynotekeeperprovidercontract.NoteKeeperProviderContract.Courses;
import com.jafriam.mynotekeeperprovidercontract.NoteKeeperProviderContract.Notes;
import com.jafriam.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.jafriam.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;


/**
 * Created by Ogan Belema
 **/

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int NOTES_LOADER = 0;
    public static final int COURSES_LOADER = 1;
    private final String TAG = getClass().getSimpleName();
    public static final String NOTE_ID = "com.jafriam.notekeeper.NOTE_ID";
    public static final int ID_NOT_SET = -1;
    private static final String ORIGINAL_COURSE_ID = "com.jafriam.notekeeper.ORIGINAL_COURSE_ID";
    private static final String ORIGINAL_NOTE_TITLE = "com.jafriam.notekeeper.ORIGINAL_NOTE_TITLE";
    private static final String ORIGINAL_NOTE_TEXT = "com.jafriam.notekeeper.ORIGINAL_NOTE_TEXT";
    private NoteInfo mNoteInfo;
    private Boolean mIsNewNote = false;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private Boolean mIsCanceling = false;
    private int mNewNotePosition;
    private String mOriginalNoteCourseId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;
    private NoteKeeperOpenHelper mNoteKeeperOpenHelper;
    private Cursor mCursor;
    private int mCourseIdColumnIndex;
    private int mNoteTitleColumnIndex;
    private int mNoteTextColumnIndex;
    private long mNoteId;
    private SimpleCursorAdapter mAdapterCourses;
    private boolean mCourseQueryFinished;
    private boolean mNoteQueryFinished;
    private Uri mNoteUri;
    private ModuleStatusView mModuleStatusView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mModuleStatusView = findViewById(R.id.module_status);
        loadModuleStatusValues();

        mNoteKeeperOpenHelper = new NoteKeeperOpenHelper(this);

        mSpinnerCourses = findViewById(R.id.spinner_courses);

        mAdapterCourses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[]{android.R.id.text1}, 0);
        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinnerCourses.setAdapter(mAdapterCourses);

        //loadCourseData();
        getSupportLoaderManager().restartLoader(COURSES_LOADER, null, this);


        readDisplayStateValues();

        if (savedInstanceState != null){
            restoreOriginalValues(savedInstanceState);
        } else {
            saveOriginalNote();
        }

        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        if (mIsNewNote){
            createNewNote();
        } else {
            //loadNoteData();
            getSupportLoaderManager().restartLoader(NOTES_LOADER, null, this);
        }
    }

    private void loadModuleStatusValues() {

        int totalNumberOfModules = 11;
        int completedNumberOfModules = 7;
        boolean[] moduleStatus = new boolean[totalNumberOfModules];

        for (int moduleIndex = 0; moduleIndex < completedNumberOfModules; moduleIndex++){
            moduleStatus[moduleIndex] = true;
        }
        mModuleStatusView.setModuleStatus(moduleStatus);
    }


    private void restoreOriginalValues(Bundle savedInstanceState) {
        mOriginalNoteCourseId = savedInstanceState.getString(ORIGINAL_COURSE_ID);
        mOriginalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_COURSE_ID, mOriginalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
    }

    private void saveOriginalNote() {
        if (mIsNewNote)
            return;

        if (mNoteInfo != null){
            mOriginalNoteCourseId = mNoteInfo.getCourse().getCourseId();
            mOriginalNoteTitle = mNoteInfo.getTitle();
            mOriginalNoteText = mNoteInfo.getText();
        }

    }

    private void createNewNote() {
        AsyncTask<ContentValues, Integer, Uri> task = new AsyncTask<ContentValues, Integer, Uri>() {
            private ProgressBar mProgressBar;

            @Override
            protected void onPreExecute() {
                mProgressBar = findViewById(R.id.progressBar);
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(1);
            }

            @Override
            protected Uri doInBackground(ContentValues... contentValues) {
                ContentValues insertValues = contentValues[0];
                Uri rowUri = getContentResolver().insert(Notes.CONTENT_URI, insertValues);
                simulateLongRunningTask();
                publishProgress(2);
                Log.d(TAG,"doInBackground: "+ Thread.currentThread().getId());

                simulateLongRunningTask();
                publishProgress(3);
                return rowUri;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                int progressValue = values[0];
                mProgressBar.setProgress(progressValue);
            }

            @Override
            protected void onPostExecute(Uri uri) {
                mNoteUri = uri;
                mNoteId = ContentUris.parseId(uri);
                Log.d(TAG,"onPostExecute: "+ Thread.currentThread().getId());
                mProgressBar.setVisibility(View.GONE);
                displaySnackbar(uri);
            }
        };

        ContentValues contentValues = new ContentValues();
        contentValues.put(Notes.COLUMN_COURSE_ID, "");
        contentValues.put(Notes.COLUMN_NOTE_TITLE, "");
        contentValues.put(Notes.COLUMN_NOTE_TEXT, "");

        /*mNoteUri = getContentResolver().insert(Notes.CONTENT_URI, contentValues);
        mNoteId = ContentUris.parseId(mNoteUri);*/
        task.execute(contentValues);
        Log.d(TAG,"Call to execute: "+ Thread.currentThread().getId());
    }

    private void displaySnackbar(Uri uri) {
        Snackbar.make(mTextNoteText, uri.toString(), Snackbar.LENGTH_LONG)
                .show();
    }

    private void simulateLongRunningTask() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCanceling){
            if (mIsNewNote){
                deleteNotFromDatabase();
            } else {
                //storePreviousNoteValues();
            }
        } else {
            saveNote();
        }
    }

    private void deleteNotFromDatabase() {

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);
                getContentResolver().delete(mNoteUri, null, null);
                return null;
            }
        };
        task.execute();

    }

    private void storePreviousNoteValues() {
        mNoteInfo.setCourse(DataManager.getInstance().getCourse(mOriginalNoteCourseId));
        mNoteInfo.setTitle(mOriginalNoteTitle);
        mNoteInfo.setText(mOriginalNoteText);
    }

    private void saveNote() {
        String courseId = selectedCourseId();
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();
        saveNoteToDatabase(courseId, noteTitle, noteText);
    }

    private String selectedCourseId() {
        int selectedPosition = mSpinnerCourses.getSelectedItemPosition();
        Cursor cursor = mAdapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseIdIndex = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        return cursor.getString(courseIdIndex);
    }

    private void saveNoteToDatabase(String noteCourseId, String noteTitle, String noteText){

        final Uri uri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);
        final ContentValues contentValues = new ContentValues();
        contentValues.put(NoteInfoEntry.COLUMN_COURSE_ID, noteCourseId);
        contentValues.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle);
        contentValues.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText);

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                getContentResolver().update(uri, contentValues, null, null);
                return null;
            }
        };
        task.execute();
    }

    private void displayNote() {

        if (!mCursor.isClosed()){
            String courseId = mCursor.getString(mCourseIdColumnIndex);
            String noteTitle = mCursor.getString(mNoteTitleColumnIndex);
            String noteText = mCursor.getString(mNoteTextColumnIndex);


            int courseIndex = getIndexOfCourseId(courseId);
            mSpinnerCourses.setSelection(courseIndex);

            mTextNoteTitle.setText(noteTitle);
            mTextNoteText.setText(noteText);

            CourseEventBroadcastHelper.sendEventBroadcast(this, courseId, "Editing Note");

            mCursor.close();
        }


        /*if (mNoteInfo != null){

            if (mSpinnerCourses.getAdapter() instanceof ArrayAdapter){
                int itemPosition = ((ArrayAdapter) mSpinnerCourses.getAdapter()).getPosition(mNoteInfo.getCourse());
                mSpinnerCourses.setSelection(itemPosition);
            }

            mTextNoteTitle.setText(mNoteInfo.getTitle());

            mTextNoteText.setText(mNoteInfo.getText());
        }*/
    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = mAdapterCourses.getCursor();
        int CourseIdColumnIndex = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);

        int courseRowIndex = 0;

        while (cursor.moveToNext()){
            String cursorCourseId = cursor.getString(CourseIdColumnIndex);
            if (cursorCourseId.equals(courseId)){
                break;
            }

            courseRowIndex++;
        }
        return courseRowIndex;
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();

        if (intent != null && intent.hasExtra(NOTE_ID)){
            mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
            //mNoteInfo = DataManager.getInstance().getNotes().get(mNoteId);
        } else {
            mIsNewNote = true;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;
        item.setEnabled(mNewNotePosition < lastNoteIndex);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendMail();
            return true;
        } else if (id == R.id.action_cancel){
            mIsCanceling = true;
            finish();
            return true;
        } else if (id == R.id.action_next){
            moveNext();
        } else if (id == R.id.action_set_reminder){
            showReminderNotification();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showReminderNotification() {
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();
        int noteId = (int) ContentUris.parseId(mNoteUri);

        Intent intent = new Intent(this, NoteReminderNotification.class);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TITLE, noteTitle);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TEXT, noteText);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_ID, noteId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long currentTimeInMilliseconds = SystemClock.elapsedRealtime();

        //one hour in milliseconds
        long ONE_HOUR = 60 * 60 * 1000;

        long TEN_SECONDS = 10 * 1000;

        long alarmTime = currentTimeInMilliseconds + TEN_SECONDS;

        if (alarmManager != null){
            alarmManager.set(AlarmManager.ELAPSED_REALTIME, alarmTime, pendingIntent);
        }

    }

    private void moveNext() {
        saveNote();

        mNewNotePosition = mNewNotePosition + 1;
        mNoteInfo = DataManager.getInstance().getNotes().get(mNewNotePosition);

        saveOriginalNote();
        displayNote();

        invalidateOptionsMenu();
    }

    private void sendMail() {

        CourseInfo courseInfo = (CourseInfo) mSpinnerCourses.getSelectedItem();

        String subject = mTextNoteTitle.getText().toString();

        String text = "Learning \""+ courseInfo.getTitle() +"\" on Pluralsight\n"
                + mTextNoteText.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNoteKeeperOpenHelper.close();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle bundle) {
        CursorLoader loader = null;
        if (id == NOTES_LOADER){
            loader = createNotesLoader();
        } else if (id == COURSES_LOADER){
            loader = createCourseLoader();
        }
        return loader;
    }

    private CursorLoader createCourseLoader() {
        mCourseQueryFinished = false;
        Uri uri = Courses.CONTENT_URI;

        return new CursorLoader(this, uri, null, null, null,
                Courses.COLUMN_COURSE_TITLE);
    }

    private CursorLoader createNotesLoader() {
        mNoteQueryFinished = false;
        mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);
        return new CursorLoader(this, mNoteUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == NOTES_LOADER){
            loadFinishedNotes(cursor);
        } else if (loader.getId() == COURSES_LOADER){
            loadFinishedCourses(cursor);
        }
    }

    private void loadFinishedCourses(Cursor cursor) {
        mAdapterCourses.changeCursor(cursor);
        mCourseQueryFinished = true;
        displayNoteWhenQueriesFinished();
    }

    private void loadFinishedNotes(Cursor cursor) {
        mCursor = cursor;

        mCourseIdColumnIndex = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitleColumnIndex = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextColumnIndex = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mCursor.moveToNext();
        mNoteQueryFinished = true;
        displayNoteWhenQueriesFinished();
    }

    private void displayNoteWhenQueriesFinished() {
        if (mCourseQueryFinished && mNoteQueryFinished){
            displayNote();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if(loader.getId() == NOTES_LOADER){
            if (mCursor != null){
                mCursor.close();
            }
        } else if (loader.getId() == COURSES_LOADER){
            mAdapterCourses.changeCursor(null);
        }
    }
}
