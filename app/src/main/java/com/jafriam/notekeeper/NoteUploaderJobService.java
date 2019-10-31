package com.jafriam.notekeeper;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.net.Uri;
import android.os.AsyncTask;

public class NoteUploaderJobService extends JobService {

    public final static String EXTRA_DATA_URI = "com.jafriam.notekeeper.EXTRA_DATA_URI";
    private NoteUploader mNoteUploader;

    public NoteUploaderJobService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {

        AsyncTask<JobParameters, Void, Void> task = new AsyncTask<JobParameters, Void, Void>() {
            @Override
            protected Void doInBackground(JobParameters... backgroundParameters) {
                JobParameters jobParameters = backgroundParameters[0];
                String stringDataUri = jobParameters.getExtras().getString(EXTRA_DATA_URI);
                Uri dataUri = Uri.parse(stringDataUri);
                mNoteUploader.doUpload(dataUri);

                if (!mNoteUploader.isCanceled()){
                    //second parameter is whether you want the job to reschedule
                    jobFinished(jobParameters, false);
                }
                return null;
            }
        };

        mNoteUploader = new NoteUploader(this);
        task.execute(params);

        //means job has started running in the background
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        mNoteUploader.cancel();

        //whether you want your job to be rescheduled or not
        return true;
    }
}
