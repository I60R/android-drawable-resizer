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

    var variant: BaseVariant? = null



    val inpDirs
            = variant
            ?.sourceSets
            ?.flatMap { it.resDirectories }
            ?.filter(File::exists)
            ?:listOf()

    val outDirs
            = File(project.buildDir, "adr")
            .let { baseOutputDir ->
                inpDirs.map {
                    val outputFilePath = it.absolutePath.replace(project.projectDir.absolutePath, "")
                    File(baseOutputDir, outputFilePath)
                }
            }

    val targets = inpDirs.zip(outDirs)



    @TaskAction
    fun resize() {

        val variant = variant ?: throw RuntimeException("No build variant was specified")

        val  adr = project.extensions.findByType<ADRExtension>(ADRExtension::class.java)
        with(adr) {

            val drawableFolderPattern = Pattern
                    .compile("^(drawable|mipmap)-.*" + maxDensityInfo.qualifierName + ".*$")

            val densitiesRange = (minDensityInfo.ordinal..(maxDensityInfo.ordinal - 1))

            val resizers = Density
                    .values()
                    .slice(densitiesRange)
                    .filterNot(excludeInfo::contains)
                    .flatMap { density ->

                        targets.flatMap { (inputResDir, outputResDir) ->
                            inputResDir
                                    .listFiles()
                                    .filter { drawableFolderPattern.matcher(it.name).matches() }
                                    .map { inputDrawableDir ->

                                        val outputDirName = inputDrawableDir.name.replace(maxDensityInfo.qualifierName, density.qualifierName)

                                        File(outputResDir, outputDirName)
                                                .apply { if (!exists()) mkdirs() }
                                                .let { outputDrawableDir ->

                                                    val resultDensity = (density.value.toDouble() / maxDensityInfo.value)

                                                    BatchDrawableResizer(inputDrawableDir, outputDrawableDir, resultDensity)
                                                            .also(BatchDrawableResizer::start)
                                                }
                                    }
                        }
                    }

        with(variant) {
            val adrSet = ResourceSet(name, "ADR").apply { addSources(outDirs) }
            mergeResources
                    .inputResourceSets
                    .add(adrSet) // Add output res dirs to resource merger
        }


        resizers.forEach(Thread::join)

        didWork = resizers
                .map { it.resizedDrawablesCount }
                .fold(0, Int::plus) > 0

        }
    }

}
