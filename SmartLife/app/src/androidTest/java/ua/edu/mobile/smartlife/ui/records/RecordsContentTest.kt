package ua.edu.mobile.smartlife.ui.records

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ua.edu.mobile.smartlife.data.model.HealthRecord
import ua.edu.mobile.smartlife.data.model.RecordType
import ua.edu.mobile.smartlife.ui.theme.SmartLifeTheme

@RunWith(AndroidJUnit4::class)
class RecordsContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyListShowsPlaceholder() {
        composeTestRule.setContent {
            SmartLifeTheme { RecordsContent(records = emptyList(), onAddRecord = { _, _, _ -> }, onRecordClick = {}) }
        }

        composeTestRule.onNodeWithText("Записів поки немає").assertIsDisplayed()
    }

    @Test
    fun recordsAreDisplayed() {
        val records = listOf(
            HealthRecord(1, RecordType.PULSE, 72.0, "спокій"),
            HealthRecord(2, RecordType.WATER, 0.5, "")
        )
        composeTestRule.setContent {
            SmartLifeTheme { RecordsContent(records = records, onAddRecord = { _, _, _ -> }, onRecordClick = {}) }
        }

        composeTestRule.onNodeWithText("72 уд/хв").assertIsDisplayed()
        composeTestRule.onNodeWithText("0.5 л").assertIsDisplayed()
        composeTestRule.onNodeWithText("Журнал показників (2)").assertIsDisplayed()
    }

    @Test
    fun invalidValueDisablesSaveButton() {
        composeTestRule.setContent {
            SmartLifeTheme { RecordsContent(records = emptyList(), onAddRecord = { _, _, _ -> }, onRecordClick = {}) }
        }

        composeTestRule.onNodeWithContentDescription("Додати запис").performClick()
        composeTestRule.onNodeWithText("Пульс").performClick()
        // Некоректне значення: під полем — помилка, кнопка "Зберегти" неактивна
        composeTestRule.onNodeWithText("Значення, уд/хв").performTextInput("abc")

        composeTestRule.onNodeWithText("Введіть додатне число").assertIsDisplayed()
        composeTestRule.onNodeWithText("Зберегти").assertIsNotEnabled()
    }

    @Test
    fun validValueEnablesSaveAndCallsCallback() {
        var saved: Triple<RecordType, Double, String>? = null
        composeTestRule.setContent {
            SmartLifeTheme {
                RecordsContent(
                    records = emptyList(),
                    onAddRecord = { type, value, note -> saved = Triple(type, value, note) },
                    onRecordClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Додати запис").performClick()
        composeTestRule.onNodeWithText("Вага").performClick()
        composeTestRule.onNodeWithText("Значення, кг").performTextInput("64,5") // кома теж допустима
        composeTestRule.onNodeWithText("Зберегти").assertIsEnabled().performClick()

        assertEquals(Triple(RecordType.WEIGHT, 64.5, ""), saved)
    }
}
