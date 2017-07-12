package com.github.forsety.task

import com.android.build.gradle.api.BaseVariant
import com.android.ide.common.res2.ResourceSet
import com.github.forsety.ADRExtension
import com.github.forsety.BatchDrawableResizer
import com.github.forsety.Density
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.regex.Pattern


/***
 * Created by Forsety on 27.08.2014.
 *
 * Rewrite to Kotlin by 160R
 */
open class ResizeDrawablesTask : DefaultTask() {

    lateinit var baseVariant: BaseVariant


    val inpDirs by lazy {
        baseVariant
                .sourceSets
                .flatMap { it.resDirectories }
                .filter(File::exists)
    }

    val outDirs by lazy {
        val baseOutputDir = File(project.buildDir, "adr")
        inpDirs
                .map {
                    File(baseOutputDir, it.absolutePath.replace(project.projectDir.absolutePath, ""))
                }
                .toSet()
    }


    @TaskAction
    fun resize() {
        project.extensions
                .findByType<ADRExtension>(ADRExtension::class.java)
                .run {

                    val drawableFolderPattern = Pattern
                            .compile("^(drawable|mipmap)-.*" + maxDensityInfo.qualifierName + ".*$")

                    val densitiesRange = (minDensityInfo.ordinal..(maxDensityInfo.ordinal - 1))

                    val targetDensities = Density
                            .values()
                            .slice(densitiesRange)
                            .filterNot(excludeInfo::contains)

                    val resizers: List<BatchDrawableResizer> = targetDensities
                            .flatMap { targetDensity ->

                                inpDirs.zip(outDirs).flatMap { (inputResDir, outputResDir) ->
                                    inputResDir
                                            .listFiles()
                                            .filter { drawableFolderPattern.matcher(it.name).matches() }
                                            .map { inputDrawableDir ->

                                                val outputDrawableDirName = inputDrawableDir.name.replace(maxDensityInfo.qualifierName, targetDensity.qualifierName)

                                                val outputDrawableDir = File(outputResDir, outputDrawableDirName).apply { if (!exists()) mkdirs() }

                                                val resultDensity = (targetDensity.value.toDouble() / maxDensityInfo.value)

                                                BatchDrawableResizer(inputDrawableDir, outputDrawableDir, resultDensity)
                                            }
                                }
                            }

                    resizers.forEach {
                        it.start()
                        it.join()
                    }

                    baseVariant
                            .mergeResources
                            .extraGeneratedResFolders
                            .files += outDirs   // Add output res dirs to resource merger


                    didWork = resizers.map { it.resizedDrawablesCount }.fold(0, Int::plus) > 0

                }
    }

}
