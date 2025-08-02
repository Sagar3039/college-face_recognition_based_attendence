package com.kbyai.facerecognition

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Camera
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.kbyai.facesdk.FaceBox
import com.kbyai.facesdk.FaceDetectionParam
import com.kbyai.facesdk.FaceSDK
import java.io.File
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    companion object {
        private val SELECT_PHOTO_REQUEST_CODE = 1
        private val CAPTURE_PHOTO_REQUEST_CODE = 2
    }

    private var cameraImageUri: Uri? = null

    private lateinit var dbManager: DBManager
    private lateinit var textWarning: TextView
    private lateinit var personAdapter: PersonAdapter
    private lateinit var editSearch: EditText
    private var allPersons: ArrayList<Person> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textWarning = findViewById<TextView>(R.id.textWarning)
        val textTotalStudents = findViewById<TextView>(R.id.textTotalStudents) // Add this line

        var ret = FaceSDK.setActivation(
            "S18+rOL1H3BXjAWGP7gEdgbJVotQ4g1o+YMcZruzEaKWFUQJHB2P1ylgw1FAfi+enDQA3nE4E9h6\n" +
                    "NF6xL8uRrs33P9vekwdJCBLlIPcx+keHdNiFjq/3848TZjgMeJ3Xpvh1grWIh9kdGbEfnh6x0/xI\n" +
                    "eCRCuxDn3Za5bRneYyKuUnmt2DGUx9ipZXZawZRT1kob9WxqABMMymYvCFpJMn6XVTZoRU2kRBxM\n" +
                    "ZbMHN43Hu8HePUIPe01ytEGzEx7y0wRL3w794FpPQwAUepimUfifhSOhdx56SIwy4N0HZtGCNVaS\n" +
                    "ZhP4SRsAKRbpmIXZ43daLCo4QKx1Kjh8IOrwHg=="
        )

        if (ret == FaceSDK.SDK_SUCCESS) {
            ret = FaceSDK.init(assets)
        }

        if (ret != FaceSDK.SDK_SUCCESS) {
            textWarning.setVisibility(View.VISIBLE)
            if (ret == FaceSDK.SDK_LICENSE_KEY_ERROR) {
                textWarning.setText("Invalid license!")
            } else if (ret == FaceSDK.SDK_LICENSE_APPID_ERROR) {
                textWarning.setText("Invalid error!")
            } else if (ret == FaceSDK.SDK_LICENSE_EXPIRED) {
                textWarning.setText("License expired!")
            } else if (ret == FaceSDK.SDK_NO_ACTIVATED) {
                textWarning.setText("No activated!")
            } else if (ret == FaceSDK.SDK_INIT_ERROR) {
                textWarning.setText("Init error!")
            }
        }

        dbManager = DBManager(this)
        dbManager.loadPerson()
        allPersons = ArrayList(DBManager.personList)

        // Update total students count
        textTotalStudents.text = "Total Students: ${DBManager.personList.size}"

        personAdapter = PersonAdapter(this, DBManager.personList)
        val listView: ListView = findViewById<View>(R.id.listPerson) as ListView
        listView.setAdapter(personAdapter)

        val textBrand = findViewById<TextView>(R.id.textBrand)
        val editSearch = findViewById<EditText>(R.id.editSearch)

        textBrand.setOnClickListener {
            textBrand.visibility = View.GONE
            editSearch.visibility = View.VISIBLE
            editSearch.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(editSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }

        // Hide search bar when it loses focus
        editSearch.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                editSearch.visibility = View.GONE
                textBrand.visibility = View.VISIBLE
                editSearch.setText("") // Optional: clear search text
                // Optionally reset the list
                personAdapter.clear()
                personAdapter.addAll(allPersons)
                personAdapter.notifyDataSetChanged()
            }
        }

        // Search logic
        editSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s.toString().trim()
                val filtered = if (query.isEmpty()) {
                    allPersons
                } else {
                    ArrayList(allPersons.filter { it.name.contains(query, ignoreCase = true) })
                }
                personAdapter.clear()
                personAdapter.addAll(filtered)
                personAdapter.notifyDataSetChanged()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        findViewById<Button>(R.id.buttonEnroll).setOnClickListener {
            val options = arrayOf("Gallery", "Camera")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select Image Source")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent()
                        intent.setType("image/*")
                        intent.setAction(Intent.ACTION_PICK)
                        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), SELECT_PHOTO_REQUEST_CODE)
                    }
                    1 -> {
                        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                        val imageFile = File.createTempFile("person_", ".jpg", cacheDir)
                        cameraImageUri = FileProvider.getUriForFile(this, "${packageName}.provider", imageFile)
                        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, cameraImageUri)
                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        startActivityForResult(intent, CAPTURE_PHOTO_REQUEST_CODE)
                    }
                }
            }
            builder.show()
        }

        findViewById<Button>(R.id.buttonIdentify).setOnClickListener {
            startActivity(Intent(this, CameraActivityKt::class.java))
        }

        findViewById<Button>(R.id.buttonSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<Button>(R.id.buttonAbout).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
        findViewById<Button>(R.id.buttonSubject).setOnClickListener {
            startActivity(Intent(this, SubjectActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        personAdapter.notifyDataSetChanged()
        findViewById<TextView>(R.id.textTotalStudents).text = "Total Students: ${DBManager.personList.size}"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == SELECT_PHOTO_REQUEST_CODE || requestCode == CAPTURE_PHOTO_REQUEST_CODE) && resultCode == RESULT_OK) {
            try {
                val imageUri = when (requestCode) {
                    SELECT_PHOTO_REQUEST_CODE -> data?.data
                    CAPTURE_PHOTO_REQUEST_CODE -> cameraImageUri
                    else -> null
                }
                if (imageUri == null) {
                    Toast.makeText(this, "Image not found!", Toast.LENGTH_SHORT).show()
                    return
                }

                val bitmap: Bitmap? = try {
                    Utils.getCorrectlyOrientedImage(this, imageUri)
                } catch (e: Exception) {
                    null
                }

                if (bitmap == null) {
                    Toast.makeText(this, "Failed to load image!", Toast.LENGTH_SHORT).show()
                    return
                }

                val faceDetectionParam = FaceDetectionParam()
                faceDetectionParam.check_liveness = true
                faceDetectionParam.check_liveness_level = SettingsActivity.getLivenessLevel(this)
                val faceBoxes: List<FaceBox>? = FaceSDK.faceDetection(bitmap, faceDetectionParam)

                if (faceBoxes.isNullOrEmpty()) {
                    Toast.makeText(this, getString(R.string.no_face_detected), Toast.LENGTH_SHORT).show()
                } else if (faceBoxes.size > 1) {
                    Toast.makeText(this, getString(R.string.multiple_face_detected), Toast.LENGTH_SHORT).show()
                } else if (faceBoxes.size == 1) {
                    val faceImage = Utils.cropFace(bitmap, faceBoxes[0])
                    val templates = FaceSDK.templateExtraction(bitmap, faceBoxes[0])

                    val input = EditText(this)
                    input.hint = "Enter name"
                    AlertDialog.Builder(this)
                        .setTitle("Person Name")
                        .setView(input)
                        .setPositiveButton("Save") { _, _ ->
                            val name = input.text.toString().ifEmpty { "Person" + Random.nextInt(10000, 20000) }
                            dbManager.insertPerson(name, faceImage, templates)
                            personAdapter.notifyDataSetChanged()
                            findViewById<TextView>(R.id.textTotalStudents).text = "Total Students: ${DBManager.personList.size}"
                            Toast.makeText(this, getString(R.string.person_enrolled), Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to process image!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}