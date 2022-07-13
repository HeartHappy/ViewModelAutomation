package com.hearthappy.processor.model

import com.hearthappy.annotations.BodyType
import javax.lang.model.element.Element

/**
 *
 * @property bodyType BodyType
 * @property jsonParameterName Any? 注意：只有Body（JSON）时，才有值，否则空
 * @property xwfParameters Pair<String, Map<String, String>>? 描述： Pair.first : 方法参数名,  Pair.second : Fields的Map集合. 注意： 只有Body（FormUrlEncoded）时才会有值，否则空
 * @constructor
 */
data class RequestBodyData(val bodyType: BodyType, val jsonParameterName: Any? = null, val xwfParameters: Pair<String, Map<String, String>>? = null)
