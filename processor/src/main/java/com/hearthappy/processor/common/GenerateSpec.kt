package com.hearthappy.processor.common

import com.squareup.kotlinpoet.*

/**
 * 生成属性并直接初始化
 * @param propertyName String 属性命名
 * @param propertyType TypeName 支持传入ParameterizedTypeName、ClassName。（即：泛型或一般类型）
 * @param initValue String 初始化值
 * @param modifier Array<out KModifier>
 * @return PropertySpec
 */
fun generatePropertySpec(propertyName: String, propertyType: TypeName, initValue: String, vararg modifier: KModifier): PropertySpec {
    return PropertySpec.builder(propertyName, propertyType).initializer(initValue).addModifiers(*modifier).build()
}


/**
 * 生成委托属性
 * @param propertyName String
 * @param propertyType TypeName
 * @param delegateValue String
 * @param modifier Array<out KModifier>
 * @return PropertySpec
 */
fun generateDelegatePropertySpec(propertyName: String, propertyType: TypeName, delegateValue: String, vararg modifier: KModifier): PropertySpec {
    return PropertySpec.builder(propertyName, propertyType).delegate("lazy{$delegateValue}").addModifiers(*modifier).build()
}

fun generateClass(className: String, constructorParameters: List<ParameterSpec> = listOf(), isAddConstructorProperty: Boolean = true, superClassName: ClassName? = null): TypeSpec.Builder {
    return TypeSpec.classBuilder(className).apply {
        if (constructorParameters.isNotEmpty()) { //创建构造参数
            primaryConstructor(FunSpec.constructorBuilder().addParameters(constructorParameters).build())

            if (isAddConstructorProperty) { //创建构造参数属性，（即：添加val ，提供内部引用）
                constructorParameters.forEach { addProperty(generatePropertySpec(it.name, it.type, it.name, KModifier.PRIVATE)) }
            }
            constructorParameters.forEach { addSuperclassConstructorParameter(it.name) }
        }
        superClassName?.let { superclass(superClassName) }
    }
}

fun generateFunctionSpec() {}