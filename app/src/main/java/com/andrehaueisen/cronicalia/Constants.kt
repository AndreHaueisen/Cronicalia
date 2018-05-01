package com.andrehaueisen.cronicalia

/**
 * Created by andre on 2/18/2018.
 */

const val DOCUMENT_UID_MAPPINGS = "UID_mappings"
const val DOCUMENT_MESSAGE_TOKENS = "Message_tokens"
const val DOCUMENT_EMAIL_UID = "UID"

const val COLLECTION_USERS = "Users"
const val COLLECTION_CREDENTIALS = "Credentials"
const val COLLECTION_BOOKS_ENGLISH = "English_Books"
const val COLLECTION_BOOKS_PORTUGUESE = "Portuguese_Books"
const val COLLECTION_BOOKS_DEUTSCH = "Deutsch_Books"
const val COLLECTION_BOOK_OPINIONS = "Book_Opinions"
const val COLLECTION_USER_OPINIONS = "User_Opinions"

const val STORAGE_ENGLISH_BOOKS = "english_books"
const val STORAGE_PORTUGUESE_BOOKS = "portuguese_books"
const val STORAGE_DEUTSCH_BOOKS = "deutsch_books"
      const val STORAGE_CHAPTERS_FILES = "chapters"
const val STORAGE_USERS = "users"

const val METADATA_TITLE_IMAGE_TYPE = "imageType"
const val METADATA_PROPERTY_IMAGE_TYPE_COVER = "cover"
const val METADATA_PROPERTY_IMAGE_TYPE_POSTER = "poster"
const val METADATA_PROPERTY_IMAGE_TYPE_PROFILE = "profile"
const val METADATA_PROPERTY_IMAGE_TYPE_BACKGROUND = "background"
const val METADATA_CHAPTER_NUMBER = "chapter_number"

const val FILE_NAME_BOOK_POSTER = "poster.jpg"
const val FILE_NAME_BOOK_COVER = "cover.jpg"
const val FILE_NAME_PROFILE_PICTURE = "profile_picture.jpg"
const val FILE_NAME_BACKGROUND_PICTURE = "background_picture.jpg"

const val PDF_ADD_CODE = 400
const val PDF_REQUEST_CODE = 500
const val PDF_EDIT_CODE = 600
const val LOGIN_REQUEST_CODE = 700

const val UPLOAD_STATUS_OK = 100
const val UPLOAD_STATUS_FAIL = -1

const val PARCELABLE_USER = "parcelable_user"
const val PARCELABLE_BOOK = "parcelable_book"
const val PARCELABLE_BOOK_OPINIONS = "parcelable_book_opinions"
const val PARCELABLE_IS_SAVE_BUTTON_SHOWING = "parcelable_is_save_button_showing"
const val PARCELABLE_FILES_TO_BE_DELETED = "parcelable_files_to_be_deleted"
const val PARCELABLE_URI_KEYS = "parcelable_uri_keys"
const val PARCELABLE_TITLE_VALUES = "parcelable_title_values"
const val PARCELABLE_LAYOUT_MANAGER = "parcelable_layout_manager"
const val PARCELABLE_BOOK_KEY = "parcelable_book_key"
const val PARCELABLE_IMAGE_DESTINATION = "parcelable_image_destination"
const val PARCELABLE_ACTION_BOOK_LIST = "parcelable_action_book_list"
const val PARCELABLE_ADVENTURE_BOOK_LIST = "parcelable_adventure_book_list"
const val PARCELABLE_COMEDY_BOOK_LIST = "parcelable_comedy_book_list"
const val PARCELABLE_DRAMA_BOOK_LIST = "parcelable_drama_book_list"
const val PARCELABLE_FANTASY_BOOK_LIST = "parcelable_fantasy_book_list"
const val PARCELABLE_FICTION_BOOK_LIST = "parcelable_fiction_book_list"
const val PARCELABLE_HORROR_BOOK_LIST = "parcelable_horror_book_list"
const val PARCELABLE_MYTHOLOGY_BOOK_LIST = "parcelable_mythology_book_list"
const val PARCELABLE_ROMANCE_BOOK_LIST = "parcelable_romance_book_list"
const val PARCELABLE_SATIRE_BOOK_LIST = "parcelable_satire_book_list"

const val SHARED_PREFERENCES = "com_andre_haueisen_shared_pref"
const val SHARED_MESSAGE_TOKEN = "message_token"

const val INTENT_CALLING_ACTIVITY = "intent_calling_activity"

const val FRAGMENT_EDIT_CREATION_TAG = "fragment_edit_creation_tag"
const val FRAGMENT_BOOK_SELECTED_TAG = "fragment_book_selected"
const val FRAGMENT_FEATURED_BOOKS_TAG = "fragment_featured_books_tag"
const val DIALOG_DELETE_BOOK_TAG = "dialog_delete_book_tag"
const val BACK_STACK_CREATIONS_TO_EDIT_TAG = "back_stack_creations_to_edit_tag"
const val BACK_STACK_FEATURED_TO_SELECTED_TAG = "back_stack_featured_to_selected_tag"

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
