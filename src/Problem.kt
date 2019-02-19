import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO


class Problem(fileName: String) {

    val image: BufferedImage
    val colourList: MutableList<Triple<Int, Int, Int>> = mutableListOf()

    init {
        if (fileName.equals("TEST")) {
            image = BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB)
        } else {
            image = ImageIO.read(File(fileName))
        }
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
}