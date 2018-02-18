package com.andrehaueisen.cronicalia

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_pdf.*
import java.io.File

class PDFActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf)

        val localFile = File(this.filesDir, "Home_theater_manual.pdf")
        Log.i(PDFActivity::class.java.simpleName, localFile.absolutePath)
        pdf_view.fromFile(localFile)
            .enableDoubletap(true)
            .load()
    }
}
