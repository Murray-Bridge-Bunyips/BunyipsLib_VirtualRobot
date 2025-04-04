package au.edu.sa.mbhs.studentrobotics.bunyipslib.hardware;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.internal.ui.GamepadUser;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.function.Predicate;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsOpMode;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.UnaryFunction;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.logic.Condition;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.transforms.Controls;

/**
 * A wrapper around a {@link Gamepad} object that provides a {@link Controls} interface and custom input calculations.
 * These gamepad objects are used natively in {@link BunyipsOpMode}, and are the drop-in replacements for {@code gamepad1} and {@code gamepad2}.
 *
 * @author Lucas Bubner, 2024
 * @see BunyipsOpMode
 * @since 1.0.0-pre
 */
public class Controller extends Gamepad {
    /**
     * The SDK gamepad that this Controller wraps and takes input from.
     * <p>
     * This is public for advanced users who need to access the raw gamepad values, or need to access hardware
     * related gamepad methods that are not available in the Controller class which copies state.
     */
    public final Gamepad sdk;
    /**
     * The user defined designated user for this gamepad controller.
     * <p>
     * See the constructor notes for more information.
     */
    public final GamepadUser designatedUser;
    private final HashMap<Controls, Predicate<Boolean>> buttons = new HashMap<>();
    private final HashMap<Controls.Analog, UnaryFunction> axes = new HashMap<>();
    private final HashMap<Controls, Boolean> debounces = new HashMap<>();
    /**
     * Shorthand for left_stick_x
     */
    public volatile float lsx;
    /**
     * Shorthand for left_stick_y
     */
    public volatile float lsy;
    /**
     * Shorthand for right_stick_x
     */
    public volatile float rsx;
    /**
     * Shorthand for right_stick_y
     */
    public volatile float rsy;
    /**
     * Shorthand for left_trigger
     */
    public volatile float lt;
    /**
     * Shorthand for right_trigger
     */
    public volatile float rt;
    /**
     * Shorthand for left_bumper
     */
    public volatile boolean lb;
    /**
     * Shorthand for right_bumper
     */
    public volatile boolean rb;
    /**
     * Shorthand for dpad_up
     */
    public volatile boolean du;
    /**
     * Shorthand for dpad_down
     */
    public volatile boolean dd;
    /**
     * Shorthand for dpad_left
     */
    public volatile boolean dl;
    /**
     * Shorthand for dpad_right
     */
    public volatile boolean dr;
    /**
     * Shorthand for left_stick_button
     */
    public volatile boolean lsb;
    /**
     * Shorthand for right_stick_button
     */
    public volatile boolean rsb;

    /**
     * Create a new Controller to manage.
     *
     * @param gamepad        The Gamepad to wrap (gamepad1, gamepad2)
     * @param designatedUser The user this gamepad is designated to. Normally, accessing the {@code gamepad.getUser()}
     *                       method can return null if the gamepad has not been "activated" via the controller by
     *                       the user doing Start + A or B. This behaviour is a strange oddity of the SDK but leads
     *                       to unexpected behaviour for users extracting controller data in init.
     *                       This field is used to re-expose as the {@link #designatedUser} public field, which ensures
     *                       that a user object can be retrieved without risk of it being null. Note that the presence
     *                       of this field does not impact the {@link #user} field on the actual gamepad, as it is
     *                       being parsed directly from the SDK to ensure consistent behaviour and eliminate risk
     *                       of race conditions. A try-getter, {@link #tryGetUser} exists for convenience.
     */
    public Controller(@NonNull Gamepad gamepad, @NonNull GamepadUser designatedUser) {
        this.designatedUser = designatedUser;
        sdk = gamepad;
        copy(gamepad);
        update();
    }

    /**
     * Attempts to get the user of this controller.
     * <p>
     * If a user cannot be found directly and this gamepad is a Controller instance, the {@link #designatedUser}
     * will be returned. Otherwise, if no user can be found, null is returned.
     *
     * @param gamepad the gamepad to get from
     * @return the user of the controller, if available by user getter and designated user means, else null
     */
    @Nullable
    public static GamepadUser tryGetUser(@NonNull Gamepad gamepad) {
        GamepadUser user = gamepad.getUser();
        if (user == null && gamepad instanceof Controller ctrl) {
            return ctrl.designatedUser;
        }
        return user;
    }

    private void parseUnmanagedControllerBuffer() {
        byte[] buf = sdk.toByteArray();
        // Size of the Robocol header length is 5, minus 2 to skip the sequence number
        ByteBuffer byteBuffer = ByteBuffer.wrap(buf, 3, buf.length - 3);
        // Skip over the sequence number as we don't need it, but still need to read the next bytes
        byteBuffer.getShort();

        byte version = byteBuffer.get();

        if (version >= 1) {
            id = byteBuffer.getInt();
            timestamp = byteBuffer.getLong();
            // Skip over buffers we don't care about
            for (int i = 0; i < 6; i++) {
                byteBuffer.getFloat();
            }

            int buttons = byteBuffer.getInt();
            // These controls are not handled by Controller, and we can just pass them through,
            // while ignoring values we will update ourselves
            touchpad_finger_1 = (buttons & 0x20000) != 0;
            touchpad_finger_2 = (buttons & 0x10000) != 0;
            touchpad = (buttons & 0x08000) != 0;
            guide = (buttons & 0x00010) != 0;
        }

        if (version >= 2) {
            user = byteBuffer.get();
        }

        if (version >= 3) {
            type = Type.values()[byteBuffer.get()];
        }

        if (version >= 4) {
            byte v4TypeValue = byteBuffer.get();
            if (v4TypeValue < Type.values().length) {
                type = Type.values()[v4TypeValue];
            }
        }

        if (version >= 5) {
            // These values are also not managed by Controller
            touchpad_finger_1_x = byteBuffer.getFloat();
            touchpad_finger_1_y = byteBuffer.getFloat();
            touchpad_finger_2_x = byteBuffer.getFloat();
            touchpad_finger_2_y = byteBuffer.getFloat();
        }
    }

    /**
     * Update the public fields of this Controller with the values from the wrapped Gamepad, performing calculations
     * on the inputs as specified by the user.
     * This method must be called in a loop of the OpMode to update the Controller's values. This is automatically
     * called in BunyipsOpMode on another thread.
     */
    public void update() {
        parseUnmanagedControllerBuffer();

        // Recalculate all custom inputs, these accommodate for the custom functions set by the user
        // and are the same controls that we intentionally didn't update in parseUnmanagedControllerBuffer().
        left_stick_x = get(Controls.Analog.LEFT_STICK_X);
        left_stick_y = get(Controls.Analog.LEFT_STICK_Y);
        right_stick_x = get(Controls.Analog.RIGHT_STICK_X);
        right_stick_y = get(Controls.Analog.RIGHT_STICK_Y);
        left_trigger = get(Controls.Analog.LEFT_TRIGGER);
        right_trigger = get(Controls.Analog.RIGHT_TRIGGER);
        left_bumper = get(Controls.LEFT_BUMPER);
        right_bumper = get(Controls.RIGHT_BUMPER);
        dpad_up = get(Controls.DPAD_UP);
        dpad_down = get(Controls.DPAD_DOWN);
        dpad_left = get(Controls.DPAD_LEFT);
        dpad_right = get(Controls.DPAD_RIGHT);
        left_stick_button = get(Controls.LEFT_STICK_BUTTON);
        right_stick_button = get(Controls.RIGHT_STICK_BUTTON);
        a = get(Controls.A);
        b = get(Controls.B);
        x = get(Controls.X);
        y = get(Controls.Y);
        start = get(Controls.START);
        back = get(Controls.BACK);

        updateButtonAliases();
    }

    @Override
    protected void updateButtonAliases() {
        super.updateButtonAliases();
        // Assign public field aliases
        lsx = left_stick_x;
        lsy = left_stick_y;
        rsx = right_stick_x;
        rsy = right_stick_y;
        lt = left_trigger;
        rt = right_trigger;
        lb = left_bumper;
        rb = right_bumper;
        du = dpad_up;
        dd = dpad_down;
        dl = dpad_left;
        dr = dpad_right;
        lsb = left_stick_button;
        rsb = right_stick_button;
    }

    /**
     * Customise how a button is read.
     *
     * @param button    The button to customise
     * @param predicate The custom function to use based on the button's value
     * @return this
     */
    @NonNull
    public Controller set(@NonNull Controls button, @Nullable Predicate<Boolean> predicate) {
        if (predicate == null) {
            buttons.remove(button);
            return this;
        }
        if (button != Controls.NONE)
            buttons.put(button, predicate);
        return this;
    }

    /**
     * Customise how an axis is read.
     *
     * @param axis     The axis to customise
     * @param function The custom function to use based on the axis's value
     * @return this
     */
    @NonNull
    public Controller set(@NonNull Controls.Analog axis, @Nullable UnaryFunction function) {
        if (function == null) {
            axes.remove(axis);
            return this;
        }
        axes.put(axis, function);
        return this;
    }

    /**
     * Customise how a group of buttons is read.
     *
     * @param group     The group of buttons to customise
     * @param predicate The custom function to use based on the group's value
     * @return this
     */
    @NonNull
    public Controller set(@NonNull Controls.ButtonGroup group, @Nullable Predicate<Boolean> predicate) {
        for (Controls button : Controls.getButtons(group)) {
            set(button, predicate);
        }
        return this;
    }

    /**
     * Customise how a group of axes is read.
     *
     * @param group    The group of axes to customise
     * @param function The custom function to use based on the group's value
     * @return this
     */
    @NonNull
    public Controller set(@NonNull Controls.AnalogGroup group, @Nullable UnaryFunction function) {
        for (Controls.Analog axis : Controls.getAxes(group)) {
            set(axis, function);
        }
        return this;
    }

    /**
     * Get the value of a button.
     *
     * @param button The button to get the value of
     * @return The value of the button
     */
    public boolean get(@NonNull Controls button) {
        boolean isPressed = Controls.isSelected(sdk, button);
        Predicate<Boolean> predicate = buttons.get(button);
        return predicate == null ? isPressed : predicate.test(isPressed);
    }

    /**
     * Get the value of an axis.
     *
     * @param axis The axis to get the value of
     * @return The value of the axis
     */
    public float get(@NonNull Controls.Analog axis) {
        float value = Controls.Analog.get(sdk, axis);
        UnaryFunction function = axes.get(axis);
        return function == null ? value : (float) function.apply(value);
    }

    /**
     * Check if a button is currently pressed on a gamepad, with debounce to ignore a press that was already detected
     * upon the <b>first call of this function and button</b>. This is an implementation of rising edge detection, but also
     * applies a check for the initial state of the button, making it useful for task toggles.
     * <p>
     * See the {@link Condition} class for more boolean state management.
     *
     * @param button The button to check
     * @return True if the button is pressed and not debounced by the definition of this method
     */
    public boolean getDebounced(@NonNull Controls button) {
        boolean buttonPressed = get(button);
        boolean pressedPreviously = Boolean.TRUE.equals(debounces.getOrDefault(button, buttonPressed));
        if (buttonPressed && !pressedPreviously) {
            debounces.put(button, true);
            return true;
        } else if (!buttonPressed) {
            debounces.put(button, false);
        }
        return false;
    }

    /**
     * Call to reset the initial debounce state of {@link #getDebounced(Controls)}, allowing further calls to
     * this method to capture the initial state of the button again. For implementations that do not call this method,
     * the {@link #getDebounced(Controls)} method will operate as a simple rising edge detector.
     *
     * @param button The button to reset the debounce state of
     */
    public void resetDebounce(@NonNull Controls button) {
        debounces.remove(button);
    }
}
