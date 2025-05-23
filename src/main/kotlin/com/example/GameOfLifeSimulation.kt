package com.example

typealias Cell = Pair<Int, Int>

class GameOfLifeSimulation {

    private var _aliveCells: MutableSet<Cell> = mutableSetOf()
    val aliveCells: Set<Cell> get() = _aliveCells

    // Toggle a cell's state (alive/dead)
    fun toggleCell(x: Int, y: Int) {
        val cell = Cell(x, y)
        if (cell in _aliveCells) {
            _aliveCells.remove(cell)
        } else {
            _aliveCells.add(cell)
        }
    }

    // Clear all cells
    fun clear() {
        _aliveCells.clear()
    }

    // Compute next iteration
    fun nextIteration() {
        _aliveCells = computeNextState().toMutableSet()
    }

    private fun computeNextState(): Set<Cell> {
        val candidates = getCandidateCells()
        val nextAlive = mutableSetOf<Cell>()

        for (cell in candidates) {
            val aliveNeighbors = countAliveNeighbors(cell)
            val isAlive = cell in _aliveCells

            if (isAlive && (aliveNeighbors == 2 || aliveNeighbors == 3)) {
                nextAlive.add(cell) // Survival
            } else if (!isAlive && aliveNeighbors == 3) {
                nextAlive.add(cell) // Birth
            }
        }
        return nextAlive
    }

    // Get all cells to evaluate (alive cells + their neighbors)
    private fun getCandidateCells(): Set<Cell> {
        val candidates = mutableSetOf<Cell>()
        for (cell in _aliveCells) {
            candidates.add(cell)
            candidates.addAll(getNeighbors(cell))
        }
        return candidates
    }

    // Count alive neighbors for a cell
    private fun countAliveNeighbors(cell: Cell): Int {
        return getNeighbors(cell).count { it in _aliveCells }
    }

    // Generate all 8 neighbors of a cell (wrapping via Int overflow)
    private fun getNeighbors(cell: Cell): List<Cell> {
        val (x, y) = cell
        return listOf(
            x - 1 to y - 1, x - 1 to y, x - 1 to y + 1,
            x to y - 1,          /* cell */      x to y + 1,
            x + 1 to y - 1, x + 1 to y, x + 1 to y + 1
        )
    }

}