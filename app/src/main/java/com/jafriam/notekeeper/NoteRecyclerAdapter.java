package com.jafriam.notekeeper;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jafriam.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.jafriam.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder>{

    private Context mContext;
    private Cursor mCursor;
    private int mCourseTitleColumnIndex;
    private int mNoteTitleColumnIndex;
    private int mNoteIdColumnIndex;

    public NoteRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        populateColumnIndexes();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View itemView = layoutInflater.inflate(R.layout.item_note_list, viewGroup, false);
        return new ViewHolder(itemView);
    }

    public void changeCursor(Cursor cursor){
        if (mCursor != null){
            mCursor.close();
        }

        mCursor = cursor;
        populateColumnIndexes();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        if (!mCursor.isClosed()){
            mCursor.moveToPosition(position);
            String courseId = mCursor.getString(mCourseTitleColumnIndex);
            String noteTitle = mCursor.getString(mNoteTitleColumnIndex);
            int noteId = mCursor.getInt(mNoteIdColumnIndex);

            viewHolder.mCourseTextView.setText(courseId);
            viewHolder.mTitleTextView.setText(noteTitle);
            viewHolder.mId = noteId;
        }
    }

    private void populateColumnIndexes() {
        if (mCursor == null)
            return;

        mCourseTitleColumnIndex = mCursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);
        mNoteTitleColumnIndex = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteIdColumnIndex = mCursor.getColumnIndex(NoteInfoEntry._ID);

    }

    @Override
    public int getItemCount() {

        if (mCursor != null){
            return mCursor.getCount();
        }

        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mCourseTextView;
        public TextView mTitleTextView;
        public int mId;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mCourseTextView = itemView.findViewById(R.id.text_course);
            mTitleTextView = itemView.findViewById(R.id.text_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent noteActivityIntent = new Intent(mContext, NoteActivity.class);
                    noteActivityIntent.putExtra(NoteActivity.NOTE_ID, mId);
                    mContext.startActivity(noteActivityIntent);
                }
            });
        }
    }
}
