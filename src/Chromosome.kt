import java.awt.image.BufferedImage
import java.io.File
import kotlin.random.Random
import javax.imageio.ImageIO


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
        // TODO
    }

    fun initializeRandom() {
        for (i in 0.until(height * width)) {
            genes.add(Random.nextInt(5))
        }
    }

    fun getIndex(original: Int, direction: Int): Int {
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
            val pIndex = getIndex(index, genes[index])
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
        // TODO (check if all neighbours of pixel is in same segment, if not, add to edge list)
        // returns list of list of indexes, where each sublist is indexes on segment edges
        return mutableListOf()
    }

    fun overallDeviation() {
        // TODO
        val segments = getSegments()
    }
}


class Problem(fileName: String) {

    val image: BufferedImage
    // val colourList: List<Triple<Int, Int, Int>>

    init {
        if (fileName.equals("TEST")) {
            image = BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB)
        } else {
            image = ImageIO.read(File(fileName))
        }
    }

    fun getDistance() {

    }

}