package com.airwar.android.ui;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.airwar.android.R;
import com.airwar.android.net.MultiplayerSession;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import hitsz.aircraftwar.backend.AircraftWar;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class RoomActivityLaunchTest {

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        LocalMultiplayerPrefs.saveBaseConfig(context, "http://10.0.2.2:8080", "alice");
        LocalMultiplayerPrefs.saveRoomId(context, "123456");
        MultiplayerSession.getInstance().configure("http://10.0.2.2:8080", "123456", "alice");
        MultiplayerSession.getInstance().applyRoomState(
                AircraftWar.Room.newBuilder()
                        .setRoomId("123456")
                        .setStatus(AircraftWar.RoomStatus.ROOM_STATUS_WAITING)
                        .build(),
                java.util.List.of(),
                false,
                null
        );
    }

    @After
    public void tearDown() {
        Context context = ApplicationProvider.getApplicationContext();
        MultiplayerSession.getInstance().clearRoomContext();
        LocalMultiplayerPrefs.clearRoomId(context);
    }

    @Test
    public void launchesAndShowsRoomCode() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, RoomActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<RoomActivity> ignored = ActivityScenario.launch(intent)) {
            onView(withId(R.id.room_header)).check(matches(withText(R.string.multiplayer_room_title)));
            onView(withId(R.id.room_code_value)).check(matches(withText("123456")));
            onView(withId(R.id.button_room_ready)).check(matches(isDisplayed()));
        }
    }
}
