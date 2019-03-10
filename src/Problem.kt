import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.sqrt


class Problem(private val fileName: String) {

    val image: BufferedImage
    val colourList: MutableList<Triple<Int, Int, Int>> = mutableListOf()
    val folder = "solutions"

    init {
        image = ImageIO.read(File(fileName))
        for (y in 0.until(image.height)) {
            for (x in 0.until(image.width)) {
                val rgb = image.getRGB(x, y)
                // val alpha = rgb shr 24 and 0xFF
                val red = rgb shr 16 and 0xFF
                val green = rgb shr 8 and 0xFF
                val blue = rgb and 0xFF
                colourList.add(Triple(red, green, blue))
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
                newImage.setRGB(pixel%image.width, pixel/image.width, black)
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
                newImage.setRGB(pixel%image.width, pixel/image.width, green)
            }
        }
        ImageIO.write(newImage, "png", File(folder + '/' + name))
    }

    fun distance(pixel1: Int, pixel2: Int): Int {
        return sqrt(Math.pow((colourList[pixel1].first.toDouble() - colourList[pixel2].first.toDouble()), 2.0) +
                Math.pow((colourList[pixel1].second.toDouble() - colourList[pixel2].second.toDouble()), 2.0) +
                Math.pow((colourList[pixel1].third.toDouble() - colourList[pixel2].third.toDouble()), 2.0)).toInt()
    }
}