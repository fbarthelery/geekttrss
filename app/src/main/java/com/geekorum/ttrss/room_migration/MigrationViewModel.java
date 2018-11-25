/**
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2018 by Frederic-Charles Barthelery.
 *
 * This file is part of Geekttrss.
 *
 * Geekttrss is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Geekttrss is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Geekttrss.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.geekorum.ttrss.room_migration;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import com.geekorum.ttrss.BR;

/**
 * {@link androidx.lifecycle.ViewModel } for the {@link RoomMigrationActivity}.
 */
public class MigrationViewModel extends AndroidViewModel {
    RunningMigrationLiveData migrationProgress = new RunningMigrationLiveData(getApplication());
    MigrationModel model = new MigrationModel();

    public MigrationViewModel(Application application) {
        super(application);
    }

    MigrationModel getModel() {
        return model;
    }

    public static class MigrationModel extends BaseObservable {
        private int max = 1;
        private int progress;
        private boolean isSuccessful;

        @Bindable
        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
            notifyPropertyChanged(BR.max);
        }

        @Bindable
        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
            notifyPropertyChanged(BR.progress);
        }

        @Bindable({"progress", "max"})
        public boolean isRunning() {
            return progress < max;
        }

        @Bindable({"progress", "max"})
        public boolean isComplete() {
            return progress != 0 && progress == max;
        }

        @Bindable({"complete"})
        public boolean isSuccessful() {
            return  isComplete() && isSuccessful;
        }

        public void setSuccessful(boolean successful) {
            this.isSuccessful = successful;
            notifyPropertyChanged(BR.successful);
        }
    }
}
