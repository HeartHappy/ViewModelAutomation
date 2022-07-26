## ViewModel、网络请求自动生成框架

#### 一、项目地址：[Github](https://github.com/HeartHappy/ViewModelAutomation)

#### 二、集成

- 如果你的项目 Gradle 配置是在 `7.0 以下`，需要在 `build.gradle` 文件中加入(默认jdk1.8)

  ```groovy
  allprojects {
      repositories {
          //远程仓库：https://jitpack.io
          maven { url 'https://jitpack.io' }
      }
  }
  ```

- 如果你的 Gradle 配置是 `7.0 及以上`，则需要在 `settings.gradle` 文件中加入（默认jdk11）

  ```groovy
  dependencyResolutionManagement {
      repositories {
          //远程仓库：https://jitpack.io
          maven { url 'https://jitpack.io' }
      }
  }
  ```

```groovy
plugins {
    id 'kotlin-kapt'
}
dependencies {
	//ktor网络框架
    implementation("io.ktor:ktor-client-core:1.6.0")
	//ktor扩展库
    implementation("com.github.hearthappy.viewmodelautomation:ktor-expand:1.0.0")
    //注解库
    compileOnly("com.github.hearthappy.viewmodelautomation:annotations:1.0.0")
    //处理注解自动生成库
    kapt("com.github.hearthappy.viewmodelautomation:processor:1.0.0")
}
```



#### 三、使用示例（或参考Demo）：

##### 1、使用@BindStateFlow或@BindLiveData生成命名函数并绑定StateFlow/LiveData属性

```kotlin
@AndroidViewModel
@BindLiveData("getImages", ReImages::class, ResImages::class)
class MainActivity : AppCompatActivity() {
 
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
```

##### 2、使用@Request注解生成网络请求（GET示例。其他类型参见，标题四：“HTTP请求”）

```kotlin
/**
 * GET:获取图片
 * @property page Int
 * @property size Int
 * @constructor
 */
@Request(urlString = "/getImages")
data class ReImages(val page: Int, val size: Int)
```

##### 3、使用ViewModel

```kotlin
//Activity中添加
private val viewModel by viewModels<MainViewModel>()

//监听
lifecycleScope.launchWhenCreated {
  viewModel.getImagesStateFlow.collect {
    when (it) {
      is RequestState.LOADING-> progress.show()
      is RequestState.SUCCEED -> tvResult.showSucceedMsg(it.body.toString())
      is RequestState.FAILED -> tvResult.showFailedMsg(it)
      is RequestState.Throwable -> tvResult.showThrowableMsg(it)
      else -> Unit
    }
  }
}
```

##### 4、生成MainViewModel

###### （1）完成步骤2基础上，点击AndroidStudio->Build->Make Project，编译生成MainViewModel类

###### （2）ViewModel生成路径：build/generated/source/kaptKotlin/debug/com/hearthappy/compiler/viewmodel/MainViewModel.kt



#### 四、HTTP请求

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

##### 7、全局配置

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

##### 8、添加header

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

##### 9、传入自定义请求

###### (1)生成test函数，并绑定StateFlow

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

###### (2)编写自己的网络请求

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



#### 五、注解说明：

##### 1、@AndroidViewModel

|        参数        |        描述         | 必须 |                            默认值                            |
| :----------------: | :-----------------: | :--: | :----------------------------------------------------------: |
| viewModelClassName | 自定义ViewModel类名 |  否  | 根据Activity和Fragment前缀生成。例如:LoginActivity则生成LoginViewModel |



##### 2、@BindLiveData与@BindStateFlow

|            参数            |              描述              | 必须 |                 默认值                  |
| :------------------------: | :----------------------------: | :--: | :-------------------------------------: |
|         methodName         |          自定义方法名          |  是  |                   无                    |
|        requestClass        |    标记@Request注解的请求类    |  否  |   默认生成:io参数，参数是一个挂起函数   |
|       responseClass        |      Json数据转换的实体类      |  否  |        默认返回String类型的文本         |
| liveDataName\stateFlowName | 自定义LiveData\StateFlow属性名 |  否  | 默认生成：methodName+LiveData\StateFlow |



##### 3、@Request

|    参数    |                       描述                       | 必须 |    默认值     |
| :--------: | :----------------------------------------------: | :--: | :-----------: |
|    type    | 指定 HTTP 方法（支持：GET、POST、PATCH、DELETE） |  否  |      GET      |
| urlString  |        URL字符串（域、路径、查询参数等）         |  是  |      无       |
| serviceKey |  使用多个全局配置时，可通过key关联指定全局配置   |  否  | defaultConfig |



##### 4、@Service（标记全局配置注解，使用@ServiceConfig注解时必须添加该注解）

##### 5、@ServiceConfig（支持多全局配置，通过key区分）

|   参数    |      描述       | 必须 |    默认值     |
| :-------: | :-------------: | :--: | :-----------: |
|  baseURL  |       域        |  是  |      无       |
| enableLog |  输出网络日志   |  否  |     true      |
|  proxyIp  |     代理IP      |  否  |      无       |
| proxyPort |    代理端口     |  否  |      无       |
|    key    | 关联指定请求key |  否  | defaultConfig |








