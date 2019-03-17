

class Direction(val width: Int, val height: Int) {

    fun getIndexDirection(original: Int, direction: Int): Int {
        // return index of pixel based on a pixel and a direction to go
        var returnValue = original
        when (direction) {
            0 -> returnValue = original
            1 -> returnValue = original - width
            2 -> returnValue = original + 1
            3 -> returnValue = original + width
            4 -> returnValue = original - 1
        }
        return if (returnValue >= 0 && returnValue < height * width) returnValue else original
    }

    fun getDirection(pixel1: Int, pixel2: Int): Int {
        var returnValue = 0
        when (pixel1 - pixel2) {
            0 -> returnValue = 0 // None
            width -> returnValue = 1 // Up
            -1 -> returnValue = 2 // Right
            -width -> returnValue = 3 // Down
            1 -> returnValue = 4 // Left
            else -> println("Error in Direction.getDirection()")
        }
        return returnValue
    }

    fun getNeighbours(pixel: Int): MutableList<Int> {
        val neighbours = mutableListOf(pixel+1, pixel-1, pixel-width, pixel+width, pixel-width+1, pixel+width+1, pixel-width-1, pixel+width-1)
        var i = 0
        while (i < neighbours.size) {
            if (neighbours[i] < 0 || neighbours[i] >= height * width) {
                neighbours.removeAt(i)
                i--
            }
            i++
        }
        return neighbours
    }

    fun getDirectNeighbours(pixel: Int): MutableList<Int> {
        val neighbours = mutableListOf(pixel+1, pixel-1, pixel-width, pixel+width)
        var i = 0
        while (i < neighbours.size) {
            if (neighbours[i] < 0 || neighbours[i] >= height * width) {
                neighbours.removeAt(i)
                i--
            }
            i++
        }
        return neighbours
    }
}