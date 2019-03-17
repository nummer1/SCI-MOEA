import java.util.concurrent.Executors
import kotlin.random.Random


class GA(private val problem: Problem, private val generationCount: Int, private val populationSize: Int, private val mutationRate: Double, private val maxSegmentCount: Int, private val minSegmentCount: Int) {
    private val direction = Direction(problem.width, problem.height)
    var population = MutableList<Chromosome>(populationSize) { Chromosome(problem, direction) }

    private fun initializePopulation() {
        val executor = Executors.newFixedThreadPool(3)
        for (i in 0.until(populationSize)) {
            val worker = Runnable { population[i].initializeMSTPrim() }
            executor.execute(worker)
        }
        executor.shutdown()
        while (!executor.isTerminated) { }
    }

    fun updatePopulation() {
        val newPopulation = mutableListOf<Chromosome>()
        val executor = Executors.newFixedThreadPool(8)
        val largestConn = population.maxBy { it.connectivityMeasure }!!.connectivityMeasure
        val largestDev = population.maxBy { it.overallDeviation }!!.overallDeviation
        val largestEdge = population.maxBy { it.edgeValue }!!.edgeValue
        for (i in 0.until(populationSize)) {
            val worker = Runnable {
                val allParents = MutableList<Int>(populationSize) { it }
                val potParents = mutableListOf<Int>()
                for (j in 0.until(4)) {
                    val randParentIndex = Random.nextInt(0, allParents.size)
                    potParents.add(allParents[randParentIndex])
                    allParents.removeAt(randParentIndex)
                }
                val parent1 = if (population[potParents[0]].getFitness(largestConn, largestDev, largestEdge, maxSegmentCount, minSegmentCount) > population[potParents[1]].getFitness(largestConn, largestDev, largestEdge, maxSegmentCount, minSegmentCount))
                    population[potParents[0]] else population[potParents[1]]
                val parent2 = if (population[potParents[2]].getFitness(largestConn, largestDev, largestEdge, maxSegmentCount, minSegmentCount) > population[potParents[3]].getFitness(largestConn, largestDev, largestEdge, maxSegmentCount, minSegmentCount))
                    population[potParents[2]] else population[potParents[3]]
                val child = Chromosome(problem, direction)
                child.uniformCrossover(parent1, parent2)
                if (Random.nextDouble(0.0, 1.0) < mutationRate) child.flipRandomSegmentsMutation()
                newPopulation.add(child)
            }
            executor.execute(worker)
        }
        executor.shutdown()
        while (!executor.isTerminated) { }
        population = newPopulation
        population.sortByDescending { it.getFitness(largestConn, largestDev, largestEdge, maxSegmentCount, minSegmentCount) }
    }

    fun getBest(): Chromosome {
        return population[0]
    }

    fun run() {
        println("Started initialization")
        initializePopulation()
        println("Initialization finished")
        for (i in 0.until(generationCount)) {
            updatePopulation()
            val sum = MutableList<Double>(8) { 0.0 }
            population.forEach { sum[0] += it.overallDeviation; sum[1] += it.connectivityMeasure; sum[2] += it.edgeValue; sum[3] += it.segmentClass.partitions!!.size.toDouble() }
            println("AVERAGE POP: ${sum[0]/population.size}, ${sum[1]/population.size}, ${sum[2]/population.size}, ${sum[3]/population.size}")
            println("BEST POP: ${population.minBy { it.overallDeviation }!!.overallDeviation}, ${population.minBy { it.connectivityMeasure }!!.connectivityMeasure}, ${population.maxBy { it.edgeValue }!!.edgeValue}")
            println()
        }
    }
}