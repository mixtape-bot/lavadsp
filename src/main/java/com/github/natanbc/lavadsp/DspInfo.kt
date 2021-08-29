/*
 * Copyright 2018 natanbc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.natanbc.lavadsp

/**
 * Contains information about the library version.
 */
object DspInfo {
    private val stream = DspInfo::class.java.classLoader.getResourceAsStream("version.txt")
    private val versionTxt = (stream?.reader()?.readText() ?: "0.0.0\nunknown").lines()

    /**
     * Latest commit hash as of build time.
     */
    var COMMIT_HASH: String = versionTxt.last()

    /**
     * Library version, in the format `MAJOR.MINOR.REVISION`.
     */
    var VERSION: String = versionTxt.first()

    /**
     * Library major version.
     */
    var VERSION_MAJOR: String = VERSION.split('.').first()

    /**
     * Library minor version.
     */
    var VERSION_MINOR: String = VERSION.split('.')[1]

    /**
     * Library revision.
     */
    var VERSION_REVISION: String = VERSION.split('.').last()

}
