#### HTTP请求

##### 1、GET

```kotlin
@Request(urlString = "/getImages") 
data class ReImages(val page: Int, val size: Int)
```

##### 2、POST

```kotlin
/**
 *方式一：ReRegister为Body
 *
 */
@Request(Http.POST, urlString = "/register") 
@Body
data class ReRegister(val account: String, val code: String, val password: String)


/**
 * 方式二：需要传入动态header或REST时
 */
@Request(Http.POST, urlString = "/login")
data class ReLogin(@Header("X-Auth-Token") val token:String,@Body val loginBody: LoginBody)

data class LoginBody(val account: String, val password: String)
```

##### 3、FormUrlEncode

```kotlin
@Request(Http.POST, urlString = "/login")
data class ReLogin(@Body(BodyType.FormUrlEncoded) val loginBody: LoginBody)

data class LoginBody(@Query("account")val account: String,@Query("password") val password: String)
```

##### 4、PATCH

```kotlin
@Request(Http.PATCH, urlString = "/identity/v3/users/{userid}")
data class ReUpdatePwd(@Header("X-Auth-Token") val token: String, val userid: String, @Body(BodyType.JSON) val updatePasswordBody: UpdatePasswordBody)


data class UpdatePasswordBody(val user:UserBean){
    data class UserBean(val password:String)
}
```

##### 5、DELETE

```kotlin
@Request(Http.DELETE,"/veaudit/v2/vir_msg/{instance_id}")
data class ReErrorMessReset(@Header("X-Auth-Token") val token: String,val instance_id:String)
```

##### 6、REST

```kotlin
/**
 * 
 * @property token String
 * @property flavorid String ：REST(注意：参数flavorid名称必须与{flavorid}一致 )
 * @constructor
 */
@Request(urlString = "/compute/v2.1/flavors/{flavorid}")
data class ReVMHardwareInfo(@Header("X-Auth-Token") val token: String, val flavorid: String)
```



##### 7、添加header

```kotlin
/**
 * @annotation @Headers([ContentType.xxx.xxx]):固定Headers
 * @property token String ：动态Header
 * @property instance_id String REST
 * @constructor
 */
@Headers([ContentType.Application.Json])
@Request(Http.DELETE,"/veaudit/v2/vir_msg/{instance_id}")
data class ReErrorMessReset(@Header("X-Auth-Token") val token: String,val instance_id:String)
```

##### 8、全局配置

```kotlin
/**
 * 一、静态全局配置（支持动态baseURL、动态代理、网络日志输出）
 */
@Service
@ServiceConfig(baseURL = "https://api.apiopen.top/api")
class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
    }
}

/**
 * 二：动态配置
 *（注：defaultConfig（）函数。根据@ServiceConfig注解参数“key”生成）
 * 
 */
class LoginActivity: BaseActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
     			
     		//动态修改全局配置
     		application.defaultConfig().apply {
            baseURL = "http://xxx.xxx.com"
            proxyIP = "xxx.xxx.x.x"
            proxyPort = 8888
            enableLog = true
        }
   }
}

```

##### 9、文件下载

###### （1）定义一个全局文件上传下载的服务地址

```kotlin
@Service
@ServiceConfig(baseURL = "https://api.apiopen.top/api")
//定义一个文件上传下载的服务地址
@ServiceConfig(baseURL = "http://192.168.51.62:9998", key = "fileOperate")
class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
```

###### （2）定义请求类

```kotlin
/**
 * 注意：使用@Streaming注解标记该请求为文件下载
 * @property fileName String
 * @constructor
 */
@Streaming
@Request(Http.GET, urlString = "/fs/{fileName}", serviceKey = "fileOperate")
data class ReqDownloadFile(val fileName: String)
```

###### （3）Activity中声明BindStateFlow、BindLiveData绑定请求

```kotlin

/**
 * 注意：下载请求的接收类型必须指定为：InputStream
 */
@AndroidViewModel
@BindStateFlow("getDownloadFile", ReqDownloadFile::class, InputStream::class)
class MainActivity : BaseActivity()
```

###### （4）Make Project生成代码如下

```kotlin
public class MainViewModel(private val app: Application) : AndroidViewModel(app){
  		private val _getDownloadFileStateFlow: MutableStateFlow<RequestState<InputStream>> by lazy { MutableStateFlow(RequestState.DEFAULT) }

      public val getDownloadFileStateFlow: StateFlow<RequestState<InputStream>> = _getDownloadFileStateFlow
      
      /**
     * 根据ReqDownloadFile请求类，自动生成如下代码。
     * @param fileName String
     * @param listener SuspendFunction2<[@kotlin.ParameterName] Long, [@kotlin.ParameterName] Long, Unit> 添加ProgressListener类型监听。接收下载进度
     * @return Unit
     */
      public fun getDownloadFile(fileName: String, listener: ProgressListener): Unit {
        _getDownloadFileStateFlow.value = RequestState.LOADING
        requestScope<InputStream>(io = {
            sendKtorDownload(httpType = GET, url = "${app.fileOperate().baseURL}/fs/$fileName", listener = listener, defaultConfig = app.fileOperate())
        }, onFailure = {
            _getDownloadFileStateFlow.value = RequestState.FAILED(it)
        }, onSucceed = { body, response ->
            _getDownloadFileStateFlow.value = RequestState.SUCCEED(body, response)
        }, onThrowable = { _getDownloadFileStateFlow.value = RequestState.Throwable(it) })
    }
}

```

###### （5）调用下载请求

```kotlin
viewModel.getDownloadFile("01 Rolling In the Deep.m4p") { receive, contentLength ->
   Log.d(TAG, "onCreate: receive:$receive,contentLength:$contentLength")}
```

###### （6）监听请求结果

```kotlin
lifecycleScope.launchWhenCreated {
  viewModel.getDownloadFileStateFlow.collect {
    when (it) {
      is RequestState.LOADING   -> streamProgressBar.show()
      is RequestState.SUCCEED   -> {
        ivShowFile.setImageBitmap(BitmapFactory.decodeStream(it.body))
      }
      is RequestState.FAILED    -> tvResult.showFailedMsg(it, streamProgressBar)
      is RequestState.Throwable -> tvResult.showThrowableMsg(it,streamProgressBar)
      else                      -> Unit
    }
  }
}
```

##### 10、文件上传

###### （1）定义一个全局文件上传下载的服务地址（代码参见文件下载步骤:（1））

###### （2）定义请求类

```kotlin
/**
 * 注意：使用@Multipart注解，标记该请求为文件上传
 * @property multipartBody MultipartBody
 * @constructor
 */
@Multipart
@Request(Http.POST, urlString = "/multipart-upload-json", serviceKey = "fileOperate")
data class ReUploadFile(val multipartBody: MultipartBody)
```

###### （3）Activity中声明BindStateFlow、BindLiveData绑定请求

```kotlin
/**
 * 注意：这里未指定接收类型，默认为：String
 */
@AndroidViewModel
@BindStateFlow("uploadFile", ReUploadFile::class)
class MainActivity : BaseActivity()
```

###### （4）Make Project生成代码（可通过生成的ViewModel类自行查看）

###### （5）调用上传请求

```kotlin

//单一文件上传
val part = MultipartBody.Part { part("file", file = file, contentDisposition = "filename=\"uploadFileName.png\"", contentType = ContentType.Image.PNG) }

//多文件同时上传
val multiPart = MultipartBody.MultiPart {
    //方式一：自定义headers构建part
    part("file", file = file, headers = Headers.build {
      append(HttpHeaders.ContentType, ContentType.Video.MP4)
      append(HttpHeaders.ContentDisposition, "filename=\"ViewModelAutomation1.mp4\"")
    })
    //方式二：上传并指定文件名称
    part("file", file = file, contentDisposition = "filename=\"ViewModelAutomation2.mp4\"", contentType = ContentType.Video.MP4)
    //方式三：上传使用原有文件名
    part("file", file = file)
}.formData {
  append("description", "ViewModelAutomation")
  append("username", "Leonardo DiCaprio")
  append("password", "123456")
}

//显示进度条，并设置进度最大值
streamProgressBar.show(multiPart.contentLength.toInt())
//调用上传
viewModel.uploadFile(multiPart) { bytesSentTotal, contentLength ->
     streamProgressBar.progress = bytesSentTotal.toInt()
     Log.d(TAG, "onCreate: bytesSentTotal:$bytesSentTotal,contentLength:$contentLength")}
```

###### （6）监听请求结果

```kotlin
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
```

##### 11、并发相同请求

###### （1）请求类（假若在同一时间并发该请求，添加@Order注解修饰）

```kotlin
@Order
@Request(urlString = "/getImages")
data class ReImages(val page: Int, val size: Int)
```

###### （2）生成代码如下，并且新增一个order为Int类型的参数，在响应结果时一起返回

```Kotlin
public fun getImages(page: Int, size: Int, order: Int): Unit {
        _getImagesStateFlow.value = RequestState.LOADING
        requestScope<ResImages>(io = {
            sendKtorRequest(httpType = GET, url = "${app.defaultConfig().baseURL}/getImages", parameters = {
                parameter("page", page)
                parameter("size", size)
            }, defaultConfig = app.defaultConfig())
        }, onFailure = { _getImagesStateFlow.value = RequestState.FAILED(it, order) }, onSucceed = { body, response ->
            _getImagesStateFlow.value = RequestState.SUCCEED(body, response, order)
        }, onThrowable = { _getImagesStateFlow.value = RequestState.Throwable(it, order) })
    }
```

###### （3）调用

```kotlin
repeat(10){
     viewModel.getImages(0, 5, it)
}
```

###### （4）监听

```kotlin
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
```

###### （5）输出日志（可以清晰看出接收结果是哪个请求）

<img src="/Users/chenrui/Library/Application Support/typora-user-images/image-20220811170554569.png" alt="image-20220811170554569" style="zoom:50%;" />



##### 12、传入自定义请求

###### （1）生成test函数，并绑定StateFlow

```kotlin
@AndroidViewModel
@BindStateFlow("test")
class LoginActivity : AppCompatActivity()
```

生成代码如下：

```kotlin
public fun test(io: suspend () -> HttpResponse): Unit {
    _testStateFlow.value = RequestState.LOADING
    requestScope<String>(
        io = io,
        onFailure = { _testStateFlow.value = RequestState.FAILED(it) },
        onSucceed = { body, response -> 
                     _testStateFlow.value = RequestState.SUCCEED(body,response) 
                    },
        onThrowable = { _testStateFlow.value = RequestState.Throwable(it) }
    )
}
```

###### （2）编写自己的网络请求

```kotlin
suspend inline fun <reified T> login() = ktorClient().use {
    it.get<T>("https://ktor.io/") {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        parameter("price","asc")
    }
}
```

###### （3）调用test函数并传入网络请求，再通过生成的StateFlow实现监听

```kotlin
viewModel.test{ login() }
```

