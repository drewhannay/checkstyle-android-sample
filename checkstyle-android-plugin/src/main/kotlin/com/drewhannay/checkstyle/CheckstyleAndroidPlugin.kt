@file:Suppress("UnstableApiUsage")

package com.drewhannay.checkstyle

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Component
import com.android.build.gradle.BasePlugin
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.IConventionAware
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.tasks.TaskProvider
import org.gradle.util.internal.GUtil
import java.io.File
import java.util.Locale

internal const val CHECKSTYLE_CONFIG_DIR_NAME = "config/checkstyle"
internal const val DEFAULT_CHECKSTYLE_VERSION = "8.37"

open class CheckstyleAndroidPlugin : Plugin<Project> {

    private lateinit var project: Project
    private lateinit var extension: CheckstyleAndroidExtension

    private lateinit var parentTask: TaskProvider<Task>

    override fun apply(project: Project) {
        this.project = project

        project.pluginManager.apply(ReportingBasePlugin::class.java)

        createConfiguration()
        extension = createExtension()
        configureExtensionRule()
        configureTaskRule()
        configureVariants()
        configureCheckTask()
    }

    private fun createConfiguration() {
        val configuration = project.configurations.create("checkstyle").apply {
            isVisible = false
            isTransitive = true
            description = "The checkstyle libraries to be used for this project."
        }

        // Add checkstyle dependencies
        configuration.defaultDependencies { dependencies ->
            dependencies.add(project.dependencies.create("com.puppycrawl.tools:checkstyle:" + extension.toolVersion))
        }
    }

    private fun createExtension(): CheckstyleAndroidExtension {
        parentTask = project.tasks.register("checkstyle")
        parentTask.configure {
            it.description = "Parent task that runs all other Checkstyle tasks in the project"
            it.group = JavaBasePlugin.VERIFICATION_GROUP
        }

        val extension =
            project.extensions.create("checkstyle", CheckstyleAndroidExtension::class.java, project)
        extension.toolVersion = DEFAULT_CHECKSTYLE_VERSION
        val directory = project.rootProject.layout.projectDirectory.dir(CHECKSTYLE_CONFIG_DIR_NAME)
        extension.configDirectory.convention(directory)
        extension.config =
            project.resources.text.fromFile(extension.configDirectory.file("checkstyle.xml"))
        return extension
    }

    private fun configureExtensionRule() {
        val extensionMapping = extension as IConventionAware
        extensionMapping.conventionMapping.map("reportsDir") {
            project.extensions.getByType(ReportingExtension::class.java).file("checkstyle")
        }
    }

    private fun configureTaskRule() {
        project.tasks.withType(Checkstyle::class.java).configureEach { task ->
            // Strip taskBaseName from task.name if exists
            // Then force the first character to be lowercase.
            // This is essentially the reverse of [AndroidSourceSet.getTaskName]
            var prunedName = task.name.replaceFirst("checkstyle", "")
            if (prunedName.isEmpty()) {
                prunedName = task.name
            }
            prunedName =
                ("" + prunedName[0]).lowercase(Locale.getDefault()) + prunedName.substring(1)
            val configuration = project.configurations.getAt("checkstyle")
            // Use the extension properties to configure task
            with(task.conventionMapping) {
                map("checkstyleClasspath") { configuration }
                map("config") { extension.config }
                map("configProperties") { extension.configProperties }
                map("ignoreFailures") { extension.isIgnoreFailures }
                map("showViolations") { extension.isShowViolations }
                map("maxErrors") { extension.maxErrors }
                map("maxWarnings") { extension.maxWarnings }
            }
            task.configDirectory.set(extension.configDirectory)

            val layout = project.layout
            val providers = project.providers
            val reportsDir = layout.dir(providers.provider { extension.reportsDir })
            task.reports.all { report: SingleFileReport ->
                report.required.convention(true)
                report.outputLocation.convention(
                    layout.projectDirectory.file(providers.provider {
                        val reportFileName = prunedName + "." + report.name
                        File(reportsDir.get().asFile, reportFileName).absolutePath
                    })
                )
            }
        }
    }

    private fun configureVariants() {
        withBaseAndroidPlugin {
            val extension = project.extensions.getByType(AndroidComponentsExtension::class.java)
            extension.onVariants { variant ->
                configureComponent(variant)
                variant.nestedComponents.forEach { component ->
                    configureComponent(component)
                }
            }
        }
    }

    private fun configureComponent(component: Component) {
        val javaSources = component.sources.java ?: return

        val provider = project.tasks.register(
            component.getTaskName("checkstyle"),
            Checkstyle::class.java
        ) { task ->
            task.description = "Run Checkstyle analysis for ${component.name} source set"
            task.group = JavaBasePlugin.VERIFICATION_GROUP
            task.classpath = project.files()
            task.source = project.files().from(javaSources.all).asFileTree
        }
        parentTask.configure { it.dependsOn(provider) }
    }

    private fun configureCheckTask() {
        withBaseAndroidPlugin {
            project.tasks.named(JavaBasePlugin.CHECK_TASK_NAME) { task ->
                task.dependsOn(parentTask)
            }
        }
    }

    private fun withBaseAndroidPlugin(action: Action<BasePlugin>) {
        project.plugins.withType(BasePlugin::class.java, action)
    }

    private fun Component.getTaskName(verb: String): String {
        return GUtil.toLowerCamelCase("$verb ${name.capitalize()}")
    }
}
