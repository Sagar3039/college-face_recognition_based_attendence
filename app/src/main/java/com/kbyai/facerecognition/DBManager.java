package com.kbyai.facerecognition;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class DBManager extends SQLiteOpenHelper {

    public static ArrayList<Person> personList = new ArrayList<>();

    private static final String DATABASE_NAME = "person.db";
    private static final int DATABASE_VERSION = 2; // <-- Change from 1 to 2

    public DBManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE person (" +
            "name TEXT PRIMARY KEY, " +
            "roll TEXT, " + // <-- Add this line
            "face BLOB, " +
            "templates BLOB, " +
            "attendance INTEGER DEFAULT 0)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS person");
        onCreate(db);
    }

    public void insertPerson(String name, String roll, Bitmap face, byte[] templates) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        face.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] faceJpg = byteArrayOutputStream.toByteArray();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("roll", roll); // <-- Add this line
        contentValues.put("face", faceJpg);
        contentValues.put("templates", templates);
        contentValues.put("attendance", 0);
        db.insert("person", null, contentValues);

        personList.add(new Person(name, roll, face, templates, 0));
    }

    public void loadPerson() {
        personList.clear();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM person", null);
        if (res.moveToFirst()) {
            do {
                String name = res.getString(res.getColumnIndex("name"));
                String roll = res.getString(res.getColumnIndex("roll")); // <-- Add this line
                byte[] faceJpg = res.getBlob(res.getColumnIndex("face"));
                byte[] templates = res.getBlob(res.getColumnIndex("templates"));
                int attendance = res.getInt(res.getColumnIndex("attendance"));
                Bitmap face = BitmapFactory.decodeByteArray(faceJpg, 0, faceJpg.length);

                Person person = new Person(name, roll, face, templates, attendance); // <-- Add roll
                personList.add(person);
            } while (res.moveToNext());
        }
        res.close();
    }

    public void deletePerson(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("person", "name=?", new String[]{name});
        for (int i = 0; i < personList.size(); i++) {
            if (personList.get(i).name.equals(name)) {
                personList.remove(i);
                break;
            }
        }
    }

    public void updatePersonName(Person person, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", newName);
        db.update("person", values, "name=?", new String[]{person.name});
        person.name = newName;
    }

    public void increaseAttendance(Person person) {
        SQLiteDatabase db = this.getWritableDatabase();
        person.attendance += 1;
        ContentValues values = new ContentValues();
        values.put("attendance", person.attendance);
        db.update("person", values, "name=?", new String[]{person.name});
    }

    public void clearDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("person", null, null);
        personList.clear();
    }
}