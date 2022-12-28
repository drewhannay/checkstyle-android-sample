package com.drewhannay.checkstyle

import com.android.build.api.dsl.AndroidSourceSet
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.tasks.SourceSet

open class CheckstyleAndroidExtension(project: Project) : CheckstyleExtension(project) {

    var androidSourceSets: NamedDomainObjectContainer<out AndroidSourceSet>? = null

    /**
     * [SourceSet] does not apply for Android projects
     */
    override fun setSourceSets(sourceSets: MutableCollection<SourceSet>) =
        throw UnsupportedOperationException()
}
