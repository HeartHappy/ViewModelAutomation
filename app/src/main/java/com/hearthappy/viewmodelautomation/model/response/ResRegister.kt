package com.hearthappy.viewmodelautomation.model.response

data class ResRegister(val code: Int, val message: String, val result: Result?) {
    data class Result(val account: String, val createdAt: CreatedAt, val deletedAt: DeletedAt, val id: Int, val level: Int, val token: String, val updatedAt: UpdatedAt) {
        data class CreatedAt(val time: String)

        data class DeletedAt(val time: String)

        data class UpdatedAt(val time: String)
    }
}