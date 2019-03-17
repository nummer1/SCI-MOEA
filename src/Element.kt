

class Element() {
    var parent: Element = this
    var rank: Int = 0
    var size = 1
    var value: Int = -1

    fun makeSet(v: Int) {
        value = v
    }

    fun findSet(): Element {
        if (this == this.parent) {
            return this
        }
        this.parent = this.parent.findSet()
        return this.parent
    }

    fun union(e: Element) {
        val r1 = this.findSet()
        val r2 = e.findSet()

        if (r1 == r2) {
            return
        }

        if (r1.rank > r2.rank) {
            r2.parent = r1
            r1.size += r2.size
        } else if (r1.rank < r2.rank) {
            r1.parent = r2
            r2.size += r1.size
        } else {
            r1.parent = r2
            r2.rank = r2.rank + 1
            r2.size += r1.size
        }
    }
}