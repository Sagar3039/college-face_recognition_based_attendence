package com.kbyai.facerecognition;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;

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
        ImageView faceView = (ImageView) convertView.findViewById(R.id.imageFace);
        convertView.findViewById(R.id.buttonDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbManager.deletePerson(DBManager.personList.get(position).name);
                notifyDataSetChanged();
            }
        });

        textName.setText(person.name + " (Attendance: " + person.attendance + ")");
        faceView.setImageBitmap(person.face);

        textName.setOnClickListener(v -> {
            EditText input = new EditText(getContext());
            input.setText(person.name);
            new AlertDialog.Builder(getContext())
                .setTitle("Edit Name")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString();
                    person.name = newName;
                    // Update in DB if needed
                    dbManager.updatePersonName(person, newName);
                    notifyDataSetChanged();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
        // Return the completed view to render on screen
        return convertView;
    }
}