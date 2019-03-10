class SegmentClass(val chrome: Chromosome, val direction: Direction) {

    var partitions: List<MutableList<Int>>?
    var partitionsSet: List<Set<Int>>?
    var edges: List<MutableList<Int>>?

    init {
        // sets segments
        partitions = null
        partitionsSet = null
        edges = null
    }

    fun initialize() {
        partitions = getSegments()
        partitionsSet = getSegmentsAsSet()
        edges = getSegmentEdges()
    }

    private fun getSegments(): List<MutableList<Int>> {
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

        // TODO: create segment neighbour list
        // merge all segments that are under 100 pixels with closest neighbour (colour wise)
        
//        val segments = getSegments()
//        for (sIndex in segments.indices) {
//            if (segments[sIndex].size < 100) {
//                // merge segment
//            }
//        }

        return segments
    }

    private fun getSegmentsAsSet(): List<Set<Int>> {
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

    private fun getSegmentEdges(): List<MutableList<Int>> {
        // returns list of list of indexes, where each sublist is indexes on segment edges
        // check if all neighbours of pixel is in same segment, if not, add to edge list
        val segments = getSegmentsAsSet()
        val segmentEdges = List<MutableList<Int>>(segments.size) { mutableListOf() }
        for (sIndex in segments.indices) {
            for (pixel in segments[sIndex]) {
                val neighbors = direction.getNeighbours(pixel)
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
}