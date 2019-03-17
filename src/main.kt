import kotlin.math.roundToInt


fun main(args: Array<String>) {
    // NOTE: minSegmentSize=300 works well
    val minSegmentSize = 300
    val genCount = 30
    val popSize = 16
    val mutationRate = 0.3
    val crossoverRate = 1.0
    val minSegmentCount = 3
    val maxSegmentCount = 40
    val useLab = false

    val problem = Problem("216066/Test image.jpg", minSegmentSize, useLab)
    val nsga2 = NSGA2(problem, genCount, popSize, mutationRate, minSegmentCount, maxSegmentCount)
    nsga2.run()
    nsga2.shutdown()

//    val ga = GA(problem, genCount, popSize, mutationRate, maxSegmentCount, minSegmentCount)
//    ga.run()
//    val pop = ga.getBest()
//    val name = "popbest_segments=${pop.segmentClass.partitions!!.size}_OD=${pop.overallDeviation.roundToInt()}_CM=${pop.connectivityMeasure.roundToInt()}_EV=${pop.edgeValue.roundToInt()}.png"
//    problem.drawOnBlank(pop, "GT$name")
//    problem.drawOnImage(pop, name)
//
//    var i = 0
//    for (pop in ga.population) {
//        val name = "pop${i}_=${pop.segmentClass.partitions!!.size}_OD=${pop.overallDeviation.roundToInt()}_CM=${pop.connectivityMeasure.roundToInt()}_EV=${pop.edgeValue.roundToInt()}.png"
//        problem.drawOnBlank(pop, "GT$name")
//        problem.drawOnImage(pop, name)
//        i++
//    }
}