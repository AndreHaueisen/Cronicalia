package com.andrehaueisen.cronicalia.c_creations.mvp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.utils.extensions.addFragment
import com.google.firebase.storage.FirebaseStorage


class MyCreationsActivity : AppCompatActivity() {

    private val LOG_TAG = MyCreationsActivity::class.java.simpleName
    private val mStorageRef = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.c_activity_my_creactions)

        val myCreationsViewFragment = MyCreationsPresenterFragment.newInstance()
        addFragment(R.id.fragment_container, myCreationsViewFragment)

        /*upload_button.setOnClickListener {

            val userStorageReference = mStorageRef
                .child("brazil/ahb@gmail.com/the_black_river/chp_01/Home_theater_manual.pdf")
            val fileInputStream = assets.open("Home_theater_manual.pdf")

            val uploadTask = userStorageReference.putStream(fileInputStream)
            uploadTask.addOnSuccessListener {
                Log.i(LOG_TAG, "File uploaded!")
                Snackbar.make(fragment_container, "File uploaded!", Snackbar.LENGTH_INDEFINITE).show()
            }

            uploadTask.addOnFailureListener(object : OnFailureListener {
                override fun onFailure(exception: Exception) {
                    Log.e(LOG_TAG, exception.message)
                    Snackbar.make(
                        fragment_container,
                        "Upload failed!",
                        Snackbar.LENGTH_INDEFINITE
                    ).show()
                }
            })

        }

        download_display_button.setOnClickListener {
            val localFile = File(this.filesDir, "Home_theater_manual.pdf")
            val userStorageReference = mStorageRef
                .child("ahb@gmail.com/the_black_river/chp_01/Home_theater_manual.pdf")

            userStorageReference.getFile(localFile).addOnSuccessListener {
                val intent = Intent(this, PDFActivity::class.java)
                startActivity(intent)

            }.addOnFailureListener(object : OnFailureListener{
                    override fun onFailure(exception: Exception) {
                        Log.e(LOG_TAG, exception.message)
                        Snackbar.make(
                            fragment_container,
                            "Download failed!",
                            Snackbar.LENGTH_INDEFINITE
                        )
                    }
                })
        }*/
    }
}
