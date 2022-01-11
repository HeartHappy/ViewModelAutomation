package com.hearthappy.viewmodelautomation.model.response


class ResRegister(val msg: String, val data: Data) {

    data class Data(val registerTime: Long)
}
