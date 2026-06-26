/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package springfox.gradlebuild.security

class DependencyVersionChecker {

  static final Map<String, String> MINIMUM_VERSIONS = [
      jackson  : '2.21.4',
      snakeyaml: '2.6',
  ]

  private final File dependenciesFile

  DependencyVersionChecker(File dependenciesFile) {
    this.dependenciesFile = dependenciesFile
  }

  Map<String, String> readDeclaredVersions() {
    if (!dependenciesFile.exists()) {
      throw new IllegalStateException("dependencies.gradle not found: ${dependenciesFile.absolutePath}")
    }
    def content = dependenciesFile.getText('UTF-8')
    MINIMUM_VERSIONS.keySet().collectEntries { key ->
      def matcher = content =~ /${key}\s*=\s*['"]([^'"]+)['"]/
      if (!matcher.find()) {
        throw new IllegalStateException("Could not find declared version for '${key}' in ${dependenciesFile.name}")
      }
      [(key): matcher.group(1)]
    }
  }

  List<String> findViolations() {
    def declared = readDeclaredVersions()
    def violations = []
    MINIMUM_VERSIONS.each { key, minimum ->
      def actual = declared[key]
      if (compareVersions(actual, minimum) < 0) {
        violations << "${key}: declared ${actual}, minimum required ${minimum}"
      }
    }
    violations
  }

  void verify() {
    def violations = findViolations()
    if (!violations.isEmpty()) {
      throw new IllegalStateException(
          "Dependency security baseline violated:\n  - " + violations.join('\n  - '))
    }
  }

  static int compareVersions(String left, String right) {
    def leftParts = left.split(/\./)*.toInteger()
    def rightParts = right.split(/\./)*.toInteger()
    def length = Math.max(leftParts.size(), rightParts.size())
    for (int i = 0; i < length; i++) {
      int lv = i < leftParts.size() ? leftParts[i] : 0
      int rv = i < rightParts.size() ? rightParts[i] : 0
      if (lv != rv) {
        return lv <=> rv
      }
    }
    0
  }
}
