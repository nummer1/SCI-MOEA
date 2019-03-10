import java.util.*
import kotlin.random.Random
import kotlin.math.sqrt


class Chromosome(val problem: Problem, val direction: Direction) {

    // genes is list of integers in range 0 to and including 4
    // 0: None, 1: Up, 2: Right, 3: Down, 4: Left
    val genes: MutableList<Int>
    private val width: Int
    private val height: Int
    var overallDeviation = 0.0
    var connectivityMeasure = 0.0
    public var crowdingDistance = 0.0
    val segmentClass: SegmentClass

    init {
        width = problem.image.width
        height = problem.image.height
        genes = MutableList((height*width)) { -1 }
        segmentClass = SegmentClass(this, direction)
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
            for (n in direction.getDirectNeighbours(currentVertex.first)) {
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

        segmentClass.initialize()
        overallDeviation()
        connectivityMeasure()
    }

    fun initializeRandom() {
        for (i in genes.indices) {
            genes[i] = Random.nextInt(0,5)
        }
        segmentClass.initialize()
        overallDeviation()
        connectivityMeasure()
    }

    fun uniformCrossover(parent1: Chromosome, parent2: Chromosome) {
        // creates chromosome from uniform crossover of parent1 and parent2
        for (i in 0.until(genes.size)) {
            genes[i] = if (Random.nextBoolean()) parent1.genes[i] else parent2.genes[i]
        }
        segmentClass.initialize()
        overallDeviation()
        connectivityMeasure()
    }

    fun randomBitFlipMutation() {
        genes[Random.nextInt(0, genes.size)] = Random.nextInt(0,5)
    }

    fun mergeRandomSegmentsMutation() {
        // TODO
    }

    private fun overallDeviation() {
        overallDeviation = 0.0
        val segments = segmentClass.partitions!!
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
        val segments = segmentClass.partitionsSet!!
        for (segment in segments) {
            for (pixel in segment) {
                val neighbors = direction.getNeighbours(pixel)
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