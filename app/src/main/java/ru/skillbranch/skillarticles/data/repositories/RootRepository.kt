package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.req.LoginReq

object RootRepository {

    private val preferences = PrefManager
    private val network = NetworkManager.api

    fun isAuth() : LiveData<Boolean> = preferences.isAuthLive

    suspend fun login(login: String, pass: String) {
        val auth = network.login(LoginReq(login, pass))
        preferences.profile = auth.user
        preferences.accessToken = "Bearer ${auth.accessToken}"  //aсcess Token нужен чтобы подписывать запросы после авторизации, имеет ограниченный срок жизни (1 день или типо того)
        preferences.refreshToken = auth.refreshToken   //refreshToken нужен чтобы обновить aсcess Token без повторной авторизации при помощи interceptor
    }
}