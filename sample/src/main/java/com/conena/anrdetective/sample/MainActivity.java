/*
 * Copyright (C) 2023 Fabian Andera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.conena.anrdetective.sample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.conena.anrdetective.sample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private long blockFor = 3L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        updateIntroText();
        binding.slider.addOnChangeListener((slider, value, fromUser) -> {
            blockFor = (long) value;
            updateIntroText();
        });
        binding.btnSleep.setOnClickListener(v -> {
            try {
                Thread.sleep(blockFor * 1_000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        binding.btnLoop.setOnClickListener(v -> {
            long waitUntil = System.currentTimeMillis() + (blockFor * 1_000L);
            while (System.currentTimeMillis() < waitUntil) {}
        });
    }

    private void updateIntroText() {
        binding.text.setText(getString(R.string.label_intro, blockFor));
    }

}