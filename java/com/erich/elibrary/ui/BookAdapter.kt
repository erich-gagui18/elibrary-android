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
import com.erich.elibrary.R
import com.erich.elibrary.databinding.ItemBookBinding

class BookAdapter(
    private val onDownload: (Book) -> Unit,
    private val onSave: (Book) -> Unit,
) : ListAdapter<Book, BookAdapter.VH>(Diff) {

    class VH(val b: ItemBookBinding) : RecyclerView.ViewHolder(b.root)

    /** Ids of books already in My Library; their button reads "Saved". */
    var savedIds: Set<Int> = emptySet()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val book = getItem(position)
        val saved = book.id in savedIds
        holder.b.title.text = book.title
        holder.b.author.text = book.author
        holder.b.cover.colorFilter = gray // black-and-white covers to match the theme
        holder.b.cover.load(book.coverUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_cover)
            error(R.drawable.bg_cover) // gray tile if the cover server cannot be reached
        }
        holder.b.download.setOnClickListener { onDownload(book) }
        holder.b.save.text = if (saved) "Saved" else "Save"
        holder.b.save.isEnabled = !saved
        holder.b.save.setOnClickListener { onSave(book) }
    }

    private companion object {
        val gray = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })

        val Diff = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(old: Book, new: Book) = old.id == new.id
            override fun areContentsTheSame(old: Book, new: Book) = old == new
        }
    }
}
