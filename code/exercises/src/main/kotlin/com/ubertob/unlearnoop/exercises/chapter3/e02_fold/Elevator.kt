package com.ubertob.unlearnoop.exercises.chapter3.e02_fold

data class Elevator(val floor: Int)

sealed class Direction

object Up : Direction()

object Down : Direction()
