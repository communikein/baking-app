package it.communikein.bakingapp;

import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.communikein.bakingapp.data.model.Step;
import it.communikein.bakingapp.ui.StepDetailActivity;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;

@RunWith(AndroidJUnit4.class)
public class StepDetailActivityTest {

    @Rule
    public ActivityTestRule<StepDetailActivity> mActivityTestRule =
            new ActivityTestRule<>(StepDetailActivity.class);

    /**
     * Clicks on a GridView item and checks it opens up the OrderActivity with the correct details.
     */
    @Test
    public void clickGridViewItem_OpensOrderActivity() {
        onView(withId(R.id.playerView)).check(matches(isDisplayed()));


    }

}
