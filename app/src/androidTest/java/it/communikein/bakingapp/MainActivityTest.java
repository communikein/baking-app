package it.communikein.bakingapp;

import android.content.res.Configuration;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.communikein.bakingapp.ui.MainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testActivityResultIsHandledProperly() {
        if ((isLandscape() && !isTablet()) || (isTablet() && !isLandscape())) {
            // Check that the button to change the list layout is not available
            onView(withId(R.id.change_layout_fab)).check(matches(not(isDisplayed())));
        }
        else {
            // Check that the button to change the list layout is not available
            onView(withId(R.id.change_layout_fab)).check(matches(isDisplayed()));
        }

        // Check that the recyclerview containing the recipes is displayed
        onView(withId(R.id.list_recyclerview)).check(matches(isDisplayed()));

        // Click on the first recipe in the recyclerview
        onView(withId(R.id.list_recyclerview)).perform(actionOnItemAtPosition(0, click()));

        // Check that the recyclerview containing the steps is displayed
        onView(withId(R.id.steps_list)).check(matches(isDisplayed()));

        // Check that the favourite fab is displayed
        onView(withId(R.id.favorite_fab)).check(matches(isDisplayed()));
    }

    public boolean isTablet() {
        return mActivityTestRule.getActivity().getResources()
                .getBoolean(R.bool.isTablet);
    }

    public boolean isLandscape() {
        return mActivityTestRule.getActivity().getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
    }
}
