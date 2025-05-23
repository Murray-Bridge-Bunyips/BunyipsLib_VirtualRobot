package com.acmerobotics.dashboard.message;

import com.acmerobotics.dashboard.message.redux.*;

/**
 * Dashboard message types. These values match the corresponding Redux actions in the frontend.
 */
public enum MessageType {
    /* status (also serves as a heartbeat) */
    GET_ROBOT_STATUS(GetRobotStatus.class),
    RECEIVE_ROBOT_STATUS(ReceiveRobotStatus.class),

    /* op mode management */
    INIT_OP_MODE(InitOpMode.class),
    START_OP_MODE(StartOpMode.class),
    STOP_OP_MODE(StopOpMode.class),
    RECEIVE_OP_MODE_LIST(ReceiveOpModeList.class),

    /* config */
    GET_CONFIG(GetConfig.class),
    SAVE_CONFIG(SaveConfig.class),
    RECEIVE_CONFIG(ReceiveConfig.class),

    /* telemetry */
    RECEIVE_TELEMETRY(ReceiveTelemetry.class),

    /* camera */
    RECEIVE_IMAGE(ReceiveImage.class),

    /* gamepad */
    RECEIVE_GAMEPAD_STATE(ReceiveGamepadState.class);

    final Class<? extends Message> msgClass;

    MessageType(Class<? extends Message> msgClass) {
        this.msgClass = msgClass;
    }
}
