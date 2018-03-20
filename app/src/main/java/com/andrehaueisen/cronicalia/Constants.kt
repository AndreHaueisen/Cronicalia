package com.andrehaueisen.cronicalia

/**
 * Created by andre on 2/18/2018.
 */
const val AUTHOR_NAME_PLACE_HOLDER = "Andre Haueisen"
const val ENCODED_EMAIL_PLACE_HOLDER = "andrehaueisen@gmail,com"
const val READINGS_PLACE_HOLDER = 500
const val RATING_PLACE_HOLDER = 8.5
const val INCOME_PLACE_HOLDER = 230

const val DOCUMENT_UID_MAPPINGS = "UID_mappings"
const val DOCUMENT_MESSAGE_TOKENS = "Message_tokens"
const val DOCUMENT_EMAIL_UID = "UID"

const val COLLECTION_USERS = "Users"
const val COLLECTION_CREDENTIALS = "Credentials"
const val COLLECTION_BOOKS_ENGLISH = "English_Books"
const val COLLECTION_BOOKS_PORTUGUESE = "Portuguese_Books"
const val COLLECTION_BOOKS_DEUTSCH = "Deutsch_Books"

const val STORAGE_ENGLISH_BOOKS = "english_books"
const val STORAGE_PORTUGUESE_BOOKS = "portuguese_books"
const val STORAGE_DEUTSCH_BOOKS = "deutsch_books"
      const val STORAGE_CHAPTERS_FILES = "chapters"
const val STORAGE_USER_PHOTOS = "user_photos"

const val METADATA_TITLE_IMAGE_TYPE = "imageType"
const val METADATA_PROPERTY_IMAGE_TYPE_COVER = "cover"
const val METADATA_PROPERTY_IMAGE_TYPE_POSTER = "poster"
const val METADATA_CHAPTER_NUMBER = "chapter_number"

const val FILE_NAME_BOOK_POSTER = "poster.jpg"
const val FILE_NAME_BOOK_COVER = "cover.jpg"
const val FILE_NAME_USER_PICTURE = "user_profile.jpg"

const val PDF_REQUEST_CODE = 500
const val LOGIN_REQUEST_CODE = 700

const val UPLOAD_STATUS_OK = 100
const val UPLOAD_STATUS_FAIL = -1

const val PARCELABLE_USER = "parcelable_user"
const val PARCELABLE_BOOK = "parcelable_book"
const val PARCELABLE_URI_KEYS = "parcelable_uri_keys"
const val PARCELABLE_TITLE_VALUES = "parcelable_title_values"
const val PARCELABLE_LAYOUT_MANAGER = "parcelable_layout_manager"

const val SHARED_PREFERENCES = "com_andre_haueisen_shared_pref"
const val SHARED_MESSAGE_TOKEN = "message_token"

const val INTENT_CALLING_ACTIVITY = "intent_calling_activity"

const val FRAGMENT_MY_CREATION_TAG = "fragment_my_creation_tag"

//coroutines test code

/* val uiThread: CoroutineContext = UI
    val bgThread: CommonPool

    launch(uiThread) {
        async(CommonPool) {ok().await()}
    }

    suspend fun ok(): Deferred<Book> {
        return async {
            Book("")
        }
 }*/
