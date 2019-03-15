import java.util.*
import kotlin.random.Random
import kotlin.math.sqrt


class Chromosome(val problem: Problem, val direction: Direction) {

    // genes is list of integers in range 0 to and including 4
    // 0: None, 1: Up, 2: Right, 3: Down, 4: Left
    val genes: MutableList<Int>
    var overallDeviation = 0.0
    var connectivityMeasure = 0.0
    var edgeValue = 0.0
    var crowdingDistance = 0.0
    val segmentClass: SegmentClass

    init {
        genes = MutableList((problem.height*problem.width)) { -1 }
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
                problem.width -> genes[currentVertex.first] = 1 // Up
                -1 -> genes[currentVertex.first] = 2 // Right
                -problem.width -> genes[currentVertex.first] = 3 // Down
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
        edgeValue()
    }

    fun initializeRandom() {
        for (i in genes.indices) {
            genes[i] = Random.nextInt(0,5)
        }
        segmentClass.initialize()
        overallDeviation()
        connectivityMeasure()
        edgeValue()
    }

    fun uniformCrossover(parent1: Chromosome, parent2: Chromosome) {
        // creates chromosome from uniform crossover of parent1 and parent2
        for (i in 0.until(genes.size)) {
            genes[i] = if (Random.nextBoolean()) parent1.genes[i] else parent2.genes[i]
        }
        segmentClass.initialize()
        overallDeviation()
        connectivityMeasure()
        edgeValue()
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
            val sum = mutableListOf(0.0, 0.0, 0.0)
            for (pixel in segment) {
                val colour = problem.colourList[pixel]
                sum[0] += colour.first
                sum[1] += colour.second
                sum[2] += colour.third
            }
            val average = MutableList<Double>(3) { sum[it]/segment.size.toDouble() }
            for (pixel in segment) {
                val colour = problem.colourList[pixel]
                overallDeviation += sqrt(Math.pow(colour.first - average[0], 2.0) + Math.pow(colour.second - average[1], 2.0) + Math.pow(colour.third - average[2], 2.0))
            }
        }
    }

    private fun connectivityMeasure() {
        connectivityMeasure = 0.0
        val segments = segmentClass.partitions!!
        val pixelMap = segmentClass.pixelToPartitionMap!!
        for (sIndex in segments.indices) {
            for (pixel in segments[sIndex]) {
                val neighbors = direction.getNeighbours(pixel)
                for (n in neighbors) {
                    if (pixelMap[n] != sIndex) {
                        connectivityMeasure += 1.0/8.0
                    }
                }
            }
        }
    }

    private fun edgeValue() {
        edgeValue = 0.0
        val edges = segmentClass.edges!!
        val map = segmentClass.pixelToPartitionMap!!
        for (edge in edges) {
            for (pixel in edge) {
                for (n in direction.getDirectNeighbours(pixel)) {
                    if (map[n] != map[pixel]) {
                        val c1 = problem.colourList[pixel]
                        val c2 = problem.colourList[n]
                        edgeValue += sqrt(Math.pow(c1.first - c2.first, 2.0) + Math.pow(c1.second - c2.second, 2.0) + Math.pow(c1.third - c2.third, 2.0))
                    }
                }
            }
        }
    }

    fun dominates(other: Chromosome): Boolean {
        // return true if this dominates other, else returns false
        return (connectivityMeasure <= other.connectivityMeasure && overallDeviation <= other.overallDeviation && edgeValue >= other.edgeValue) &&
                (connectivityMeasure < other.connectivityMeasure || overallDeviation < other.overallDeviation || edgeValue > other.edgeValue)
    }
}