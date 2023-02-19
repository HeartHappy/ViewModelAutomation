[![](https://jitpack.io/v/HeartHappy/viewmodelautomation.svg)](https://jitpack.io/#HeartHappy/viewmodelautomation)

## 自动生成ViewModel、网络请求框架

#### 一、项目地址：[Github](https://github.com/HeartHappy/ViewModelAutomation)

- 框架优势：通过提供的注解自动生成ViewModel层的LiveData、StateFlow，以及网络请求。节省您大量的开发时间
- 交流方式： 
   -   1、欢迎使用issues提建议或bug
   -   2、欢迎Star以兹鼓励

  

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
- 在项目 app 模块下的 build.gradle 文件中加入远程依赖和kapt插件
```groovy

plugins {
    id 'kotlin-kapt'
}


dependencies {
    //ktor网络框架
    implementation("io.ktor:ktor-client-core:1.6.0")
    //ktor扩展库
    implementation("com.github.hearthappy.viewmodelautomation:ktor-expand:2.0.6")
    //注解库
    compileOnly("com.github.hearthappy.viewmodelautomation:annotations:2.0.6")
    //处理注解自动生成库
    kapt("com.github.hearthappy.viewmodelautomation:processor:2.0.6")
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


#### 四、[HTTP请求](https://github.com/HeartHappy/ViewModelAutomation/blob/2.0.3/HTTP.md)

#### 五、[注解简介](https://github.com/HeartHappy/ViewModelAutomation/blob/2.0.3/Annotation.md)








