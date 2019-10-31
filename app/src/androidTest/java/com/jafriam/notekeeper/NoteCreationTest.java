package com.jafriam.notekeeper;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.AccessibilityChecks;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NoteCreationTest {

    //we need to make sure that everything we need for our test is public

    private static DataManager sDataManager;

    @BeforeClass
    public static void classSetUp(){
        sDataManager = DataManager.getInstance();
    }

    @BeforeClass
    public static void turnOnAccessibility(){
        AccessibilityChecks.enable();
    }

    @Rule
    public ActivityTestRule<NoteListActivity> mActivityTestRule =
            new ActivityTestRule<>(NoteListActivity.class);

    @Test
    public void createNewNote(){

        CourseInfo courseInfo = sDataManager.getCourse("android_intents");
        String title = "Text Note Title";
        String text = "This is the body of my test note";

        /*
         * alternatively we can do this in one line
         * onView(withId(R.id.fab_add_new_note)).perform(click());
         */
        ViewInteraction createNewNoteFab = onView(withId(R.id.fab_add_new_note));
        createNewNoteFab.perform(click());

        //the reason we have to call onView for the spinner is because we have to mimic how we
        //interact with the spinner which is we click the spinner then select the data from the dropdown
        //If it was a listView or recyclerView we won't need onView().perform(click())
        onView(withId(R.id.spinner_courses)).perform(click());
        onData(allOf(instanceOf(CourseInfo.class), equalTo(courseInfo))).perform(click());

        //verifying that what is displayed on the spinner is the course title
        onView(withId(R.id.spinner_courses)).check(matches(withSpinnerText(containsString(courseInfo.getTitle()))));

        onView(withId(R.id.text_note_title)).perform(typeText(title))
                .check(matches(withText(containsString(title))));

        //ViewInteraction.perform() can take multiple actions to perform as its parameter
        onView(withId(R.id.text_note_text)).perform(typeText(text),
                closeSoftKeyboard());

        //instead of calling onView again we can use method chaining to call check after perform
        onView(withId(R.id.text_note_text)).check(matches(withText(containsString(text))));

        pressBack();

        int createdNoteIndex = sDataManager.getNotes().size() - 1;
        NoteInfo createdNote = sDataManager.getNotes().get(createdNoteIndex);
        assertEquals(courseInfo, createdNote.getCourse());
        assertEquals(title, createdNote.getTitle());
        assertEquals(text, createdNote.getText());
    }
}