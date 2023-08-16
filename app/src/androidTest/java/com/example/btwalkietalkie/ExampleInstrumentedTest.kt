package com.example.btwalkietalkie

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnotherAppUiAutomatorTest {

    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun testInteractWithAnotherApp() {
        // Launch the app you want to interact with
        device.pressHome()
        val screenHeight = device.displayHeight
        val screenWidth = device.displayWidth
        device.swipe(screenWidth / 2, screenHeight - 100, screenWidth / 2, 0, 10)

//        device.findObject(UiSelector().text("App Cloner")).click()
        val scrollableList3 = UiScrollable(
            UiSelector().className("androidx.recyclerview.widget.RecyclerView")
        )
        scrollableList3.waitForExists(15000)
        val textSelector3 = By.text("App Cloner")
        // Check if the text is not present before scrolling
        while (!device.hasObject(textSelector3)) {
            scrollList(scrollableList3)
        }
        // Perform interactions with UI elements of the other app
        device.findObject(UiSelector().text("App Cloner")).click()
        val scrollableList = UiScrollable(
            UiSelector().className("android.widget.ListView")
        )
        scrollableList.waitForExists(15000)
        val textSelector = By.text("Chess")
        // Check if the text is not present before scrolling
        while (!device.hasObject(textSelector)) {
            scrollList(scrollableList)
        }
        // Perform interactions with UI elements of the other app
        val button2: UiObject? = device.findObject(UiSelector().text("Chess"))
        button2?.click()

        val scrollableList2 = UiScrollable(
            UiSelector().className("android.widget.ListView")
        )
        val textSelector2 = By.text("Networking options")
        // Check if the text is not present before scrolling
        while (!device.hasObject(textSelector2)) {
            scrollList(scrollableList2)
        }
        val button: UiObject? = device.findObject(UiSelector().text("Networking options"))
        button?.click()
    }
    private fun scrollList(list: UiScrollable) {
        // Scroll down by swiping up
        list.scrollForward()

        // Scroll up by swiping down
        // list.scroll(Direction.UP, 0.8f)
    }
}