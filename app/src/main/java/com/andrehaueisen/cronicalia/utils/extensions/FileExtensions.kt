package com.andrehaueisen.cronicalia.utils.extensions

import java.io.File

/**
 * Created by andre on 2/24/2018.
 */
fun File.createBookPictureDirectory(directoryName: String, fileName: String): File{

    val directory = File(this, directoryName)
    val file: File

    if (!directory.exists()) {
        directory.mkdir()
    }

    file = File(directory, fileName)
    file.createNewFile()

    return file
}