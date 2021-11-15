package io.github.runnlin.fakeaidl

import android.os.IInterface
import dt.sprint.fackaidl.data.Book

interface IOnNewBookArrivedListener:IInterface {

    @Throws
    fun onNewBookArrived(newbook: Book)

}
