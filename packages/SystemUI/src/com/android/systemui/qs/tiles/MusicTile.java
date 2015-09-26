/*
 * Copyright (C) 2012 The Android Open Source Project
 * Copyright (C) 2013 The SlimRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.qs.tiles;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.RemoteController;
import android.os.SystemClock;
import android.view.KeyEvent;

import com.android.systemui.R;
import com.android.systemui.qs.QSTile;

/** Quick settings tile: Music **/
public class MusicTile extends QSTile<QSTile.BooleanState> {
    private final AudioManager mAudioManager;
    private final RemoteController mRemoteController;

    private boolean mActive = false;
    private String mCurrentTrack = null;

    public MusicTile(Host host) {
        super(host);
        mRemoteController = new RemoteController(mContext, mRCClientUpdateListener);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.registerRemoteController(mRemoteController);
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    public void setListening(boolean listening) {
    }

    @Override
    public void handleClick() {
        sendMediaButtonClick(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        refreshState();
    }

    @Override
    public void handleLongClick() {
        sendMediaButtonClick(KeyEvent.KEYCODE_MEDIA_NEXT);
        refreshState();
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.visible = true;
        if (mActive) {
            state.icon = ResourceIcon.get(R.drawable.ic_qs_media_pause);
            state.label = mCurrentTrack != null
                ? mCurrentTrack : mContext.getString(R.string.quick_settings_music_pause);
        } else {
            state.icon = ResourceIcon.get(R.drawable.ic_qs_media_play);
            state.label = mContext.getString(R.string.quick_settings_music_play);
        }
    }

    private void playbackStateUpdate(int state) {
        boolean active;
        switch (state) {
            case RemoteControlClient.PLAYSTATE_PLAYING:
                active = true;
                break;
            case RemoteControlClient.PLAYSTATE_ERROR:
            case RemoteControlClient.PLAYSTATE_PAUSED:
            default:
                active = false;
                break;
        }
        if (active != mActive) {
            mActive = active;
            refreshState();
        }
    }

    private void sendMediaButtonClick(int keyCode) {
        long eventTime = SystemClock.uptimeMillis();
        KeyEvent key = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0);
        mAudioManager.dispatchMediaKeyEvent(key);
        mAudioManager.dispatchMediaKeyEvent(KeyEvent.changeAction(key, KeyEvent.ACTION_UP));
    }

    private RemoteController.OnClientUpdateListener mRCClientUpdateListener =
            new RemoteController.OnClientUpdateListener() {
        @Override
        public void onClientChange(boolean clearing) {
            if (clearing) {
                mCurrentTrack = null;
                mActive = false;
                refreshState();
            }
        }

        @Override
        public void onClientPlaybackStateUpdate(int state, long stateChangeTimeMs,
                long currentPosMs, float speed) {
            playbackStateUpdate(state);
        }

        @Override
        public void onClientPlaybackStateUpdate(int state) {
            playbackStateUpdate(state);
        }

        @Override
        public void onClientFolderInfoBrowsedPlayer(String stringUri) {
        }

        @Override
        public void onClientUpdateNowPlayingEntries(long[] playList) {
        }

        @Override
        public void onClientNowPlayingContentChange() {
        }

        @Override
        public void onClientPlayItemResponse(boolean success) {
        }

        @Override
        public void onClientMetadataUpdate(RemoteController.MetadataEditor data) {
            String trackTitle = data.getString(MediaMetadataRetriever.METADATA_KEY_TITLE,
                    mCurrentTrack);
            if (trackTitle != null && !trackTitle.equals(mCurrentTrack)) {
                mCurrentTrack = trackTitle;
                refreshState();
            }
        }

        @Override
        public void onClientTransportControlUpdate(int transportControlFlags) {
        }
    };
}
