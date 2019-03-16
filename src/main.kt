// http://shiralab.ynu.ac.jp/data/paper/cec09_shirakawa.pdf
// http://www.academia.edu/6394976/Multi-Objective_Evolutionary_Clustering_using_Variable-Length_Real_Jumping_Genes_Genetic_Algorithm
// https://github.com/simjohan/bio-ai-project2/blob/master/pdf/978-3-319-71928-3_17.pdf

fun main(args: Array<String>) {
    // TODO: if number segments is larger or smaller than max and min number of possible segments, set crowdingDistance to 0
    val problem = Problem("86016/Test image.jpg", 400)
    val nsga2 = NSGA2(problem, 1, 4, 0.2)
    nsga2.run()

    // TODO: only write nondominated solution (both parent and child)
    var i = 0
    for (child in nsga2.childPopulation) {
        problem.drawOnBlank(child, "child_${i}segments=${child.segmentClass.partitions!!.size}.png")
        problem.drawOnImage(child, "child2_${i}segments=${child.segmentClass.partitions!!.size}.png")
        i += 1
    }
    i = 0
    for (parent in nsga2.getNondominatedParents()) {
        problem.drawOnBlank(parent, "parent_${i}segments=${parent.segmentClass.partitions!!.size}.png")
        problem.drawOnImage(parent, "parent2_${i}segments=${parent.segmentClass.partitions!!.size}.png")
        i += 1
    }

//    val ga = GA(problem, 10, 10, 0.2)
//    ga.run()
//    var i = 0
//    for (pop in ga.population) {
//        problem.drawOnBlank(pop, "parent_${i}segments=${pop.segmentClass.partitions!!.size}.png")
//        problem.drawOnImage(pop, "parent2_${i}segments=${pop.segmentClass.partitions!!.size}.png")
//        i += 1
//    }


//    val dir = Direction(problem.width, problem.height)
//    val chrome = Chromosome(problem, dir)
//    chrome.initializeMSTKruskal()
//    problem.drawOnImage(chrome, "test.png")
//    println(chrome.segmentClass.partitions!!.size)

//    problem.drawOnBlank(nsga2.childPopulation[2], "test1.png")
//    problem.drawOnImage(nsga2.childPopulation[2], "test2.png")

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