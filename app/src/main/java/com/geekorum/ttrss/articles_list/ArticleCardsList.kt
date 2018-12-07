package com.geekorum.ttrss.articles_list

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.ViewDataBinding
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.geekorum.geekdroid.views.recyclerview.SpacingItemDecoration
import com.geekorum.ttrss.BR
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.databinding.HeadlinesRowBinding


internal fun RecyclerView.setupCardSpacing() {
    val spacing = resources.getDimensionPixelSize(R.dimen.article_list_spacing)
    val spaceItemDecoration = SpacingItemDecoration(spacing, spacing)
    addItemDecoration(spaceItemDecoration)
}


internal open class ArticlesListAdapter(
    private val layoutInflater: LayoutInflater,
    private val eventHandler: CardEventHandler
) : PagedListAdapter<Article, BindingViewHolder>(ARTICLE_DIFF_CALLBACK) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id ?: RecyclerView.NO_ID
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val binding = HeadlinesRowBinding.inflate(layoutInflater, parent, false)
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        with(holder) {
            val article = getItem(position)
            setArticle(article)
            setHandler(eventHandler)
            setPosition(position)
        }
        holder.binding.executePendingBindings()
    }
}

internal class BindingViewHolder(val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun setArticle(article: Article?) {
        binding.setVariable(BR.article, article)
    }

    fun setHandler(cardEventHandler: CardEventHandler) {
        binding.setVariable(BR.handler, cardEventHandler)
    }

    fun setPosition(position: Int) {
        binding.setVariable(BR.position, position)
    }
}


abstract class CardEventHandler(
    private val context: Context
) {

    abstract fun onCardClicked(card: View, article: Article, position: Int)

    abstract fun onStarChanged(article: Article, newValue: Boolean)

    abstract fun onShareClicked(article: Article)

    abstract fun onMenuToggleReadSelected(article: Article)

    fun onMenuButtonClicked(button: View, article: Article, position: Int) {
        PopupMenu(context, button).run {
            menuInflater.inflate(R.menu.item_article, menu)
            setOnMenuItemClickListener { item -> onArticleMenuItemSelected(item, article, position) }
            show()
        }
    }

    private fun onArticleMenuItemSelected(item: MenuItem, article: Article, position: Int): Boolean {
        return when (item.itemId) {
            R.id.headlines_article_unread -> {
                onMenuToggleReadSelected(article)
                true
            }
            else -> false
        }
    }
}

private val ARTICLE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem == newItem
    }
}
