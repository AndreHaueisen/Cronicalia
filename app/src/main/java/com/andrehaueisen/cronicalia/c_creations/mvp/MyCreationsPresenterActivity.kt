package com.andrehaueisen.cronicalia.c_creations.mvp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.andrehaueisen.cronicalia.FRAGMENT_MY_CREATION_TAG
import com.andrehaueisen.cronicalia.R
import com.andrehaueisen.cronicalia.a_application.BaseApplication
import com.andrehaueisen.cronicalia.c_creations.dagger.DaggerMyCreationsComponent
import com.andrehaueisen.cronicalia.c_creations.dagger.MyCreationsModule
import com.andrehaueisen.cronicalia.models.Book
import com.andrehaueisen.cronicalia.models.User
import com.andrehaueisen.cronicalia.utils.extensions.addFragment
import javax.inject.Inject


class MyCreationsPresenterActivity : AppCompatActivity(), MyCreationsViewFragment.CreationClickListener {

    interface PresenterActivity {
        fun refreshFragmentData(book: Book)
    }

    @Inject
    lateinit var mUser: User

    private val LOG_TAG = MyCreationsPresenterActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerMyCreationsComponent.builder()
            .applicationComponent(BaseApplication.get(this).getAppComponent())
            .myCreationsModule(MyCreationsModule())
            .build()
            .inject(this)

        setContentView(R.layout.c_activity_my_creations)

        val myCreationsViewFragment = MyCreationsViewFragment.newInstance()
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

    override fun onCreationClick(book: Book) {

        var creationFragment = (supportFragmentManager.findFragmentByTag(FRAGMENT_MY_CREATION_TAG) as? MyCreationEditViewFragment)

        if (creationFragment == null) {
            creationFragment = MyCreationEditViewFragment.newInstance()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(creationFragment, FRAGMENT_MY_CREATION_TAG)
            transaction.addToBackStack(null)
            transaction.commit()

        } else {
            if (creationFragment.isVisible)
                creationFragment.refreshFragmentData(book)
        }

    }
}
