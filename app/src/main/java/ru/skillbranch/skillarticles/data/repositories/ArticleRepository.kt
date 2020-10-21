package ru.skillbranch.skillarticles.data.repositories

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import ru.skillbranch.skillarticles.data.local.DbManager.db
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.local.dao.ArticleContentsDao
import ru.skillbranch.skillarticles.data.local.dao.ArticleCountsDao
import ru.skillbranch.skillarticles.data.local.dao.ArticlePersonalInfosDao
import ru.skillbranch.skillarticles.data.local.dao.ArticlesDao
import ru.skillbranch.skillarticles.data.local.entities.ArticleFull
import ru.skillbranch.skillarticles.data.models.AppSettings
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.RestService
import ru.skillbranch.skillarticles.data.remote.req.MessageReq
import ru.skillbranch.skillarticles.data.remote.res.CommentRes
import ru.skillbranch.skillarticles.extensions.data.toArticleContent

interface IArticleRepository {
    fun findArticle(articleId: String): LiveData<ArticleFull>
    fun getAppSettings(): LiveData<AppSettings>
    suspend fun toggleLike(articleId: String)
    suspend fun toggleBookmark(articleId: String)
    fun isAuth(): LiveData<Boolean>
    suspend fun sendMessage(articleId: String, text: String, answerToSlug: String?)
    fun loadAllComments(articleId: String, total: Int, errHandler: (Throwable) -> Unit): CommentsDataFactory
    suspend fun decrementLike(articleId: String)
    suspend fun incrementLike(articleId: String)
    fun updateSettings(copy: AppSettings)
    suspend fun fetchArticleContent(articleId: String)
    fun findArticleCommentCount(articleId: String): LiveData<Int>
}

object ArticleRepository : IArticleRepository {
    private val network = NetworkManager.api
    private val preferences = PrefManager
    private var articlesDao = db.articlesDao()
    private var articlePersonalDao = db.articlePersonalInfosDao()
    private var articleCountsDao = db.articleCountsDao()
    private var articleContentDao = db.articleContentsDao()

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun setupTestDao(
        articlesDao: ArticlesDao,
        articlePersonalDao: ArticlePersonalInfosDao,
        articleCountsDao: ArticleCountsDao,
        articleContentDao: ArticleContentsDao
    ) {
        this.articlesDao = articlesDao
        this.articlePersonalDao = articlePersonalDao
        this.articleCountsDao = articleCountsDao
        this.articleContentDao = articleContentDao
    }

    override fun findArticle(articleId: String): LiveData<ArticleFull> {
        return articlesDao.findFullArticle(articleId)
    }

    override fun getAppSettings(): LiveData<AppSettings> =
        preferences.appSettings

    override suspend fun toggleLike(articleId: String) {
        articlePersonalDao.toggleLikeOrInsert(articleId)
    }

    override suspend fun toggleBookmark(articleId: String) {
        articlePersonalDao.toggleBookmarkOrInsert(articleId)
    }

    override fun updateSettings(appSettings: AppSettings) {
        preferences.updateSettings(appSettings)
    }

    override suspend fun fetchArticleContent(articleId: String) {
        val content = network.loadArticleContent(articleId)
        articleContentDao.insert(content.toArticleContent())
    }

    override fun findArticleCommentCount(articleId: String): LiveData<Int> {
        return articleCountsDao.getCommentsCount(articleId)
    }

    override fun isAuth(): LiveData<Boolean> {
        return preferences.isAuthLive
    }

    override fun loadAllComments(articleId: String, total: Int, errHandler: (Throwable) -> Unit): CommentsDataFactory =
        CommentsDataFactory(
            itemProvider = network,
            articleId = articleId,
            totalCount = total,
            errHandler = errHandler
        )


    override suspend fun decrementLike(articleId: String) {
        articleCountsDao.decrementLike(articleId)
    }

    override suspend fun incrementLike(articleId: String) {
        articleCountsDao.incrementLike(articleId)
    }

    override suspend fun sendMessage(articleId: String, message: String, answerToMessageId: String?) {
        val (_, messageCount) = network.sendMessage(
            articleId,
            MessageReq(message, answerToMessageId),
            preferences.accessToken
        )
        articleCountsDao.incrementCommentsCount(articleId)
    }

    suspend fun refreshCommentsCount(articleId: String) {
        val counts = network.loadArticleCounts(articleId)
        articleCountsDao.updateCommentsCount(articleId, counts.comments)
    }
}



class CommentsDataFactory(
    private val itemProvider: RestService,
    private val articleId: String,
    private val totalCount: Int,
    private val errHandler: (Throwable) -> Unit
) : DataSource.Factory<String?, CommentRes>() {
    override fun create(): DataSource<String?, CommentRes> =
        CommentsDataSource(itemProvider, articleId, totalCount, errHandler)
    class CommentsDataSource(
        private val itemProvider: RestService,
        private val articleId: String,
        private val totalCount: Int,
        private val errHandler: (Throwable) -> Unit
    ) : ItemKeyedDataSource<String, CommentRes>() {
        override fun loadInitial(
            params: LoadInitialParams<String>,
            callback: LoadInitialCallback<CommentRes>
        ) {

            try {
                val result = itemProvider.loadComments(
                    articleId,
                    params.requestedInitialKey,
                    params.requestedLoadSize
                ).execute()

                callback.onResult(
                    if (totalCount > 0) result.body()!! else emptyList(),
                    0,
                    totalCount
                )
            } catch (e: Throwable) {
                errHandler(e)
            }
        }

        override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<CommentRes>) {
            try {
                val result = itemProvider.loadComments(articleId, params.key, params.requestedLoadSize).execute()
                callback.onResult(result.body()!!)
            } catch (e: Throwable) {
                errHandler(e)
            }
        }

        override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<CommentRes>) {
            try {
                val result = itemProvider.loadComments(articleId, params.key, -params.requestedLoadSize).execute()
                callback.onResult(result.body()!!)
            } catch (e: Throwable) {
                errHandler(e)
            }
        }

        override fun getKey(item: CommentRes): String = item.slug

    }

}
