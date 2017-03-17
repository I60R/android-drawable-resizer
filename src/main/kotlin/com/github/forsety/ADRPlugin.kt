package com.github.forsety

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.tasks.MergeResources
import com.github.forsety.task.ResizeDrawablesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.util.HashMap

/***
 * Created by Forsety on 27.08.2014.
 *
 * Rewrite to Kotlin by 160R
 */
open class ADRPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {

        extensions
                .create("adr", ADRExtension::class.java)


        target.afterEvaluate {

            extensions
                    .findByType(ADRExtension::class.java)
                    .validate()

            when {
                plugins.hasPlugin(AppPlugin::class.java) -> {
                    extensions
                            .findByType(AppExtension::class.java)
                            .applicationVariants
                            .all { setup(this, it) }
                }

                plugins.hasPlugin(LibraryPlugin::class.java) -> {
                    extensions.findByType(LibraryExtension::class.java)
                            .libraryVariants
                            .all { setup(target, it) }
                }

                else -> {
                    throw RuntimeException("No android plugin found")
                }
            }
        }
    }

    private fun setup(target: Project, baseVariant: BaseVariant) {
        target.task(
                mutableMapOf(
                        "type" to ResizeDrawablesTask::class.java,
                        "description" to "This task resizes current build variant drawables and saves them in project build directory"
                ),
                generateTaskName("resize", baseVariant, "Drawables")
        ).apply {
            this as ResizeDrawablesTask
            this.variant = baseVariant
            baseVariant
                    .mergeResources
                    .dependsOn(this)
        }

    }

    fun generateTaskName(prefix: String, variant: BaseVariant, postfix: String)
        = with (variant) {
            "$prefix${ name.substring(0, 1).toUpperCase() }${ name.substring(1) }$postfix"
        }
}
