package com.airwar.core.model;

import com.airwar.core.model.aircraft.AbstractAircraft;
import com.airwar.core.model.bullet.BaseBullet;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlyingObjectCollisionTest {

    @Test
    void nonAircraftObjectsOverlapShouldCrash() {
        DummyObject self = new DummyObject(100, 100, 40, 40);
        DummyObject other = new DummyObject(119, 100, 40, 40);

        assertTrue(self.crash(other));
    }

    @Test
    void nonAircraftObjectsAtBoundaryShouldNotCrash() {
        DummyObject self = new DummyObject(100, 100, 40, 40);
        DummyObject other = new DummyObject(140, 100, 40, 40);

        assertFalse(self.crash(other));
    }

    @Test
    void aircraftVerticalHitboxShouldBeCompressed() {
        DummyAircraft self = new DummyAircraft(100, 100, 40, 40, 100);
        DummyBullet other = new DummyBullet(100, 130, 40, 40, 10);

        assertFalse(self.crash(other));
    }

    @Test
    void aircraftVerticalHitboxShouldStillCollideWithinRange() {
        DummyAircraft self = new DummyAircraft(100, 100, 40, 40, 100);
        DummyBullet other = new DummyBullet(100, 119, 40, 40, 10);

        assertTrue(self.crash(other));
    }

    private static final class DummyObject extends FlyingObject {
        private DummyObject(int x, int y, int width, int height) {
            super(x, y, 0, 0, width, height);
        }
    }

    private static final class DummyAircraft extends AbstractAircraft {
        private DummyAircraft(int x, int y, int width, int height, int hp) {
            super(x, y, 0, 0, hp);
            this.width = width;
            this.height = height;
        }

        @Override
        public List<BaseBullet> shoot() {
            return Collections.emptyList();
        }
    }

    private static final class DummyBullet extends BaseBullet {
        private DummyBullet(int x, int y, int width, int height, int power) {
            super(x, y, 0, 0, power);
            this.width = width;
            this.height = height;
        }
    }
}
