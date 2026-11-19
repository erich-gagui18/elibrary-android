package com.erich.elibrary.ui

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.erich.elibrary.Book
import com.erich.elibrary.LibraryItem
import com.erich.elibrary.R
import com.erich.elibrary.databinding.ItemLibraryBinding

class LibraryAdapter(
    private val onDownload: (Book) -> Unit,
    private val onFavorite: (LibraryItem) -> Unit,
    private val onRemove: (LibraryItem) -> Unit,
) : ListAdapter<LibraryItem, LibraryAdapter.VH>(Diff) {

    class VH(val b: ItemLibraryBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemLibraryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val book = item.book
        holder.b.title.text = book.title
        holder.b.author.text = book.author
        holder.b.cover.colorFilter = gray
        holder.b.cover.load(book.coverUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_cover)
            error(R.drawable.bg_cover)
        }
        holder.b.favorite.text = if (item.isFavorite) "Unfavorite" else "Favorite"
        holder.b.download.setOnClickListener { onDownload(book) }
        holder.b.favorite.setOnClickListener { onFavorite(item) }
        holder.b.remove.setOnClickListener { onRemove(item) }
    }

    private companion object {
        val gray = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })

        val Diff = object : DiffUtil.ItemCallback<LibraryItem>() {
            override fun areItemsTheSame(old: LibraryItem, new: LibraryItem) = old.id == new.id
            override fun areContentsTheSame(old: LibraryItem, new: LibraryItem) = old == new
        }
    }
}
