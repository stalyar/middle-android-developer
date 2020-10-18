package ru.skillbranch.skillarticles.ui.transcriptions

import androidx.fragment.app.viewModels
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.base.BaseFragment
import ru.skillbranch.skillarticles.viewmodels.transcriptions.TranscriptionsViewModel

class TranscriptionsFragment : BaseFragment<TranscriptionsViewModel>() {

    override val viewModel: TranscriptionsViewModel by viewModels()
    override val layout: Int = R.layout.fragment_profile

    override fun setupViews() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
