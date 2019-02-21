// http://shiralab.ynu.ac.jp/data/paper/cec09_shirakawa.pdf
// http://www.academia.edu/6394976/Multi-Objective_Evolutionary_Clustering_using_Variable-Length_Real_Jumping_Genes_Genetic_Algorithm
// https://github.com/simjohan/bio-ai-project2/blob/master/pdf/978-3-319-71928-3_17.pdf

fun main(args: Array<String>) {
    // TODO: if number segments is larger or smaller than max and min number of possible segments, set crowdingDistance to 0
    val problem = Problem("353013/Test image.jpg")
    val nsga2 = NSGA2(problem, 10, 50, 0.2, 4)
    nsga2.run()
}