package ca.warp7.android.scouting.model2019

data class DataPoint(val type: Byte, val value: Byte, val time: Byte): Iterable<Byte>{
    override fun iterator() = byteArrayOf(type, value, time).iterator()
}