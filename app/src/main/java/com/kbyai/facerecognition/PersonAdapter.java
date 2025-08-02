package com.kbyai.facerecognition;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class PersonAdapter extends ArrayAdapter<Person> {

    DBManager dbManager;
    public PersonAdapter(Context context, ArrayList<Person> personList) {
        super(context, 0, personList);

        dbManager = new DBManager(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Person person = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_person, parent, false);
        }

        TextView textName = convertView.findViewById(R.id.textName);
        ImageView faceView = convertView.findViewById(R.id.imageFace);
        Button buttonDelete = convertView.findViewById(R.id.buttonDelete);

        // Set name and face
        textName.setText(person.name + " (Attendance: " + person.attendance + ")");
        faceView.setImageBitmap(person.face);

        // Delete only when clicking the delete button
        buttonDelete.setOnClickListener(view -> {
            dbManager.deletePerson(DBManager.personList.get(position).name);
            notifyDataSetChanged();
        });

        // Open details page when clicking the name
        textName.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), StudentDetailActivity.class);
            intent.putExtra("name", person.name);
            intent.putExtra("roll", person.roll != null ? person.roll : "");
            if (person.face != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                person.face.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] faceBytes = stream.toByteArray();
                intent.putExtra("face", faceBytes);
            }
            getContext().startActivity(intent);
        });

        // Open details page when clicking the image
        faceView.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), StudentDetailActivity.class);
            intent.putExtra("name", person.name);
            intent.putExtra("roll", person.roll != null ? person.roll : "");
            if (person.face != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                person.face.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] faceBytes = stream.toByteArray();
                intent.putExtra("face", faceBytes);
            }
            getContext().startActivity(intent);
        });

        textName.setClickable(true);
        buttonDelete.setClickable(true);

        return convertView;
    }
}