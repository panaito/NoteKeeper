package com.jafriam.notekeeper;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.jafriam.mynotekeeperprovidercontract.NoteKeeperProviderContract;
import com.jafriam.mynotekeeperprovidercontract.NoteKeeperProviderContract.Notes;
import com.jafriam.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.jafriam.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final int NOTES_LOADER = 1;
    public static final int NOTE_UPLOADER_JOB_ID = 1;
    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private RecyclerView mRecyclerViewItems;
    private LinearLayoutManager mNoteLayoutManager;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private GridLayoutManager mCoursesLayoutManager;
    private NoteKeeperOpenHelper mNoteKeeperOpenHelper;
    private final static int COURSE_LOADER = 0;
    private Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNoteKeeperOpenHelper = new NoteKeeperOpenHelper(this);

        FloatingActionButton fab =  findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NoteActivity.class));
            }
        });

        //setting the default values for my preferences, passed false so the preference manager does
        //not override the user's input with the default preference
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initializeDisplayContent();

        enableStrictMode();
    }

    private void enableStrictMode() {
        if (BuildConfig.DEBUG){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();

            StrictMode.setThreadPolicy(policy);
        }
    }

    private void initializeDisplayContent() {
        DataManager.loadDataFromDatabase(mNoteKeeperOpenHelper);

        mRecyclerViewItems = findViewById(R.id.list_items);
        mNoteLayoutManager = new LinearLayoutManager(this);
        mCoursesLayoutManager = new GridLayoutManager(this,
                getResources().getInteger(R.integer.course_grid_span));

        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, null);


        mCourseRecyclerAdapter = new CourseRecyclerAdapter(this, null);
        //loadCourses();
        getSupportLoaderManager().restartLoader(COURSE_LOADER, null, this);

        displayNotes();
    }

    private void loadCourses() {
        SQLiteDatabase sqLiteDatabase = mNoteKeeperOpenHelper.getReadableDatabase();
        String[] columns = new String[]{
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry.COLUMN_COURSE_TITLE
        };
        Cursor cursor = sqLiteDatabase.query(CourseInfoEntry.TABLE_NAME, columns, null,
                null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
        mCourseRecyclerAdapter.swapCursor(cursor);
    }

    private void displayNotes() {
        mRecyclerViewItems.setLayoutManager(mNoteLayoutManager);
        mRecyclerViewItems.setAdapter(mNoteRecyclerAdapter);

        selectNavigationMenuItem(R.id.nav_notes);
    }

    private void selectNavigationMenuItem(int id) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        MenuItem item = menu.findItem(id);
        item.setChecked(true);
    }

    private void displayCourses(){
        mRecyclerViewItems.setLayoutManager(mCoursesLayoutManager);
        mRecyclerViewItems.setAdapter(mCourseRecyclerAdapter);

        selectNavigationMenuItem(R.id.nav_courses);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //loadNotes();
        getSupportLoaderManager().restartLoader(NOTES_LOADER, null, this);
        updateNavHeader();

        openDrawer();
    }

    private void openDrawer() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                drawerLayout.openDrawer(Gravity.START);
            }
        }, 1000);

    }

    private void loadNotes() {
        SQLiteDatabase database = mNoteKeeperOpenHelper.getReadableDatabase();
        String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry._ID};
        String orderNoteBy = NoteInfoEntry.COLUMN_COURSE_ID + ", " + NoteInfoEntry.COLUMN_NOTE_TITLE;
        Cursor noteCursor = database.query(NoteInfoEntry.TABLE_NAME, noteColumns, null, null,
                null, null, orderNoteBy);
        mNoteRecyclerAdapter.changeCursor(noteCursor);
    }

    private void updateNavHeader() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View navigationHeader = navigationView.getHeaderView(0);
        TextView usernameTextView = navigationHeader.findViewById(R.id.text_user_name);
        TextView emailTextView = navigationHeader.findViewById(R.id.text_email_address);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String username = preferences.getString("user_display_name", "");
        String emailAddress = preferences.getString("user_email_address", "");

        usernameTextView.setText(username);
        emailTextView.setText(emailAddress);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_back_up){
            doBackupNotes();
        } else if (id == R.id.action_upload_note){
            scheduleNoteUpload();
        }

        return super.onOptionsItemSelected(item);
    }

    private void scheduleNoteUpload() {
        PersistableBundle extras = new PersistableBundle();
        extras.putString(NoteUploaderJobService.EXTRA_DATA_URI, Notes.CONTENT_URI.toString());

        ComponentName componentName = new ComponentName(this, NoteUploaderJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(NOTE_UPLOADER_JOB_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setExtras(extras)
                .build();

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            jobScheduler.schedule(jobInfo);
        }
    }

    private void doBackupNotes() {
        Intent intent = new Intent(this, NoteBackupService.class);
        intent.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);
        startService(intent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_notes) {
            displayNotes();
        } else if (id == R.id.nav_courses) {
            displayCourses();
        } else if (id == R.id.nav_share) {
            handleShare();
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleShare() {
        View view = findViewById(R.id.list_items);
        String favouriteSocial = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("user_favourite_social", "");
        Snackbar.make(view, "Share to "+favouriteSocial, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        mNoteKeeperOpenHelper.close();
        super.onDestroy();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle bundle) {
        Loader<Cursor> cursorLoader = null;

        if (id == COURSE_LOADER){
            cursorLoader = getCourseLoader();
        } else if (id == NOTES_LOADER){
            cursorLoader =  getNoteLoader();
        }

        return cursorLoader;
    }

    private Loader<Cursor> getNoteLoader() {
        String[] noteColumns = {
                Notes.COLUMN_NOTE_TITLE,
                Notes._ID,
                NoteKeeperProviderContract.Courses.COLUMN_COURSE_TITLE};
        String orderNoteBy = NoteKeeperProviderContract.Courses.COLUMN_COURSE_TITLE + ", " + Notes.COLUMN_NOTE_TITLE;

        return new CursorLoader(this, Notes.CONTENT_EXPANDED_URI, noteColumns, null, null, orderNoteBy);
    }

    private Loader<Cursor> getCourseLoader() {
        return new CursorLoader(this){
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase sqLiteDatabase = mNoteKeeperOpenHelper.getReadableDatabase();
                String[] columns = new String[]{
                        CourseInfoEntry.COLUMN_COURSE_ID,
                        CourseInfoEntry.COLUMN_COURSE_TITLE
                };
                return sqLiteDatabase.query(CourseInfoEntry.TABLE_NAME, columns, null,
                        null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == COURSE_LOADER){
            mCursor = cursor;
            mCourseRecyclerAdapter.swapCursor(cursor);
        } else if (loader.getId() == NOTES_LOADER){
            mNoteRecyclerAdapter.changeCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == COURSE_LOADER){
            if (mCursor != null){
                mCursor.close();
            }
        } else if (loader.getId() == NOTES_LOADER){
            mNoteRecyclerAdapter.changeCursor(null);
        }
    }
}
