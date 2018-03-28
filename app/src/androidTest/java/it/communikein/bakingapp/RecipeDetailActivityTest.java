package it.communikein.bakingapp;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.communikein.bakingapp.ui.RecipeDetailActivity;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;

@RunWith(AndroidJUnit4.class)
public class RecipeDetailActivityTest {

    @Rule
    public ActivityTestRule<RecipeDetailActivity> mActivityTestRule =
            new ActivityTestRule<>(RecipeDetailActivity.class);

    @Test
    public void testActivityResultIsHandledProperly() {
        onData(anything()).inAdapterView(withId(R.id.steps_list)).atPosition(1).perform(click());

        /*// Build a result to return when a particular activity is launched.
        Intent resultData = new Intent();
        String phoneNumber = "123-345-6789";
        resultData.putExtra("phone", phoneNumber);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        // Set up result stubbing when an intent sent to "contacts" is seen.
        intending(toPackage("com.android.contacts")).respondWith(result);

        // User action that results in "contacts" activity being launched.
        // Launching activity expects phoneNumber to be returned and displays it on the screen.
        user.clickOnView(system.getView(R.id.pickButton));

        // Assert that data we set up above is shown.
        assertTrue(user.waitForText(phoneNumber));*/
    }

}
