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
            //colourList.add(rgbToLab(red, green, blue))
            colourList.add(Triple(red/sqrtPSize, green/sqrtPSize, blue/sqrtPSize))
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

    private fun rgbToLab(r: Double, g: Double, b: Double): Triple<Double, Double, Double> {
        var var_R: Double = (r/255).toDouble()
        var var_G: Double = (g/255).toDouble()
        var var_B: Double = (b/255).toDouble()

        var_R = if (var_R > 0.04045) Math.pow((var_R + 0.055) / 1.055, 2.4) else var_R/12.92
        var_G = if (var_G > 0.04045) Math.pow((var_G + 0.055) / 1.055, 2.4) else var_G/12.92
        var_B = if (var_B > 0.04045) Math.pow((var_B + 0.055) / 1.055, 2.4) else var_B/12.92

        var_R *= 100
        var_G *= 100
        var_B *= 100

        var X = var_R * 0.4124 + var_G * 0.3576 + var_B * 0.1805
        var Y = var_R * 0.2126 + var_G * 0.7152 + var_B * 0.0722
        var Z = var_R * 0.0193 + var_G * 0.1192 + var_B * 0.9505

        X /= 100
        Y /= 100
        Z /= 100

        X = if (X > 0.008856) Math.pow(X, 1.0/3.0) else (7.787 * X) + (16 / 116)
        Y = if (Y > 0.008856) Math.pow(Y, 1.0/3.0) else (7.787 * Y) + (16 / 116)
        Z = if (Z > 0.008856) Math.pow(Z, 1.0/3.0) else (7.787 * Z) + (16 / 116)

        val L = (116 * Y) - 16
        val a = 500 * (X - Y)
        val b = 200 * (Y - Z)
        println("$L, $a, $b")
        return Triple(L, a, b)
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