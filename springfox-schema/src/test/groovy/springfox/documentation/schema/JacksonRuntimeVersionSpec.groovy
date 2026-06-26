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
package springfox.documentation.schema

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

class JacksonRuntimeVersionSpec extends Specification {

  private static final String MINIMUM_JACKSON_VERSION = '2.21.4'

  def "jackson-core on the test classpath meets the minimum security version"() {
    given:
    def runtimeVersion = JsonFactory.package.implementationVersion

    expect:
    runtimeVersion != null
    compareVersions(runtimeVersion, MINIMUM_JACKSON_VERSION) >= 0
  }

  def "object mapper round-trips schema models without error"() {
    given:
    def mapper = new ObjectMapper()
    def model = [name: 'springfox', nested: [enabled: true]]

    when:
    def json = mapper.writeValueAsString(model)
    def parsed = mapper.readValue(json, Map)

    then:
    parsed.name == 'springfox'
    parsed.nested.enabled == true
  }

  def "object mapper parses moderately nested json"() {
    given:
    def mapper = new ObjectMapper()
    def depth = 200
    def json = ('{"v":' * depth) + '1' + ('}' * depth)

    when:
    def parsed = mapper.readValue(json, Map)

    then:
    parsed != null
  }

  private static int compareVersions(String left, String right) {
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
