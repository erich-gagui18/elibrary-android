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
import com.erich.elibrary.databinding.ItemBookBinding

class BookAdapter(private val onDownload: (Book) -> Unit) :
    ListAdapter<Book, BookAdapter.VH>(Diff) {

    class VH(val b: ItemBookBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val book = getItem(position)
        holder.b.title.text = book.title
        holder.b.author.text = book.author
        holder.b.cover.colorFilter = gray // black-and-white covers to match the theme
        holder.b.cover.load(book.coverUrl) { crossfade(true) }
        holder.b.download.setOnClickListener { onDownload(book) }
    }

    private companion object {
        val gray = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })

        val Diff = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(old: Book, new: Book) = old.id == new.id
            override fun areContentsTheSame(old: Book, new: Book) = old == new
        }
    }
}
