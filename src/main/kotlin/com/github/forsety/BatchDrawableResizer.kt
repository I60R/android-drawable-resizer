package com.github.forsety

import com.google.common.cache.CacheBuilder
import org.imgscalr.Scalr
import org.imgscalr.Scalr.*
import java.awt.image.BufferedImage
import java.awt.image.DataBuffer
import java.io.File
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

/***
 * Created by Forsety on 28.08.2014.
 *
 * Rewrite to Kotlin by 160R
 */
class BatchDrawableResizer(
        private val inpDir: File,
        private val outDir: File,
        private val ratio: Double
) : Thread() {

    private val DEFAULT_EXTENSIONS = listOf("png", "jpg", "gif")

    private val cache = CacheBuilder
            .newBuilder()
            .weigher { _: File, value: BufferedImage ->
                val buff = value.raster.dataBuffer
                val bytes = buff.size * DataBuffer.getDataTypeSize(buff.dataType) / 8
                bytes / 1024
            }
            .maximumWeight((64 * 1024).toLong())
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build<File, BufferedImage>()


    var resizedDrawablesCount = 0
        private set



    override fun run() {
        inpDir
                .listFiles()
                .filter {
                    DEFAULT_EXTENSIONS.contains(it.extension)
                    && !it.name.endsWith(".9." + it.extension)  // Exclude 9-patches
                }
                .map { inp ->

                    File(outDir, inp.name).let { out ->
                        val updated = inp.lastModified() > out.lastModified()
                        if (!out.exists() || updated) {
                            resizeDrawable(inp, out, inp.extension, ratio)
                        }
                    }
                }
    }



    fun resizeDrawable(inp: File, out: File, extension: String, ratio: Double) {
        val inpImg = cache
                .get(inp) {
                    ImageIO.read(inp)
                }

        val outImg = Scalr
                .resize(inpImg,
                        Method.QUALITY,
                        Mode.FIT_TO_WIDTH,
                        (inpImg.width * ratio).toInt(),
                        OP_ANTIALIAS)

        ImageIO.write(outImg, extension, out)
        resizedDrawablesCount++
    }


}
