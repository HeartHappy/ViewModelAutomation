package com.hearthappy.viewmodelautomation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hearthappy.annotations.AndroidViewModel
import com.hearthappy.annotations.BindLiveData
import com.hearthappy.annotations.BindStateFlow
import com.hearthappy.viewmodelautomation.model.request.ReLogin
import com.hearthappy.viewmodelautomation.model.response.ResLogin

@AndroidViewModel
@BindStateFlow("login", ReLogin::class, ResLogin::class)
class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
    }
}