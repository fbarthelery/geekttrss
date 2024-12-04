/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2024 by Frederic-Charles Barthelery.
 *
 * This file is part of Geekttrss.
 *
 * Geekttrss is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Geekttrss is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Geekttrss.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.geekorum.build

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.BuildConfigField
import com.android.build.api.variant.VariantOutputConfiguration.OutputType
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

internal fun ExecOperations.getGitSha1(projectDir: File): String? = runCommand("git rev-parse HEAD", workingDir = projectDir)?.trim()

internal fun ExecOperations.getHgSha1(projectDir: File): String? = runCommand("hg id --debug -i -r .", workingDir = projectDir)?.trim()

internal fun ExecOperations.getHgLocalRevisionNumber(projectDir: File): String? {
    val hg = File(projectDir, ".hg")
    return if (hg.exists()) {
        runCommand("hg id -n -r .", workingDir = projectDir)?.trim()
    } else null
}

private fun ExecOperations.getChangeSet(projectDir: File): String {
    val git = File(projectDir, ".git")
    val hg = File(projectDir, ".hg")
    return when {
        git.exists() -> "git:${getGitSha1(projectDir)}"
        hg.exists() -> "hg:${getHgSha1(projectDir)}"
        else -> "unknown"
    }
}

/**
 * Compute a version code following this format : MmmPBBB
 * M is major, mm is minor, P is patch
 * BBB is build version number from hg
 */
private fun ExecOperations.computeChangesetVersionCode(projectDir: File, major: Int = 0, minor: Int = 0, patch: Int = 0): Int {
    val base = (major * 1000000) + (minor * 10000) + (patch * 1000)
    return base + (getHgLocalRevisionNumber(projectDir)?.trim()?.toIntOrNull() ?: 0)
}

private fun ExecOperations.runCommand(
    command: String,
    workingDir: File = File(".")
): String? {
    val output = ByteArrayOutputStream()
    val result = exec {
        commandLine(command.split("\\s".toRegex()))
        setWorkingDir(workingDir)
        setStandardOutput(output)
        setErrorOutput(output)
    }
    result.rethrowFailure()
    return output.toString(Charsets.UTF_8)
}

abstract class VersionCodeTask : DefaultTask() {

    @get:OutputFile
    abstract val versionCodeOutputFile: RegularFileProperty

    @get:OutputFile
    abstract val changesetOutputFile: RegularFileProperty

    @get:Input
    abstract val repositoryDirectory: Property<String>

    @get:Input
    abstract val major: Property<Int>

    @get:Input
    abstract val minor: Property<Int>

    @get:Input
    abstract val patch: Property<Int>

    @get:Inject
    abstract val exec: ExecOperations

    @TaskAction
    fun computeVersionCode() {
        val projectDir = File(repositoryDirectory.get())
        val versionCode = exec.computeChangesetVersionCode(projectDir, major.getOrElse(0), minor.getOrElse(0), patch.getOrElse(0))
        versionCodeOutputFile.get().asFile.writeText("$versionCode")
    }

    @TaskAction
    fun computeChangeset() {
        val projectDir = File(repositoryDirectory.get())
        val changeset = exec.getChangeSet(projectDir)
        changesetOutputFile.get().asFile.writeText(changeset)
    }
}

/**
 * @param versionNameSuffix extra string to add to version name
 */
fun ApplicationAndroidComponentsExtension.configureVersionChangeset(project: Project, major: Int, minor: Int, patch: Int, versionNameSuffix: String = "") {
    // Note: Everything in there is incubating.

    // onVariantProperties registers an action that configures variant properties during
    // variant computation (which happens during afterEvaluate)
    onVariants {
        // Because app module can have multiple output when using mutli-APK, versionCode/Name
        // are only available on the variant output.
        // Here gather the output when we are in single mode (ie no multi-apk)
        val mainOutput = it.outputs.single { it.outputType == OutputType.SINGLE }

        // create version Code generating task
        val versionCodeTask = project.tasks.register<VersionCodeTask>("computeVersionCodeFor${it.name.capitalized()}") {
            this.major.set(major)
            this.minor.set(minor)
            this.patch.set(patch)
            repositoryDirectory.set(project.rootDir.absolutePath)
            versionCodeOutputFile.set(project.layout.buildDirectory.file("intermediates/versionCode.txt"))
            changesetOutputFile.set(project.layout.buildDirectory.file("intermediates/changeset.txt"))
        }

        // wire version code from the task output
        // map will create a lazy Provider that
        // 1. runs just before the consumer(s), ensuring that the producer (VersionCodeTask) has run
        //    and therefore the file is created.
        // 2. contains task dependency information so that the consumer(s) run after the producer.
        mainOutput.versionCode.set(versionCodeTask.map { it.versionCodeOutputFile.get().asFile.readText().toInt() })
        mainOutput.versionName.set("$major.$minor.$patch$versionNameSuffix")

        it.buildConfigFields.put("REPOSITORY_CHANGESET", versionCodeTask.map {
            BuildConfigField("String", "\"${it.changesetOutputFile.get().asFile.readText()}\"", "Repository changeset")
        })
    }
}

private fun String.capitalized() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }