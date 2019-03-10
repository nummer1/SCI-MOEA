import java.util.concurrent.Executors
import kotlin.random.Random


class NSGA2(private val problem: Problem, private val generationCount: Int, private val populationSize: Int, private val mutationRate: Double) {

    private val direction = Direction(problem.image.width, problem.image.height)
    var parentPopulation = MutableList<Chromosome>(populationSize) { Chromosome(problem, direction) }
    var childPopulation = MutableList<Chromosome>(populationSize) { Chromosome(problem, direction) }

    private fun initializePopulation() {
        val executor = Executors.newFixedThreadPool(8)
        for (i in 0.until(populationSize)) {
            val worker = Runnable { parentPopulation[i].initializeMST() }
            executor.execute(worker)
        }
        for (j in 0.until(populationSize)) {
            val worker = Runnable { childPopulation[j].initializeMST() }
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
        population.first().crowdingDistance = Double.MAX_VALUE/2
        population.last().crowdingDistance = Double.MAX_VALUE/2
        for (i in 1.until(population.size-1)) {
            population[i].crowdingDistance += population[i+1].overallDeviation - population[i-1].overallDeviation
        }

        population.sortBy { it.connectivityMeasure }
        population.first().crowdingDistance = Double.MAX_VALUE/2
        population.last().crowdingDistance = Double.MAX_VALUE/2
        for (i in 1.until(population.size-1)) {
            population[i].crowdingDistance += population[i+1].connectivityMeasure - population[i-1].connectivityMeasure
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
            if (parentPopulation.size + nonDominated.size <= populationSize) {
                parentPopulation.addAll(nonDominated)
                if (parentPopulation.size == populationSize) {
                    break
                }
            } else {
                crowdingDistanceAssignment(nonDominated)
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
                val parent1 = if (parentPopulation[potParents[0]].dominates(parentPopulation[potParents[1]])) parentPopulation[potParents[0]] else parentPopulation[potParents[1]]
                val parent2 = if (parentPopulation[potParents[2]].dominates(parentPopulation[potParents[3]])) parentPopulation[potParents[2]] else parentPopulation[potParents[3]]
                val child = Chromosome(problem, direction)
                child.uniformCrossover(parent1, parent2)
                if (Random.nextDouble(0.0, 1.0) < mutationRate) child.randomBitFlipMutation()
                childPopulation.add(child)
            }
            executor.execute(worker)
        }
        executor.shutdown()
        while (!executor.isTerminated) { }
    }

    fun getNondominatedParents(): List<Chromosome> {
        return fastNondominatedSort(parentPopulation, editPopulation = false)
    }

    fun run() {
        println("Started initialization")
        initializePopulation()
        println("Initialization finished")
        for (i in 0.until(generationCount)) {
            updatePopulation()
            var sum1 = 0.0
            var sum2 = 0.0
            var sum3 = 0.0
            childPopulation.forEach { sum1 += it.overallDeviation; sum2 += it.connectivityMeasure; sum3 += it.segmentClass.partitions!!.size }
            println("AVERAGE CHILD: ${sum1/childPopulation.size}, ${sum2/childPopulation.size}, ${sum3/childPopulation.size}")
            println("BEST CHILD: ${childPopulation.minBy { it.overallDeviation }!!.overallDeviation}, ${childPopulation.minBy { it.connectivityMeasure }!!.connectivityMeasure}")
            parentPopulation.forEach { sum1 += it.overallDeviation; sum2 += it.connectivityMeasure; sum3 += it.segmentClass.partitions!!.size }
            println("AVERAGE PARENT: ${sum1/parentPopulation.size}, ${sum2/parentPopulation.size}, ${sum3/parentPopulation.size}")
            println("BEST PARENT: ${parentPopulation.minBy { it.overallDeviation }!!.overallDeviation}, ${parentPopulation.minBy { it.connectivityMeasure }!!.connectivityMeasure}")
            println()
            // childPopulation.forEach { println("${it.overallDeviation}, ${it.connectivityMeasure}, ${it.getSegments().size}") }
        }
    }
}