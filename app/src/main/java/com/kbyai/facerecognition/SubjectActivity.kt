package com.kbyai.facerecognition

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SubjectActivity : AppCompatActivity() {
    private val PREFS_NAME = "subjects_prefs"
    private val SUBJECTS_KEY = "subjects_list"

    private lateinit var subjectAdapter: SubjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject)

        val editSubject = findViewById<EditText>(R.id.editSubject)
        val buttonAddSubject = findViewById<Button>(R.id.buttonAddSubject)
        val listSubjects = findViewById<ListView>(R.id.listSubjects)

        val subjects = getSubjects().toMutableList()
        subjectAdapter = SubjectAdapter(this, subjects) { subjectToDelete ->
            subjects.remove(subjectToDelete)
            saveSubjects(subjects)
            subjectAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Deleted: $subjectToDelete", Toast.LENGTH_SHORT).show()
        }
        listSubjects.adapter = subjectAdapter

        buttonAddSubject.setOnClickListener {
            val subject = editSubject.text.toString().trim()
            if (subject.isNotEmpty()) {
                if (!subjects.contains(subject)) {
                    subjects.add(subject)
                    saveSubjects(subjects)
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

class SubjectAdapter(
    context: Context,
    private val subjects: MutableList<String>,
    private val onDelete: (String) -> Unit
) : ArrayAdapter<String>(context, 0, subjects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_subject, parent, false)
        val textSubject = view.findViewById<TextView>(R.id.textSubjectName)
        val buttonDelete = view.findViewById<Button>(R.id.buttonDeleteSubject)

        textSubject.text = subjects[position]
        buttonDelete.setOnClickListener {
            onDelete(subjects[position])
        }
        return view
    }
}