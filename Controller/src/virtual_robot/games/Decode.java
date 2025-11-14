package virtual_robot.games;


import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.*;
import virtual_robot.controller.Filters;
import virtual_robot.controller.VirtualField;


/**
 * Decode is just like NoGame, except that there will be stationary (infinite mass) bodies
 * <p>
 * <p>
 * representing the Goals and Ramps
 */
public final class Decode extends UltimateGoal {
    @Override
    public void initialize() {
        super.initialize();
        Polygon blueGoalPoly = new Polygon(new Vector2(0, 0), new Vector2(0, -0.547),
                new Vector2(0.217, -0.547), new Vector2(0.604, 0));
        Rectangle blueRampRect = new Rectangle(0.217, 1.282);
        Polygon redGoalPoly = new Polygon(new Vector2(0, 0), new Vector2(-0.624, 0),
                new Vector2(-0.217, -0.547), new Vector2(0, -0.547));
        Rectangle redRampRect = new Rectangle(0.217, 1.282);
        blueGoalPoly.translate(-VirtualField.HALF_FIELD_WIDTH_METERS, VirtualField.HALF_FIELD_WIDTH_METERS);
        blueRampRect.translate(-VirtualField.HALF_FIELD_WIDTH_METERS + blueRampRect.getWidth() / 2.0,
                blueRampRect.getHeight() / 2.0);
        redGoalPoly.translate(VirtualField.HALF_FIELD_WIDTH_METERS, VirtualField.HALF_FIELD_WIDTH_METERS);
        redRampRect.translate(VirtualField.HALF_FIELD_WIDTH_METERS - redRampRect.getWidth() / 2.0,
                redRampRect.getHeight() / 2.0);
        Convex[] convexes = new Convex[]{blueGoalPoly, blueRampRect, redGoalPoly, redRampRect};
        for (Convex convex : convexes) {
            Body body = new Body();
            BodyFixture fixture = body.addFixture(convex);
            fixture.setFilter(Filters.WALL_FILTER);
            body.setMass(MassType.INFINITE);
            body.rotate(Math.PI); // boom
            world.addBody(body);
        }
    }
}
