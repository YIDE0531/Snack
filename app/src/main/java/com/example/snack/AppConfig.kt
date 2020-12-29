package com.example.snack

class AppConfig {
    val target = Target.PROD
    val instance by lazy { AppConfig() }
}

enum class Target{
    DEV, TEST, PROD
}