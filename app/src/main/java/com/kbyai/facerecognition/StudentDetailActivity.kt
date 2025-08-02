package com.kbyai.facerecognition

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import android.view.View
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity

class StudentDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // <-- CRITICAL!
        setContentView(R.layout.activity_student_detail)

        val name = intent.getStringExtra("name") ?: ""
        val roll = intent.getStringExtra("roll") ?: ""
        val faceBytes = intent.getByteArrayExtra("face")

        val nameView = findViewById<TextView>(R.id.textStudentName)
        val rollView = findViewById<TextView>(R.id.textStudentRoll)
        val imageView = findViewById<ImageView>(R.id.imageStudentFace)
        val attendanceLayout = findViewById<LinearLayout>(R.id.layoutAttendanceList)

        if (nameView == null || rollView == null || imageView == null || attendanceLayout == null) {
            finish()
            return
        }

        nameView.text = "Name: $name"
        rollView.text = "Roll: $roll"

        if (faceBytes != null && faceBytes.isNotEmpty()) {
            try {
                val bmp = BitmapFactory.decodeByteArray(faceBytes, 0, faceBytes.size)
                imageView.setImageBitmap(bmp)
            } catch (e: Exception) {
                imageView.setImageResource(android.R.drawable.ic_menu_report_image)
            }
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_report_image)
        }

        // Show attendance for each subject
        val subjects = getSubjects()
        val attendance = subjects.map { getAttendanceForSubject(this, it, name) }

        attendanceLayout.removeAllViews()

        for (i in subjects.indices) {
            // Subject Name
            val subjectText = TextView(this).apply {
                text = subjects[i]
                setTextColor(Color.BLACK)
                textSize = 20f // Bigger text
                setTypeface(null, Typeface.BOLD)
            }
            attendanceLayout.addView(subjectText)

            // Attendance Value
            val attendanceText = TextView(this).apply {
                text = "Attendance: ${attendance[i]}"
                setTextColor(Color.DKGRAY)
                textSize = 16f
                setPadding(0, 0, 0, 8)
            }
            attendanceLayout.addView(attendanceText)

            // Divider (except after the last item)
            if (i < subjects.size - 1) {
                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 2
                    ).apply {
                        setMargins(0, 8, 0, 8)
                    }
                    setBackgroundColor(Color.parseColor("#DDDDDD"))
                }
                attendanceLayout.addView(divider)
            }
        }
    }

    private fun getSubjects(): List<String> {
        val prefs = getSharedPreferences("subjects_prefs", Context.MODE_PRIVATE)
        val set = prefs.getStringSet("subjects_list", setOf()) ?: setOf()
        return set.toList().sorted()
    }

    private fun getAttendanceForSubject(context: Context, subject: String, studentName: String): Int {
        val prefs = context.getSharedPreferences("attendance_prefs", Context.MODE_PRIVATE)
        val key = "attendance_${subject}_$studentName"
        return prefs.getInt(key, 0)
    }
}