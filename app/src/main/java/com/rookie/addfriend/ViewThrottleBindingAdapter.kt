package com.rookie.addfriend

import android.view.View
import androidx.databinding.BindingAdapter

object ViewThrottleBindingAdapter {
    @BindingAdapter("android:onClickListener")
    @JvmStatic fun setViewOnClickListener(view: View, callback: View.OnClickListener) {
        view.setOnClickListener(ThrottleOnClickListener(callback))
    }

    @BindingAdapter("android:onClick")
    @JvmStatic fun setViewOnClick(view: View, callback: View.OnClickListener) {
        view.setOnClickListener(ThrottleOnClickListener(callback))
    }

    /** 原始OnClickListener的包装 */
    class ThrottleOnClickListener(
        private val callback: View.OnClickListener
    ) : View.OnClickListener {

        // 上次点击时间
        private var mLastTime = 0L

        override fun onClick(v: View?) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - mLastTime >= CLICK_THRESHOLD) {
                mLastTime = currentTime
                // 调用点击方法
                callback.onClick(v)
            } else {
                logD("[ThrottleOnClickListener] [onClick] throttle")
            }
        }

        companion object {
            // 1秒之类的点击过滤掉
            private const val CLICK_THRESHOLD = 1000
        }
    }
}
