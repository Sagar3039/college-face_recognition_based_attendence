package com.kbyai.facerecognition

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StudentDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_detail)

        val name = intent.getStringExtra("name")
        val attendance = intent.getIntExtra("attendance", 0)
        val faceBytes = intent.getByteArrayExtra("face")

        findViewById<TextView>(R.id.textStudentName).text = "Name: $name"
        findViewById<TextView>(R.id.textStudentAttendance).text = "Attendance: $attendance"
        if (faceBytes != null) {
            val bmp = BitmapFactory.decodeByteArray(faceBytes, 0, faceBytes.size)
            findViewById<ImageView>(R.id.imageStudentFace).setImageBitmap(bmp)
        }
    }
}