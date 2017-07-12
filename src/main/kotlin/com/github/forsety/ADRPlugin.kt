package com.github.forsety

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.github.forsety.task.ResizeDrawablesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/***
 * Created by Forsety on 27.08.2014.
 *
 * Rewrite to Kotlin by 160R
 */
open class ADRPlugin : Plugin<Project> {

    override fun apply(targetProject: Project) {
        with(targetProject) {

            extensions.create("adr", ADRExtension::class.java)

            afterEvaluate {

                extensions
                        .findByType(ADRExtension::class.java)
                        .validate()

                when {

                    plugins.hasPlugin(AppPlugin::class.java)     -> {
                        extensions
                                .findByType(AppExtension::class.java)
                                .applicationVariants
                                .all {
                                    setupAndroidDrawableResizer(this, it)
                                }
                    }

                    plugins.hasPlugin(LibraryPlugin::class.java) -> {
                        extensions.findByType(LibraryExtension::class.java)
                                .libraryVariants
                                .all {
                                    setupAndroidDrawableResizer(targetProject, it)
                                }
                    }

                    else                                         -> {
                        throw RuntimeException("No android plugin found")
                    }
                }
            }
        }
    }

    private fun setupAndroidDrawableResizer(targetProject: Project, baseVariant: BaseVariant) {
        val resizeDrawablesTask = targetProject.task(mutableMapOf(
                "type" to ResizeDrawablesTask::class.java,
                "description" to "This task resizes current build variant drawables and saves them in project build directory"),
                generateTaskName("resize", baseVariant, "Drawables")
        ) as ResizeDrawablesTask

        resizeDrawablesTask.baseVariant = baseVariant

        baseVariant
                .mergeResources
                .dependsOn(resizeDrawablesTask)
    }


    fun generateTaskName(prefix: String, variant: BaseVariant, postfix: String) = variant.run {
        "$prefix${name.substring(0, 1).toUpperCase()}${name.substring(1)}$postfix"
    }
}
