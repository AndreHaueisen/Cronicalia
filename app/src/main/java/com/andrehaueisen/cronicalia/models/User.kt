package com.andrehaueisen.cronicalia.models

/**
 * Created by andre on 2/18/2018.
 */
data class User(val books: ArrayList<Book> = arrayListOf()) {

    fun toSimpleMap(): Map<String, Any> {

        val simpleUserMap = HashMap<String, Any>()
        simpleUserMap["books"] = books

        return simpleUserMap
    }

}