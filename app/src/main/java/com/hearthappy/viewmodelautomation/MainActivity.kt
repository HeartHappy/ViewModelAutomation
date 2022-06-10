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
import com.hearthappy.compiler.MainViewModel
import com.hearthappy.ktorexpand.code.network.RequestState
import com.hearthappy.viewmodelautomation.model.request.*
import com.hearthappy.viewmodelautomation.model.response.ResHome
import com.hearthappy.viewmodelautomation.model.response.ResLogin
import com.hearthappy.viewmodelautomation.model.response.ResLoginBean
import com.hearthappy.viewmodelautomation.model.response.ResRegister
import kotlinx.coroutines.flow.collect


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
@BindStateFlow(methodName = "register", requestClass = ReRegister::class, responseClass = ResRegister::class)
@BindLiveData(methodName = "login2", requestClass = ReLoginBean::class, responseClass = ResLoginBean::class)
@BindLiveData(methodName = "delete", requestClass = ReDelete::class, responseClass = String::class)
@BindLiveData(methodName = "userInfo", requestClass = ReUserInfo::class, responseClass = String::class)
class MainActivity: AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //        viewModel.loginLiveData.observe(this) {
        //            when (it) {
        //                is Result.Success -> {
        //                    Log.d(TAG, "onCreate: ${it.data}")
        //                }
        //                is Result.Error -> {
        //                    Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
        //                }
        //            }
        //        }
        //        viewModel.login("wxx_1", "24cff18577e8dc8c6fdf53a6621a0b4d") //        viewModel.login("asc")
        lifecycleScope.launchWhenCreated {
            viewModel.loginStateFlow.collect {
                when (it) {
                    is RequestState.SUCCEED<*> -> {
                        Log.d(TAG, "onCreate: ${it.responseBody}")
                    }
                    is RequestState.FAILED -> {
                        Log.d(TAG, "onCreate: ${it.failedBody}")
                    }
                    is RequestState.Throwable -> {
                        Log.d(TAG, "onCreate: ${it.throwable}")
                    }
                    else -> {
                    }
                }
            }
        }


    }

    companion object {
        private const val TAG = "MainActivity"
    }

    fun btnRequest(view: View) {
        viewModel.login(ReLogin("user_3", "24cff18577e8dc8c6fdf53a6621a0b4d"))
    }
}