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
package xyz.jpenilla.toothpick.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

internal data class ShadePlugin(
  val executions: List<ShadeExecution> = emptyList(),
  val configuration: ShadeConfiguration = ShadeConfiguration()
) : MavenPlugin(PLUGIN_NAME) {
  companion object {
    const val PLUGIN_NAME = "maven-shade-plugin"
  }
}

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class ShadeConfiguration(
  val relocations: List<Relocation> = emptyList(),
  val filters: List<Filter> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class ShadeExecution(
  val phase: String,
  val configuration: ShadeConfiguration = ShadeConfiguration()
)

internal data class Filter(
  val artifact: String,
  val excludes: List<String> = emptyList()
)

internal data class Relocation(
  val pattern: String,
  val shadedPattern: String,
  val rawString: Boolean = false,
  val excludes: List<String> = emptyList()
)
