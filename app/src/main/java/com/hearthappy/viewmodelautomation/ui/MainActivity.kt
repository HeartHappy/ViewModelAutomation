package com.hearthappy.viewmodelautomation.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.hearthappy.annotations.AndroidViewModel
import com.hearthappy.annotations.BindLiveData
import com.hearthappy.annotations.BindStateFlow
import com.hearthappy.compiler.viewmodel.MainViewModel
import com.hearthappy.ktorexpand.code.network.RequestState
import com.hearthappy.ktorexpand.code.network.Result
import com.hearthappy.viewmodelautomation.databinding.ActivityMainBinding
import com.hearthappy.viewmodelautomation.model.request.ReImages
import com.hearthappy.viewmodelautomation.model.request.ReVideoList
import com.hearthappy.viewmodelautomation.model.response.ResImages
import com.hearthappy.viewmodelautomation.model.response.ResVideoList
import com.hearthappy.viewmodelautomation.ui.base.BaseActivity


/**
 *
 *
 * 测试用例：
 * 1、参考requestClass中@Request的注解参数，传入请求类型,url（注意：requestClass必须是data class类型）
 *
 * @property viewModel MainViewModel
 */
@AndroidViewModel

@BindLiveData("getVideoList", ReVideoList::class, ResVideoList::class)
@BindStateFlow("getImages", ReImages::class, ResImages::class)
class MainActivity: BaseActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.apply {
            viewModelListener()

            btnGetImages.setOnClickListener {
                viewModel.getImages(0, 5)
            }

            btnGetVideoList.setOnClickListener {
                progress.show()
                viewModel.getVideoList(0, 2)
            }
        }
    }

    private fun ActivityMainBinding.viewModelListener() {

        viewModel.getVideoListLiveData.observe(this@MainActivity) {
            progress.hide()
            when (it) {
                is Result.Success -> tvResult.showSucceedMsg(it.body.toString())
                is Result.Failed -> tvResult.showFailedMsg(it)
                is Result.Throwable -> tvResult.showThrowableMsg(it)
                else -> {
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.getImagesStateFlow.collect {
                when (it) {
                    is RequestState.LOADING -> progress.show()
                    is RequestState.SUCCEED -> tvResult.showSucceedMsg(it.body.toString(), progress)
                    is RequestState.FAILED -> tvResult.showFailedMsg(it, progress)
                    is RequestState.Throwable -> tvResult.showThrowableMsg(it, progress)
                    else -> Unit
                }
            }
        }
    }
}