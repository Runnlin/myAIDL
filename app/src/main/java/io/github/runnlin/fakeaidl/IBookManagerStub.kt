package io.github.runnlin.fakeaidl

import android.annotation.SuppressLint
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import dt.sprint.fackaidl.data.Book


/************************************************************************************************
 * ----------------------------- 进程间通信 -----------------------------
 * 1.Client 发起远程调用请求 也就是RPC 到Binder。同时将自己挂起，挂起的原因是要等待RPC调用结束以后返回的结果
 * <p/>
 * 2.Binder 收到RPC请求以后 把参数收集一下，调用transact方法，把RPC请求转发给service端。
 * <p/>
 * 3.service端 收到rpc请求以后 就去线程池里 找一个空闲的线程去走service端的 onTransact方法 ，实际上也就是
 * 真正在运行service端的 方法了，等方法运行结束 就把结果 写回到binder中。
 * <p/>
 * 4.Binder 收到返回数据以后 就唤醒原来的Client 线程，返回结果。至此，一次进程间通信 的过程就结束了
 * ----------------------------- 进程间通信 -----------------------------
 * <p/>
 * Created by yanping on 16/7/20.
 * 参考: http://www.cnblogs.com/punkisnotdead/p/5163464.html
 ************************************************************************************************/
abstract class IBookManagerStub() : IBookManager, Binder() {
    /*******************************************************************************
     * 这个就是 Stub 的构造方法,回顾一下,我们写好aidl文件以后,写的 service 里面 是怎么写的?
     * private Binder mBinder = new IBookManager.Stub(){}
     * <p/>
     * 目前,我们自己实现 aidl, 那么 service 里面是这么写的:
     * private Binder mBinder = new IBookManagerStub(){}
     ******************************************************************************/

    init {
        this.attachInterface(this, DESCRIPTOR)
    }


    /**************************************************************************************************************
     * 只有在多进程通信的时候 才会调用这个方法, 同一个进程是不会调用的;
     * 这个方法一般情况下都是返回true的,如果返回false就代表这个方法执行失败;
     * 通常使用这个方法做权限认证.
     * <p/>
     * onTransact 这个方法是运行在 Binder 线程池中的,
     * 一般就是 客户端发起请求,然后 android底层代码把这个客户端发起的请求 封装成 3个参数 来调用这个 onTransact 方法,
     * 第一个参数 code 就代表客户端想要调用服务端 方法的 标志位;
     * (服务端可能有n个方法,每个方法 都有一个对应的 int 值来代表, 这个 code 就是这个 int 值, 用来标示客户端想调用服务端的方法)
     * 第二个参数 data 就是方法参数;
     * 第三个参数 reply 就是方法返回值;
     * <p/>
     * onTransact 这个方法运行在 Binder 线程池, 所以这个方法调用的服务端方法也是运行在 Binder 线程池中的,
     * 所以我们的 服务端程序 有可能和多个客户端相关联, 所在方法里使用的这些参数 必须是支持异步的, 否则的话
     * 值就会错乱了! 结论就是 Binder 方法, 一定是同步方法!!!!!!
     *************************************************************************************************************/
    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        when (code) {
            INTERFACE_TRANSACTION -> {
                reply!!.writeString(DESCRIPTOR)
                return true
            }
            TRANSACTION_getBookList -> {
                data.enforceInterface(DESCRIPTOR)
                val _result = getBookList()
                reply!!.writeNoException()
                reply.writeTypedList(_result)
                return true
            }
            TRANSACTION_addBook -> {
                data.enforceInterface(DESCRIPTOR)
                val _arg0: Book?
                _arg0 = if (0 != data.readInt()) {
                    Book.CREATOR.createFromParcel(data)
                } else {
                    null
                }
                addBook(_arg0)
                reply!!.writeNoException()
                return true
            }
            TRANSACTION_registerListener -> {
                data.enforceInterface(DESCRIPTOR)
                val _arg0: IOnNewBookArrivedListener
                _arg0 = asInterface(data.readStrongBinder() as Binder?)
                registerListener(_arg0)
                reply!!.writeNoException()
                return true
            }
            TRANSACTION_unregisterListener -> {
                data.enforceInterface(DESCRIPTOR)
                val _arg0: IOnNewBookArrivedListener
                _arg0 = asInterface(data.readStrongBinder() as Binder?)
                unregisterListener(_arg0)
                reply!!.writeNoException()
                return true
            }
        }

        return super.onTransact(code, data, reply, flags)
    }

    companion object {
        // binder唯一标识，就是路径
        val DESCRIPTOR = "io.github.runnlin.fakeaidl.IBookManagerStub"


        /***************************************************
         * asInterface 这个方法就做了一件事情:
         * 如果在同一个进程,就返回 Stub 对象本身;
         * 如果不在同一个进程,就返回 Stub.Proxy 代理对象;
         **************************************************/


        fun asInterface(obj: Binder?): IBookManager? {
            if (null == obj)
                return null

            var iin = obj.queryLocalInterface(DESCRIPTOR) as IInterface
            // 如果是同一个进程
            if (iin is IBookManager) {
                return iin
            } else {
                return Proxy(obj)
            }

        }


        class Proxy(remote: IBinder) : IBookManager {

            private val mRemote: IBinder = remote


            /***************************************************************************************
             * 返回书籍列表;
             * 首先创建3个对象, _data 输入对象, _reply 输出对象, _result 返回值对象;
             * 然后把参数信息写入到 _data 里面, 接着就调用了 transact 这个方法 来发送 rpc 请求;
             * 然后当前线程挂起;
             * 服务端的 onTransace 方法被调用,调用结束以后,当前线程继续执行,
             * 直到从 _reply 中取出 rpc 的返回结果,然后返回 _reply 的数据;
             * <p/>
             * 当客户端发起调用远程请求时,当前客户端的线程会被挂起,
             * 所以如果一个远程方法 很耗时, 客户端一定不能在 UI Main 线程里发起这个 RPC 请求,不然就 ANR 了.
             ***************************************************************************************/


            @SuppressLint("Recycle")
            override fun getBookList(): List<Book> {
                var _data = Parcel.obtain()
                var _reply = Parcel.obtain()

                var _result: ArrayList<Book>

                try {
                    _data.writeInterfaceToken(DESCRIPTOR)
                    mRemote.transact(IBookManagerStub.TRANSACTION_getBookList, _data, _reply, 0)
                    _reply.readException()
                    _result =
                        _reply.createTypedArrayList<Book>(Book.CREATOR)!!
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
                return _result
            }

            override fun addBook(book: Book?) {
                TODO("Not yet implemented")
            }

            override fun registerListener(listener: IOnNewBookArrivedListener?) {
                TODO("Not yet implemented")
            }

            override fun unregisterListener(listener: IOnNewBookArrivedListener?) {
                TODO("Not yet implemented")
            }

            override fun asBinder(): IBinder {
                return mRemote
            }

            fun getInterfaceDescriptor(): String {
                return DESCRIPTOR
            }

        }


        val TRANSACTION_getBookList = FIRST_CALL_TRANSACTION + 0
        val TRANSACTION_addBook = FIRST_CALL_TRANSACTION + 1
        val TRANSACTION_registerListener = FIRST_CALL_TRANSACTION + 2
        val TRANSACTION_unregisterListener = FIRST_CALL_TRANSACTION + 3
    }
}