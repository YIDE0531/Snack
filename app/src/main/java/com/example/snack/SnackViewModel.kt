package com.example.snack

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.concurrent.fixedRateTimer
import kotlin.random.Random

class SnackViewModel: ViewModel() {
    val body = MutableLiveData<List<Position>>()
    val apple = MutableLiveData<Position>()
    val score = MutableLiveData<Int>()
    var gameState = MutableLiveData<GameState>()
    private val snackBody = mutableListOf<Position>()
    private var direction = Direction.LEFT
    private lateinit var applePos: Position
    private var point: Int = 0

    @ExperimentalStdlibApi
    fun start(){
        score.postValue(point)
        snackBody.apply {
            add(Position(10, 10))
            add(Position(11, 10))
            add(Position(12, 10))
            add(Position(13, 10))
        }.also {
            body.value = it
        }
        generateApple()
        fixedRateTimer("timer", true, 1000, 250){
            val pos = snackBody.first().copy().apply {
                when(direction){
                    Direction.LEFT -> x--
                    Direction.RIGHT -> x++
                    Direction.TOP -> y--
                    Direction.DOWN -> y++
                }
                if(snackBody.contains(this)||x<=0||x>=20||y<=0||y>=20){
                    cancel()
                    gameState.postValue(GameState.GAME_OVER)
                }
            }
            snackBody.add(0, pos)
            if(pos != applePos){
                snackBody.removeLast()
            }else{
                point+=100
                score.postValue(point)
                generateApple()
            }
            body.postValue(snackBody)
        }
    }

    private fun generateApple(){
        val spots = mutableListOf<Position>().apply {
            for(i in 0..19){
                for(j in 0..19){
                    add(Position(i, j))
                }
            }
        }
        spots.removeAll(snackBody)
        spots.shuffle()
        applePos = spots[0]
//        do {
//            applePos = Position(Random.nextInt(20), Random.nextInt(20))
//        } while (snackBody.contains(applePos))

        apple.postValue(applePos)
    }


    @ExperimentalStdlibApi
    fun reset(){
        point = 0
        score.postValue(0)
        snackBody.clear()
        direction = Direction.LEFT
        start()
    }

    fun move(direction: Direction){
        this.direction = direction
    }
}

data class Position(var x: Int, var y: Int)

enum class Direction{
    TOP, DOWN, LEFT, RIGHT
}

enum class GameState{
    ONGOING, GAME_OVER
}