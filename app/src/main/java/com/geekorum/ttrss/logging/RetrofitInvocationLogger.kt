package com.geekorum.ttrss.logging

import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import timber.log.Timber

/**
 * Log [Retrofit] method calls when installed as an OkHttp [Interceptor]
 */
class RetrofitInvocationLogger : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        request.tag(Invocation::class.java)?.let {
            val method = it.method()
            Timber.tag("Retrofit")
                .d("calling ${method.declaringClass.simpleName}.${method.name} ${it.arguments()}")
        }
        return chain.proceed(request)
    }
}
