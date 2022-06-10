package com.hearthappy.viewmodelautomation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hearthappy.annotations.AndroidViewModel

@AndroidViewModel
class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
    }
}