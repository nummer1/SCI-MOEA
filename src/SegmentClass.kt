import java.util.*
import kotlin.math.sqrt


class SegmentClass(val chrome: Chromosome, val direction: Direction) {

    var partitions: MutableList<MutableList<Int>>?
    var pixelToPartitionMap: Map<Int, Int>?
    var edges: List<Set<Int>>?
    var partitionsNeighbours: MutableMap<Int, MutableSet<Int>>?

    init {
        // sets segments
        partitions = null
        pixelToPartitionMap = null
        edges = null
        partitionsNeighbours = null
    }

    fun initialize() {
        makeSegments()
        makeSegmentMap()
        makeSegmentEdges()
        // merge segments smaller then 100 pixels
        mergeSmallSegments()
        // remake map and edges to correspond with new segments
        makeSegmentMap()
        makeSegmentEdges()
    }

    private fun mergeSmallSegments() {
        // merges segments under 100 pixels

        fun setColour(segmentNumber: Int, colourList: MutableList<Triple<Double, Double, Double>>, pixelList: MutableList<Int>) {
            val sum = mutableListOf(0.0, 0.0, 0.0)
            for (pixel in pixelList) {
                val colour = chrome.problem.colourList[pixel]
                sum[0] += colour.first
                sum[1] += colour.second
                sum[2] += colour.third
            }
            val lSize = pixelList.size.toDouble()
            colourList[segmentNumber] = (Triple(sum[0]/lSize, sum[1]/lSize, sum[2]/lSize))
        }

        fun getDistance(colourList: MutableList<Triple<Double, Double, Double>>, i: Int, n: Int): Double {
            return sqrt(Math.pow(colourList[i].first - colourList[n].first, 2.0) + Math.pow(colourList[i].second - colourList[n].second, 2.0) + Math.pow(colourList[i].third - colourList[n].third, 2.0))
        }

        // TODO: convert to set?
        val remainingIndexes = mutableListOf<Int>()
        val avgColour = mutableListOf<Triple<Double, Double, Double>>()

        for (pIndex in partitions!!.indices) {
            if (partitions!![pIndex].size > chrome.problem.minSegmentSize) {
                remainingIndexes.add(pIndex)
            }
        }

        for (sIndex in partitions!!.indices) {
            avgColour.add(Triple(0.0, 0.0, 0.0))
            setColour(sIndex, avgColour, partitions!![sIndex])
        }

        val merged = MutableList<Boolean>(partitions!!.size) { it in remainingIndexes }
        // TODO: make queue-comparator accurate (do no use toInt())
        val queue = PriorityQueue<Triple<Int,Int,Double>>(Comparator { o1, o2 -> (o1.third - o2.third).toInt() })
        for (i in remainingIndexes) {
            for (n in partitionsNeighbours!![i]!!) {
                if (!merged[n]) {
                    queue.add(Triple(n, i, getDistance(avgColour, i, n)))
                }
            }
        }

        while (queue.isNotEmpty()) {
            val next = queue.poll()
            if (merged[next.first]) { continue }
            merged[next.first] = true
            for (n in partitionsNeighbours!![next.first]!!) {
                if (!merged[n]) {
                    queue.add(Triple(n, next.second, getDistance(avgColour, next.second, n)))
                }
            }
            partitions!![next.second].addAll(partitions!![next.first])
            partitions!![next.first] = mutableListOf<Int>()
        }

//        val merged = MutableList<Boolean>(partitions!!.size) { it in remainingIndexes }
//        var queueList = mutableListOf<Triple<Int, Int, Double>>()
//
//        for (i in remainingIndexes) {
//            for (n in partitionsNeighbours!![i]!!) {
//                if (!merged[n]) {
//                    queueList.add(Triple(n, i, getDistance(avgColour, i, n)))
//                }
//            }
//        }
//
//        while (queueList.isNotEmpty()) {
//            queueList.sortBy { it.third }
//            val next = queueList[0]
//            if (merged[next.first]) { println("MERGED ADDED TO QUEUE") }
//            merged[next.first] = true
//
//            partitions!![next.second].addAll(partitions!![next.first])
//            partitions!![next.first] = mutableListOf<Int>()
//            partitionsNeighbours!![next.first]!!.forEach { partitionsNeighbours!![next.second]!!.add(it) }
//            remainingIndexes.forEach { partitionsNeighbours!![it]!!.remove(it); partitionsNeighbours!![it]!!.remove(next.first) }
//            // partitionsNeighbours!![next.second]!!.addAll(partitionsNeighbours!![next.first]!!)
//            setColour(next.second, avgColour, partitions!![next.second])
//
//            queueList = mutableListOf()
//            for (i in remainingIndexes) {
//                for (n in partitionsNeighbours!![i]!!) {
//                    if (!merged[n]) {
//                        queueList.add(Triple(n, i, getDistance(avgColour, i, n)))
//                    }
//                }
//            }
//        }

        partitions = partitions!!.filter { it.isNotEmpty() }.toMutableList()

//        var sum = 0
//        partitions!!.forEach { sum += it.size }
//        println("sum: $sum, ${partitions!!.size}")

    }

    private fun makeSegments() {
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
            val pIndex = direction.getIndexDirection(index, chrome.genes[index])
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
        val inSegmentsList = MutableList(chrome.genes.size) { -1 }
        for (i in chrome.genes.indices) {
            addToSegment(inSegmentsList, segments, mutableListOf(), i)
        }

        partitions = segments
    }

    private fun makeSegmentMap() {
        val pixelToSegmentMap = mutableMapOf<Int, Int>()
        for (sIndex in partitions!!.indices) {
            for (pixel in partitions!![sIndex]) {
                pixelToSegmentMap[pixel] = sIndex
            }
        }
        pixelToPartitionMap = pixelToSegmentMap
    }

    private fun makeSegmentEdges() {
        // returns list of list of indexes, where each sublist is indexes on segment edges
        // check if all neighbours of pixel is in same segment, if not, add to edge list
        val segments = partitions!!
        val segmentsEdges = List<MutableSet<Int>>(segments.size) { mutableSetOf() }
        val segmentsNeighbours = mutableMapOf<Int, MutableSet<Int>>()

        for (sIndex in segments.indices) {
            segmentsNeighbours[sIndex] = mutableSetOf()
            for (pixel in segments[sIndex]) {
                val neighbours = direction.getDirectNeighbours(pixel)
                for (n in neighbours) {
                    if (pixelToPartitionMap!![n] != sIndex) {
                        segmentsEdges[sIndex].add(pixel)
                        if (segmentsNeighbours.containsKey(sIndex)) {
                            segmentsNeighbours[sIndex]!!.add(pixelToPartitionMap!![n]!!)
                        }
//                        } else {
//                            segmentsNeighbours[sIndex] = mutableSetOf(pixelToPartitionMap!![n]!!)
//                        }
                    }
                }
            }
        }
        edges = segmentsEdges
        partitionsNeighbours = segmentsNeighbours
    }
}