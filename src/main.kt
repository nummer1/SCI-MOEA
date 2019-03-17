// http://shiralab.ynu.ac.jp/data/paper/cec09_shirakawa.pdf
// http://www.academia.edu/6394976/Multi-Objective_Evolutionary_Clustering_using_Variable-Length_Real_Jumping_Genes_Genetic_Algorithm
// https://github.com/simjohan/bio-ai-project2/blob/master/pdf/978-3-319-71928-3_17.pdf

fun main(args: Array<String>) {
    val minSegmentSize = 300
    val genCount = 30
    val popSize = 20
    val mutationRate = 0.3
    val crossoverRate = 1.0
    val minSegmentCount = 3
    val maxSegmentCount = 40

    val problem = Problem("216066/Test image.jpg", minSegmentSize)
    val nsga2 = NSGA2(problem, genCount, popSize, mutationRate, minSegmentCount, maxSegmentCount)
    nsga2.run()
    nsga2.shutdown()

//    val ga = GA(problem, genCount, popSize, mutationRate, maxSegmentCount, minSegmentCount)
//    ga.run()
//    val pop = ga.getBest()
//    val name = "pop_best_segments=${pop.segmentClass.partitions!!.size}_OD=${pop.overallDeviation.roundToInt()}_CM=${pop.connectivityMeasure.roundToInt()}_EV=${pop.edgeValue.roundToInt()}.png"
//    problem.drawOnBlank(pop, "GT$name")
//    problem.drawOnImage(pop, name)



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