/*
 * This file is part of Toothpick, licensed under the MIT License.
 *
 * Copyright (c) 2020-2021 Jason Penilla & Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.jpenilla.toothpick

import kotlinx.dom.elements
import kotlinx.dom.search
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.project
import org.w3c.dom.Element

@Deprecated("This function no longer does anything and should not be called. Toothpick will properly load repositories on it's own without calling this function.")
public fun RepositoryHandler.loadRepositories(project: Project) {
  project.gradle.buildFinished {
    project.logger.warn("${project.name} uses deprecated API 'loadRepositories' in it's buildscript.")
  }
}

@Deprecated("This function no longer does anything and should not be called. Toothpick will properly load dependencies on it's own without calling this function.")
public fun DependencyHandlerScope.loadDependencies(project: Project) {
  project.gradle.buildFinished {
    project.logger.warn("${project.name} uses deprecated API 'loadDependencies' in it's buildscript.")
  }
}

internal fun RepositoryHandler.loadRepositories0(project: Project) {
  val dom = project.parsePom() ?: return
  val repositoriesBlock = dom.search("repositories").firstOrNull() ?: return

  // Load repositories
  repositoriesBlock.elements("repository").forEach { repositoryElem ->
    val url = repositoryElem.search("url").firstOrNull()?.textContent ?: return@forEach
    maven(url)
  }
}

internal fun DependencyHandlerScope.loadDependencies0(project: Project) {
  val dom = project.parsePom() ?: return

  // Load dependencies
  dom.search("dependencies").filter {
    it.parentNode.nodeName == "project"
      || it.parentNode.nodeName == "dependencyManagement"
  }.forEach {
    loadDependencies(project, it)
  }
}

private fun DependencyHandlerScope.loadDependencies(project: Project, dependenciesBlock: Element) {
  dependenciesBlock.elements("dependency").forEach { dependencyElem ->
    val groupId = dependencyElem.search("groupId").first().textContent
    val artifactId = dependencyElem.search("artifactId").first().textContent
    val version = dependencyElem.search("version").firstOrNull()?.textContent
    val scope = dependencyElem.search("scope").firstOrNull()?.textContent
    val classifier = dependencyElem.search("classifier").firstOrNull()?.textContent

    val dependencyString = listOfNotNull(groupId, artifactId, version, classifier).joinToString(":")
    project.logger.debug("Read $scope scope dependency '$dependencyString' from '${project.name}' pom.xml")

    // Special case API
    if (artifactId == project.toothpick.apiProject.project.name
      || artifactId == "${project.toothpick.upstreamLowercase}-api"
    ) {
      if (project == project.toothpick.serverProject.project) {
        add("api", project(":${project.toothpick.apiProject.project.name}"))
      }
      return@forEach
    }

    when (scope) {
      "import" -> add("api", platform(dependencyString))
      "compile", null -> {
        add("api", dependencyString)
        if (version != null) {
          add("annotationProcessor", dependencyString)
        }
      }
      "provided" -> {
        add("compileOnly", dependencyString)
        add("testImplementation", dependencyString)
        add("annotationProcessor", dependencyString)
      }
      "runtime" -> add("runtimeOnly", dependencyString)
      "test" -> add("testImplementation", dependencyString)
    }
  }
}
