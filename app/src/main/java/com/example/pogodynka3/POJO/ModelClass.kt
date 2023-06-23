package com.example.pogodynka.POJO

import com.google.gson.annotations.SerializedName

data class ModelClass(

    @SerializedName("weather") val weather:List<Weather>,
    @SerializedName("main") val main:Main,
    @SerializedName("wind") val wind:Wind,
    @SerializedName("sys") val sys:Sys,
    @SerializedName("name") val name:String,

)