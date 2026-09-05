package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Card
import com.example.data.model.HandType
import com.example.data.model.Rank
import com.example.data.model.Suit
import com.example.data.model.TeenPattiEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Teen Patti", appName)
    }

    @Test
    fun `deck creates 52 distinct cards`() {
        val deck = TeenPattiEvaluator.createStandardDeck()
        assertEquals(52, deck.size)
        assertEquals(52, deck.distinct().size)
    }

    @Test
    fun `hand evaluator identifies trail and pure sequence`() {
        val trailCards = listOf(
            Card(Rank.ACE, Suit.SPADES),
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.ACE, Suit.CLUBS)
        )
        val trailHand = TeenPattiEvaluator.evaluate(trailCards)
        assertEquals(HandType.TRAIL, trailHand.type)

        val pureSequenceCards = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.HEARTS)
        )
        val pureSequenceHand = TeenPattiEvaluator.evaluate(pureSequenceCards)
        assertEquals(HandType.PURE_SEQUENCE, pureSequenceHand.type)

        assertTrue(trailHand > pureSequenceHand)
    }
}


