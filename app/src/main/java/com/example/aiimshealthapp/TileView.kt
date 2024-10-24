package com.example.aiimshealthapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.math.pow

class TileView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val tileSize = 256  // Tile size (in pixels)
    private var zoomLevel = 5   // Initial zoom level
    private var currentTileX = 0
    private var currentTileY = 0

    private val paint = Paint()

    private val client = OkHttpClient()

    private val tileCache: MutableMap<String, Bitmap> = mutableMapOf()

    // Set the maximum concurrent tile requests to avoid spamming the API
    private val tileQueue: LinkedHashSet<String> = LinkedHashSet()

    // Throttle delay between API requests (e.g., 500ms)
    private val REQUEST_DELAY_MS = 500L

    // Maximum number of retries for 429 error
    private val MAX_RETRIES = 3

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawVisibleTiles(canvas)
    }

    private fun drawVisibleTiles(canvas: Canvas) {
        // Get the width and height of the view (the visible area)
        val viewWidth = width
        val viewHeight = height

        // Calculate the number of tiles to load in both dimensions (+2 for adjacent ones)
        val numTilesX = viewWidth / tileSize + 2
        val numTilesY = viewHeight / tileSize + 2

        // Start from the top-left tile that should be visible
        val startX = (currentTileX - numTilesX / 2).coerceAtLeast(0) // Ensure non-negative
        val startY = (currentTileY - numTilesY / 2).coerceAtLeast(0) // Ensure non-negative

        // Loop through and draw each tile in the visible range
        for (x in startX until (startX + numTilesX)) {
            for (y in startY until (startY + numTilesY)) {
                val tileKey = "$zoomLevel/$x/$y"

                val tileBitmap = tileCache[tileKey]
                if (tileBitmap == null) {
                    // If the tile is not cached, fetch it
                    fetchTile(zoomLevel, x, y)
                } else {
                    // Draw the tile at the correct position
                    val tileX = (x - startX) * tileSize
                    val tileY = (y - startY) * tileSize
                    canvas.drawBitmap(tileBitmap, tileX.toFloat(), tileY.toFloat(), paint)
                }
            }
        }
    }



    // Cache for storing tiles in memory


    private fun validateCoordinates(x: Int, y: Int): Boolean {
        val maxTiles = 2.0.pow(zoomLevel.toDouble()).toInt() // Use the correct zoom level
        return x in 0 until maxTiles && y in 0 until maxTiles
    }


    private fun fetchTile(zoom: Int, x: Int, y: Int, retries: Int = 0) {
        // Ensure x and y are valid
        val maxTiles = 2.0.pow(z.toDouble()).toInt()
        if(!validateCoordinates(x, y)) {
            Log.e("TileView", "Invalid tile coordinates: x = $x, y = $y")
            return
        }

        val tileKey = "$zoom/$x/$y"

        // Check if the tile is already cached
        if (tileCache.containsKey(tileKey)) {
            Log.d("TileView", "Tile $tileKey is already cached.")
            postInvalidate()  // Redraw the view
            return
        }

        // If too many requests are in progress, defer the new ones
        if (tileQueue.size >= 1) {
            Log.d("TileView", "Max concurrent requests reached. Adding $tileKey to the queue.")
            tileQueue.add(tileKey)
            return
        }

        // Add to request queue
        tileQueue.add(tileKey)

        val tileUrl = "https://retina-tiles.p.rapidapi.com/local/osm/v1/$zoom/$x/$y.png"
        Log.d("TileView", "Fetching tile from URL: $tileUrl")

        // Create the request with headers
        val request = Request.Builder()
            .url(tileUrl)
            .addHeader("x-rapidapi-key", "YOUR_API_KEY")
            .addHeader("x-rapidapi-host", "retina-tiles.p.rapidapi.com")
            .build()

        // Throttle requests
        Thread.sleep(REQUEST_DELAY_MS)

        // Make the network call asynchronously using OkHttp
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Log.e("TileView", "Failed to fetch tile: $tileUrl", e)
                tileQueue.remove(tileKey)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                tileQueue.remove(tileKey)

                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        Log.d("TileView", "Loading image from response for tile: $tileUrl")

                        // Load the image using Glide from the response body
                        Glide.with(context)
                            .asBitmap()
                            .load(responseBody.byteStream())
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {
                                    // Cache the tile
                                    tileCache[tileKey] = resource
                                    Log.d("TileView", "Tile loaded and cached: $tileKey")

                                    // Redraw the view to show the loaded tile
                                    postInvalidate()

                                    // Load the next tile from the queue
                                    loadNextTileFromQueue()
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {}
                            })
                    }
                } else {
                    Log.e("TileView", "Unsuccessful response for tile: $tileUrl, code: ${response.code()}")

                    // Handle API rate limit (429 error) with retry mechanism
                    if (response.code() == 429 && retries < MAX_RETRIES) {
                        Log.w("TileView", "Received 429 - Too many requests. Retrying... attempt ${retries + 1}")
                        Thread.sleep((REQUEST_DELAY_MS * (retries + 1)))  // Exponential backoff
                        fetchTile(zoom, x, y, retries + 1)
                    } else {
                        Log.e("TileView", "Failed to load tile after retries or other error.")
                    }
                }
            }
        })
    }

    private fun loadNextTileFromQueue() {
        if (tileQueue.isNotEmpty()) {
            // Get the next tile to load
            val nextTileKey = tileQueue.iterator().next()
            tileQueue.remove(nextTileKey)

            // Extract zoom, x, and y from the tile key and fetch the tile
            val (zoom, x, y) = nextTileKey.split("/").map { it.toInt() }
            fetchTile(zoom, x, y)
        }
    }



    // Update the current zoom level and redraw
    fun setZoomLevel(zoomLevel: Int) {
        this.zoomLevel = zoomLevel
        invalidate() // Redraw with the new zoom level
    }

    // Update the current tile being viewed and redraw
    fun setCurrentTile(x: Int, y: Int) {
        this.currentTileX = x
        this.currentTileY = y
        invalidate() // Redraw with the new center tile
    }
}
