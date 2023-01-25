package com.drewhannay.checkstyle

import org.gradle.api.Project
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.tasks.SourceSet

open class CheckstyleAndroidExtension(project: Project) : CheckstyleExtension(project) {

    /**
     * [SourceSet] does not apply for Android projects
     */
    override fun setSourceSets(sourceSets: MutableCollection<SourceSet>) =
        throw UnsupportedOperationException()
}
