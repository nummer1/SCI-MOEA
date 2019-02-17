import java.awt.image.BufferedImage
import java.io.File
import kotlin.random.Random
import javax.imageio.ImageIO


class Chromosome(val problem: Problem) {
    // genes is list of integers in range 0 to and including 4
    // 0: Up, 1: Right, 2: Down, 3: Left, 4: None
    val genes = mutableListOf<Int>()

    fun initializeMST() {

    }

    fun initializeRAndom() {
        for (i in 0.until(problem.image.height * problem.image.width)) {
            genes.add(Random.nextInt(5))
        }
    }
}


class Problem(fileName: String) {

    val image: BufferedImage

    init {
        image = ImageIO.read(File(fileName))
    }

}