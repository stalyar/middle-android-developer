package ru.skillbranch.skillarticles.viewmodels

import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format
import ru.skillbranch.skillarticles.extensions.indexes
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.Notify

class ArticleViewModel(private val articleId: String) : BaseViewModel<ArticleState>(ArticleState()), IArticleViewModel {
    private val repository = ArticleRepository
    private var menuIsShown:Boolean = false

    init{
        //subscribe on mutable data
        subscribeOnDataSource(getArticleData()){article, state ->
            article ?: return@subscribeOnDataSource null
            state.copy(
                shareLink = article.shareLink,
                title = article.title,
                author = article.author,
                category = article.category,
                categoryIcon = article.categoryIcon,
                date = article.date.format()
            )
        }

        subscribeOnDataSource(getArticleContent()){content, state ->
            content ?: return@subscribeOnDataSource null
            state.copy(
                isLoadingContent = false,
                content = content
            )
        }

        subscribeOnDataSource(getArticlePersonalInfo()){info, state ->
            info ?: return@subscribeOnDataSource null
            state.copy(
                isBookmark = info.isBookmark,
                isLike = info.isLike
            )
        }

        //subscribe on settings
        subscribeOnDataSource(repository.getAppSettings()){settings, state ->
            state.copy(
                isDarkMode = settings.isDarkMode,
                isBigText = settings.isBigText
            )
        }
    }

    //load text from network
    override fun getArticleContent(): LiveData<List<Any>?> {
        return  repository.loadArticleContent(articleId)
    }

    //load data from db
    override fun getArticleData(): LiveData<ArticleData?>{
        return repository.getArticle(articleId)
    }

    //load data from db
    override fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?>{
        return repository.loadArticlePersonalInfo(articleId)
    }

    //app settings
    override fun handleNightMode(){
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isDarkMode = !settings.isDarkMode))
    }

    override fun handleUpText(){
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
    }

    override fun handleDownText(){
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
    }

    //personal article info
    override fun handleBookmark(){
        val info = currentState.toArticlePersonalInfo()
        repository.updateArticlePersonalInfo(info.copy(isBookmark = !info.isBookmark))

        val msg = if (currentState.isBookmark) "Add to bookmarks" else "Remove from bookmarks"
        notify(Notify.TextMessage(msg))
    }

    override fun handleLike(){
        Log.e("ArticleViewModel", "handle like: ")
        val isLiked = currentState.isLike
        val toggleLike = {
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isLike = !info.isLike))
        }

        toggleLike()

        val msg = if (!isLiked) Notify.TextMessage("Mark is liked")
        else {
            Notify.ActionMessage(
                "Don't like it anymore", //message
            "No, still like it", //action label on snackbar
            toggleLike // handler function, if pressed "No, still like it" on snackbar, then toggle again)
            )
        }
        notify(msg)
    }

    //not implemented
    override fun handleShare(){
        val msg = "Share is not implemented"
        notify(Notify.ErrorMessage(msg, "OK", null))
    }

    //session state
    override fun handleToggleMenu(){
        updateState { state ->
            state.copy(isShowMenu = !state.isShowMenu).also{menuIsShown = !state.isShowMenu}
        }
    }

    override fun handleSearchMode(isSearch: Boolean){
        updateState {it.copy(isSearch = isSearch, isShowMenu = false, searchPosition = 0)}
    }
/*
    fun hideMenu(){
        updateState{it.copy(isShowMenu = false)}
    }

    fun showMenu(){
        updateState { it.copy(isShowMenu = menuIsShown) }
    }
*/
    override fun handleSearch(query: String?){
        query ?: return
        val result = (currentState.content.firstOrNull() as? String)
            .indexes(query)
            .map{it to it + query.length}
        updateState{it.copy(searchQuery = query, searchResults =  result, searchPosition = 0)}
    }

    fun handleUpResult(){
        updateState{it.copy(searchPosition = it.searchPosition.dec())}
    }

    fun handleDownResult(){
        updateState{it.copy(searchPosition = it.searchPosition.inc())}
    }

}

data class ArticleState(
    val isAuth: Boolean = false, //пользователь авторизован
    val isLoadingContent: Boolean = true, //content загружается
    val isLoadingReviews: Boolean = true, //отзывы загружаются
    val isLike: Boolean = false, //лайкнуто
    val isBookmark: Boolean = false, //в закладках
    val isShowMenu: Boolean = false,
    val isBigText: Boolean = false,
    val isDarkMode: Boolean = false, //темный режим
    val isSearch: Boolean = false, //режим поиска
    val searchQuery: String? = null, //поисковый запрос
    val searchResults: List<Pair<Int, Int>> = emptyList(), //результат поиска (стартовая и конечная позиции)
    val searchPosition: Int = 0, //текущая позиция найденного результата
    val shareLink: String? = null, //ссылка share
    val title: String? = null, //заголовок статьи
    val category: String? = null, //категория
    val categoryIcon: Any? = null, //иконка категории
    val date: String? = null, //дата публикации
    val author: Any? = null, //автор статьи
    val poster: String? = null, //обложка статьи
    val content: List<Any> = emptyList(), //контент
    val reviews: List<Any> = emptyList() //отзывы
) : IViewModelState {
    override fun save(outState: Bundle) {
        outState.putAll(
            bundleOf(
                "isSearch" to isSearch,
                "searchQuery" to searchQuery,
                "searchResults" to searchResults,
                "searchPosition" to searchPosition
            )
        )
    }

    override fun restore(savedState: Bundle): IViewModelState {
        return copy(
            isSearch = savedState["isSearch"] as Boolean,
            searchQuery = savedState["searchQuery"] as? String,
            searchResults = savedState["searchResults"] as List<Pair<Int,Int>>,
            searchPosition = savedState["searchPosition"] as Int
        )
    }
}