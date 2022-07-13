package com.hearthappy.processor

import com.hearthappy.annotations.ServiceConfig
import com.hearthappy.processor.common.GENERATE_CONFIG_PKG
import com.hearthappy.processor.common.KTOR_DEFAULT_CONFIG
import com.hearthappy.processor.common.KTOR_NETWORK_PKG
import com.hearthappy.processor.common.application
import com.hearthappy.processor.model.ServiceConfigData
import com.squareup.kotlinpoet.*
import java.io.File
import javax.lang.model.element.Element

internal fun getServiceConfigList(serviceElements: Set<Element>): List<ServiceConfigData> {
    val list = mutableListOf<ServiceConfigData>()
    serviceElements.forEach {
        val serviceConfigs = it.getAnnotationsByType(ServiceConfig::class.java)
        serviceConfigs.forEach { serviceConfig ->
            list.add(ServiceConfigData(serviceConfig.key, serviceConfig.baseURL, serviceConfig.enableLog, serviceConfig.proxyIp, serviceConfig.proxyPort))
        }
    }
    return list
}

internal fun ViewModelProcessor.generateServiceConfig(createServiceConfigList: List<ServiceConfigData>, generatedSource: String) {
    if (createServiceConfigList.isNotEmpty()) {
        val fileName = "ServiceConfig"
        val file = FileSpec.builder(GENERATE_CONFIG_PKG, fileName)
        val defaultConfigClassName = ClassName(KTOR_NETWORK_PKG, KTOR_DEFAULT_CONFIG)
        createServiceConfigList.forEach { baseConfig ->
            file.addProperty(PropertySpec.builder(baseConfig.key, defaultConfigClassName, KModifier.PRIVATE).delegate("lazy{${delegateValue(baseConfig)}}").build())
            file.addFunction(FunSpec.builder(baseConfig.key).receiver(application).returns(defaultConfigClassName).addStatement("return ${baseConfig.key}").build())
        }
        file.build().writeTo(File(generatedSource))
        sendNoteMsg("==================> Create a $fileName file and write the service configuration to the file")
    }
}

internal fun delegateValue(serviceConfig: ServiceConfigData): String {
    val startBuilder = StringBuilder("DefaultConfig(baseURL=\"${serviceConfig.baseUrl}\",enableLog=${serviceConfig.enabledLog}")
    if (serviceConfig.proxyIp.isNotEmpty() && serviceConfig.proxyPort != -1) {
        startBuilder.append(",proxyIp=\"${serviceConfig.proxyIp}\" ,proxyPort=${serviceConfig.proxyPort}")
    }
    val endBuilder = startBuilder.append(")")
    return endBuilder.toString()
}