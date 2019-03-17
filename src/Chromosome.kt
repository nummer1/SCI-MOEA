import java.util.*
import kotlin.math.sqrt
import kotlin.random.Random


class Chromosome(val problem: Problem, val direction: Direction) {

    // genes is list of integers in range 0 to and including 4
    // 0: None, 1: Up, 2: Right, 3: Down, 4: Left
    val genes: MutableList<Int>
    var overallDeviation = 0.0
    var connectivityMeasure = 0.0
    var edgeValue = 0.0
    var crowdingDistance = 0.0
    var segmentClass: SegmentClass

    init {
        genes = MutableList((problem.height*problem.width)) { -1 }
        segmentClass = SegmentClass(this, direction)
    }

//    fun initializeMSTKruskal() {
//        val edges = mutableListOf<Triple<Int, Int, Double>>()
//        for (i in genes.indices) {
//            for (n in direction.getDirectNeighbours(i)) {
//                edges.add(Triple(i, n, problem.distance(i, n)))
//            }
//        }
//        edges.sortBy { it.third }
//
//        val disjointMap = mutableListOf<Element>()
//        val mst = mutableListOf<Triple<Int, Int, Double>>()
//        val added = MutableList<Boolean>(genes.size) { false }
//
//        for (v in 0.until(genes.size)) {
//            val e = Element()
//            e.makeSet(v)
//            disjointMap.add(e)
//        }
//
//        for (edge in edges) {
//            val from = disjointMap[edge.first].findSet()
//            val to = disjointMap[edge.second].findSet()
//            if (from.value != to.value) {
//                //if (from.size + to.size < 10000) {
//                if (!added[disjointMap[edge.first].value]) {
//                    mst.add(edge)
//                    added[disjointMap[edge.first].value] = true
//                    disjointMap[edge.first].union(disjointMap[edge.second])
//                }
//            }
//        }
//
//        println("sizes: ${edges.size}, ${mst.size}")
//
//        val disMap2 = mutableMapOf<Int, Element>()
//        for (v in 0.until(genes.size)) {
//            val e = Element()
//            e.makeSet(v)
//            disMap2[v] = e
//        }
//
//        for (edge in mst) {
//            disMap2[edge.first]!!.union(disMap2[edge.second]!!)
//        }
//
//        val segMap = mutableMapOf<Int, MutableList<Int>>()
//        for (v in genes.indices) {
//            val rep = disMap2[v]!!.findSet().value
//            if (segMap.contains(rep)) {
//                segMap[rep]!!.add(v)
//            } else {
//                segMap[rep] = mutableListOf(v)
//            }
//        }
//
//        val segList = mutableListOf<MutableList<Int>>()
//        for (seg in segMap.values) {
//            println(seg.size)
//            segList.add(seg)
//        }
//        val segClass = SegmentClass(this, direction)
//        segClass.partitions = segList
//        segClass.makeSegmentMap()
//        segClass.makeSegmentEdges()
//        this.segmentClass = segClass
//
//
////        for (key in segMap.keys) {
////            val closed = mutableSetOf<Int>()
////            val open = segMap[key]!!
////            while (open.size > 0) {
////                val node = open.random()
////                closed.add(node)
////                open.removeAt(0)
////                for (n in direction.getDirectNeighbours(node)) {
////                    if (closed.contains(n) || open.contains(node)) {
////                        continue
////                    }
////
////                }
////            }
////        }
////
////        for (edge in mst.subList(10, mst.size)) {
////            when (edge.first - edge.second) {
////                0 -> genes[edge.first] = 0 // None
////                problem.width -> genes[edge.first] = 1 // Up
////                -1 -> genes[edge.first] = 2 // Right
////                -problem.width -> genes[edge.first] = 3 // Down
////                1 -> genes[edge.first] = 4 // Left
////                else -> println("Error with parent in Chromosome.initializeMST")
////            }
////        }
//
////        segmentClass.initialize()
////        overallDeviation()
////        connectivityMeasure()
////        edgeValue()
//    }

    fun initializeMSTPrim() {
        val rand = Random.nextInt(0, genes.size)
        var currentVertex = Triple(rand, rand, 0.0)

        val mst = MutableList<Boolean>(genes.size) { false }
        val queue = PriorityQueue<Triple<Int,Int,Double>>(kotlin.Comparator { o1, o2 -> 1000*(o1.third - o2.third).toInt() })
        val key = MutableList<Double>(genes.size) { Double.MAX_VALUE }

        queue.add(currentVertex)
        key[rand] = 0.0

        while (queue.isNotEmpty()) {
            currentVertex = queue.poll()
            if (mst[currentVertex.first]) {
                continue
            }
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

//    fun randomBitFlipMutation() {
//        genes[Random.nextInt(0, genes.size)] = Random.nextInt(1,5)
//    }

    fun flipRandomSegmentsMutation() {
        // TODO
        val rand = Random.nextInt(0, segmentClass.partitions!!.size)
        for (pixel in segmentClass.partitions!![rand]) {
            genes[pixel] = Random.nextInt(1, 5)
        }
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
                        edgeValue += problem.distance(pixel, n)
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

    fun isLarger(other: Chromosome): Boolean {
        // returns true if this is larger than other
        val returnVal: Boolean
        if (this.dominates(other)) {
            returnVal = true
        } else if (other.dominates(this)) {
            returnVal = false
        }
        else {
            returnVal = this.crowdingDistance > other.crowdingDistance
        }
        return returnVal
    }

    fun getFitness(largestConn: Double, largestDev: Double, largestEdge: Double, maxSegmentCount: Int, minSegmentCount: Int): Double {
        var fitness = 0.0
        fitness += 1 * (1 - connectivityMeasure/largestConn) + 3 * (1 - overallDeviation/largestDev) + 2 * (edgeValue/largestEdge)
        if (getNumberSegments() > maxSegmentCount) {
            fitness -= 1 * (getNumberSegments() / maxSegmentCount)
        } else if (getNumberSegments() < minSegmentCount) {
            fitness -= 1 * (minSegmentCount / getNumberSegments())
        }
            getNumberSegments()
        return fitness
    }

    fun getNumberSegments(): Int {
        return segmentClass.partitions!!.size
    }
}