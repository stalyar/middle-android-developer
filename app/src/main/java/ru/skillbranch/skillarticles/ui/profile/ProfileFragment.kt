package ru.skillbranch.skillarticles.ui.profile

import androidx.fragment.app.viewModels
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.base.BaseFragment
import ru.skillbranch.skillarticles.viewmodels.profile.ProfileViewModel

class ProfileFragment : BaseFragment<ProfileViewModel>() {

    override val viewModel: ProfileViewModel by viewModels()
    override val layout: Int = R.layout.fragment_profile

    override fun setupViews() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
