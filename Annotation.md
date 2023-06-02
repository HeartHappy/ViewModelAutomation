
#### 注解简介：

##### 1、@AndroidViewModel
###### 描述：生成ViewModel类注解

|        参数        | 类型  |        描述         | 必须 |                            默认值                            |
| :----------------: | ----- | :-----------------: | :--: | :----------------------------------------------------------: |
| viewModelClassName | Sting | 自定义ViewModel类名 |  否  | 根据Activity和Fragment前缀生成。例如:LoginActivity则生成LoginViewModel |



##### 2、@BindLiveData与@BindStateFlow
###### 描述：生成LiveData或StateFlow属性和请求函数注解

|            参数            | 类型      |              描述              | 必须 |                 默认值                  |
| :------------------------: | --------- | :----------------------------: | :--: | :-------------------------------------: |
|         methodName         | String    |          自定义方法名          |  是  |                   无                    |
|        requestClass        | KClass<*> |    标记@Request注解的请求类    |  否  |   默认生成:io参数，参数是一个挂起函数   |
|       responseClass        | KClass<*> |      Json数据转换的实体类      |  否  |        默认返回String类型的文本         |
| liveDataName\stateFlowName | String    | 自定义LiveData\StateFlow属性名 |  否  | 默认生成：methodName+LiveData\StateFlow |

##### 3、@Service
###### 描述：标记全局配置注解，使用@ServiceConfig注解时必须添加该注解

##### 4、@ServiceConfig
###### 描述：生成全局配置。支持多全局，通过key区分

|   参数    | 类型    |      描述       | 必须 |    默认值     |
| :-------: | ------- | :-------------: | :--: | :-----------: |
|  baseURL  | String  |       域        |  是  |      无       |
| enableLog | Boolean |  输出网络日志   |  否  |     true      |
|  proxyIp  | String  |     代理IP      |  否  |      无       |
| proxyPort | Int     |    代理端口     |  否  |      无       |
|    key    | String  | 关联指定请求key |  否  | defaultConfig |

##### 5、@Request
###### 描述：生成请求函数注解，根据该注解类参数生成对应请求函数

|    参数    | 类型   |                       描述                       | 必须 |    默认值     |
| :--------: | ------ | :----------------------------------------------: | :--: | :-----------: |
|    type    | Http   | 指定 HTTP 方法（支持：GET、POST、PATCH、DELETE） |  否  |      GET      |
| urlString  | String |        URL字符串（域、路径、查询参数等）         |  是  |      无       |
| serviceKey | String |  使用多个全局配置时，可通过key关联指定全局配置   |  否  | defaultConfig |

##### 6、@Body

|   参数   | 类型     |                      描述                       | 必须 | 默认值 |
| :------: | -------- | :---------------------------------------------: | :--: | :----: |
| bodyType | BodyType | 类型：支持TEXT、JSON、FormData、FormUrlEncoded |  否  |  JSON  |

##### 7、@Query
###### 描述：生成formData请求时使用。Body类型为FormData时，必须添加该注解修饰参数

| 参数  | 类型   | 描述 | 必须 | 默认值 |
| :---: | ------ | :--: | :--: | :----: |
| value | String | key  |  是  |   无   |

##### 8、@Header 
###### 描述：动态header

| 参数  |  类型  |    描述     | 必须 | 默认值 |
| :---: | :----: | :---------: | :--: | :----: |
| value | String | header的key |  是  |   无   |

##### 9、@Headers
###### 描述：固定headers，目前支持com.hearthappy.annotations.ContentType类中的常量

|  参数   |     类型      |       描述       | 必须 | 默认值 |
| :-----: | :-----------: | :--------------: | :--: | :----: |
| headers | Array<String> | 固定的header类型 |  否  |   无   |

##### 10、@Multipart
###### 描述：标记文件上传注解

##### 11、@Streaming
###### 描述：标记文件下载注解

##### 12、@Order
###### 描述：标记顺序注解。场景：区分并发相同请求，接收类型相同，但无法判断结果是哪个请求时。使用该注解，标记接收结果是哪个请求，示例参考：HTTP.md--->示例11
