package ru.skillbranch.skillarticles.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.skillbranch.skillarticles.data.local.entities.ArticleContent

@Dao
interface ArticleContentsDao {
    @Query("SELECT * FROM article_contents")
    suspend fun findArticleContentsTest() : List<ArticleContent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(obj:ArticleContent): Long
}