package com.example.snack

class AppConfig {
    companion object {
        private val target = Target.PROD
        val instance by lazy {
            AppConfig()
        }
    }

    fun getBannerAdUnitId(): String{
        return if(target == Target.PROD){
            "ca-app-pub-9136945281854153/6833482996"
        }else {
            "ca-app-pub-3940256099942544/6300978111"
        }
    }

    fun getInterstitialAdUnitId(): String{
        return if(target == Target.PROD){
            "ca-app-pub-9136945281854153/6318477670"
        }else {
            "ca-app-pub-3940256099942544/1033173712"
        }
    }
}

enum class Target{
    DEV, TEST, PROD
}