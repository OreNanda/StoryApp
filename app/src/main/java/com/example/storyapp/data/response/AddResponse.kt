package com.example.storyapp.data.response

import com.google.gson.annotations.SerializedName

data class AddResponse(

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null
)