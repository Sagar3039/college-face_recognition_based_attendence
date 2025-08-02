package com.kbyai.facerecognition

import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SubjectActivity : AppCompatActivity() {
    private lateinit var subjectAdapter: ArrayAdapter<String>
    private val PREFS_NAME = "subjects_prefs"
    private val SUBJECTS_KEY = "subjects_list"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject)

        val editSubject = findViewById<EditText>(R.id.editSubject)
        val buttonAddSubject = findViewById<Button>(R.id.buttonAddSubject)
        val listSubjects = findViewById<ListView>(R.id.listSubjects)

        // Always use a new list instance for the adapter
        subjectAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList(getSubjects()))
        listSubjects.adapter = subjectAdapter

        buttonAddSubject.setOnClickListener {
            val subject = editSubject.text.toString().trim()
            if (subject.isNotEmpty()) {
                val subjects = getSubjects().toMutableList()
                if (!subjects.contains(subject)) {
                    subjects.add(subject)
                    saveSubjects(subjects)
                    // Reload the list from storage using a new list instance
                    subjectAdapter.clear()
                    subjectAdapter.addAll(ArrayList(getSubjects()))
                    subjectAdapter.notifyDataSetChanged()
                    Toast.makeText(this, "Subject added: $subject", Toast.LENGTH_SHORT).show()
                    editSubject.text.clear()
                } else {
                    Toast.makeText(this, "Subject already exists", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a subject", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getSubjects(): List<String> {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(SUBJECTS_KEY, setOf()) ?: setOf()
        // Always return a new list instance
        return set.toList().sorted()
    }

    private fun saveSubjects(subjects: List<String>) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(SUBJECTS_KEY, HashSet(subjects)).apply()
    }
}