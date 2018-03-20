package com.andrehaueisen.cronicalia.c_creations.dagger

import com.andrehaueisen.cronicalia.a_application.dagger.ApplicationComponent
import com.andrehaueisen.cronicalia.c_creations.mvp.MyCreationsPresenterActivity
import dagger.Component

/**
 * Created by andre on 3/13/2018.
 */
@MyCreationsScope
@Component(modules = [MyCreationsModule::class], dependencies = [ApplicationComponent::class])
interface MyCreationsComponent {

    fun inject(myCreationsPresenterActivity: MyCreationsPresenterActivity)
}