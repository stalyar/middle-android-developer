package ru.skillbranch.skillarticles.extensions

import androidx.navigation.NavDestination
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.skillbranch.skillarticles.R

fun BottomNavigationView.selectDestination(destination: NavDestination) {
    val valuableIds = listOf(R.id.nav_articles, R.id.nav_bookmarks, R.id.nav_transcriptions, R.id.nav_profile, R.id.nav_auth)
    var idDest = destination.id
    if (idDest !in valuableIds) return
    val menu = this.menu
    if (idDest == R.id.nav_auth) idDest = R.id.nav_profile
    menu.findItem(idDest).isChecked = true
}