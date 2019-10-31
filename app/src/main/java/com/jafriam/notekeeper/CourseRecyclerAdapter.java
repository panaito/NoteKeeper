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

public class CourseRecyclerAdapter extends RecyclerView.Adapter<CourseRecyclerAdapter.ViewHolder>{

    private Context mContext;
    private Cursor mCursor;
    private int mCourseTitleColumnIndex;
    private int mCourseIdIndex;

    public CourseRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        setUpColumns();
    }

    public void swapCursor(Cursor cursor){
        if (mCursor != null){
            mCursor.close();
        }

        mCursor = cursor;
        setUpColumns();
        notifyDataSetChanged();
    }

    private void setUpColumns() {
        if (mCursor != null){
            mCourseTitleColumnIndex = mCursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);
            mCourseIdIndex = mCursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View itemView = layoutInflater.inflate(R.layout.item_course_list, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        if (mCursor == null)
            return;

        mCursor.moveToPosition(position);

        String courseTitle = mCursor.getString(mCourseTitleColumnIndex);
        String courseId = mCursor.getString(mCourseIdIndex);

        viewHolder.mCourseTextView.setText(courseTitle);
        viewHolder.mCourseId = courseId;
    }

    @Override
    public int getItemCount() {

        if (mCursor != null && !mCursor.isClosed()){
            return mCursor.getCount();
        }

        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mCourseTextView;
        public String mCourseId;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mCourseTextView = itemView.findViewById(R.id.text_course);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent courseNotesIntent = new Intent(mContext, NoteListActivity.class);
                    courseNotesIntent.putExtra(NoteListActivity.COURSE_ID,
                            mCourseId);
                    mContext.startActivity(courseNotesIntent);
                }
            });
        }
    }
}
