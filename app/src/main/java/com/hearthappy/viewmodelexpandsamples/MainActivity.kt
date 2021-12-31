package com.hearthappy.viewmodelexpandsamples

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.hearthappy.annotations.AndroidViewModel
import com.hearthappy.annotations.BindLiveData
import com.hearthappy.annotations.BindStateFlow
import com.hearthappy.compiler.MainViewModel
import com.hearthappy.ktorexpand.code.network.Result
import com.hearthappy.viewmodelexpandsamples.model.login
import com.hearthappy.viewmodelexpandsamples.model.response.ResHome

@AndroidViewModel(viewModelClassName = "MainViewModel")
@BindLiveData(methodName = "login", responseClass = String::class, liveDataName = "loginLiveData")
@BindStateFlow(methodName = "home", responseClass = ResHome::class, stateFlowName = "homeStateFlow")
class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.loginLiveData.observe(this) {
            when (it) {
                is Result.Success -> {}
                is Result.Error -> {}
            }
        }
        viewModel.login { login() }
    }
}