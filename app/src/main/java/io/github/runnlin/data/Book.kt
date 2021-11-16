package io.github.runnlin.data

import android.os.Parcel
import android.os.Parcelable

/**
 * 书的序列化类
 * Created by yanping on 16/7/20.
 */
class Book : Parcelable {
    var bookId: Int
    var bookName: String?

    constructor(bookId: Int, bookName: String?) {
        this.bookId = bookId
        this.bookName = bookName
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(bookId)
        dest.writeString(bookName)
    }

    private constructor(source: Parcel) {
        bookId = source.readInt()
        bookName = source.readString()
    }

    override fun toString(): String {
        return "ID: $bookId, BookName: $bookName"
    }


    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Book?> = object : Parcelable.Creator<Book?> {
            override fun createFromParcel(source: Parcel): Book {
                return Book(source)
            }

            override fun newArray(size: Int): Array<Book?> {
                return arrayOfNulls(size)
            }
        }
    }
}