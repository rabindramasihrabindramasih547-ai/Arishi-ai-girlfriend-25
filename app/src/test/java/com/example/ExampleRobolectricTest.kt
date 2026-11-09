package com.example

import com.example.data.model.CompanionMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTestCompanion {

  @Test
  fun testCompanionModes() {
    val romantic = CompanionMode.fromId("romantic")
    assertEquals(CompanionMode.ROMANTIC, romantic)
    assertEquals("❤️", romantic.emoji)

    val normal = CompanionMode.fromId("unknown")
    assertEquals(CompanionMode.NORMAL, normal)
  }

  @Test
  fun testAllModesHaveValidPrompts() {
    CompanionMode.entries.forEach { mode ->
      assertNotNull(mode.promptInstruction)
      assert(mode.title.isNotBlank())
    }
  }
}
