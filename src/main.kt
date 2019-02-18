// http://shiralab.ynu.ac.jp/data/paper/cec09_shirakawa.pdf
// http://www.academia.edu/6394976/Multi-Objective_Evolutionary_Clustering_using_Variable-Length_Real_Jumping_Genes_Genetic_Algorithm
// https://github.com/simjohan/bio-ai-project2/blob/master/pdf/978-3-319-71928-3_17.pdf

fun main(args: Array<String>) {

    val problem = Problem("353013/Test image.jpg")
    val c1 = Chromosome(problem)
    // val testGenes = listOf(2, 3, 4, 0, 1, 4, 1, 1, 2, 2, 3, 1, 1, 4, 2, 0)
    // c1.genes.addAll(testGenes)
    c1.initializeRandom()
    println(c1.genes)
    println(c1.height * c1.width)
    val segments = c1.getSegments()
    println(segments)
}