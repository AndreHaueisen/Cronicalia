package com.andrehaueisen.cronicalia.c_my_books.dagger

import com.andrehaueisen.cronicalia.a_application.dagger.ApplicationComponent
import com.andrehaueisen.cronicalia.c_my_books.mvp.MyBooksPresenterActivity
import dagger.Component

/**
 * Created by andre on 3/13/2018.
 */
@MyBooksScope
@Component(modules = [MyBooksModule::class], dependencies = [ApplicationComponent::class])
interface MyBooksComponent {

    fun inject(myBooksPresenterActivity: MyBooksPresenterActivity)
}