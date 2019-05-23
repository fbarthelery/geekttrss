package com.geekorum.ttrss.manage_feeds

import android.app.Activity
import android.os.Bundle
import android.widget.TextView


class TestActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "hello world"
        })
    }
}
