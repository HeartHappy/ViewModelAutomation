package com.hearthappy.viewmodelautomation.ui

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.hearthappy.annotations.AndroidViewModel
import com.hearthappy.annotations.BindLiveData
import com.hearthappy.annotations.BindStateFlow
import com.hearthappy.compiler.viewmodel.MainViewModel
import com.hearthappy.ktorexpand.code.network.*
import com.hearthappy.viewmodelautomation.databinding.ActivityMainBinding
import com.hearthappy.viewmodelautomation.model.request.ReImages
import com.hearthappy.viewmodelautomation.model.request.ReUploadFile
import com.hearthappy.viewmodelautomation.model.request.ReVideoList
import com.hearthappy.viewmodelautomation.model.request.ReqDownloadFile
import com.hearthappy.viewmodelautomation.model.response.ResImages
import com.hearthappy.viewmodelautomation.model.response.ResVideoList
import com.hearthappy.viewmodelautomation.ui.base.BaseActivity
import java.io.File
import java.io.InputStream


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
@BindStateFlow("getDownloadFile", ReqDownloadFile::class, InputStream::class)
@BindStateFlow("uploadFile", ReUploadFile::class)
class MainActivity : BaseActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private lateinit var viewBinding: ActivityMainBinding
    private val file = File("${Environment.getExternalStorageDirectory().path}/DCIM/test.mp4")

    private lateinit var activityResult: ActivityResultLauncher<Array<String>>
    private var operate = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.apply {

            viewModelListener()

            registerActivityResult()

            btnGetImages.setOnClickListener {
                viewModel.getImages(0, 5, 1)
            }

            btnGetVideoList.setOnClickListener {
                progress.show()
                viewModel.getVideoList(0, 2)
            }

            btnDownloadFile.setOnClickListener {
                operate = 1
                activityResult.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            }

            btnUploadFile.setOnClickListener {
                operate = 2
                activityResult.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            }
        }
    }


    private fun ActivityMainBinding.viewModelListener() {

        viewModel.getVideoListLiveData.observe(this@MainActivity) {
            progress.hide()
            when (it) {
                is Result.Success   -> tvResult.showSucceedMsg(it.body.toString())
                is Result.Failed    -> tvResult.showFailedMsg(it)
                is Result.Throwable -> tvResult.showThrowableMsg(it)
                else                -> Unit
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.getImagesStateFlow.collect {
                when (it) {
                    is RequestState.LOADING   -> progress.show()
                    is RequestState.SUCCEED   -> tvResult.showSucceedMsg(it.body.toString(), progress)
                    is RequestState.FAILED    -> tvResult.showFailedMsg(it, progress)
                    is RequestState.Throwable -> tvResult.showThrowableMsg(it, progress)
                    else                      -> Unit
                }
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.getDownloadFileStateFlow.collect {
                when (it) {
                    is RequestState.LOADING   -> streamProgressBar.show()
                    is RequestState.SUCCEED   -> {
                        streamProgressBar.hide()
                        it.body.copyTo(file.outputStream())
//                        ivShowFile.setImageURI(Uri.fromFile(file))
//                        ivShowFile.setImageBitmap(BitmapFactory.decodeStream(it.body))
                    }
                    is RequestState.FAILED    -> tvResult.showFailedMsg(it, streamProgressBar)
                    is RequestState.Throwable -> tvResult.showThrowableMsg(it, streamProgressBar)
                    else                      -> Unit
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.uploadFileStateFlow.collect {
                when (it) {
                    is RequestState.LOADING   -> streamProgressBar.show((file.length() * 4).toInt())
                    is RequestState.SUCCEED   -> tvResult.showSucceedMsg(it.body, streamProgressBar)
                    is RequestState.FAILED    -> tvResult.showFailedMsg(it, streamProgressBar)
                    is RequestState.Throwable -> tvResult.showThrowableMsg(it, streamProgressBar)
                    else                      -> Unit
                }
            }
        }
    }


    private fun ActivityMainBinding.registerActivityResult() {
        activityResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val result = it.values.contains(false).not()
            if (!result) toast("权限拒绝！请打开权限")
            else {
                when (operate) {
                    1    -> {
                        viewModel.getDownloadFile("01 Rolling In the Deep.m4p") { receive, contentLength ->
                            streamProgressBar.max = contentLength.toInt()
                            streamProgressBar.progress = receive.toInt()
                            Log.d(TAG, "onCreate: receive:$receive,contentLength:$contentLength")
                        }
                    }
                    2    -> {

//                        val multipartBody = MultipartBody.Part { PartData("file", file = file, contentDisposition = "uploadFileName.png", mediaType = ContentType.Image.PNG) }

                        val multiPart = MultipartBody.MultiPart { list ->
                            list.add(PartData("file", file = file, contentDisposition = "ViewModelAutomation1.mp4"))
                            list.add(PartData("file", file = file, contentDisposition = "ViewModelAutomation2.mp4"))
                            list.add(PartData("file", file = file, contentDisposition = "ViewModelAutomation3.mp4"))
                            list.add(PartData("file", file = file, contentDisposition = "ViewModelAutomation4.mp4"))
                        }
                        viewModel.uploadFile(multiPart) { bytesSentTotal, contentLength ->
                            streamProgressBar.progress = bytesSentTotal.toInt()
                            Log.d(TAG, "onCreate: bytesSentTotal:$bytesSentTotal,contentLength:$contentLength")
                        }
                    }
                    else -> {}
                }
            }
        }
    }


    companion object {
        private const val TAG = "MainActivity"
    }
}