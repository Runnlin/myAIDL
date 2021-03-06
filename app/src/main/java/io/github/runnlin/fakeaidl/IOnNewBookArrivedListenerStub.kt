package io.github.runnlin.fakeaidl

import android.os.*
import io.github.runnlin.data.Book

abstract class IOnNewBookArrivedListenerStub : Binder(),
    IOnNewBookArrivedListener {
    override fun asBinder(): IBinder {
        return this
    }

    @Throws(RemoteException::class)
    public override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        when (code) {
            INTERFACE_TRANSACTION -> {
                reply!!.writeString(Companion.getInterfaceDescriptor())
                return true
            }
            TRANSACTION_onNewBookArrived -> {
                data.enforceInterface(Companion.getInterfaceDescriptor())
                val _arg0: Book? = if (0 != data.readInt()) {
                    Book.CREATOR.createFromParcel(data)
                } else {
                    null
                }
                if (_arg0 != null) {
                    this.onNewBookArrived(_arg0)
                }
                reply!!.writeNoException()
                return true
            }
        }
        return super.onTransact(code, data, reply, flags)
    }

    private class Proxy internal constructor(private val mRemote: IBinder) :
        IOnNewBookArrivedListener {
        override fun asBinder(): IBinder {
            return mRemote
        }

        @Throws(RemoteException::class)
        override fun onNewBookArrived(newBook: Book) {
            val _data = Parcel.obtain()
            val _reply = Parcel.obtain()
            try {
                _data.writeInterfaceToken(getInterfaceDescriptor())
                _data.writeInt(1)
                newBook.writeToParcel(_data, 0)
                mRemote.transact(TRANSACTION_onNewBookArrived, _data, _reply, 0)
                _reply.readException()
            } finally {
                _reply.recycle()
                _data.recycle()
            }
        }
    }

    companion object {

        fun getInterfaceDescriptor():String {
            return "io.github.runnlin.fakeaidl.IOnNewBookArrivedListener"
        }

        fun asInterface(obj: IBinder?): IOnNewBookArrivedListener? {
            if (obj == null) {
                return null
            }
            val iin = obj.queryLocalInterface(getInterfaceDescriptor())
            // ????????????????????????,?????????????????????????????????,???????????????Stub??????;
            return if (iin != null && iin is IOnNewBookArrivedListener) {
                iin
            } else Proxy(obj)
            // ???????????????????????????,??????????????????????????? Stub.Proxy ??? (Stub??????)??????;
        }

        const val TRANSACTION_onNewBookArrived = FIRST_CALL_TRANSACTION + 0
    }

    init {
        attachInterface(this, Companion.getInterfaceDescriptor())
    }
}