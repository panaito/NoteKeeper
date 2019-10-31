package com.jafriam.notekeeper;

import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NextThroughNotesTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void nextThroughNotes(){
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_notes));

        onView(withId(R.id.list_items)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        List<NoteInfo> notes = DataManager.getInstance().getNotes();

        //single note text
        /*int position = 0;
        NoteInfo noteInfo = notes.get(position);

        onView(withId(R.id.spinner_courses)).check(
                matches(withSpinnerText(noteInfo.getCourse().getTitle())));
        onView(withId(R.id.text_note_title)).check(matches(withText(noteInfo.getTitle())));
        onView(withId(R.id.text_note_text)).check(matches(withText(noteInfo.getText())));*/

        //multiple note test
        for (int position = 0; position < notes.size(); position++){
            NoteInfo noteInfo = notes.get(position);

            onView(withId(R.id.spinner_courses))
                    .check(matches(withSpinnerText(noteInfo.getCourse().getTitle())));

            onView(withId(R.id.text_note_title)).check(matches(withText(noteInfo.getTitle())));
            onView(withId(R.id.text_note_text)).check(matches(withText(noteInfo.getText())));

            if (position < notes.size() - 1) {
                onView(allOf(withId(R.id.action_next), isEnabled())).perform(click());
            }
        }

        onView(withId(R.id.action_next)).check(matches(not(isEnabled())));
        pressBack();
    }
}