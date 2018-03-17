package com.andrehaueisen.cronicalia.models

import android.os.Parcel
import android.os.Parcelable
import com.andrehaueisen.cronicalia.utils.extensions.decodeEmail
import com.google.firebase.firestore.Exclude

/**
 * Created by andre on 2/18/2018.
 */
data class User(
    var name: String? = null,
    var encodedEmail: String? = null,
    var artisticName: String? = null,
    var aboutSelf: String? = null,
    var profilePictureUri: String? = null,
    var fans: Int = 0,
    var books: HashMap<String, Book> = hashMapOf()) : Parcelable {

    @Exclude
    fun getUserBookNumber() = books.size

    fun toSimpleMap(): Map<String, Any> {

        val simpleUserMap = HashMap<String, Any>()
        simpleUserMap["books"] = books

        return simpleUserMap
    }

    fun generateDecodedEmail() = encodedEmail?.decodeEmail()

    fun refreshUser(user: User){
        this.name = user.name
        this.encodedEmail = user.encodedEmail
        this.artisticName = user.artisticName
        this.aboutSelf = user.aboutSelf
        this.profilePictureUri = user.profilePictureUri
        this.fans = user.fans
        books.clear()
        books.putAll(user.books)
    }

    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readInt(),
        source.readSerializable() as HashMap<String, Book>
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(name)
        writeString(encodedEmail)
        writeString(artisticName)
        writeString(aboutSelf)
        writeString(profilePictureUri)
        writeInt(fans)
        writeSerializable(books)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<User> = object : Parcelable.Creator<User> {
            override fun createFromParcel(source: Parcel): User = User(source)
            override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
        }
    }
}