package io.github.runnlin.fakeaidl

import android.os.IBinder
import android.os.IInterface
import android.os.RemoteException
import dt.sprint.fackaidl.data.Book

interface IBookManager: IInterface {

    @Throws(RemoteException::class)
    fun getBookList(): List<Book>

    @Throws(RemoteException::class)
    fun addBook(book: Book?)

    @Throws(RemoteException::class)
    fun registerListener(listener: IOnNewBookArrivedListener?)

    @Throws(RemoteException::class)
    fun unregisterListener(listener: IOnNewBookArrivedListener?)

}