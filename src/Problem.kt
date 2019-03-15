import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.sqrt


class Problem(private val fileName: String, var minSegmentSize: Int) {

    val image: BufferedImage
    val pixelSize = 1
    val sqrtPSize = pixelSize * pixelSize
    val height: Int
    val width: Int
    val colourList: MutableList<Triple<Double, Double, Double>> = mutableListOf()
    val folder = "solutions"

    init {
        minSegmentSize /= sqrtPSize
        image = ImageIO.read(File(fileName))
        height = image.height/pixelSize
        width = image.width/pixelSize
        for (y in 0.until(height)) {
            for (x in 0.until(width)) {
                var red = 0.0
                var green = 0.0
                var blue = 0.0
                // , Pair(x+1, y), Pair(x, y+1), Pair(x+1, y+1)
                for (cor in mutableListOf<Pair<Int, Int>>(Pair(x, y))) {
                    val rgb = image.getRGB(cor.first, cor.second)
                    // val alpha = rgb shr 24 and 0xFF
                    red += rgb shr 16 and 0xFF
                    green += rgb shr 8 and 0xFF
                    blue += rgb and 0xFF
                }

                colourList.add(Triple(red/sqrtPSize, green/sqrtPSize, blue/sqrtPSize))
            }
        }
    }

    fun drawOnBlank(chrome: Chromosome, name: String) {
        // draw on blank canvas with black edges
        val black = Integer.parseInt("000000", 16)
        val white = Integer.parseInt("FFFFFF", 16)
        val newImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
        for (x in 0.until(newImage.width)) {
            for (y in 0.until(newImage.height)) {
                newImage.setRGB(x, y, white)
            }
        }
        val edges = chrome.segmentClass.edges!!
        for (edge in edges) {
            for (pixel in edge) {
                newImage.setRGB((pixel%width)*pixelSize, (pixel/width)*pixelSize, black)
            }
        }
        ImageIO.write(newImage, "png", File(folder + '/' + name))
    }

    fun drawOnImage(chrome: Chromosome, name: String) {
        // draw on original image with green edges
        val green = Integer.parseInt("00FF00", 16)
        val newImage = ImageIO.read(File(fileName))
        val edges = chrome.segmentClass.edges!!
        for (edge in edges) {
            for (pixel in edge) {
                newImage.setRGB((pixel%width)*pixelSize, (pixel/width)*pixelSize, green)
            }
        }
        ImageIO.write(newImage, "png", File(folder + '/' + name))
    }

    fun distance(pixel1: Int, pixel2: Int): Int {
        return sqrt(Math.pow((colourList[pixel1].first - colourList[pixel2].first), 2.0) +
                Math.pow((colourList[pixel1].second - colourList[pixel2].second), 2.0) +
                Math.pow((colourList[pixel1].third - colourList[pixel2].third), 2.0)).toInt()
    }
}