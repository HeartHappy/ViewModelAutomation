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
@BindLiveData(methodName = "deleteUser", requestClass = ReDelete::class, responseClass = String::class)
@BindLiveData(methodName = "userInfo", requestClass = ReUserInfo::class, responseClass = String::class)
@BindStateFlow("getAppOption", ReAppOption::class, String::class)
class MainActivity: AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        lifecycleScope.launchWhenCreated {

            viewModel.loginStateFlow.collect {
                when (it) {
                    is RequestState.SUCCEED -> {
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

        viewModel.userInfo(ReUserInfo("", ""))

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
        viewModel.login(ReLogin("user_3", "24cff18577e8dc8c6fdf53a6621a0b4d"))
    }

    fun btnRequest2(view: View) {
        Log.d(TAG, "btnRequest2: ")
        val token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ2ZXN5c3RlbSIsInN1YiI6ImF1dGhfdG9rZW4iLCJhdWQiOiJodHRwczovL3d3dy52ZXN5c3RlbS5jb20vIiwiZXhwIjoxNjU1MTAxMjQzLCJuYmYiOjE2NTUwOTk0NDMsImlhdCI6MTY1NTA5NzY0MywianRpIjoiNjkxODM2ODIwMzA0NTEwMTU3MCIsImlkIjoiNjkxODM2ODIwMzA0NTEwMTU3MCIsInVzZXJuYW1lIjoidXNlcl8zIiwiYWNjZXNzIjp7InJvb3QiOmZhbHNlLCJ1c2VyIjpudWxsLCJ3b3JrX29yZGVyIjpudWxsLCJtZXNzYWdlIjpudWxsLCJhcHAiOm51bGwsInN0b3JhZ2UiOm51bGx9fQ.pvJKgwwZ0g3xGCyI7yCSz5Z_iudbBVb0DDGMsyuPbPGCmZa5z5EMqLTB6cKwUpe8yu3BWAE7Z6FSxoVAw4U1HDP4Ilo_5OJubwIDagWkhGdR65aKDE5UHJVH_js8dlkVk9r31IAFohToUFqCzm4gR7E4pyuW1z4B4QMFbtvkZzbb1ZinmRAG21zJfTg8MjjCVIRiftDAf7CkB0E5kkIXPLXYl8HzhbXG6k22AZCqrg0-lD-fbd85ORbifh5Twh6dEcfmJsIub027PISdG1glj534tXwFT15UchtuCs4lusM84F9SA_DyUHvC0aJ0FNB60w2bbi3lFjK_WdJ3-SD-Iw" //        viewModel.getAppOption(token, ReAppOptionData("6927483212241215488", "6926050667729281024"))
    }
}