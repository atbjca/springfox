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

import spock.lang.Specification
import springfox.gradlebuild.DirectoryBacked

class DependencyVersionCheckerSpec extends Specification implements DirectoryBacked {

  def "compareVersions orders semver segments numerically"() {
    expect:
    DependencyVersionChecker.compareVersions(left, right) == expected

    where:
    left     | right    | expected
    '2.21.4' | '2.21.4' | 0
    '2.21.4' | '2.21.0' | 1
    '2.21.0' | '2.21.4' | -1
    '2.21.10'| '2.21.4' | 1
  }

  def "findViolations reports dependencies below minimum"() {
    given:
    def tempDir = directory(this)
    def file = new File(tempDir, 'dependencies.gradle')
    file.text = """
      ext {
        jackson = '2.21.0'
        snakeyaml = '1.33'
      }
    """

    when:
    def violations = new DependencyVersionChecker(file).findViolations()

    then:
    violations.size() == 2
    violations.any { it.contains('jackson') }
    violations.any { it.contains('snakeyaml') }
  }

  def "project dependencies.gradle meets security baseline"() {
    given:
    def file = locateProjectDependenciesFile()

    expect:
    new DependencyVersionChecker(file).findViolations().isEmpty()
  }

  private static File locateProjectDependenciesFile() {
    def current = new File(System.getProperty('user.dir'))
    while (current != null) {
      def candidate = new File(current, 'gradle/dependencies.gradle')
      if (candidate.exists()) {
        return candidate
      }
      current = current.parentFile
    }
    throw new IllegalStateException('Could not locate gradle/dependencies.gradle')
  }
}
