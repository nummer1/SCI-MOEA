// http://shiralab.ynu.ac.jp/data/paper/cec09_shirakawa.pdf
// http://www.academia.edu/6394976/Multi-Objective_Evolutionary_Clustering_using_Variable-Length_Real_Jumping_Genes_Genetic_Algorithm
// https://github.com/simjohan/bio-ai-project2/blob/master/pdf/978-3-319-71928-3_17.pdf

fun main(args: Array<String>) {
    // TODO: if number segments is larger or smaller than max and min number of possible segments, set crowdingDistance to 0
    val problem = Problem("353013/Test image.jpg")
    val nsga2 = NSGA2(problem, 1, 8, 0.2)
    nsga2.run()
    println(nsga2.childPopulation[2].getSegments().size)
    problem.drawOnBlank(nsga2.childPopulation[2], "test1.png")
    problem.drawOnImage(nsga2.childPopulation[2], "test2.png")

//    val chrome = Chromosome(problem)
//    val chrome2 = Chromosome(problem)
//    chrome.initializeMST()
//    println(chrome.getSegmentEdges().size)
//    chrome2.initializeRandom()
//    println("${chrome.genes.size}, ${chrome2.genes.size}")
//    println("${chrome.getSegments().size}, ${chrome2.getSegments().size}")
//    println("${chrome.overallDeviation}, ${chrome2.overallDeviation}")
//    println("${chrome.connectivityMeasure}, ${chrome2.connectivityMeasure}")
}