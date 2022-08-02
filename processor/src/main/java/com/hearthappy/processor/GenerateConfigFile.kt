package com.hearthappy.processor

import com.hearthappy.annotations.ServiceConfig
import com.hearthappy.processor.common.*
import com.hearthappy.processor.model.ServiceConfigData
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import java.io.File
import javax.lang.model.element.Element

internal fun getServiceConfigList(serviceElements: Set<Element>): List<ServiceConfigData> {
    val list = mutableListOf<ServiceConfigData>()
    for (serviceElement in serviceElements) {
        val serviceConfigs = serviceElement.getAnnotationsByType(ServiceConfig::class.java)
        for (serviceConfig in serviceConfigs) list.add(ServiceConfigData(serviceConfig.key, serviceConfig.baseURL, serviceConfig.enableLog, serviceConfig.proxyIP, serviceConfig.proxyPort))
    }
    return list
}

internal fun ViewModelProcessor.generateServiceConfigFile(createServiceConfigList: List<ServiceConfigData>, generatedSource: String) {
    if (createServiceConfigList.isNotEmpty()) {
        val fileName = "ServiceConfig"
        val file = FileSpec.builder(GENERATE_CONFIG_PKG, fileName)
        val defaultConfigClassName = ClassName(NETWORK_PKG, NETWORK_DEFAULT_CONFIG)
        for (baseConfig in createServiceConfigList) {
            val generateConfigProperty = generateDelegatePropertySpec(baseConfig.key, defaultConfigClassName, createDefaultConfig(baseConfig), KModifier.PRIVATE)
            file.addProperty(generateConfigProperty)
            file.addFunction(FunSpec.builder(baseConfig.key).receiver(application).returns(defaultConfigClassName).addStatement("return ${baseConfig.key}").build())
        }
        file.build().writeTo(File(generatedSource))
        sendNoteMsg("==================> Create a $fileName file and write the service configuration to the file")
    }
}

private fun createDefaultConfig(serviceConfig: ServiceConfigData): String {
    val startBuilder = StringBuilder("DefaultConfig(baseURL=\"${serviceConfig.baseUrl}\",enableLog=${serviceConfig.enabledLog}")
    if (serviceConfig.proxyIP.isNotEmpty() && serviceConfig.proxyPort != -1) {
        startBuilder.append(",proxyIP=\"${serviceConfig.proxyIP}\" ,proxyPort=${serviceConfig.proxyPort}")
    }
    val endBuilder = startBuilder.append(")")
    return endBuilder.toString()
}