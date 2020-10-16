package ru.skillbranch.skillarticles.ui.articles

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.ui.custom.ArticleItemView


class ArticlesAdapter(private val listener: (ArticleItemData)-> Unit) :
    PagedListAdapter<ArticleItemData, ArticleVH>(ArticleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleVH {
        val view = ArticleItemView(parent.context)
        return ArticleVH(view)
    }

    override fun onBindViewHolder(holder: ArticleVH, position: Int) {
        holder.bind(getItem(position), listener)
    }
}

class ArticleDiffCallback: DiffUtil.ItemCallback<ArticleItemData>(){
    override fun areItemsTheSame(oldItem: ArticleItemData, newItem: ArticleItemData): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ArticleItemData, newItem: ArticleItemData): Boolean =
        oldItem == newItem
}

class ArticleVH(val containerView: View) : RecyclerView.ViewHolder(containerView){
    fun bind(
        item: ArticleItemData?,
        listener: (ArticleItemData) -> Unit
    ){

        //if use placeholder item may be null
        (containerView as ArticleItemView).bind(item!!) { _, _ -> Log.e("T", "click") }
        itemView.setOnClickListener{listener(item!!)}
    }
}