package ru.skillbranch.skillarticles.ui.bookmarks

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_bookmarks.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.articles.ArticlesAdapter
import ru.skillbranch.skillarticles.ui.articles.ArticlesFragmentDirections
import ru.skillbranch.skillarticles.ui.base.BaseFragment
import ru.skillbranch.skillarticles.ui.base.Binding
import ru.skillbranch.skillarticles.ui.base.ToolbarBuilder
import ru.skillbranch.skillarticles.ui.delegates.RenderProp
import ru.skillbranch.skillarticles.viewmodels.articles.ArticlesState
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand
import ru.skillbranch.skillarticles.viewmodels.bookmarks.BookmarksViewModel

class BookmarksFragment : BaseFragment<BookmarksViewModel>() {

    override val viewModel: BookmarksViewModel by viewModels()
    override val layout: Int = R.layout.fragment_bookmarks
    override val binding: BookmarksBinding by lazy { BookmarksBinding() }

    override val prepareToolbar: (ToolbarBuilder.() -> Unit)? = {
        addMenuItem(
            ru.skillbranch.skillarticles.ui.base.MenuItemHolder(
                "Search",
                R.id.action_search,
                R.drawable.ic_search_black_24dp,
                R.layout.search_view_layout
            )
        )
    }

    private val articlesAdapter = ArticlesAdapter(
        listener = { item ->
            Log.e("ArticlesFragment", "click on article: ${item.id} ")
            val action = ArticlesFragmentDirections.actionNavArticlesToPageArticle(
                item.id,
                item.author,
                item.authorAvatar,
                item.category,
                item.categoryIcon,
                item.poster,
                item.title,
                item.date
            )

            viewModel.navigate(NavigationCommand.To(action.actionId, action.arguments))
        },
        bookmarkListener = { articleId, isBookmarked ->
            viewModel.handleToggleBookmark(articleId, isBookmarked)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView
        if (binding.isSearch) {
            menuItem.expandActionView()
            searchView.setQuery(binding.searchQuery, false)
            if (binding.isFocusedSearch) searchView.requestFocus()
            else searchView.clearFocus()
        }

        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(false)
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.handleSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.handleSearch(newText)
                return true
            }
        })

        searchView.setOnCloseListener {
            viewModel.handleSearchMode(false)
            true
        }
    }

    override fun setupViews() {
        with(rv_bookmarks) {
            layoutManager = LinearLayoutManager(context)
            adapter = articlesAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
        viewModel.observeList(viewLifecycleOwner) {
            articlesAdapter.submitList(it)
        }
    }

    inner class BookmarksBinding : Binding() {
        var isFocusedSearch: Boolean = false
        var searchQuery: String? = null
        var isSearch: Boolean = false
        var isLoading: Boolean by RenderProp(true) {
            // TODO show shimmer here
        }

        override fun bind(data: IViewModelState) {
            data as ArticlesState
            searchQuery = data.searchQuery
            isSearch = data.isSearch
            isLoading = data.isLoading
        }

    }

}