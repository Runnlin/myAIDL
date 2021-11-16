package io.github.runnlin.fakeaidl

import android.os.IInterface
import io.github.runnlin.data.Book

interface IOnNewBookArrivedListener:IInterface {

    @Throws
    fun onNewBookArrived(newBook: Book)

}
