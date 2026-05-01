package com.airwar.android.ui;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.airwar.android.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MenuActivityLaunchTest {

    @Rule
    public ActivityScenarioRule<MenuActivity> activityRule =
            new ActivityScenarioRule<>(MenuActivity.class);

    @Test
    public void launchesAndShowsMenuTitle() {
        onView(withId(R.id.menu_header)).check(matches(withText(R.string.menu_title)));
        onView(withId(R.id.button_create_room)).check(matches(isDisplayed()));
    }
}
