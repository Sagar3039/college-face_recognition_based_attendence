package com.kbyai.facerecognition;

import android.graphics.Bitmap;

public class Person {

    public String name;
    public String roll; // Add this line
    public Bitmap face;
    public byte[] templates;
    public int attendance;

    public Person() {

    }

    public Person(String name, String roll, Bitmap face, byte[] templates, int attendance) {
        this.name = name;
        this.roll = roll;
        this.face = face;
        this.templates = templates;
        this.attendance = attendance;
    }
}
