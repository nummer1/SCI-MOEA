import kotlin.random.Random
import kotlin.math.sqrt


class Chromosome(val problem: Problem) {

    // genes is list of integers in range 0 to and including 4
    // 0: None, 1: Up, 2: Right, 3: Down, 4: Left
    val genes = mutableListOf<Int>()
    val width: Int
    val height: Int

    init {
        width = problem.image.width
        height = problem.image.height
    }

    fun initializeMST() {
        // TODO: use Prim's algorithm
    }

    fun initializeRandom() {
        for (i in 0.until(height * width)) {
            genes.add(Random.nextInt(5))
        }
    }

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

    fun getSegments(): List<List<Int>> {
        // returns list of list of indexes, where each sublist is a segment

        // for all indexes:
        // if index already in segments: return index of that segment
        // else: add to parent segment

        fun addToSegment(inSegmentsList: MutableList<Int>, segments: MutableList<MutableList<Int>>, currentSegment: MutableList<Int>, index: Int): Int {
            if (inSegmentsList[index] != -1) {
                return inSegmentsList[index]
            }
            currentSegment.add(index)

            // p_index is not in segments
            val pIndex = getIndexDirection(index, genes[index])
            if (pIndex == index || pIndex in currentSegment) {
                segments.add(mutableListOf())
                segments.last().add(index)
                inSegmentsList[index] = segments.size - 1
                return segments.size - 1
            } else {
                val sIndex = addToSegment(inSegmentsList, segments, currentSegment, pIndex)
                segments[sIndex].add(index)
                inSegmentsList[index] = sIndex
                return sIndex
            }
        }

        val segments = MutableList<MutableList<Int>>(0)  { mutableListOf()}
        val inSegmentsList = MutableList(height*width) { -1 }
        for (i in genes.indices) {
            addToSegment(inSegmentsList, segments, mutableListOf(), i)
        }

        return segments
    }

    fun getSegmentEdges(): List<List<Int>> {
        // returns list of list of indexes, where each sublist is indexes on segment edges
        // check if all neighbours of pixel is in same segment, if not, add to edge list
        val segments = getSegments()
        val segmentEdges = List<MutableList<Int>>(segments.size) { mutableListOf() }
        for (sIndex in segments.indices) {
            for (pixel in segments[sIndex]) {
                val neighbors = getNeighbours(pixel)
                for (n in neighbors) {
                    if (!(n in segments[sIndex])) {
                        segmentEdges[sIndex].add(pixel)
                        break
                    }
                }
            }
        }
        return segmentEdges
    }

    fun overallDeviation(): Double {
        var overallDeviation = 0.0
        val segments = getSegments()
        for (segment in segments) {
            val sum = mutableListOf(0, 0, 0)
            for (pixel in segment) {
                val colour = problem.colourList[pixel]
                sum[0] += colour.first
                sum[1] += colour.second
                sum[2] += colour.third
            }
            val average = MutableList<Double>(3) { sum[it].toDouble()/segment.size.toDouble() }
            for (pixel in segment) {
                val colour = problem.colourList[pixel]
                overallDeviation += sqrt(Math.pow(colour.first - average[0], 2.0) + Math.pow(colour.second - average[1], 2.0) + Math.pow(colour.third - average[2], 2.0))
            }
        }
        return overallDeviation
    }

    fun connectivityMeasure(): Double {
        var connectivityMeasure = 0.0
        val segments = getSegments()
        for (segment in segments) {
            for (pixel in segment) {
                val neighbors = getNeighbours(pixel)
                for (n in neighbors) {
                    if (!(n in segment)) {
                        connectivityMeasure += 1.0/8.0
                    }
                }
            }
        }
        return connectivityMeasure
    }
}