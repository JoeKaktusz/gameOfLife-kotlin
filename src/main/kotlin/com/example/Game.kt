package com.example

import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.stage.Screen
import javafx.stage.Stage
import kotlin.math.floor

class Game : Application() {
    companion object {
        private val WIDTH = Screen.getPrimary().visualBounds.width
    private val HEIGHT = Screen.getPrimary().visualBounds.height
        private const val BASE_CELL_SIZE = 20.0
    }

    private lateinit var mainScene: Scene
    private lateinit var graphicsContext: GraphicsContext
    private lateinit var simulation: GameOfLifeSimulation

    // Viewport state
    private var viewportOffsetX = 0.0
    private var viewportOffsetY = 0.0
    private var viewportScale = 1.0
    private var isDragging = false
    private var dragStartX = 0.0
    private var dragStartY = 0.0

    // UI Controls
    private lateinit var speedComboBox: ComboBox<String>
    private lateinit var playPauseButton: Button
    private var isPlaying = false

    override fun start(mainStage: Stage) {
        mainStage.title = "Conway's Game of Life"
        simulation = GameOfLifeSimulation()

        val root = Group()
        mainScene = Scene(root, WIDTH, HEIGHT)
        mainStage.scene = mainScene
        //mainStage.isFullScreen = true

        // Create control panel
        val canvas = Canvas(WIDTH, HEIGHT)
        root.children.add(canvas)
        val controlPanel = createControlPanel()
        root.children.add(controlPanel)
        graphicsContext = canvas.graphicsContext2D


        setupInputHandlers()
        setupSimulationLoop()

        mainStage.show()
    }

    private fun createControlPanel(): HBox {
        return HBox(10.0).apply {
            padding = Insets(10.0)

            speedComboBox = ComboBox<String>().apply {
                items.addAll("1 sec", "0.5 sec", "0.2 sec", "0.1 sec", "0.01 sec")
                selectionModel.selectFirst()
            }

            val stepButton = Button("Step").apply {
                setOnAction { simulation.nextIteration() }
            }

            val clearButton = Button("Clear").apply {
                setOnAction {
                    simulation.clear()
                    isPlaying = false
                    playPauseButton.text = "Start"
                }
            }

            playPauseButton = Button("Start").apply {
                setOnAction {
                    isPlaying = !isPlaying
                    text = if (isPlaying) "Pause" else "Start"
                }
            }

            children.addAll(speedComboBox, stepButton, clearButton, playPauseButton)
        }
    }

    private fun setupInputHandlers() {
        mainScene.setOnMousePressed { event ->
            if (event.button == MouseButton.SECONDARY) {
                isDragging = true
                dragStartX = event.sceneX
                dragStartY = event.sceneY
            }
        }

        mainScene.setOnMouseDragged { event ->
            if (isDragging) {
                val deltaX = (event.sceneX - dragStartX) / (BASE_CELL_SIZE * viewportScale)
                val deltaY = (event.sceneY - dragStartY) / (BASE_CELL_SIZE * viewportScale)
                viewportOffsetX += deltaX
                viewportOffsetY += deltaY
                dragStartX = event.sceneX
                dragStartY = event.sceneY
            }
        }

        mainScene.setOnMouseReleased { isDragging = false }

        mainScene.setOnScroll { event ->
            val zoomFactor = if (event.deltaY > 0) 1.1 else if(event.deltaY < 0) 0.9 else 1.0
            viewportScale *= zoomFactor
            viewportScale = viewportScale.coerceIn(0.1, 200.0)
        }

        mainScene.setOnMouseClicked { event ->
            if (event.button == MouseButton.PRIMARY) {
                val (gridX, gridY) = screenToGrid(event.x, event.y)
                simulation.toggleCell(floor(gridX).toInt(), floor(gridY).toInt())
            }
        }
    }

    private fun setupSimulationLoop() {
        var lastUpdate = 0L
        val intervals = listOf(1_000_000_000, 500_000_000, 200_000_000, 100_000_000, 10_000_000)

        val timer = object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                // Handle simulation updates
                if (isPlaying) {
                    val interval = intervals[speedComboBox.selectionModel.selectedIndex]

                    if (currentNanoTime - lastUpdate > interval) {
                        simulation.nextIteration()
                        lastUpdate = currentNanoTime
                    }
                }
                // Rendering
                graphicsContext.fill = Color.rgb(61, 77, 87)
                graphicsContext.fillRect(0.0, 0.0, WIDTH, HEIGHT)
                drawGrid()
                drawCells()
            }
        }
        timer.start()
    }


    private fun drawGrid() {
        graphicsContext.stroke = Color.rgb(109, 168, 201)
        val cellSize = BASE_CELL_SIZE * viewportScale

        val (startX, startY) = screenToGrid(0.0, 0.0)
        val (endX, endY) = screenToGrid(WIDTH, HEIGHT)

        // Use floor/ceil for proper grid range calculation
        val minX = floor(startX).toInt() - 1
        val maxX = floor(endX).toInt() + 1
        val minY = floor(startY).toInt() - 1
        val maxY = floor(endY).toInt() + 1

        for (x in minX..maxX) {
            for (y in minY..maxY) {
                val (screenX, screenY) = gridToScreen(x, y)
                graphicsContext.strokeRect(
                    screenX,
                    screenY,
                    cellSize - 1,
                    cellSize - 1
                )
            }
        }
    }


    private fun drawCells() {
        graphicsContext.fill = Color.ORANGE
        val cellSize = BASE_CELL_SIZE * viewportScale

        for (cell in simulation.aliveCells) {
            val (screenX, screenY) = gridToScreen(cell.first, cell.second)
            graphicsContext.fillRect(
                screenX,
                screenY,
                cellSize - 1,
                cellSize - 1
            )
        }
    }

    private fun screenToGrid(screenX: Double, screenY: Double): Pair<Double, Double> {
        return Pair(
            (screenX / (BASE_CELL_SIZE * viewportScale)) - viewportOffsetX,
            (screenY / (BASE_CELL_SIZE * viewportScale)) - viewportOffsetY
        )
    }

    private fun gridToScreen(gridX: Int, gridY: Int): Pair<Double, Double> {
        return Pair(
            (gridX + viewportOffsetX) * BASE_CELL_SIZE * viewportScale,
            (gridY + viewportOffsetY) * BASE_CELL_SIZE * viewportScale
        )
    }
}