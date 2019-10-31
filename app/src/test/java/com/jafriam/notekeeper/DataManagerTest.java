package com.jafriam.notekeeper;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataManagerTest {

    private static DataManager sDataManager;

    @BeforeClass
    public static void classSetup(){
        sDataManager = DataManager.getInstance();
    }

    @Before
    public void setUp(){
        sDataManager.getNotes().clear();
        sDataManager.initializeExampleNotes();
    }

    @Test
    public void createNewNote() {
        CourseInfo courseInfo = sDataManager.getCourse("android_intents");
        String title = "Implicit intents";
        String text = "Implicit intents are those intents that you do not specify the destination. " +
                "The android system looks up applications that can handle the data or type and " +
                "then sends the intent to them.";

        int newNotePosition = sDataManager.createNewNote();
        NoteInfo newNote = sDataManager.getNotes().get(newNotePosition);
        newNote.setCourse(courseInfo);
        newNote.setTitle(title);
        newNote.setText(text);

        NoteInfo compareNote = sDataManager.getNotes().get(newNotePosition);
        assertEquals(courseInfo, compareNote.getCourse());
        assertEquals(title, compareNote.getTitle());
        assertEquals(text, compareNote.getText());
    }

    @Test
    public void findNote() {
        CourseInfo courseInfo = sDataManager.getCourse("android_async");
        String title = "AsyncTask Loader";
        String text1 = "AsyncTask loader handles configuration changes better";
        String text2 = "AsyncTask loader has a reference that you can use to find it after configuration change.";

        int note1Index = sDataManager.createNewNote();
        NoteInfo note1 = sDataManager.getNotes().get(note1Index);
        note1.setCourse(courseInfo);
        note1.setTitle(title);
        note1.setText(text1);

        int note2Index = sDataManager.createNewNote();
        NoteInfo note2 = sDataManager.getNotes().get(note2Index);
        note2.setCourse(courseInfo);
        note2.setTitle(title);
        note2.setText(text2);

        int foundNote1Index = sDataManager.findNote(note1);
        assertEquals(note1Index, foundNote1Index);

        int foundNote2Index = sDataManager.findNote(note2);
        assertEquals(note2Index, foundNote2Index);
    }

    @Test
    public void createNewNoteInOneStep(){
        CourseInfo courseInfo = sDataManager.getCourse("android_async");
        String title = "AsyncTask Loader";
        String text = "AsyncTask loader handles configuration changes better";

        int newNoteIndex = sDataManager.createNewNote(courseInfo, title, text);

        NoteInfo createdNote = sDataManager.getNotes().get(newNoteIndex);

        assertEquals(courseInfo, createdNote.getCourse());
        assertEquals(title, createdNote.getTitle());
        assertEquals(text, createdNote.getText());
    }
}