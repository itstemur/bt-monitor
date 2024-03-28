package com.tz.btmonitor

import com.tz.btmonitor.bluetooth.Parser
import com.tz.btmonitor.model.Channel
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    @Test
    fun testParseConfigMessage() {
        // Sample input message
        val message = "CH=1,D0=35, D1=40,D2=55,D3=60"

        // Call the parseConfigMessage method
        val channelsMap: Map<Int, Channel> = Parser.parseConfigMessage(message)

        // Assert the result
        assertNotNull(channelsMap)
        assertEquals(4, channelsMap.size)

        // Check if the channels are correctly parsed
        assertTrue(channelsMap.containsKey(0))
        assertTrue(channelsMap.containsKey(1))
        assertTrue(channelsMap.containsKey(2))
        assertTrue(channelsMap.containsKey(3))

        // Check the configuration of each channel
        assertEquals(35, channelsMap[0]?.d)
        assertEquals(40, channelsMap[1]?.d)
        assertEquals(55, channelsMap[2]?.d)
        assertEquals(60, channelsMap[3]?.d)
    }


}