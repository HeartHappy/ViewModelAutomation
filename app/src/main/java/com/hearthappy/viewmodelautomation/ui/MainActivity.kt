package com.hearthappy.viewmodelautomation.ui

import android.Manifest
import android.graphics.BitmapFactory
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
import io.ktor.http.*
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
    private val file = File("${Environment.getExternalStorageDirectory().path}/DCIM/test.png")

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
                repeat(10){
                    viewModel.getImages(0, 5, it)
                }
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
                    is RequestState.SUCCEED   -> {
                        tvResult.showSucceedMsg(it.body.toString(), progress)
                        //order：接收结果order是传入的值，区分哪次请求的结果。
                        Log.d(TAG, "viewModelListener--->order: ${it.order}")
                    }
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
                        ivShowFile.setImageBitmap(BitmapFactory.decodeStream(it.body))
                        it.body.copyTo(file.outputStream())
//                        ivShowFile.setImageURI(Uri.fromFile(file))
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
                    is RequestState.LOADING   -> streamProgressBar.show()
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
                        viewModel.getDownloadFile("test.png") { receive, contentLength ->
                            Log.d(TAG, "onCreate: receive:$receive,contentLength:$contentLength")
                        }
                    }
                    2    -> {

//                        val part = MultipartBody.Part { part("file", file = file, contentDisposition = "filename=\"uploadFileName.png\"", contentType = ContentType.Image.PNG) }

                        val multiPart = MultipartBody.MultiPart {
                            //方式一：自定义headers构建part
                            part("file", file = file, headers = Headers.build {
                                append(HttpHeaders.ContentType, ContentType.Video.MP4)
                                append(HttpHeaders.ContentDisposition, "filename=\"ViewModelAutomation1.png\"")
                            })
                            //方式二：上传并指定文件名称
                            part("file", file = file, contentDisposition = "filename=\"ViewModelAutomation2.png\"", contentType = ContentType.Video.MP4)
                            //方式三：上传使用原有文件名
                            part("file", file = file)
                        }.formData {
                            append("description", "ViewModelAutomation")
                            append("username", "Leonardo DiCaprio")
                            append("password", "123456")
                        }


                        streamProgressBar.show(multiPart.contentLength.toInt())
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