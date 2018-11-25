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

import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.geekorum.ttrss.R;
import com.geekorum.ttrss.articles_list.ArticleListActivity;
import com.geekorum.ttrss.databinding.ActivityRoomMigrationBinding;

public class RoomMigrationActivity extends AppCompatActivity {

    private ActivityRoomMigrationBinding binding;
    private boolean migrationSucceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_room_migration);

        MigrationViewModel migrationViewModel = ViewModelProviders.of(this).get(MigrationViewModel.class);
        MigrationViewModel.MigrationModel migrationModel = migrationViewModel.getModel();
        binding.setMigration(migrationModel);

        migrationViewModel.migrationProgress.observe(this, migrationProgress -> {
            migrationModel.setMax(migrationProgress.max);
            migrationModel.setProgress(migrationProgress.progress);
            migrationModel.setSuccessful(migrationProgress.isSuccessFull);
            migrationSucceed = migrationProgress.isSuccessFull;
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (migrationSucceed) {
            Intent intent = new Intent(this, ArticleListActivity.class);
            startActivity(intent);
        }
    }
}
