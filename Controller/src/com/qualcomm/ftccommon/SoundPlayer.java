package com.qualcomm.ftccommon;

import java.io.File;

public class SoundPlayer {
    public static class PlaySoundParams
    {
        /** an additional volume scaling that will be applied to this particular play action */
        public float volume = 1.0f;

        /** whether to wait for any currently-playing non-looping sound to finish before playing */
        public boolean waitForNonLoopingSoundsToFinish = true;

        /** -1 means playing loops forever, 0 is play once, 1 is play twice, etc */
        public int loopControl = 0;

        /** playback rate (1.0 = normal playback, range 0.5 to 2.0) */
        public float rate = 1.0f;

        //--------------

        public PlaySoundParams() { }

        public PlaySoundParams(boolean wait) { this.waitForNonLoopingSoundsToFinish = wait; }

        public PlaySoundParams(PlaySoundParams them)
        {
            this.volume = them.volume;
            this.waitForNonLoopingSoundsToFinish = them.waitForNonLoopingSoundsToFinish;
            this.loopControl = them.loopControl;
            this.rate = them.rate;
        }

        public boolean isLooping()
        {
            return loopControl == -1;
        }
    }

    private static SoundPlayer instance = new SoundPlayer();

    public static SoundPlayer getInstance() {
        return instance;
    }

    public void stopPlayingAll() {

    }

    public void preload(Object a, Object b) {

    }

    public void startPlaying(Object a, File b, PlaySoundParams c, Object d, Object e) {

    }

    public void startPlaying(Object a, int b, PlaySoundParams c, Object d, Object e) {

    }
}