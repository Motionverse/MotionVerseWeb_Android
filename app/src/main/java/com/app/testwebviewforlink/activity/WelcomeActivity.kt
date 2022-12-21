package com.app.testwebviewforlink.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.app.testwebviewforlink.R

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        findViewById<TextView>(R.id.tvStartNow).setOnClickListener {
            startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
        }
    }
}