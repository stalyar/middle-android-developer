package ru.skillbranch.skillarticles.data.remote

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*
import ru.skillbranch.skillarticles.data.models.User
import ru.skillbranch.skillarticles.data.remote.req.*
import ru.skillbranch.skillarticles.data.remote.res.*

interface RestService {
    @GET("articles")
    suspend fun articles(
        @Query("last") last: String? = null,
        @Query("limit") limit: Int = 10
    ): List<ArticleRes>

    @GET("articles/{article}/content")
    suspend fun loadArticleContent(@Path("article") articleId: String): ArticleContentRes

    @GET("articles/{article}/messages")
    fun loadComments(
        @Path("article") articleId: String,
        @Query("last") last: String? = null,
        @Query("limit") limit: Int = 5
    ): Call<List<CommentRes>>

    @POST("articles/{article}/messages")
    suspend fun sendMessage(
        @Path("article") articleId: String,
        @Body message: MessageReq,
        @Header("Authorization") token: String?
    ): MessageRes

    @GET("articles/{article}/counts")
    suspend fun loadArticleCounts(@Path("article") articleId: String): ArticleCountsRes

    @POST("auth/login")
    suspend fun login(@Body loginReq: LoginReq): AuthRes

    @POST("auth/login")
    fun loginCall(@Body loginReq: LoginReq): Call<AuthRes>

    @POST("articles/{article}/decrementLikes")
    suspend fun decrementLike(
        @Path("article") articleId: String,
        @Header("Authorization") token: String?
    ): LikeRes

    @POST("articles/{article}/incrementLikes")
    suspend fun incrementLike(
        @Path("article") articleId: String,
        @Header("Authorization") token: String?
    ): LikeRes

    @POST("articles/{article}/addBookmark")
    suspend fun addBookmark(
        @Path("article") articleId: String,
        @Header("Authorization") token: String?
    ): BookmarkRes

    @POST("articles/{article}/removeBookmark")
    suspend fun removeBookmark(
        @Path("article") articleId: String,
        @Header("Authorization") token: String?
    ): BookmarkRes

    @POST("auth/refresh")
    fun refreshAccessToken(@Body refresh: RefreshReq): Call<RefreshRes>

    @Multipart
    @POST("profile/avatar/upload")
    suspend fun upload(
        @Part file: MultipartBody.Part?,
        @Header ("Authorization") token: String?
    ): UploadRes

    //https://skill-articles.skill-branch.ru/api/v1/profile/avatar/remove
    @PUT("profile/avatar/remove")
    suspend fun remove(
        @Header("Authorization") accessToken: String?
    ): UploadRes

    @PUT("profile")
    suspend fun editProfile(@Body editProfileReq: EditProfileReq, @Header("Authorization") auth: String?): User

    // Authorisation
    //https://skill-articles.skill-branch.ru/api/v1/auth/register
    @POST("auth/register")
    suspend fun register(@Body loginReq: RegistrationReq): AuthRes
    // Profile
    //https://skill-articles.skill-branch.ru/api/v1/profile
    @GET("profile")
    suspend fun loadProfile(
        @Header("Authorization") accessToken: String
    ): ProfileRes

    //https://skill-articles.skill-branch.ru/api/v1/profile
    @PUT("profile")
    suspend fun updateProfile(
        @Body profileInfo: EditProfileReq,
        @Header("Authorization") accessToken: String
    ): ProfileRes

}