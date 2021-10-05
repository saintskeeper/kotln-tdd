sealed class Door {
    data class Open(val angle: Double) : Door() {
        fun close() = Unlocked
        fun changeAngle(delta: Double) = Open(angle + delta)
    }

    object Unlocked : Door() {
        fun open(degrees: Double) = Open(degrees)
        fun lock() = Locked(1)
    }

    data class Locked(val turns: Int) : Door() {
        fun unlock() = Unlocked
        fun turnKey(delta: Int) = Locked(turns + delta)
    }
}


val door = Door
    .Unlocked
    .lock()
    .turnKey(3)
    .unlock()
    .open(12.4)
    .changeAngle(34.5)
    .close()

println(door)
