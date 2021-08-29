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

import kotlin.jvm.JvmOverloads
import java.util.function.Supplier
import lavaplayer.filter.FloatPcmAudioFilter
import java.lang.Exception
import kotlin.Throws
import java.lang.InterruptedException

open class ConverterPcmAudioFilter<T : Converter?> @JvmOverloads constructor(
    converterFactory: Supplier<T>,
    protected val downstream: FloatPcmAudioFilter,
    channelCount: Int,
    private val bufferSize: Int = DEFAULT_BUFFER_SIZE
) : FloatPcmAudioFilter {
    companion object {
        private const val DEFAULT_BUFFER_SIZE = 4096
    }

    protected val converters: List<T> = MutableList(channelCount) { converterFactory.get() }
    private val outputSegments: List<FloatArray>? =
        if (channelCount < 1) null else List(channelCount) { FloatArray(bufferSize) }

    @Throws(InterruptedException::class)
    override fun process(input: Array<FloatArray>, offset: Int, length: Int) {
        if (outputSegments == null) {
            for (i in input.indices) {
                converters[i]?.process(input[i], offset, input[i], 0, length)
            }

            downstream.process(input, 0, length)
        } else {
            var l = length
            while (l > 0) {
                val size = l.coerceAtMost(bufferSize)
                for (i in input.indices) {
                    converters[i]?.process(input[i], offset, outputSegments[i], 0, size)
                }

                downstream.process(outputSegments.toTypedArray(), 0, size)
                l -= bufferSize
            }
        }
    }

    override fun seekPerformed(requestedTime: Long, providedTime: Long) {
        //nothing to do here
    }

    override fun flush() {
        //nothing to do here
    }

    override fun close() {
        converters.forEach { it?.close() }
    }

    @Deprecated("", ReplaceWith("close()"))
    @Throws(Throwable::class)
    protected open fun finalize() {
        close()
    }
}
