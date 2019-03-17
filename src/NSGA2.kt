import java.util.concurrent.Executors
import kotlin.math.roundToInt
import kotlin.random.Random


class NSGA2(private val problem: Problem, private val generationCount: Int, private val populationSize: Int, private val mutationRate: Double, private val minSegmentCount: Int, private val maxSegmentCount: Int) {

    private val direction = Direction(problem.width, problem.height)
    var parentPopulation = MutableList<Chromosome>(populationSize) { Chromosome(problem, direction) }
    var childPopulation = MutableList<Chromosome>(populationSize) { Chromosome(problem, direction) }

    private fun initializePopulation() {
        val executor = Executors.newFixedThreadPool(8)
        for (i in 0.until(populationSize)) {
            val worker = Runnable { parentPopulation[i].initializeMSTPrim() }
            executor.execute(worker)
        }
        for (j in 0.until(populationSize)) {
            val worker = Runnable { childPopulation[j].initializeMSTPrim() }
            executor.execute(worker)
        }
        executor.shutdown()
        while (!executor.isTerminated) { }
    }

    private fun fastNondominatedSort(population: MutableList<Chromosome>, editPopulation: Boolean=true): MutableList<Chromosome> {
        // NOTE: this function edits population
        // finds and returns first nondominated front of population
        // all instances in newPopulation is removed from population
        val newPopIndexes = mutableListOf<Int>()
        newPopIndexes.add(0)
        for (j in 1.until(population.size)) {
            var addToNewPop = true
            var i = 0
            while (i < newPopIndexes.size) {
                if (population[j].dominates(population[newPopIndexes[i]])) {
                    // if population[j] dominates newPopulation[i]: delete newPopulation[i]
                    newPopIndexes.removeAt(i)
                    i--
                } else if (population[newPopIndexes[i]].dominates(population[j])) {
                    // if newPopulation[i] dominates population[j]: don't add population[j]
                    addToNewPop = false
                    break
                }
                i++
            }
            if (addToNewPop) newPopIndexes.add(j)
        }
        val newPopulation = mutableListOf<Chromosome>()
        if (editPopulation) {
            var offset = 0
            for (index in newPopIndexes) {
                newPopulation.add(population[index - offset])
                population.removeAt(index - offset)
                offset++
            }
        } else {
            for (index in newPopIndexes) {
                newPopulation.add(population[index])
            }
        }
        return newPopulation
    }

    private fun crowdingDistanceAssignment(population: MutableList<Chromosome>) {
        // assigns each member of population a crowdingDistance, uses Chromosome.crowdingDistance to do this
        // then sorts population descending on crowding distance
        population.forEach { it.crowdingDistance = 0.0 }

        population.sortBy { it.overallDeviation }
        population.first().crowdingDistance += 3.0
        population.last().crowdingDistance += 3.0
        val largestDev = population.last().overallDeviation
        for (i in 1.until(population.size-1)) {
            population[i].crowdingDistance += population[i+1].overallDeviation/largestDev - population[i-1].overallDeviation/largestDev
        }

        population.sortBy { it.connectivityMeasure }
        population.first().crowdingDistance += 3.0
        population.last().crowdingDistance += 3.0
        val largestConn = population.last().connectivityMeasure
        for (i in 1.until(population.size-1)) {
            population[i].crowdingDistance += population[i+1].connectivityMeasure/largestConn - population[i-1].connectivityMeasure/largestConn
        }

        population.sortBy { it.edgeValue }
        population.first().crowdingDistance += 3.0
        population.last().crowdingDistance += 3.0
        val largestEdge = population.last().edgeValue
        for (i in 1.until(population.size-1)) {
            population[i].crowdingDistance += population[i+1].edgeValue/largestEdge - population[i-1].edgeValue/largestEdge
        }

        for (pop in population) {
            if (pop.getNumberSegments() > maxSegmentCount) {
                pop.crowdingDistance -= 3 * (pop.getNumberSegments() / maxSegmentCount)
            } else if (pop.getNumberSegments() < minSegmentCount) {
                pop.crowdingDistance -= 3 * (minSegmentCount / pop.getNumberSegments())
            }
        }

        population.sortByDescending { it.crowdingDistance }
    }

    private fun updatePopulation() {
        val population = mutableListOf<Chromosome>()
        population.addAll(parentPopulation)
        population.addAll(childPopulation)
        // make new parent population
        parentPopulation.clear()
        while (true) {
            val nonDominated = fastNondominatedSort(population)
            crowdingDistanceAssignment(nonDominated)
            if (parentPopulation.size + nonDominated.size <= populationSize) {
                parentPopulation.addAll(nonDominated)
                if (parentPopulation.size == populationSize) {
                    break
                }
            } else {
                for (i in 0.until(populationSize - parentPopulation.size)) {
                    parentPopulation.add(nonDominated[i])
                }
                break
            }
        }
        // make new child population
        childPopulation.clear()
        val executor = Executors.newFixedThreadPool(8)
        for (i in 0.until(populationSize)) {
            val worker = Runnable {
                val allParents = MutableList<Int>(populationSize) { it }
                val potParents = mutableListOf<Int>()
                for (j in 0.until(4)) {
                    val randParentIndex = Random.nextInt(0, allParents.size)
                    potParents.add(allParents[randParentIndex])
                    allParents.removeAt(randParentIndex)
                }
                val parent1 = if (parentPopulation[potParents[0]].isLarger(parentPopulation[potParents[1]])) parentPopulation[potParents[0]] else parentPopulation[potParents[1]]
                val parent2 = if (parentPopulation[potParents[2]].isLarger(parentPopulation[potParents[3]])) parentPopulation[potParents[2]] else parentPopulation[potParents[3]]
                val child = Chromosome(problem, direction)
                child.uniformCrossover(parent1, parent2)
                if (Random.nextDouble(0.0, 1.0) < mutationRate) child.flipRandomSegmentsMutation()
                childPopulation.add(child)
            }
            executor.execute(worker)
        }
        executor.shutdown()
        while (!executor.isTerminated) { }
    }

    fun getNondominatedPopulation(): List<Chromosome> {
        val pop = parentPopulation.subList(0, parentPopulation.size)
        pop.addAll(childPopulation)
        return fastNondominatedSort(pop, editPopulation = false)
    }

    fun shutdown() {
        var i = 0
        for (pop in getNondominatedPopulation()) {
            val name = "pop_${i}segments=${pop.segmentClass.partitions!!.size}_OD=${pop.overallDeviation.roundToInt()}_CM=${pop.connectivityMeasure.roundToInt()}_EV=${pop.edgeValue.roundToInt()}.png"
            problem.drawOnBlank(pop, "GT$name")
            problem.drawOnImage(pop, name)
            i += 1
        }
    }

    fun run() {
        println("Started initialization")
        initializePopulation()
        println("Initialization finished")
        for (i in 0.until(generationCount)) {
            updatePopulation()
            val sum = MutableList<Double>(8) { 0.0 }
            childPopulation.forEach { sum[0] += it.overallDeviation; sum[1] += it.connectivityMeasure; sum[2] += it.edgeValue; sum[3] += it.segmentClass.partitions!!.size.toDouble() }
            println("AVERAGE CHILD: ${sum[0]/childPopulation.size}, ${sum[1]/childPopulation.size}, ${sum[2]/childPopulation.size}, ${sum[3]/childPopulation.size}")
            println("BEST CHILD: ${childPopulation.minBy { it.overallDeviation }!!.overallDeviation}, ${childPopulation.minBy { it.connectivityMeasure }!!.connectivityMeasure}, ${childPopulation.maxBy { it.edgeValue }!!.edgeValue}")
            parentPopulation.forEach { sum[4] += it.overallDeviation; sum[5] += it.connectivityMeasure; sum[6] += it.edgeValue; sum[7] += it.segmentClass.partitions!!.size.toDouble() }
            println("AVERAGE PARENT: ${sum[4]/parentPopulation.size}, ${sum[5]/parentPopulation.size}, ${sum[6]/parentPopulation.size}, ${sum[7]/parentPopulation.size}")
            println("BEST PARENT: ${parentPopulation.minBy { it.overallDeviation }!!.overallDeviation}, ${parentPopulation.minBy { it.connectivityMeasure }!!.connectivityMeasure}, ${parentPopulation.maxBy { it.edgeValue }!!.edgeValue}")
            println()
        }
    }
}