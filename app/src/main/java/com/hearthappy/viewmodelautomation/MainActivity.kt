package com.hearthappy.viewmodelautomation

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hearthappy.annotations.AndroidViewModel
import com.hearthappy.annotations.BindLiveData
import com.hearthappy.annotations.BindStateFlow
import com.hearthappy.compiler.viewmodel.MainViewModel
import com.hearthappy.ktorexpand.code.network.RequestState
import com.hearthappy.ktorexpand.code.network.Result
import com.hearthappy.viewmodelautomation.databinding.ActivityMainBinding
import com.hearthappy.viewmodelautomation.model.request.*
import com.hearthappy.viewmodelautomation.model.response.ResHome
import com.hearthappy.viewmodelautomation.model.response.ResLogin
import com.hearthappy.viewmodelautomation.model.response.ResRegister
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime


/**
 *
 *
 * 测试用例：
 * 1、生成挂起高阶函数，由开发者自定义请求类型并传入请求
 * 2、开发者定义请求Bean，根据定义的请求类型生成相应请求参数，如Get，Post，Form，Delete，Patch.注意：requestClass必须是data class类型
 *
 * @property viewModel MainViewModel
 */
@AndroidViewModel
@BindStateFlow(methodName = "login", requestClass = ReLogin::class, responseClass = ResLogin::class)
@BindStateFlow(methodName = "home", requestClass = ReHome::class, responseClass = ResHome::class)
@BindStateFlow(
    methodName = "register", requestClass = ReRegister::class, responseClass = ResRegister::class
) @BindLiveData(
    methodName = "deleteUser", requestClass = ReDelete::class, responseClass = String::class
) @BindLiveData(
    methodName = "userInfo", requestClass = ReUserInfo::class, responseClass = String::class
) @BindStateFlow("getAppOption", ReAppOption::class, String::class) class MainActivity :
    AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private lateinit var viewBinding: ActivityMainBinding
    var  currentTimeMillis=0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)


        lifecycleScope.launchWhenCreated {

            viewModel.loginStateFlow.collect {
                when (it) {
                    is RequestState.SUCCEED -> {
                        val time = System.currentTimeMillis() - currentTimeMillis
                        viewBinding.tvResult.text = it.body.toString()
                        Log.d(TAG, "onCreate: time:$time")
                        Log.d(TAG, "onCreate SUCCEED: ${it.body}")
                    }
                    is RequestState.FAILED -> {
                        Log.d(TAG, "onCreate FAILED: ${it.failedBody}")
                    }
                    is RequestState.Throwable -> {
                        Log.d(TAG, "onCreate Throwable: ${it.throwable}")
                    }
                    else -> {
                    }
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.getAppOptionStateFlow.collect {
                when (it) {
                    is RequestState.SUCCEED<*> -> {
                        val s = it.body as String
                        Log.d(TAG, "onCreate: $s")
                    }
                    is RequestState.FAILED -> {
                        Log.d(TAG, "onCreate: ${it.failedBody.text}")
                    }
                    is RequestState.Throwable -> {
                        Log.d(TAG, "onCreate: ${it.throwable.message}")
                    }
                    else -> {
                    }
                }
            }
        }


        viewModel.userInfoLiveData.observe(this) {
            when (it) {
                is Result.Success -> {
                    it.data
                }
                is Result.Error -> {
                    it.message
                }
                is Result.Throwable -> {
                    it.e.message
                }
                else -> {
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    fun btnRequest(view: View) {
        Log.d(TAG, "btnRequest: ")
         currentTimeMillis= System.currentTimeMillis()
        viewModel.login(ReLogin("user_3", "24cff18577e8dc8c6fdf53a6621a0b4d"))
    }
}