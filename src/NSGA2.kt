import kotlin.random.Random


class NSGA2(private val problem: Problem, private val generationCount: Int, private val populationSize: Int, private val mutationRate: Double, private val elitistCount: Int) {

    private var parentPopulation = MutableList<Chromosome>(populationSize) { Chromosome(problem) }
    private var childPopulation = MutableList<Chromosome>(populationSize) { Chromosome(problem) }

    private fun initializePopulation() {
        parentPopulation.forEach { it.initializeRandom() }
        childPopulation.forEach { it.initializeRandom() }
    }

    private fun fastNondominatedSort(population: MutableList<Chromosome>): MutableList<Chromosome> {
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
        var offset = 0
        for (index in newPopIndexes){
            newPopulation.add(population[index-offset])
            population.removeAt(index-offset)
            offset++
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
                if (parentPopulation.size < elitistCount) {
                    // sort so adding to child population using elitism takes best solutions
                    crowdingDistanceAssignment(nonDominated)
                }
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
        for (i in 0.until(elitistCount)) {
            childPopulation.add(parentPopulation[i])
        }
        for (i in 0.until(populationSize - elitistCount)) {
            val allParents = MutableList<Int>(populationSize) { it }
            val potParents = mutableListOf<Int>()
            for (j in 0.until(4)) {
                val randParentIndex = Random.nextInt(0, allParents.size)
                potParents.add(allParents[randParentIndex])
                allParents.removeAt(randParentIndex)
            }
            val parent1 = if (parentPopulation[potParents[0]].dominates(parentPopulation[potParents[1]])) parentPopulation[potParents[0]] else parentPopulation[potParents[1]]
            val parent2 = if (parentPopulation[potParents[2]].dominates(parentPopulation[potParents[3]])) parentPopulation[potParents[2]] else parentPopulation[potParents[3]]
            val child = Chromosome(problem)
            child.uniformCrossover(parent1, parent2)
            if (Random.nextDouble(0.0, 1.0) < mutationRate) child.randomBitFlipMutation()
            childPopulation.add(child)
        }
    }

    fun run() {
        initializePopulation()
        for (i in 0.until(generationCount)) {
            updatePopulation()
            var sum1 = 0.0
            var sum2 = 0.0
            childPopulation.forEach { sum1 += it.overallDeviation; sum2 += it.connectivityMeasure }
            println("${sum1/childPopulation.size}, ${sum2/childPopulation.size}")
        }
    }
}