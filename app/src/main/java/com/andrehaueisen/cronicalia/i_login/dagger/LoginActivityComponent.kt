package com.andrehaueisen.listadejanot.j_login.dagger

import com.andrehaueisen.cronicalia.a_application.dagger.ApplicationComponent
import com.andrehaueisen.cronicalia.i_login.LoginActivity
import dagger.Component

/**
 * Created by andre on 6/8/2017.
 */
@LoginActivityScope
@Component(dependencies = [ApplicationComponent::class])
interface LoginActivityComponent{

    fun injectFirebaseAuthenticator(loginActivity: LoginActivity)
}