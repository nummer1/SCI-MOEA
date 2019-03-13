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
        // TODO: create segment neighbour list
        // merge all segments that are under 100 pixels with closest neighbour (colour wise)

        val mergingIndexes = mutableListOf<Int>()
        val avgColour = mutableListOf<Triple<Double, Double, Double>>()

        for (pIndex in partitions!!.indices) {
            if (partitions!![pIndex].size < 100) {
                mergingIndexes.add(pIndex)
            }
        }

        for (segment in partitions!!) {
            val sum = mutableListOf(0, 0, 0)
            for (pixel in segment) {
                val colour = chrome.problem.colourList[pixel]
                sum[0] += colour.first
                sum[1] += colour.second
                sum[2] += colour.third
            }
            avgColour.add(Triple(sum[0].toDouble()/segment.size.toDouble(), sum[1].toDouble()/segment.size.toDouble(), sum[2].toDouble()/segment.size.toDouble()))
        }

        val remove = mutableListOf<Int>()
        for (i in mergingIndexes) {
            var closestNeighbour = -1
            var closestDistance = Double.MAX_VALUE
            for (n in partitionsNeighbours!![i]!!) {
                val distance = sqrt(Math.pow(avgColour[i].first - avgColour[n].first, 2.0) + Math.pow(avgColour[i].second - avgColour[n].second, 2.0) + Math.pow(avgColour[i].third - avgColour[n].third, 2.0))
                if (distance < closestDistance) {
                    closestNeighbour = n
                    closestDistance = distance
                }
            }
            if (closestNeighbour == -1) {
                println("Error in SegmentClass.mergeSmallSegments(): no closest neighbour")
            }
            // merge
            partitionsNeighbours!![closestNeighbour]!!.addAll(partitionsNeighbours!![i]!!)
            partitionsNeighbours!![i]!!.addAll(partitionsNeighbours!![closestNeighbour]!!)
            partitionsNeighbours!![closestNeighbour]!!.remove(i)
            partitionsNeighbours!![i]!!.remove(closestNeighbour)
            partitions!![closestNeighbour].addAll(partitions!![i])
            partitions!![i].addAll(partitions!![closestNeighbour])
            if (closestNeighbour !in remove) {
                remove.add(i)
            }
        }

        var sum = 0
        partitions!!.forEach { sum += it.size }
        println("sum1: $sum, ${partitions!!.size}")

        for (index in remove) {
            partitions!![index] = mutableListOf()
        }
        partitions = partitions!!.filter { it.isNotEmpty() }.toMutableList()

        sum = 0
        partitions!!.forEach { sum += it.size }
        println("sum2: $sum, ${partitions!!.size}")

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
            for (pixel in segments[sIndex]) {
                val neighbours = direction.getDirectNeighbours(pixel)
                for (n in neighbours) {
                    if (pixelToPartitionMap!![n] != sIndex) {
                        segmentsEdges[sIndex].add(pixel)
                        if (segmentsNeighbours.containsKey(sIndex)) {
                            segmentsNeighbours[sIndex]!!.add(pixelToPartitionMap!![n]!!)
                        } else {
                            segmentsNeighbours[sIndex] = mutableSetOf(pixelToPartitionMap!![n]!!)
                        }
                    }
                }
            }
        }
        edges = segmentsEdges
        partitionsNeighbours = segmentsNeighbours
    }
}