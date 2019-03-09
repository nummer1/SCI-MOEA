import java.util.*
import kotlin.random.Random
import kotlin.math.sqrt


class Chromosome(val problem: Problem) {

    // genes is list of integers in range 0 to and including 4
    // 0: None, 1: Up, 2: Right, 3: Down, 4: Left
    val genes: MutableList<Int>
    private val width: Int
    private val height: Int
    var overallDeviation = 0.0
    var connectivityMeasure = 0.0
    public var crowdingDistance = 0.0

    init {
        width = problem.image.width
        height = problem.image.height
        genes = MutableList((height*width)) { -1 }
    }

    fun initializeMST() {
        val rand = Random.nextInt(0, genes.size)
        var currentVertex = Triple(rand, rand, 0)

        val mst = MutableList<Boolean>(genes.size) { false }
        val queue = PriorityQueue<Triple<Int,Int,Int>>(kotlin.Comparator { o1, o2 -> o1.third - o2.third })
        val key = MutableList<Int>(genes.size) { Int.MAX_VALUE }

        queue.add(currentVertex)
        key[rand] = 0

        while (queue.isNotEmpty()) {
            currentVertex = queue.poll()
            if (mst[currentVertex.first]) { continue }
            mst[currentVertex.first] = true
            for (n in getDirectNeighbours(currentVertex.first)) {
                val distance = problem.distance(n, currentVertex.first)
                if (!mst[n] && distance < key[n]) {
                    queue.add(Triple(n, currentVertex.first, distance))
                    key[n] = distance
                }
            }
            when (currentVertex.first - currentVertex.second) {
                0 -> genes[currentVertex.first] = 0 // None
                width -> genes[currentVertex.first] = 1 // Up
                -1 -> genes[currentVertex.first] = 2 // Right
                -width -> genes[currentVertex.first] = 3 // Down
                1 -> genes[currentVertex.first] = 4 // Left
                else -> println("Error with parent in Chromosome.initializeMST")
            }
        }

        for (gene in genes) {
            if (gene == -1) {
                println("Vertex not initialized in Chromosome.initializeMST")
                break
            }
        }

        overallDeviation()
        connectivityMeasure()
    }

    fun initializeRandom() {
        for (i in genes.indices) {
            genes[i] = Random.nextInt(0,5)
        }
        overallDeviation()
        connectivityMeasure()
    }

    fun uniformCrossover(parent1: Chromosome, parent2: Chromosome) {
        // creates chromosome from uniform crossover of parent1 and parent2
        for (i in 0.until(width*height)) {
            genes[i] = if (Random.nextBoolean()) parent1.genes[i] else parent2.genes[i]
        }

//        val segments = getSegments()
//        for (sIndex in segments.indices) {
//            if (segments[sIndex].size < 100) {
//                // merge segment
//            }
//        }

        overallDeviation()
        connectivityMeasure()
    }

    fun randomBitFlipMutation() {
        genes[Random.nextInt(0, genes.size)] = Random.nextInt(0,5)
    }

    fun mergeRandomSegmentsMutation() {
        // TODO
    }

    private fun getIndexDirection(original: Int, direction: Int): Int {
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

    private fun getNeighbours(pixel: Int): MutableList<Int> {
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

    private fun getDirectNeighbours(pixel: Int): MutableList<Int> {
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

    fun getSegmentsAsSet(): MutableList<Set<Int>> {
        // returns segments as a lis of sets
        val segmentsSet = mutableListOf<Set<Int>>()
        val segments = getSegments()
        for (segment in segments) {
            val set = mutableSetOf<Int>()
            set.addAll(segment)
            segmentsSet.add(set)
        }
        return segmentsSet
    }

    fun getSegmentEdges(): List<List<Int>> {
        // returns list of list of indexes, where each sublist is indexes on segment edges
        // check if all neighbours of pixel is in same segment, if not, add to edge list
        val segments = getSegmentsAsSet()
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

    private fun overallDeviation() {
        overallDeviation = 0.0
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
    }

    private fun connectivityMeasure() {
        connectivityMeasure = 0.0
        val segments = getSegmentsAsSet()
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
    }

    fun dominates(other: Chromosome): Boolean {
        // return true if this dominates other, else returns false
        return (connectivityMeasure <= other.connectivityMeasure && overallDeviation <= other.overallDeviation) &&
                (connectivityMeasure < other.connectivityMeasure || overallDeviation < other.overallDeviation)
    }
}