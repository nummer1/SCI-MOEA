import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.sqrt


class Problem(private val fileName: String, var minSegmentSize: Int, val useLab: Boolean) {

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
        for (pixel in 0.until(height*width)) {
            var red = 0.0
            var green = 0.0
            var blue = 0.0
            for (cor in getRealPixelList(pixel)) {
                val rgb = image.getRGB(cor.first, cor.second)
                // val alpha = rgb shr 24 and 0xFF
                red += rgb shr 16 and 0xFF
                green += rgb shr 8 and 0xFF
                blue += rgb and 0xFF
            }
            if (useLab) {
                colourList.add(rgbToLab(mutableListOf(red, green, blue)))
            } else {
                colourList.add(Triple(red / sqrtPSize, green / sqrtPSize, blue / sqrtPSize))
            }
        }
    }

    // TODO: only works when pixelSize is 1: gives coordinates out of bounds
    private fun getRealPixelList(pixel: Int): MutableList<Pair<Int, Int>> {
        val real = mutableListOf<Pair<Int, Int>>()
        for (i in 0.until(pixelSize)) {
            for (j in 0.until(pixelSize)) {
                real.add(Pair((pixel%width) + i, (pixel/width) + (j * width)))
            }
        }
        return real
    }

    fun rgbToLab(inputColor: MutableList<Double>): Triple<Double, Double, Double> {

        var num = 0
        val RGB = mutableListOf<Double>(0.0, 0.0, 0.0)

        for (val1 in inputColor) {
            var val2 = val1 / 255

            if (val2 > 0.04045) {
                val2 = Math.pow((val2 + 0.055) / 1.055, 2.4)
            } else {
                val2 = val2 / 12.92
            }
            RGB[num] = val2 * 100
            num += 1
        }

        val XYZ = mutableListOf<Double>(0.0, 0.0, 0.0)

        val X = RGB[0] * 0.4124 + RGB[1] * 0.3576 + RGB[2] * 0.1805
        val Y = RGB[0] * 0.2126 + RGB[1] * 0.7152 + RGB[2] * 0.0722
        val Z = RGB[0] * 0.0193 + RGB[1] * 0.1192 + RGB[2] * 0.9505
        XYZ[0] = X
        XYZ[1] = Y
        XYZ[2] = Z

        // Observer = 2°, Illuminant = D65
        XYZ[0] = XYZ[0] / 95.047         // ref_X = 95.047
        XYZ[1] = XYZ[1] / 100.0          // ref_Y = 100.000
        XYZ[2] = XYZ[2] / 108.883        // ref_Z = 108.883

        num = 0
        for (val1 in XYZ) {
            var val2 = val1

            if (val2 > 0.008856) {
                val2 = Math.pow(val2, 1.0/3.0)
            } else {
                val2 = (7.787 * val2) + (16 / 116)
            }
            XYZ[num] = val2
            num += + 1
        }

        val L = (116 * XYZ[1]) - 16
        val a = 500 * (XYZ[0] - XYZ[1])
        val b = 200 * (XYZ[1] - XYZ[2])
        val Lab = Triple(L, a, b)
        return Lab
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

    fun distance(pixel1: Int, pixel2: Int): Double {
        return sqrt(Math.pow((colourList[pixel1].first - colourList[pixel2].first), 2.0) +
                Math.pow((colourList[pixel1].second - colourList[pixel2].second), 2.0) +
                Math.pow((colourList[pixel1].third - colourList[pixel2].third), 2.0))
    }
}