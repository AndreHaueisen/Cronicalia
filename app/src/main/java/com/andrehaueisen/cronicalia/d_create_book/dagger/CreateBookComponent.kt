package com.andrehaueisen.cronicalia.d_create_book.dagger

import com.andrehaueisen.cronicalia.a_application.dagger.ApplicationComponent
import com.andrehaueisen.cronicalia.d_create_book.mvp.CreateBookActivity
import dagger.Component

/**
 * Created by andre on 3/5/2018.
 */
@CreateBookScope
@Component(modules = [CreateBookModule::class], dependencies = [ApplicationComponent::class])
interface CreateBookComponent {

    fun inject (createBookActivity: CreateBookActivity)
}